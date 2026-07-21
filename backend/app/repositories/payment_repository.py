import uuid
from datetime import datetime, timezone
from typing import Optional
from decimal import Decimal
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select

from app.models.payment import Payment, PaymentStatus, PaymentMethod
from app.repositories.base_repository import BaseRepository


class PaymentRepository(BaseRepository[Payment]):
    def __init__(self):
        super().__init__(Payment)

    async def get_payment(self, db: AsyncSession, payment_id: int) -> Optional[Payment]:
        """Fetch payment record by ID."""
        return await self.get(db, payment_id)

    async def get_payment_by_order(self, db: AsyncSession, razorpay_order_id: str) -> Optional[Payment]:
        """Fetch payment record by unique Razorpay Order ID."""
        result = await db.execute(select(Payment).filter(Payment.gateway_order_id == razorpay_order_id))
        return result.scalars().first()

    async def get_active_payment_by_booking(self, db: AsyncSession, booking_id: int) -> Optional[Payment]:
        """Fetch the most recent active payment record for a given booking ID."""
        result = await db.execute(
            select(Payment)
            .filter(Payment.booking_id == booking_id)
            .order_by(Payment.id.desc())
        )
        return result.scalars().first()

    async def create_payment_record(
        self,
        db: AsyncSession,
        booking_id: int,
        user_id: int,
        razorpay_order_id: Optional[str],
        amount: Decimal,
        payment_method: PaymentMethod = PaymentMethod.ONLINE,
        idempotency_key: Optional[str] = None
    ) -> Payment:
        """Creates or updates a payment record in ORDER_CREATED / PAYMENT_PENDING state."""
        # Find existing unpaid payment attempt for this booking
        existing = await self.get_active_payment_by_booking(db, booking_id=booking_id)
        if existing and existing.status in [PaymentStatus.NOT_STARTED, PaymentStatus.ORDER_CREATED, PaymentStatus.PAYMENT_PENDING, PaymentStatus.FAILED]:
            existing.gateway_order_id = razorpay_order_id or existing.gateway_order_id
            existing.amount = amount
            existing.payment_method = payment_method
            existing.status = PaymentStatus.ORDER_CREATED if razorpay_order_id else PaymentStatus.PAYMENT_PENDING
            existing.idempotency_key = idempotency_key or existing.idempotency_key or str(uuid.uuid4())
            db.add(existing)
            await db.flush()
            return existing

        ref_uuid = f"PAY-{booking_id}-{uuid.uuid4().hex[:8].upper()}"
        db_payment = Payment(
            booking_id=booking_id,
            user_id=user_id,
            gateway_order_id=razorpay_order_id,
            amount=amount,
            currency="INR",
            payment_method=payment_method,
            payment_reference=ref_uuid,
            idempotency_key=idempotency_key or str(uuid.uuid4()),
            status=PaymentStatus.ORDER_CREATED if razorpay_order_id else PaymentStatus.PAYMENT_PENDING
        )
        db.add(db_payment)
        await db.flush()
        return db_payment

    async def update_payment_captured(
        self,
        db: AsyncSession,
        razorpay_order_id: str,
        razorpay_payment_id: str,
        razorpay_signature: str
    ) -> Optional[Payment]:
        """Updates payment status to CAPTURED and logs signature/payment parameters on checkout verification."""
        payment = await self.get_payment_by_order(db, razorpay_order_id)
        if payment:
            payment.gateway_payment_id = razorpay_payment_id
            payment.gateway_signature = razorpay_signature
            payment.status = PaymentStatus.CAPTURED
            payment.payment_time = datetime.now(timezone.utc)
            payment.receipt_number = f"REC-BK{payment.booking_id}-{payment.id}"
            payment.invoice_number = f"INV-{datetime.now(timezone.utc).year}-{payment.booking_id:04d}"
            db.add(payment)
            await db.flush()
        return payment

    async def update_payment_status(
        self,
        db: AsyncSession,
        razorpay_order_id: str,
        status: PaymentStatus
    ) -> Optional[Payment]:
        """Updates payment record status (e.g. captured, failed, refunded)."""
        payment = await self.get_payment_by_order(db, razorpay_order_id)
        if payment:
            payment.status = status
            if status in [PaymentStatus.CAPTURED, PaymentStatus.COMPLETED]:
                payment.payment_time = datetime.now(timezone.utc)
            db.add(payment)
            await db.flush()
        return payment


# Module-level exports for backward compatibility
_repo = PaymentRepository()
get_payment = _repo.get_payment
get_payment_by_order = _repo.get_payment_by_order
get_active_payment_by_booking = _repo.get_active_payment_by_booking
create_payment_record = _repo.create_payment_record
update_payment_captured = _repo.update_payment_captured
update_payment_status = _repo.update_payment_status

# BaseRepository methods
get = _repo.get
get_multi = _repo.get_multi
create = _repo.create
update = _repo.update
remove = _repo.remove
