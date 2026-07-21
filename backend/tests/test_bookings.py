import pytest
from decimal import Decimal
from unittest.mock import AsyncMock, patch, MagicMock
from fastapi import HTTPException
from app.models.booking import Booking, BookingStatus, BookingStatusHistory
from app.models import User, UserRole, KycStatus, AssistantProfile as Assistant
from app.services.booking_service import BookingService, BookingStateMachine


@pytest.mark.asyncio
async def test_state_machine_validation():
    """Confirms state transition boundaries defined in the state machine."""
    # Valid transitions
    assert BookingStateMachine.is_transition_valid(None, BookingStatus.PENDING) is True
    assert BookingStateMachine.is_transition_valid(BookingStatus.PENDING, BookingStatus.ACCEPTED) is True
    assert BookingStateMachine.is_transition_valid(BookingStatus.PENDING, BookingStatus.CANCELLED) is True
    assert BookingStateMachine.is_transition_valid(BookingStatus.PENDING, BookingStatus.EXPIRED) is True
    assert BookingStateMachine.is_transition_valid(BookingStatus.ACCEPTED, BookingStatus.STARTED) is True
    assert BookingStateMachine.is_transition_valid(BookingStatus.ACCEPTED, BookingStatus.CANCELLED) is True
    assert BookingStateMachine.is_transition_valid(BookingStatus.STARTED, BookingStatus.COMPLETED) is True
    
    # Invalid transitions
    assert BookingStateMachine.is_transition_valid(BookingStatus.PENDING, BookingStatus.STARTED) is False
    assert BookingStateMachine.is_transition_valid(BookingStatus.STARTED, BookingStatus.CANCELLED) is False
    assert BookingStateMachine.is_transition_valid(BookingStatus.COMPLETED, BookingStatus.PENDING) is False


@pytest.mark.asyncio
async def test_create_booking_request(mock_db: AsyncMock):
    """Verifies that requesting a booking validates ongoing rides and writes a PENDING history record."""
    guest_id = 1
    
    # Mock lookup returning no active bookings (allowed to request)
    with patch("app.repositories.booking_repository.get_active_booking_by_guest", new_callable=AsyncMock) as mock_active, \
         patch("app.integrations.maps.get_route_details", new_callable=AsyncMock) as mock_route, \
         patch("app.repositories.booking_repository.create_status_history_record", new_callable=AsyncMock) as mock_history:
         
        mock_active.return_value = None
        mock_route.return_value = {"distance_meters": 10000, "duration_seconds": 1200, "estimated_fare": 150.0}
        
        # Set database mock add to assign id=10
        mock_db.add.side_effect = lambda instance: setattr(instance, 'id', 10)
        
        booking = await BookingService.create_booking_request(
            db=mock_db,
            guest_id=guest_id,
            pickup_latitude=12.97,
            pickup_longitude=77.59,
            pickup_address="Station A",
            destination_latitude=12.90,
            destination_longitude=77.60,
            destination_address="Hotel B"
        )
        
        assert booking.id == 10
        assert booking.status == BookingStatus.PENDING
        
        # Verify db insert & history trace
        mock_db.add.assert_any_call(booking)
        mock_history.assert_called_once_with(
            db=mock_db,
            booking_id=10,
            old_status=None,
            new_status=BookingStatus.PENDING,
            changed_by=guest_id,
            remarks="Booking request initiated"
        )


@pytest.mark.asyncio
async def test_transition_booking_unauthorized(mock_db: AsyncMock):
    """Verifies that unauthorized users (neither guest, assistant, nor admin) cannot mutate a booking."""
    booking_id = 12
    guest_id = 1
    assistant_id = 2
    hacker_id = 3
    
    mock_booking = Booking(
        id=booking_id,
        guest_id=guest_id,
        assistant_id=assistant_id,
        status=BookingStatus.ACCEPTED
    )
    
    # Mock changer user object as standard Guest role (not admin)
    mock_changer = User(id=hacker_id, role=UserRole.GUEST, is_active=True)
    
    with patch("app.repositories.booking_repository.get_booking", new_callable=AsyncMock) as mock_get, \
         patch("app.repositories.user_repository.get_user", new_callable=AsyncMock) as mock_get_user:
         
        mock_get.return_value = mock_booking
        mock_get_user.return_value = mock_changer
        
        with pytest.raises(HTTPException) as exc_info:
            await BookingService.transition_booking(
                db=mock_db,
                booking_id=booking_id,
                new_status=BookingStatus.CANCELLED,
                changed_by_user_id=hacker_id
            )
            
        assert exc_info.value.status_code == 403
        assert "Unauthorized" in exc_info.value.detail


@pytest.mark.asyncio
async def test_transition_booking_accept(mock_db: AsyncMock):
    """Verifies that accepting a booking assigns the assistant and records transition logs."""
    booking_id = 20
    guest_id = 1
    assistant_id = 2
    
    mock_booking = Booking(id=booking_id, guest_id=guest_id, status=BookingStatus.PENDING)
    mock_assistant = Assistant(user_id=assistant_id, kyc_status=KycStatus.APPROVED)
    
    with patch("app.repositories.booking_repository.get_booking", new_callable=AsyncMock) as mock_get, \
         patch("app.repositories.assistant_repository.assistant_repository.get_assistant", new_callable=AsyncMock) as mock_get_assistant, \
         patch("app.repositories.booking_repository.get_active_booking_by_assistant", new_callable=AsyncMock) as mock_active, \
         patch("app.repositories.booking_repository.create_status_history_record", new_callable=AsyncMock) as mock_history:
         
        mock_get.return_value = mock_booking
        mock_get_assistant.return_value = mock_assistant
        mock_active.return_value = None  # Assistant is free
        
        booking = await BookingService.transition_booking(
            db=mock_db,
            booking_id=booking_id,
            new_status=BookingStatus.ACCEPTED,
            changed_by_user_id=assistant_id,
            assistant_id=assistant_id
        )
        
        assert booking.status == BookingStatus.ACCEPTED
        assert booking.assistant_id == assistant_id
        
        # Verify history logs
        mock_history.assert_called_once_with(
            db=mock_db,
            booking_id=booking_id,
            old_status=BookingStatus.PENDING,
            new_status=BookingStatus.ACCEPTED,
            changed_by=assistant_id,
            remarks="Booking accepted by assistant"
        )
