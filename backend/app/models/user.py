import enum
import struct
from datetime import datetime, timezone
from typing import List, Optional, Any
from sqlalchemy import String, Boolean, ForeignKey, DateTime, Integer, Enum, Float, Numeric, LargeBinary, Index, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy.types import UserDefinedType

from app.core.database import Base
from app.models.base import TimestampMixin


def parse_wkb_point(wkb: bytes) -> tuple[float, float]:
    """Parses MySQL's internal spatial geometry bytes into (longitude, latitude)."""
    if not wkb:
        raise ValueError("Empty WKB bytes")
    # MySQL spatial format: 4 bytes SRID + 1 byte byte-order + 4 bytes geometry type + 8 bytes X + 8 bytes Y
    if len(wkb) == 25:
        # Determine endianness from the 5th byte (index 4)
        endian = "<" if wkb[4] == 1 else ">"
        srid, byte_order, geom_type, x, y = struct.unpack(f"{endian}IBIdd", wkb)
        return x, y
    elif len(wkb) == 21:
        # Standard WKB (no SRID prefix)
        endian = "<" if wkb[0] == 1 else ">"
        byte_order, geom_type, x, y = struct.unpack(f"{endian}BIdd", wkb)
        return x, y
    raise ValueError(f"Invalid WKB geometry length: {len(wkb)}")


class MySQLPoint(UserDefinedType):
    """Custom SQLAlchemy type for representing MySQL's POINT spatial data type."""
    def get_col_spec(self, **kw):
        return "POINT"


class UserRole(str, enum.Enum):
    GUEST = "guest"
    ASSISTANT = "assistant"
    ADMIN = "admin"


class UserStatus(str, enum.Enum):
    ACTIVE = "active"
    SUSPENDED = "suspended"
    DELETED = "deleted"


class OnlineStatus(str, enum.Enum):
    ONLINE = "online"
    OFFLINE = "offline"
    BUSY = "busy"


class AuthProvider(str, enum.Enum):
    LOCAL = "local"
    GOOGLE = "google"


class DeviceType(str, enum.Enum):
    ANDROID = "android"
    IOS = "ios"
    WEB = "web"


class OtpPurpose(str, enum.Enum):
    SIGNUP = "signup"
    LOGIN = "login"
    RESET_PASSWORD = "reset_password"


class User(Base, TimestampMixin):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    public_id: Mapped[str] = mapped_column(String(36), unique=True, index=True, nullable=False)
    full_name: Mapped[str] = mapped_column(String(150), nullable=False)
    email: Mapped[Optional[str]] = mapped_column(String(150), unique=True, index=True, nullable=True)
    phone_number: Mapped[str] = mapped_column(String(15), unique=True, index=True, nullable=False)
    password_hash: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    user_type: Mapped[UserRole] = mapped_column(Enum(UserRole), default=UserRole.GUEST, nullable=False)
    profile_photo_url: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    auth_provider: Mapped[AuthProvider] = mapped_column(Enum(AuthProvider), default=AuthProvider.LOCAL, nullable=False)
    google_id: Mapped[Optional[str]] = mapped_column(String(255), unique=True, nullable=True)
    is_email_verified: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    is_phone_verified: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    status: Mapped[UserStatus] = mapped_column(Enum(UserStatus), default=UserStatus.ACTIVE, nullable=False)

    # Relationships
    device_tokens: Mapped[List["DeviceToken"]] = relationship("DeviceToken", back_populates="user", cascade="all, delete-orphan")
    refresh_tokens: Mapped[List["RefreshToken"]] = relationship("RefreshToken", back_populates="user", cascade="all, delete-orphan")
    assistant_profile: Mapped[Optional["AssistantProfile"]] = relationship("AssistantProfile", back_populates="user", cascade="all, delete-orphan", uselist=False, foreign_keys="[AssistantProfile.user_id]", lazy="selectin")
    emergency_contacts: Mapped[List["EmergencyContact"]] = relationship("EmergencyContact", back_populates="user", cascade="all, delete-orphan")
    app_settings: Mapped[Optional["AppSetting"]] = relationship("AppSetting", back_populates="user", cascade="all, delete-orphan", uselist=False)

    @property
    def role(self) -> UserRole:
        return self.user_type

    @role.setter
    def role(self, val: UserRole):
        self.user_type = val

    @property
    def phone(self) -> str:
        return self.phone_number

    @phone.setter
    def phone(self, val: str):
        self.phone_number = val

    @property
    def is_active(self) -> bool:
        return self.status == UserStatus.ACTIVE

    @is_active.setter
    def is_active(self, val: bool):
        self.status = UserStatus.ACTIVE if val else UserStatus.SUSPENDED

    @property
    def guest(self) -> Optional[dict]:
        if self.user_type == UserRole.GUEST:
            return {
                "user_id": self.id,
                "name": self.full_name,
                "profile_picture_url": self.profile_photo_url,
                "created_at": self.created_at
            }
        return None

    @property
    def assistant(self) -> Optional[dict]:
        if self.user_type == UserRole.ASSISTANT and self.assistant_profile:
            return {
                "user_id": self.id,
                "name": self.assistant_profile.name,
                "profile_picture_url": self.assistant_profile.profile_picture_url,
                "kyc_status": self.assistant_profile.kyc_status,
                "online_status": self.assistant_profile.online_status,
                "current_latitude": self.assistant_profile.current_latitude,
                "current_longitude": self.assistant_profile.current_longitude,
                "created_at": self.assistant_profile.created_at,
                "updated_at": self.assistant_profile.updated_at
            }
        return None


class DeviceToken(Base):
    __tablename__ = "device_tokens"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    fcm_token: Mapped[str] = mapped_column(String(500), nullable=False)
    device_type: Mapped[DeviceType] = mapped_column(Enum(DeviceType), nullable=False)
    last_used_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    # Relationships
    user: Mapped["User"] = relationship("User", back_populates="device_tokens")

    __table_args__ = (
        Index("uq_user_fcm", "user_id", "fcm_token", unique=True),
    )


class RefreshToken(Base):
    __tablename__ = "refresh_tokens"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    token_hash: Mapped[str] = mapped_column(String(255), nullable=False)
    device_info: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    revoked: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    # Relationships
    user: Mapped["User"] = relationship("User", back_populates="refresh_tokens")


class AppSetting(Base):
    __tablename__ = "app_settings"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    language: Mapped[str] = mapped_column(String(10), default="en", nullable=False)
    dark_mode: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    notifications_enabled: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    # Relationships
    user: Mapped["User"] = relationship("User", back_populates="app_settings")


class OtpVerification(Base):
    __tablename__ = "otp_verifications"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    identifier: Mapped[str] = mapped_column(String(150), index=True, nullable=False)
    otp_hash: Mapped[str] = mapped_column(String(255), nullable=False)
    purpose: Mapped[OtpPurpose] = mapped_column(Enum(OtpPurpose), nullable=False)
    attempt_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    is_verified: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    __table_args__ = (
        Index("idx_identifier_purpose", "identifier", "purpose"),
    )
