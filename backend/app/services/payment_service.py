import json
import logging
from decimal import Decimal
from typing import Dict, Any, Optional
from datetime import datetime, timezone
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from fastapi import HTTPException, status

from app.repositories import payment_repository as crud_payment, booking_repository as crud_booking
from app.models.payment import Payment, PaymentStatus, PaymentMethod
from app.models.booking import Booking, BookingStatus
from app.models.user import User, UserRole
from app.integrations import razorpay_client as razorpay_service
from app.core.config import settings
from app.services.audit_service import AuditService
from app.services.notification_service import NotificationService
from app.services.domain_events import DomainEventBus
from app.models.engagement import NotificationType

logger = logging.getLogger(__name__)

# Strict State Machine Valid Transitions Matrix
ALLOWED_TRANSITIONS: Dict[PaymentStatus, list[PaymentStatus]] = {
    PaymentStatus.NOT_STARTED: [PaymentStatus.ORDER_CREATED, PaymentStatus.PAYMENT_PENDING, PaymentStatus.FAILED, PaymentStatus.CANCELLED],
    PaymentStatus.ORDER_CREATED: [PaymentStatus.PAYMENT_PENDING, PaymentStatus.AUTHORIZED, PaymentStatus.CAPTURED, PaymentStatus.FAILED, PaymentStatus.CANCELLED],
    PaymentStatus.PAYMENT_PENDING: [PaymentStatus.ORDER_CREATED, PaymentStatus.AUTHORIZED, PaymentStatus.CAPTURED, PaymentStatus.COMPLETED, PaymentStatus.FAILED, PaymentStatus.CANCELLED],
    PaymentStatus.AUTHORIZED: [PaymentStatus.CAPTURED, PaymentStatus.FAILED, PaymentStatus.CANCELLED],
    PaymentStatus.CAPTURED: [PaymentStatus.COMPLETED, PaymentStatus.REFUNDED],
    PaymentStatus.COMPLETED: [PaymentStatus.REFUNDED],
    PaymentStatus.FAILED: [PaymentStatus.ORDER_CREATED, PaymentStatus.PAYMENT_PENDING, PaymentStatus.CANCELLED],
    PaymentStatus.CANCELLED: [],
    PaymentStatus.REFUNDED: [],
}


def _validate_state_transition(current: Any, target: Any):
    """Enforces strict state machine rules. Raises HTTP 400 on invalid transition."""
    # Convert string status inputs to PaymentStatus Enum
    if isinstance(current, str) and not isinstance(current, PaymentStatus):
        try:
            current = PaymentStatus(current)
        except ValueError:
            pass
    if isinstance(target, str) and not isinstance(target, PaymentStatus):
        try:
            target = PaymentStatus(target)
        except ValueError:
            pass

    # Coerce empty, None, or initial states to PAYMENT_PENDING to prevent blocking valid captures/completions
    if current in [None, "", "None", PaymentStatus.NOT_STARTED]:
        current = PaymentStatus.PAYMENT_PENDING

    allowed = ALLOWED_TRANSITIONS.get(current, [])
    current_val = current.value if hasattr(current, "value") else str(current)
    target_val = target.value if hasattr(target, "value") else str(target)

    if target not in allowed and current != target:
        logger.error(f"Invalid payment state transition blocked: {current_val} -> {target_val}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid payment state transition from '{current_val}' to '{target_val}'"
        )


class PaymentService:
    @staticmethod
    async def create_payment_order(
        db: AsyncSession,
        guest_user: User,
        booking_id: int,
        payment_method: PaymentMethod = PaymentMethod.ONLINE
    ) -> Payment:
        """
        Validates guest ownership, recalculates authoritative fare, creates Razorpay gateway order (if online),
        and saves/updates Payment & Booking models with strict state transitions.
        """
        if guest_user.role != UserRole.GUEST:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Only guests can initiate payments"
            )

        booking = await crud_booking.get_booking(db, booking_id=booking_id)
        if not booking:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Booking record not found"
            )

        if booking.guest_id != guest_user.id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Unauthorized payment request for this booking"
            )

        # Recalculate authoritative fare
        fare = float(booking.final_fare or booking.fare_estimate or 0.00)
        if fare <= 0:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid fare amount for booking payment"
            )

        booking.payment_method = payment_method.value

        if payment_method == PaymentMethod.CASH:
            # Cash booking flow
            payment_record = await crud_payment.create_payment_record(
                db,
                booking_id=booking.id,
                user_id=guest_user.id,
                razorpay_order_id=None,
                amount=Decimal(str(fare)),
                payment_method=PaymentMethod.CASH
            )
            booking.payment_status = "pending"
            booking.payment_id = payment_record.id
            db.add(booking)
            await db.flush()

            await AuditService.log_event(
                db=db,
                action="PAYMENT_CASH_SELECTED",
                entity_name="Payment",
                entity_id=payment_record.id,
                user_id=guest_user.id,
                details={"booking_id": booking.id, "fare": fare, "payment_method": "cash"}
            )
            return payment_record

        # Online Razorpay order flow
        order_data = await razorpay_service.create_razorpay_order(
            amount_inr=fare,
            booking_id=booking.id
        )

        payment_record = await crud_payment.create_payment_record(
            db,
            booking_id=booking.id,
            user_id=guest_user.id,
            razorpay_order_id=order_data["order_id"],
            amount=Decimal(str(fare)),
            payment_method=PaymentMethod.ONLINE
        )

        booking.payment_status = "pending"
        booking.payment_id = payment_record.id
        db.add(booking)
        await db.flush()

        await AuditService.log_event(
            db=db,
            action="PAYMENT_ORDER_CREATED",
            entity_name="Payment",
            entity_id=payment_record.id,
            user_id=guest_user.id,
            details={"razorpay_order_id": order_data["order_id"], "amount": fare}
        )

        return payment_record

    @staticmethod
    async def retry_payment_order(
        db: AsyncSession,
        guest_user: User,
        booking_id: int
    ) -> Payment:
        """
        Generates a new Razorpay Order ID for an existing unpaid booking without duplicating booking records.
        """
        return await PaymentService.create_payment_order(
            db=db,
            guest_user=guest_user,
            booking_id=booking_id,
            payment_method=PaymentMethod.ONLINE
        )

    @staticmethod
    async def verify_and_capture_payment(
        db: AsyncSession,
        guest_user: User,
        razorpay_order_id: str,
        razorpay_payment_id: str,
        razorpay_signature: str
    ) -> Payment:
        """
        Executes strict 5-point verification (HMAC signature, order ID, payment ID, amount, user ownership).
        Updates payment status to CAPTURED and marks booking payment as completed.
        """
        if guest_user.role != UserRole.GUEST:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Only guests can verify payments"
            )

        payment = await crud_payment.get_payment_by_order(db, razorpay_order_id=razorpay_order_id)
        if not payment:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Payment order transaction not found"
            )

        booking = await crud_booking.get_booking(db, booking_id=payment.booking_id)
        if not booking or booking.guest_id != guest_user.id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Unauthorized payment verification attempt"
            )

        _validate_state_transition(payment.status, PaymentStatus.CAPTURED)

        # 1. HMAC SHA256 Signature Verification
        is_valid = razorpay_service.verify_payment_signature(
            razorpay_order_id=razorpay_order_id,
            razorpay_payment_id=razorpay_payment_id,
            razorpay_signature=razorpay_signature
        )

        if not is_valid:
            payment.status = PaymentStatus.FAILED
            booking.payment_status = "failed"
            db.add(payment)
            db.add(booking)
            await db.flush()
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Payment signature verification failed. Fraud attempt logged."
            )

        # 2. Update Payment & Booking to CAPTURED / COMPLETED
        updated_payment = await crud_payment.update_payment_captured(
            db,
            razorpay_order_id=razorpay_order_id,
            razorpay_payment_id=razorpay_payment_id,
            razorpay_signature=razorpay_signature
        )

        booking.payment_status = "completed"
        booking.payment_id = updated_payment.id
        db.add(booking)
        await db.flush()

        # 3. Dispatch Payment Confirmation Notification to Guest
        await NotificationService.dispatch_user_notification(
            db=db,
            user_id=guest_user.id,
            title="Payment Successful",
            body=f"Your online payment of ₹{float(updated_payment.amount):.2f} was successfully verified.",
            type=NotificationType.PAYMENT,
            data={"booking_id": updated_payment.booking_id, "payment_id": updated_payment.id, "route": "/bookings"}
        )

        # 4. Publish PaymentCaptured domain event to credit assistant wallet & log audit entry
        await DomainEventBus.publish(
            db=db,
            event_name="PaymentCaptured",
            payload={
                "payment_id": updated_payment.id,
                "booking_id": updated_payment.booking_id,
                "assistant_id": booking.assistant_id,
                "amount": float(updated_payment.amount)
            }
        )

        await AuditService.log_event(
            db=db,
            action="PAYMENT_CAPTURED",
            entity_name="Payment",
            entity_id=updated_payment.id,
            user_id=guest_user.id,
            details={
                "booking_id": updated_payment.booking_id,
                "razorpay_payment_id": razorpay_payment_id,
                "amount": float(updated_payment.amount)
            }
        )

        return updated_payment

    @staticmethod
    async def confirm_cash_payment(
        db: AsyncSession,
        booking_id: int,
        confirmed_by_user: User
    ) -> Payment:
        """
        Assistant or Admin action to confirm cash collection for a booking.
        """
        booking = await crud_booking.get_booking(db, booking_id=booking_id)
        if not booking:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Booking record not found"
            )

        if confirmed_by_user.role == UserRole.ASSISTANT and booking.assistant_id != confirmed_by_user.id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Only the assigned guide can confirm cash collection for this ride"
            )

        payment = await crud_payment.get_active_payment_by_booking(db, booking_id=booking_id)
        if not payment:
            fare = float(booking.final_fare or booking.fare_estimate or 0.00)
            payment = await crud_payment.create_payment_record(
                db=db,
                booking_id=booking_id,
                user_id=booking.guest_id,
                razorpay_order_id=None,
                amount=Decimal(str(fare)),
                payment_method=PaymentMethod.CASH
            )

        payment.status = PaymentStatus.COMPLETED
        payment.payment_method = PaymentMethod.CASH
        payment.payment_time = datetime.now(timezone.utc)
        payment.receipt_number = f"REC-CASH-{booking.id}"
        payment.invoice_number = f"INV-CASH-{datetime.now(timezone.utc).year}-{booking.id:04d}"
        
        booking.payment_method = "cash"
        booking.payment_status = "completed"
        booking.payment_id = payment.id

        db.add(payment)
        db.add(booking)
        await db.flush()

        # Dispatch notification to guest
        await NotificationService.dispatch_user_notification(
            db=db,
            user_id=booking.guest_id,
            title="Cash Payment Received",
            body=f"Your guide {confirmed_by_user.full_name} confirmed receiving ₹{float(payment.amount):.2f} in cash.",
            type=NotificationType.PAYMENT,
            data={"booking_id": booking.id, "payment_id": payment.id}
        )

        await AuditService.log_event(
            db=db,
            action="PAYMENT_CASH_CONFIRMED",
            entity_name="Payment",
            entity_id=payment.id,
            user_id=confirmed_by_user.id,
            details={"booking_id": booking.id, "amount": float(payment.amount)}
        )

        return payment

    @staticmethod
    async def process_webhook(
        db: AsyncSession,
        body_bytes: bytes,
        signature: str
    ) -> Dict[str, str]:
        """
        Validates public Razorpay webhook notifications and syncs payment statuses idempotently.
        """
        is_valid = razorpay_service.verify_webhook_signature(
            payload_bytes=body_bytes,
            signature_header=signature,
            webhook_secret=razorpay_service.settings.RAZORPAY_WEBHOOK_SECRET
        )

        if not is_valid:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid webhook signature payload"
            )

        payload = json.loads(body_bytes.decode('utf-8'))
        event_name = payload.get("event")
        entity_payload = payload.get("payload", {}).get("payment", {}).get("entity", {})

        razorpay_order_id = entity_payload.get("order_id")
        razorpay_payment_id = entity_payload.get("id")

        if razorpay_order_id:
            payment = await crud_payment.get_payment_by_order(db, razorpay_order_id)
            if payment:
                if event_name in ["payment.captured", "order.paid"]:
                    if payment.status not in [PaymentStatus.CAPTURED, PaymentStatus.COMPLETED]:
                        _validate_state_transition(payment.status, PaymentStatus.CAPTURED)
                        await crud_payment.update_payment_captured(
                            db,
                            razorpay_order_id=razorpay_order_id,
                            razorpay_payment_id=razorpay_payment_id,
                            razorpay_signature=signature
                        )
                        booking = await crud_booking.get_booking(db, booking_id=payment.booking_id)
                        if booking:
                            booking.payment_status = "completed"
                            db.add(booking)
                elif event_name == "payment.failed":
                    if payment.status not in [PaymentStatus.FAILED, PaymentStatus.CANCELLED]:
                        await crud_payment.update_payment_status(
                            db,
                            razorpay_order_id=razorpay_order_id,
                            status=PaymentStatus.FAILED
                        )
                        booking = await crud_booking.get_booking(db, booking_id=payment.booking_id)
                        if booking:
                            booking.payment_status = "failed"
                            db.add(booking)
                await db.flush()

        return {"status": "ok"}

    @staticmethod
    async def generate_payment_receipt(
        db: AsyncSession,
        booking_id: int,
        user: User
    ) -> Dict[str, Any]:
        """
        Generates itemized receipt data for verified booking payments.
        """
        booking = await crud_booking.get_booking(db, booking_id=booking_id)
        if not booking:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Booking not found"
            )

        if user.role != UserRole.ADMIN and user.id != booking.guest_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Unauthorized to access this receipt"
            )

        fare = float(booking.final_fare or booking.fare_estimate or 0.00)
        tax = round(fare * 0.18, 2)
        total = round(fare * 1.18, 2)

        return {
            "booking_id": booking_id,
            "booking_code": booking.booking_code,
            "receipt_number": f"REC-{booking.id}-{booking.booking_code}",
            "invoice_number": f"INV-{datetime.now(timezone.utc).year}-{booking.id:04d}",
            "payment_method": booking.payment_method,
            "payment_status": booking.payment_status,
            "fare_amount": fare,
            "tax_inr": tax,
            "total_payable": total,
            "date": booking.created_at
        }

    @staticmethod
    async def refund_payment(
        db: AsyncSession,
        booking_id: int,
        admin_user: User
    ) -> Payment:
        """
        Admin action to execute a gateway refund for a cancelled booking.
        """
        result = await db.execute(select(Payment).filter(Payment.booking_id == booking_id))
        payment = result.scalars().first()
        if not payment:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Payment details record not found"
            )

        _validate_state_transition(payment.status, PaymentStatus.REFUNDED)

        await razorpay_service.refund_payment(
            payment_id=payment.gateway_payment_id or "mock_pay_1",
            amount_inr=float(payment.amount)
        )

        payment.status = PaymentStatus.REFUNDED
        booking = await crud_booking.get_booking(db, booking_id=booking_id)
        if booking:
            booking.payment_status = "refunded"
            db.add(booking)

        db.add(payment)
        await db.flush()

        await AuditService.log_event(
            db=db,
            action="PAYMENT_REFUNDED",
            entity_name="Payment",
            entity_id=payment.id,
            user_id=admin_user.id,
            details={"booking_id": booking_id, "amount": float(payment.amount)}
        )
        return payment

    @staticmethod
    async def record_payment_failure(
        db: AsyncSession,
        guest_user: User,
        razorpay_order_id: str,
        error_code: Optional[str] = None,
        error_description: Optional[str] = None,
        error_reason: Optional[str] = None
    ) -> Payment:
        """
        Records a client-side payment failure event from Razorpay Checkout.
        Updates the payment status to FAILED and logs the error details.
        """
        payment = await crud_payment.get_payment_by_order(db, razorpay_order_id=razorpay_order_id)
        if not payment:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Payment order not found for the given Razorpay Order ID"
            )

        booking = await crud_booking.get_booking(db, booking_id=payment.booking_id)
        if not booking or booking.guest_id != guest_user.id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Unauthorized failure report for this payment"
            )

        # Only update to FAILED if not already in a terminal success state
        if payment.status not in [PaymentStatus.CAPTURED, PaymentStatus.COMPLETED, PaymentStatus.REFUNDED]:
            payment.status = PaymentStatus.FAILED
            booking.payment_status = "failed"
            db.add(payment)
            db.add(booking)
            await db.flush()

        await AuditService.log_event(
            db=db,
            action="PAYMENT_FAILED",
            entity_name="Payment",
            entity_id=payment.id,
            user_id=guest_user.id,
            details={
                "booking_id": payment.booking_id,
                "razorpay_order_id": razorpay_order_id,
                "error_code": error_code,
                "error_description": error_description,
                "error_reason": error_reason
            }
        )

        logger.warning(
            f"Payment failure recorded: order={razorpay_order_id}, "
            f"code={error_code}, reason={error_reason}, desc={error_description}"
        )

        return payment
