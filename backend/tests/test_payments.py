import pytest
from decimal import Decimal
from unittest.mock import AsyncMock, patch, MagicMock
from fastapi import HTTPException
from app.models.payment import Payment, PaymentStatus, PaymentMethod
from app.models.booking import Booking, BookingStatus
from app.models import User, UserRole
from app.services.payment_service import PaymentService, _validate_state_transition
from app.services.booking_service import BookingService


@pytest.mark.asyncio
async def test_payment_state_machine_validations():
    """Confirms that strict payment state machine rules block invalid transitions."""
    # Valid transition
    _validate_state_transition(PaymentStatus.ORDER_CREATED, PaymentStatus.CAPTURED)
    _validate_state_transition(PaymentStatus.CAPTURED, PaymentStatus.COMPLETED)

    # Invalid transition (FAILED -> COMPLETED should raise HTTP 400)
    with pytest.raises(HTTPException) as exc_info:
        _validate_state_transition(PaymentStatus.FAILED, PaymentStatus.COMPLETED)
    assert exc_info.value.status_code == 400

    # Invalid transition (REFUNDED -> CAPTURED should raise HTTP 400)
    with pytest.raises(HTTPException) as exc_info:
        _validate_state_transition(PaymentStatus.REFUNDED, PaymentStatus.CAPTURED)
    assert exc_info.value.status_code == 400


@pytest.mark.asyncio
async def test_create_payment_order_guest_only(mock_db: AsyncMock):
    """Verifies that non-guests cannot create payment orders."""
    assistant_user = User(id=5, role=UserRole.ASSISTANT, full_name="Guide User")
    with pytest.raises(HTTPException) as exc_info:
        await PaymentService.create_payment_order(
            db=mock_db,
            guest_user=assistant_user,
            booking_id=10
        )
    assert exc_info.value.status_code == 403


@pytest.mark.asyncio
async def test_ride_start_blocked_when_unpaid(mock_db: AsyncMock):
    """Verifies that starting an online ride without verified captured payment raises HTTP 402."""
    guest = User(id=1, role=UserRole.GUEST)
    assistant = User(id=2, role=UserRole.ASSISTANT)
    booking = Booking(
        id=99,
        guest_id=1,
        assistant_id=2,
        status=BookingStatus.ACCEPTED,
        payment_method="online",
        payment_status="pending",
        otp_start="123456"
    )

    with patch("app.repositories.booking_repository.get_booking", return_value=booking), \
         patch("app.repositories.payment_repository.get_active_payment_by_booking", return_value=None):

        with pytest.raises(HTTPException) as exc:
            await BookingService.update_status(
                db=mock_db,
                booking_id=99,
                new_status=BookingStatus.STARTED,
                changed_by_user_id=2,
                otp="123456"
            )
        assert exc.value.status_code == 402
        assert "Online payment has not been captured" in exc.value.detail


@pytest.mark.asyncio
async def test_confirm_cash_payment_flow(mock_db: AsyncMock):
    """Verifies cash confirmation workflow by assigned assistant."""
    assistant = User(id=2, role=UserRole.ASSISTANT, full_name="Assigned Helper")
    booking = Booking(
        id=101,
        guest_id=1,
        assistant_id=2,
        status=BookingStatus.IN_PROGRESS,
        payment_method="cash",
        payment_status="pending",
        final_fare=250.00
    )

    with patch("app.repositories.booking_repository.get_booking", return_value=booking), \
         patch("app.repositories.payment_repository.get_active_payment_by_booking", return_value=None), \
         patch("app.repositories.payment_repository.create_payment_record", return_value=Payment(id=50, booking_id=101, user_id=1, amount=Decimal("250.00"), status=PaymentStatus.PAYMENT_PENDING)), \
         patch("app.services.notification_service.NotificationService.dispatch_user_notification", new_callable=AsyncMock), \
         patch("app.services.audit_service.AuditService.log_event", new_callable=AsyncMock):

        payment = await PaymentService.confirm_cash_payment(
            db=mock_db,
            booking_id=101,
            confirmed_by_user=assistant
        )

        assert payment.payment_method == PaymentMethod.CASH
        assert payment.status == PaymentStatus.COMPLETED
        assert booking.payment_status == "completed"
