import pytest
from decimal import Decimal
from unittest.mock import AsyncMock, patch, MagicMock
from fastapi import HTTPException

from app.models.user import User, UserRole, DeviceType
from app.models.assistant import AssistantProfile, PayoutAccount, Payout, PayoutStatus, KycStatus
from app.models.payment import Wallet, WalletTransaction, WalletTransactionType, WalletTransactionRefType
from app.services.payout_service import PayoutService
from app.services.domain_events import DomainEventBus
from app.services.auth_service import AuthService


@pytest.mark.asyncio
async def test_domain_event_bus_user_registered(mock_db: AsyncMock):
    """Verifies that publishing UserRegistered event initializes wallet and app settings."""
    user_id = 42

    with patch("app.services.wallet_service.WalletService.get_or_create_wallet", new_callable=AsyncMock) as mock_wallet:
        mock_wallet.return_value = Wallet(user_id=user_id, balance=0.00)
        
        mock_result = MagicMock()
        mock_result.scalars.return_value.first.return_value = None
        mock_db.execute.return_value = mock_result

        await DomainEventBus.publish(
            db=mock_db,
            event_name="UserRegistered",
            payload={"user_id": user_id, "auth_provider": "local"}
        )

        mock_wallet.assert_called_once_with(mock_db, user_id=user_id)
        mock_db.add.assert_called()


@pytest.mark.asyncio
async def test_payout_account_creation(mock_db: AsyncMock):
    """Verifies assistant payout account registration and updates."""
    user_id = 5
    mock_profile = AssistantProfile(id=10, user_id=user_id)

    mock_result1 = MagicMock()
    mock_result1.scalars.return_value.first.return_value = mock_profile

    mock_result2 = MagicMock()
    mock_result2.scalars.return_value.first.return_value = None

    mock_db.execute.side_effect = [mock_result1, mock_result2]

    account = await PayoutService.create_or_update_payout_account(
        db=mock_db,
        user_id=user_id,
        account_holder_name="John Doe",
        account_number="1234567890",
        ifsc_code="HDFC0001234",
        upi_id="john@upi"
    )

    assert account.account_holder_name == "John Doe"
    assert account.ifsc_code == "HDFC0001234"
    assert account.upi_id == "john@upi"
    assert account.is_verified is True


@pytest.mark.asyncio
async def test_request_payout_insufficient_balance(mock_db: AsyncMock):
    """Verifies that requesting a payout with insufficient wallet balance raises HTTPException."""
    user_id = 5
    mock_profile = AssistantProfile(id=10, user_id=user_id)
    mock_account = PayoutAccount(id=1, assistant_id=10, is_verified=True)
    mock_wallet = Wallet(id=1, user_id=user_id, balance=50.00)

    with patch("app.services.payout_service.PayoutService.get_payout_account", new_callable=AsyncMock) as mock_get_acc, \
         patch("app.services.wallet_service.WalletService.get_or_create_wallet", new_callable=AsyncMock) as mock_get_wallet:
        
        mock_result = MagicMock()
        mock_result.scalars.return_value.first.return_value = mock_profile
        mock_db.execute.return_value = mock_result

        mock_get_acc.return_value = mock_account
        mock_get_wallet.return_value = mock_wallet

        with pytest.raises(HTTPException) as exc_info:
            await PayoutService.request_payout(
                db=mock_db,
                user_id=user_id,
                amount=Decimal("100.00")
            )

        assert exc_info.value.status_code == 400
        assert "Insufficient wallet balance" in exc_info.value.detail


@pytest.mark.asyncio
async def test_auth_service_device_token_registration(mock_db: AsyncMock):
    """Verifies FCM device token registration via AuthService."""
    user_id = 7
    mock_result = MagicMock()
    mock_result.scalars.return_value.first.return_value = None
    mock_db.execute.return_value = mock_result

    token_entry = await AuthService.register_device_token(
        db=mock_db,
        user_id=user_id,
        fcm_token="fcm_sample_token_123",
        device_type=DeviceType.ANDROID
    )

    assert token_entry.user_id == user_id
    assert token_entry.fcm_token == "fcm_sample_token_123"
    assert token_entry.device_type == DeviceType.ANDROID
    mock_db.add.assert_called_once_with(token_entry)
