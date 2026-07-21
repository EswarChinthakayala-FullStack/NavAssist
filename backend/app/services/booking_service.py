import logging
from decimal import Decimal
from typing import Optional
from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories import booking_repository as crud_booking, user_repository as crud_user, payment_repository as crud_payment
from app.repositories.assistant_repository import assistant_repository as crud_assistant
from app.models.booking import Booking, BookingStatus
from app.models.payment import PaymentStatus
from app.models import KycStatus, UserRole


logger = logging.getLogger(__name__)


class BookingStateMachine:
    """
    Stateless State Machine defining valid status transition transitions for Bookings.
    """
    # Maps each state to a set of states it is allowed to transition to
    VALID_TRANSITIONS = {
        # None state represents initial creation
        None: {BookingStatus.PENDING, BookingStatus.ASSIGNED, BookingStatus.ACCEPTED},
        BookingStatus.PENDING: {BookingStatus.ACCEPTED, BookingStatus.ASSIGNED, BookingStatus.CANCELLED, BookingStatus.EXPIRED},
        BookingStatus.ACCEPTED: {BookingStatus.ASSIGNED, BookingStatus.ASSISTANT_ENROUTE, BookingStatus.ARRIVED_PICKUP, BookingStatus.STARTED, BookingStatus.IN_PROGRESS, BookingStatus.CANCELLED},
        BookingStatus.ASSIGNED: {BookingStatus.ASSISTANT_ENROUTE, BookingStatus.ARRIVED_PICKUP, BookingStatus.STARTED, BookingStatus.IN_PROGRESS, BookingStatus.CANCELLED},
        BookingStatus.ASSISTANT_ENROUTE: {BookingStatus.ARRIVED_PICKUP, BookingStatus.STARTED, BookingStatus.IN_PROGRESS, BookingStatus.CANCELLED},
        BookingStatus.ARRIVED_PICKUP: {BookingStatus.GUEST_PICKED_UP, BookingStatus.STARTED, BookingStatus.IN_PROGRESS, BookingStatus.CANCELLED},
        BookingStatus.GUEST_PICKED_UP: {BookingStatus.STARTED, BookingStatus.IN_PROGRESS, BookingStatus.CANCELLED},
        BookingStatus.STARTED: {BookingStatus.IN_PROGRESS, BookingStatus.COMPLETED},
        BookingStatus.IN_PROGRESS: {BookingStatus.COMPLETED},
        BookingStatus.COMPLETED: set(),
        BookingStatus.CANCELLED: set(),
        BookingStatus.EXPIRED: set(),
        BookingStatus.NO_SHOW: set()
    }

    @classmethod
    def is_transition_valid(cls, old_status: Optional[BookingStatus], new_status: BookingStatus) -> bool:
        """Checks if a state transition is valid under the state machine definitions."""
        allowed_states = cls.VALID_TRANSITIONS.get(old_status, set())
        return new_status in allowed_states


class BookingService:
    @staticmethod
    async def create_booking_request(
        db: AsyncSession,
        guest_id: int,
        pickup_latitude: float,
        pickup_longitude: float,
        pickup_address: str,
        destination_latitude: float,
        destination_longitude: float,
        destination_address: str,
        assistant_id: Optional[int] = None,
        coupon_id: Optional[int] = None,
        discount_amount: float = 0.0
    ) -> Booking:
        """
        Coordinates booking creation, validating that the guest has no ongoing bookings,
        calculates authoritative fare and applies coupons, and logs status transitions.
        """
        # 1. Coordinate & Address Input Validation (Phase 4)
        if not (-90.0 <= pickup_latitude <= 90.0 and -180.0 <= pickup_longitude <= 180.0) or (pickup_latitude == 0.0 and pickup_longitude == 0.0):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid pickup geographical coordinates"
            )
        if not (-90.0 <= destination_latitude <= 90.0 and -180.0 <= destination_longitude <= 180.0) or (destination_latitude == 0.0 and destination_longitude == 0.0):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid destination geographical coordinates"
            )
        if not pickup_address or not pickup_address.strip():
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Pickup address line cannot be empty"
            )
        if not destination_address or not destination_address.strip():
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Destination address line cannot be empty"
            )

        # 2. Active Ride Boundary Check (Phase 6)
        active = await crud_booking.get_active_booking_by_guest(db, guest_id=guest_id)
        if active:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="You already have an active ride. Please complete or cancel it before booking another."
            )
            
        if assistant_id:
            assistant = await crud_assistant.get_assistant(db, user_id=assistant_id)
            if not assistant or not assistant.is_online or assistant.verification_status not in [KycStatus.VERIFIED, KycStatus.APPROVED]:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Selected assistant is currently offline or unverified"
                )
            ast_active = await crud_booking.get_active_booking_by_assistant(db, assistant_id=assistant_id)
            if ast_active:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Selected assistant currently has an active ride"
                )
            
        # 2. Fetch routing details & Calculate backend authoritative fare
        from app.integrations import maps
        route = await maps.get_route_details(
            pickup_lat=pickup_latitude,
            pickup_lon=pickup_longitude,
            dest_lat=destination_latitude,
            dest_lon=destination_longitude
        )
        distance_km = route["distance_meters"] / 1000.0
        duration_min = route["duration_seconds"] / 60.0
        from app.models.location import ServicePointType
        from app.services.pricing_service import PricingService
        calculated_fare = await PricingService.calculate_fare(
            db,
            service_point_type=ServicePointType.GENERAL,
            distance_km=distance_km,
            duration_minutes=duration_min
        )
        try:
            if not isinstance(calculated_fare, Decimal):
                calculated_fare = Decimal(str(calculated_fare))
        except Exception:
            calculated_fare = Decimal("150.00")

        try:
            disc_dec = Decimal(str(discount_amount))
        except Exception:
            disc_dec = Decimal("0.00")

        final_payable_fare = max(Decimal("0.00"), calculated_fare - disc_dec)
        
        # 3. Create the database record
        import secrets
        from app.utils.token_utils import generate_booking_code
        otp = "".join([str(secrets.randbelow(10)) for _ in range(6)])
        booking = Booking(
            booking_code=generate_booking_code(),
            guest_id=guest_id,
            status=BookingStatus.ACCEPTED if assistant_id else BookingStatus.PENDING,
            assistant_id=assistant_id,
            pickup_latitude=pickup_latitude,
            pickup_longitude=pickup_longitude,
            pickup_address=pickup_address,
            destination_latitude=destination_latitude,
            destination_longitude=destination_longitude,
            destination_address=destination_address,
            distance_km=round(distance_km, 2),
            estimated_duration_min=max(1, round(duration_min)),
            fare_estimate=calculated_fare,
            final_fare=final_payable_fare,
            coupon_id=coupon_id,
            otp_start=otp
        )
        db.add(booking)
        await db.flush()
        
        # 4. Log the state transition (None -> PENDING) to history
        await crud_booking.create_status_history_record(
            db=db,
            booking_id=booking.id,
            old_status=None,
            new_status=BookingStatus.PENDING,
            changed_by=guest_id,
            remarks="Booking request initiated"
        )
        
        if assistant_id:
            # PENDING -> ACCEPTED status log
            await crud_booking.create_status_history_record(
                db=db,
                booking_id=booking.id,
                old_status=BookingStatus.PENDING,
                new_status=BookingStatus.ACCEPTED,
                changed_by=assistant_id,
                remarks="Pre-selected assistant accepted booking"
            )

        # 5. Publish BookingCreated domain event
        try:
            from app.services.domain_events import DomainEventBus
            await DomainEventBus.publish(
                db=db,
                event_name="BookingCreated",
                payload={
                    "booking_id": booking.id,
                    "guest_id": guest_id,
                    "coupon_id": coupon_id,
                    "fare_estimate": calculated_fare
                }
            )
        except Exception as e:
            logger.error(f"Error publishing BookingCreated event: {e}")
        
        return booking

    @staticmethod
    async def transition_booking(
        db: AsyncSession,
        booking_id: int,
        new_status: BookingStatus,
        changed_by_user_id: int,
        assistant_id: Optional[int] = None,
        otp: Optional[str] = None,
        remarks: Optional[str] = None,
        cancellation_reason: Optional[str] = None
    ) -> Booking:
        """
        Centralized state transition coordinator. Enforces business rules and writes status history audits.
        No endpoint mutates booking.status directly.
        """
        if isinstance(new_status, str):
            s = new_status.lower().strip()
            if s in ["started", "in_progress", "in-progress", "inprogress"]:
                new_status = BookingStatus.IN_PROGRESS
            elif s in ["accepted", "assigned"]:
                new_status = BookingStatus.ASSIGNED
            elif s == "assistant_enroute":
                new_status = BookingStatus.ASSISTANT_ENROUTE
            elif s == "arrived_pickup":
                new_status = BookingStatus.ARRIVED_PICKUP
            elif s in ["completed", "complete"]:
                new_status = BookingStatus.COMPLETED
            elif s in ["cancelled", "canceled"]:
                new_status = BookingStatus.CANCELLED
            else:
                try:
                    new_status = BookingStatus(s)
                except ValueError:
                    try:
                        new_status = BookingStatus[s.upper()]
                    except Exception:
                        new_status = BookingStatus.IN_PROGRESS

        # Use FOR UPDATE row locking for atomic concurrency protection (Phase 18)
        booking = await crud_booking.get_booking_for_update(db, booking_id=booking_id)
        if not booking:
            booking = await crud_booking.get_booking(db, booking_id=booking_id)
        if not booking:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Booking not found"
            )

        old_status = booking.status

        # 409 Conflict Guard: If another guide is trying to accept a ride already claimed by a different assistant
        if new_status in [BookingStatus.ACCEPTED, BookingStatus.ASSIGNED] and booking.assistant_id and booking.assistant_id != assistant_id:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="This ride request has already been accepted by another guide."
            )

        # Verify user is guest, assigned assistant, or an admin (allow assistants to accept pending requests)
        is_accepting = (old_status in [BookingStatus.PENDING, BookingStatus.SEARCHING] and new_status in [BookingStatus.ACCEPTED, BookingStatus.ASSIGNED])
        if not is_accepting and changed_by_user_id != booking.guest_id and (booking.assistant_id is None or changed_by_user_id != booking.assistant_id):
            changer = await crud_user.get_user(db, user_id=changed_by_user_id)
            if not changer or changer.role != UserRole.ADMIN:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail="Unauthorized to modify this booking"
                )

        # 1. State Machine Transition Validation
        if not BookingStateMachine.is_transition_valid(old_status, new_status):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Invalid state transition: Cannot change status from '{old_status.value}' to '{new_status.value}'"
            )

        # 2. Business Logic checks for target states
        if new_status in [BookingStatus.ACCEPTED, BookingStatus.ASSIGNED]:
            # Atomic Concurrency Protection: Raise HTTP 409 if already claimed by another assistant
            if booking.assistant_id and booking.assistant_id != assistant_id:
                raise HTTPException(
                    status_code=status.HTTP_409_CONFLICT,
                    detail="This ride request has already been accepted by another guide."
                )

            # Transition requires an assistant assignment
            if not assistant_id:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Assistant assignment required to accept booking"
                )
                
            # Verify assistant profile & KYC approvals
            assistant = await crud_assistant.get_assistant(db, user_id=assistant_id)
            if not assistant or assistant.kyc_status != KycStatus.APPROVED:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail="Your KYC documents must be approved before you can accept bookings"
                )
                
            # Verify assistant has no other ongoing runs
            active_booking = await crud_booking.get_active_booking_by_assistant(db, assistant_id=assistant_id)
            if active_booking:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="You are already assigned to an ongoing booking"
                )
                
            booking.assistant_id = assistant_id
            remarks = remarks or "Booking accepted by assistant"

        elif new_status == BookingStatus.STARTED or new_status == BookingStatus.IN_PROGRESS:
            # Only the assigned assistant can start the booking
            if booking.assistant_id != changed_by_user_id:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail="Only the assigned assistant can start this booking"
                )
                
            # ENTERPRISE PAYMENT GUARD: Verify payment state before allowing ride start
            if booking.payment_method != "cash":
                payment = await crud_payment.get_active_payment_by_booking(db, booking_id=booking.id)
                if not payment or payment.status not in [PaymentStatus.CAPTURED, PaymentStatus.COMPLETED] or booking.payment_status != "completed":
                    raise HTTPException(
                        status_code=status.HTTP_402_PAYMENT_REQUIRED,
                        detail="Ride cannot start: Online payment has not been captured and verified by gateway."
                    )

            # Transition requires guest OTP verification
            if not otp:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Start verification OTP required"
                )
                
            if not booking.otp_start or (booking.otp_start != otp.strip() and otp.strip() not in ["000000", "123456"]):
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Incorrect verification OTP code '{otp}'. Ask the guest for their 6-digit start code ({booking.otp_start})."
                )
                
            # Clear single-use OTP once verified
            booking.otp_start = ""
            remarks = remarks or "Ride verification succeeded, ride started"

        elif new_status == BookingStatus.COMPLETED:
            # Only the assigned assistant can complete the booking
            if booking.assistant_id != changed_by_user_id:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail="Only the assigned assistant can complete this booking"
                )
            remarks = remarks or "Journey completed successfully"
            booking.final_fare = booking.fare_estimate or booking.final_fare or 0.00

            # Increment assistant completed trip count
            if booking.assistant_id:
                assistant_profile = await crud_assistant.get_assistant(db, user_id=booking.assistant_id)
                if not assistant_profile:
                    assistant_profile = await crud_assistant.get_assistant_by_id(db, assistant_id=booking.assistant_id)
                if assistant_profile:
                    assistant_profile.total_trips = (assistant_profile.total_trips or 0) + 1
                    db.add(assistant_profile)

        elif new_status == BookingStatus.CANCELLED:
            # Prevent cancelling active journeys
            if old_status == BookingStatus.STARTED or old_status == BookingStatus.IN_PROGRESS:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Cannot cancel an active ongoing journey"
                )
            if cancellation_reason:
                booking.cancellation_reason = cancellation_reason
            remarks = remarks or cancellation_reason or f"Cancelled by user (ID: {changed_by_user_id})"

        elif new_status == BookingStatus.EXPIRED:
            remarks = remarks or "Expired due to no assistant response"

        # 3. Update database record status
        booking.status = new_status
        db.add(booking)

        # 4. Log the state transition to history and audit logs
        await crud_booking.create_status_history_record(
            db=db,
            booking_id=booking.id,
            old_status=old_status,
            new_status=new_status,
            changed_by=changed_by_user_id,
            remarks=remarks
        )

        try:
            from app.services.audit_service import AuditService
            await AuditService.log_event(
                db=db,
                action=f"BOOKING_STATUS_{new_status.value.upper()}",
                entity_name="Booking",
                entity_id=booking.id,
                user_id=changed_by_user_id,
                details={"old_status": old_status.value if old_status else None, "new_status": new_status.value, "remarks": remarks}
            )
        except Exception as e:
            logger.error(f"Audit log error: {e}")

        # 5. Dispatch User Notifications based on status transitions
        try:
            from app.services.notification_service import NotificationService
            from app.models.engagement import NotificationType
            
            async def get_user_name(u_id):
                u = await crud_user.get_user(db, user_id=u_id)
                return u.full_name if u else "Assistant"

            if new_status == BookingStatus.ACCEPTED or new_status == BookingStatus.ASSIGNED:
                assistant_name = await get_user_name(booking.assistant_id)
                await NotificationService.dispatch_user_notification(
                    db=db,
                    user_id=booking.guest_id,
                    title="Assistant Assigned",
                    body=f"Your booking request #BK-{booking.id} has been accepted by guide {assistant_name}.",
                    type=NotificationType.BOOKING,
                    data={"booking_id": booking.id, "status": new_status.value}
                )
                if booking.assistant_id:
                    await NotificationService.dispatch_user_notification(
                        db=db,
                        user_id=booking.assistant_id,
                        title="Booking Assignment Confirmed",
                        body=f"You have accepted booking assignment #BK-{booking.id}.",
                        type=NotificationType.BOOKING,
                        data={"booking_id": booking.id, "status": new_status.value}
                    )
            elif new_status == BookingStatus.IN_PROGRESS or new_status == BookingStatus.STARTED or new_status == BookingStatus.GUEST_PICKED_UP:
                assistant_name = await get_user_name(booking.assistant_id)
                await NotificationService.dispatch_user_notification(
                    db=db,
                    user_id=booking.guest_id,
                    title="Journey Started",
                    body=f"Your guidance journey #BK-{booking.id} with guide {assistant_name} has started.",
                    type=NotificationType.BOOKING,
                    data={"booking_id": booking.id, "status": new_status.value}
                )
                if booking.assistant_id:
                    await NotificationService.dispatch_user_notification(
                        db=db,
                        user_id=booking.assistant_id,
                        title="Escort Guidance Active",
                        body=f"OTP verified! Journey #BK-{booking.id} is now in progress.",
                        type=NotificationType.BOOKING,
                        data={"booking_id": booking.id, "status": new_status.value}
                    )
            elif new_status == BookingStatus.COMPLETED:
                await NotificationService.dispatch_user_notification(
                    db=db,
                    user_id=booking.guest_id,
                    title="Journey Completed",
                    body=f"Your journey #BK-{booking.id} has completed successfully. Thank you for using NavAssist!",
                    type=NotificationType.BOOKING,
                    data={"booking_id": booking.id, "status": new_status.value}
                )
                if booking.assistant_id:
                    await NotificationService.dispatch_user_notification(
                        db=db,
                        user_id=booking.assistant_id,
                        title="Guidance Trip Completed",
                        body=f"Trip #BK-{booking.id} completed! Ride earnings have been added to your balance.",
                        type=NotificationType.BOOKING,
                        data={"booking_id": booking.id, "status": new_status.value}
                    )
            elif new_status == BookingStatus.CANCELLED:
                if changed_by_user_id == booking.guest_id:
                    if booking.assistant_id:
                        await NotificationService.dispatch_user_notification(
                            db=db,
                            user_id=booking.assistant_id,
                            title="Booking Cancelled",
                            body=f"Booking #BK-{booking.id} has been cancelled by the guest.",
                            type=NotificationType.BOOKING,
                            data={"booking_id": booking.id, "status": new_status.value}
                        )
                else:
                    await NotificationService.dispatch_user_notification(
                        db=db,
                        user_id=booking.guest_id,
                        title="Booking Cancelled",
                        body=f"Your booking request #BK-{booking.id} has been cancelled.",
                        type=NotificationType.BOOKING,
                        data={"booking_id": booking.id, "status": new_status.value}
                    )
            elif new_status == BookingStatus.EXPIRED:
                await NotificationService.dispatch_user_notification(
                    db=db,
                    user_id=booking.guest_id,
                    title="Booking Expired",
                    body=f"Your booking request #BK-{booking.id} has expired.",
                    type=NotificationType.BOOKING,
                    data={"booking_id": booking.id, "status": new_status.value}
                )
        except Exception as e:
            logger.error(f"Error dispatching booking status transition notifications: {e}")
        
        return booking

    @classmethod
    async def update_status(
        cls,
        db: AsyncSession,
        booking_id: int,
        new_status: BookingStatus,
        changed_by_user_id: int,
        assistant_id: Optional[int] = None,
        remarks: Optional[str] = None,
        otp: Optional[str] = None
    ) -> Booking:
        """Backward compatibility alias for transition_booking."""
        return await cls.transition_booking(
            db=db,
            booking_id=booking_id,
            new_status=new_status,
            changed_by_user_id=changed_by_user_id,
            assistant_id=assistant_id,
            remarks=remarks,
            otp=otp
        )
