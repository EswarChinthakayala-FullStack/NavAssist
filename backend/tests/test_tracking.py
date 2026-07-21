import pytest
from unittest.mock import AsyncMock, patch, MagicMock
from httpx import AsyncClient
from datetime import datetime, timezone, timedelta
from app.main import app
from app.api.deps import get_current_user
from app.models import User, UserRole, Booking, BookingStatus, TripShare

@pytest.mark.asyncio
async def test_get_current_location(client: AsyncClient, mock_db: AsyncMock):
    # Mock user and booking
    mock_user = User(id=1, role=UserRole.GUEST, is_active=True)
    mock_booking = Booking(
        id=10, 
        guest_id=1, 
        assistant_id=2, 
        status=BookingStatus.ACCEPTED,
        pickup_latitude=12.97,
        pickup_longitude=77.59
    )
    
    app.dependency_overrides[get_current_user] = lambda: mock_user
    
    with patch("app.repositories.booking_repository.get_booking", new_callable=AsyncMock) as mock_get_booking, \
         patch("app.core.redis_client.get_assistant_location", new_callable=AsyncMock) as mock_get_loc:
        
        mock_get_booking.return_value = mock_booking
        mock_get_loc.return_value = (12.98, 77.60)
        
        response = await client.get("/api/v1/tracking/10/current-location")
        assert response.status_code == 200
        data = response.json()
        assert data["booking_id"] == 10
        assert data["latitude"] == 12.98
        assert data["longitude"] == 77.60
        assert data["source"] == "redis_cache"
        
    app.dependency_overrides.clear()


@pytest.mark.asyncio
async def test_generate_share_link(client: AsyncClient, mock_db: AsyncMock):
    mock_user = User(id=1, role=UserRole.GUEST, is_active=True)
    mock_booking = Booking(
        id=10, 
        guest_id=1, 
        status=BookingStatus.ACCEPTED
    )
    
    app.dependency_overrides[get_current_user] = lambda: mock_user
    
    with patch("app.repositories.booking_repository.get_booking", new_callable=AsyncMock) as mock_get_booking:
        mock_get_booking.return_value = mock_booking
        
        response = await client.post("/api/v1/share/10/generate-link")
        assert response.status_code == 200
        data = response.json()
        assert "share_token" in data
        assert "share_link" in data
        
    app.dependency_overrides.clear()


@pytest.mark.asyncio
async def test_bootstrap_demo(client: AsyncClient, mock_db: AsyncMock):
    """Verifies that the public demo bootstrapping endpoint configures test profiles and yields tokens."""
    mock_guest = User(id=1, phone="+919876543210", role=UserRole.GUEST, is_active=True)
    mock_assistant = User(id=2, phone="+918765432109", role=UserRole.ASSISTANT, is_active=True)
    mock_profile = MagicMock()
    
    with patch("app.repositories.user_repository.get_user_by_phone", new_callable=AsyncMock) as mock_get_user, \
         patch("app.repositories.assistant_repository.get_assistant", new_callable=AsyncMock) as mock_get_assistant, \
         patch("app.core.redis_client.update_assistant_location", new_callable=AsyncMock) as mock_update_loc:
         
        # Simulate users already exist to simplify mock database paths
        mock_get_user.side_effect = [mock_guest, mock_assistant]
        mock_get_assistant.return_value = mock_profile
        
        response = await client.post("/api/v1/admin/bootstrap-demo")
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "guest_token" in data
        assert "assistant_token" in data
        assert "booking_id" in data
        assert "share_token" in data
