import pytest
from decimal import Decimal
from unittest.mock import AsyncMock, patch, MagicMock
from fastapi import HTTPException
from app.models.booking import Booking, BookingStatus
from app.models import User, UserRole, KycStatus, AssistantProfile as Assistant
from app.services.booking_service import BookingService, BookingStateMachine


@pytest.mark.asyncio
async def test_booking_state_machine_boundaries():
    """Confirms state transition boundaries defined in the Uber-standard state machine."""
    # Valid transitions
    assert BookingStateMachine.is_transition_valid(None, BookingStatus.PENDING) is True
    assert BookingStateMachine.is_transition_valid(BookingStatus.PENDING, BookingStatus.ACCEPTED) is True
    assert BookingStateMachine.is_transition_valid(BookingStatus.PENDING, BookingStatus.CANCELLED) is True
    assert BookingStateMachine.is_transition_valid(BookingStatus.ACCEPTED, BookingStatus.STARTED) is True
    assert BookingStateMachine.is_transition_valid(BookingStatus.STARTED, BookingStatus.COMPLETED) is True

    # Invalid transitions
    assert BookingStateMachine.is_transition_valid(BookingStatus.PENDING, BookingStatus.STARTED) is False
    assert BookingStateMachine.is_transition_valid(BookingStatus.COMPLETED, BookingStatus.PENDING) is False


@pytest.mark.asyncio
async def test_active_ride_boundary_blocks_duplicate_booking(mock_db: AsyncMock):
    """Verifies that a passenger with an ongoing active ride cannot create another booking."""
    existing_active = Booking(id=50, guest_id=10, status=BookingStatus.IN_PROGRESS)

    with patch("app.repositories.booking_repository.get_active_booking_by_guest", return_value=existing_active):
        with pytest.raises(HTTPException) as exc:
            await BookingService.create_booking_request(
                db=mock_db,
                guest_id=10,
                pickup_latitude=19.0760,
                pickup_longitude=72.8777,
                pickup_address="Gateway of India, Mumbai",
                destination_latitude=19.0888,
                destination_longitude=72.8888,
                destination_address="Bandra West, Mumbai"
            )
        assert exc.value.status_code == 400
        assert "You already have an active ride" in exc.value.detail


@pytest.mark.asyncio
async def test_invalid_coordinates_rejected(mock_db: AsyncMock):
    """Verifies that 0,0 coordinates or empty addresses are immediately rejected with HTTP 400."""
    with patch("app.repositories.booking_repository.get_active_booking_by_guest", return_value=None):
        with pytest.raises(HTTPException) as exc:
            await BookingService.create_booking_request(
                db=mock_db,
                guest_id=10,
                pickup_latitude=0.0,
                pickup_longitude=0.0,
                pickup_address="Unknown",
                destination_latitude=19.0888,
                destination_longitude=72.8888,
                destination_address="Bandra West, Mumbai"
            )
        assert exc.value.status_code == 400
        assert "Invalid pickup geographical coordinates" in exc.value.detail


@pytest.mark.asyncio
async def test_concurrent_accept_race_condition_protection(mock_db: AsyncMock):
    """Verifies that attempting to accept a booking already assigned to another guide raises HTTP 409 Conflict."""
    already_assigned = Booking(
        id=100,
        guest_id=10,
        assistant_id=5, # Already claimed by Assistant #5
        status=BookingStatus.ASSIGNED
    )

    with patch("app.repositories.booking_repository.get_booking_for_update", return_value=already_assigned), \
         patch("app.repositories.booking_repository.get_booking", return_value=already_assigned):
        with pytest.raises(HTTPException) as exc:
            await BookingService.transition_booking(
                db=mock_db,
                booking_id=100,
                new_status=BookingStatus.ACCEPTED,
                changed_by_user_id=8, # Assistant #8 attempting concurrent accept
                assistant_id=8
            )
        assert exc.value.status_code == 409
        assert "already been accepted by another guide" in exc.value.detail
