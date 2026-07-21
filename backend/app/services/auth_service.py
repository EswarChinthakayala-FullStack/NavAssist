import logging
import hashlib
import secrets
from datetime import datetime, timedelta, timezone
from typing import Optional, Tuple
from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select

from app.repositories import user_repository
from app.models.user import User, UserRole, RefreshToken, DeviceToken, DeviceType, AuthProvider
from app.core import security
from app.services.domain_events import DomainEventBus
from app.services.audit_service import AuditService

logger = logging.getLogger(__name__)

class AuthService:
    @staticmethod
    async def register_user(
        db: AsyncSession,
        full_name: str,
        phone_number: str,
        user_type: UserRole = UserRole.GUEST,
        email: Optional[str] = None,
        password: Optional[str] = None,
        auth_provider: AuthProvider = AuthProvider.LOCAL,
        google_id: Optional[str] = None
    ) -> User:
        """
        Creates a new user record in the DB and publishes UserRegistered event
        to initialize wallet, default app settings, and audit trail automatically.
        """
        existing = await user_repository.get_user_by_phone(db, phone=phone_number)
        if existing:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="A user with this phone number already exists"
            )

        if email:
            existing_email = await user_repository.get_user_by_email(db, email=email)
            if existing_email:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="A user with this email address already exists"
                )

        import uuid
        password_hash = security.get_password_hash(password) if password else None

        user = User(
            public_id=str(uuid.uuid4()),
            full_name=full_name,
            phone_number=phone_number,
            email=email,
            password_hash=password_hash,
            user_type=user_type,
            auth_provider=auth_provider,
            google_id=google_id,
            is_phone_verified=True
        )
        db.add(user)
        await db.flush()

        # Publish UserRegistered event to trigger automatic wallet & app_settings creation
        await DomainEventBus.publish(
            db=db,
            event_name="UserRegistered",
            payload={"user_id": user.id, "auth_provider": auth_provider.value}
        )

        return user

    @staticmethod
    async def authenticate_user(
        db: AsyncSession, 
        phone: str, 
        password: str
    ) -> User:
        """Authenticates a user via phone number and password."""
        user = await user_repository.get_user_by_phone(db, phone=phone)
        if not user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Incorrect phone number or password"
            )
        if not user.password_hash or not security.verify_password(password, user.password_hash):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Incorrect phone number or password"
            )
        return user

    @staticmethod
    async def create_access_token(user: User) -> str:
        """Helper to sign a standard access JWT token for user."""
        return security.create_access_token(subject=user.id)

    @staticmethod
    async def create_refresh_token(
        db: AsyncSession,
        user_id: int,
        device_info: Optional[str] = None
    ) -> str:
        """
        Generates a secure random refresh token string, persists its hash in `refresh_tokens`,
        and returns the raw token string.
        """
        raw_token = secrets.token_urlsafe(48)
        token_hash = hashlib.sha256(raw_token.encode('utf-8')).hexdigest()
        expires_at = datetime.now(timezone.utc) + timedelta(days=30)

        refresh_entry = RefreshToken(
            user_id=user_id,
            token_hash=token_hash,
            device_info=device_info,
            expires_at=expires_at,
            revoked=False
        )
        db.add(refresh_entry)
        await db.flush()
        return raw_token

    @staticmethod
    async def rotate_refresh_token(
        db: AsyncSession,
        raw_refresh_token: str,
        device_info: Optional[str] = None
    ) -> Tuple[str, str]:
        """
        Validates an existing refresh token, revokes it, and issues a new (access_token, new_refresh_token) pair.
        """
        token_hash = hashlib.sha256(raw_refresh_token.encode('utf-8')).hexdigest()
        now = datetime.now(timezone.utc)

        res = await db.execute(
            select(RefreshToken).filter(
                RefreshToken.token_hash == token_hash,
                RefreshToken.revoked == False,
                RefreshToken.expires_at > now
            )
        )
        token_entry = res.scalars().first()
        if not token_entry:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid or expired refresh token"
            )

        # Revoke old token
        token_entry.revoked = True
        db.add(token_entry)

        # Fetch user
        user = await user_repository.get_user(db, user_id=token_entry.user_id)
        if not user or not user.is_active:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="User account inactive or deleted"
            )

        new_access_token = security.create_access_token(subject=user.id)
        new_refresh_token = await AuthService.create_refresh_token(db, user_id=user.id, device_info=device_info)
        await db.flush()

        return new_access_token, new_refresh_token

    @staticmethod
    async def revoke_refresh_token(db: AsyncSession, raw_refresh_token: str) -> bool:
        """Revokes a refresh token on logout."""
        token_hash = hashlib.sha256(raw_refresh_token.encode('utf-8')).hexdigest()
        res = await db.execute(select(RefreshToken).filter(RefreshToken.token_hash == token_hash))
        token_entry = res.scalars().first()
        if token_entry:
            token_entry.revoked = True
            db.add(token_entry)
            await db.flush()
            return True
        return False

    @staticmethod
    async def register_device_token(
        db: AsyncSession,
        user_id: int,
        fcm_token: str,
        device_type: DeviceType
    ) -> DeviceToken:
        """
        Registers or updates an FCM device token in `device_tokens` table.
        """
        res = await db.execute(
            select(DeviceToken).filter(
                DeviceToken.user_id == user_id,
                DeviceToken.fcm_token == fcm_token
            )
        )
        dt = res.scalars().first()
        now = datetime.now(timezone.utc)

        if not dt:
            dt = DeviceToken(
                user_id=user_id,
                fcm_token=fcm_token,
                device_type=device_type,
                last_used_at=now
            )
            db.add(dt)
        else:
            dt.device_type = device_type
            dt.last_used_at = now
            db.add(dt)

        await db.flush()
        return dt
