import pytest
from unittest.mock import AsyncMock, patch
from httpx import AsyncClient
from app.models import User, UserRole


@pytest.mark.asyncio
async def test_send_otp(client: AsyncClient):
    """Verifies that OTP send requests return success and dispatch background workers."""
    import asyncio
    payload = {"phone": "+919999999999"}
    
    # Mock set_otp in Redis client and Celery task dispatch
    with patch("app.core.redis_client.set_otp", new_callable=AsyncMock) as mock_set_otp, \
         patch("app.integrations.sms.send_sms", new_callable=AsyncMock) as mock_send_sms:
         
        response = await client.post("/api/v1/auth/otp/send", json=payload)
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "debug_otp" in data
        
        # Verify redis and worker calls were executed
        mock_set_otp.assert_called_once_with("+919999999999", data["debug_otp"], expire_seconds=300)
        
        # Sleep to yield control so background task runs
        await asyncio.sleep(0.01)
        mock_send_sms.assert_called_once_with("+919999999999", f"Your NavAssist verification code is: {data['debug_otp']}. Valid for 5 minutes. Do not share this code.")


@pytest.mark.asyncio
async def test_verify_otp_new_user(client: AsyncClient, mock_db: AsyncMock):
    """Verifies that validating OTP for a non-existent phone flags registration is required."""
    payload = {"phone": "+918888888888", "otp": "123456"}
    
    # Mock Redis client get_otp and delete_otp, and db user lookup
    with patch("app.core.redis_client.get_otp", new_callable=AsyncMock) as mock_get_otp, \
         patch("app.core.redis_client.delete_otp", new_callable=AsyncMock) as mock_delete_otp, \
         patch("app.repositories.user_repository.get_user_by_phone", new_callable=AsyncMock) as mock_get_user:
         
        mock_get_otp.return_value = "123456"
        mock_get_user.return_value = None  # User doesn't exist
        
        response = await client.post("/api/v1/auth/otp/verify", json=payload)
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["registered"] is False
        assert data["phone"] == "+918888888888"
        
        mock_delete_otp.assert_called_once_with("+918888888888")
        mock_get_user.assert_called_once()


@pytest.mark.asyncio
async def test_verify_otp_existing_user(client: AsyncClient, mock_db: AsyncMock):
    """Verifies that validating OTP for an existing user returns valid JWT tokens."""
    payload = {"phone": "+919999999999", "otp": "999999"}
    
    # Mock user object returned by database
    mock_user = User(
        id=1,
        phone="+919999999999",
        role=UserRole.GUEST,
        is_active=True
    )
    
    # Mock Redis client and db user lookup
    with patch("app.core.redis_client.get_otp", new_callable=AsyncMock) as mock_get_otp, \
         patch("app.core.redis_client.delete_otp", new_callable=AsyncMock) as mock_delete_otp, \
         patch("app.repositories.user_repository.get_user_by_phone", new_callable=AsyncMock) as mock_get_user:
         
        mock_get_otp.return_value = "999999"
        mock_get_user.return_value = mock_user
        
        response = await client.post("/api/v1/auth/otp/verify", json=payload)
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["registered"] is True
        assert "tokens" in data
        assert "access_token" in data["tokens"]
        assert "refresh_token" in data["tokens"]
        
        mock_delete_otp.assert_called_once_with("+919999999999")
