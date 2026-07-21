import enum
from datetime import datetime, timezone
from typing import List, Optional, Any
from sqlalchemy import String, Boolean, ForeignKey, DateTime, Integer, Enum, Numeric, LargeBinary, Index, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base
from app.models.user import MySQLPoint, parse_wkb_point, UserRole, OnlineStatus


class KycStatus(str, enum.Enum):
    NOT_SUBMITTED = "NOT_SUBMITTED"
    PENDING = "PENDING"
    VERIFIED = "APPROVED"
    REJECTED = "REJECTED"

    # Backward compatibility alias
    APPROVED = "APPROVED"


class PayoutStatus(str, enum.Enum):
    PENDING = "pending"
    PROCESSING = "processing"
    COMPLETED = "completed"
    FAILED = "failed"


class AssistantProfile(Base):
    __tablename__ = "assistant_profiles"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    bio: Mapped[Optional[str]] = mapped_column(String(1000), nullable=True)
    experience_years: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    aadhaar_number_enc: Mapped[Optional[bytes]] = mapped_column(LargeBinary(255), nullable=True)
    aadhaar_masked: Mapped[Optional[str]] = mapped_column(String(20), nullable=True)
    verification_status: Mapped[KycStatus] = mapped_column(Enum(KycStatus), default=KycStatus.NOT_SUBMITTED, nullable=False)
    kyc_reviewed_by: Mapped[Optional[int]] = mapped_column(Integer, ForeignKey("users.id", ondelete="SET NULL"), nullable=True)
    kyc_reviewed_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    trust_score: Mapped[float] = mapped_column(Numeric(3, 2), default=0.00, nullable=False)
    avg_rating: Mapped[float] = mapped_column(Numeric(2, 1), default=0.0, nullable=False)
    total_trips: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    is_online: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    current_location: Mapped[Any] = mapped_column(MySQLPoint, nullable=False, default=func.ST_PointFromText("POINT(0 0)", 4326))
    location_updated_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    service_radius_km: Mapped[float] = mapped_column(Numeric(4, 1), default=10.0, nullable=False)
    
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=lambda: datetime.now(timezone.utc),
        onupdate=lambda: datetime.now(timezone.utc),
        nullable=False
    )

    # Spatial Index
    __table_args__ = (
        Index("idx_assistant_location", "current_location", mysql_prefix="SPATIAL"),
    )

    # Relationships
    user: Mapped["User"] = relationship("User", back_populates="assistant_profile", foreign_keys=[user_id], lazy="joined")
    reviewer: Mapped[Optional["User"]] = relationship("User", foreign_keys=[kyc_reviewed_by])
    documents: Mapped[List["AssistantDocument"]] = relationship("AssistantDocument", back_populates="assistant", cascade="all, delete-orphan", lazy="selectin")
    payout_accounts: Mapped[List["PayoutAccount"]] = relationship("PayoutAccount", back_populates="assistant", cascade="all, delete-orphan")
    payouts: Mapped[List["Payout"]] = relationship("Payout", back_populates="assistant")

    @property
    def name(self) -> str:
        return self.user.full_name if self.user else ""

    @property
    def profile_photo_url(self) -> Optional[str]:
        return self.user.profile_photo_url if self.user else None

    @name.setter
    def name(self, val: str):
        if self.user:
            self.user.full_name = val

    @property
    def kyc_status(self) -> KycStatus:
        return self.verification_status

    @kyc_status.setter
    def kyc_status(self, val: KycStatus):
        self.verification_status = val

    @property
    def status(self) -> KycStatus:
        return self.verification_status

    @property
    def online_status(self) -> OnlineStatus:
        return OnlineStatus.ONLINE if self.is_online else OnlineStatus.OFFLINE

    @online_status.setter
    def online_status(self, val: OnlineStatus):
        self.is_online = (val == OnlineStatus.ONLINE or val == OnlineStatus.BUSY)

    @property
    def current_latitude(self) -> Optional[float]:
        if self.current_location and isinstance(self.current_location, bytes):
            try:
                _, lat = parse_wkb_point(self.current_location)
                return lat
            except Exception:
                pass
        return getattr(self, "_lat", None)

    @current_latitude.setter
    def current_latitude(self, val: Optional[float]):
        self._lat = val
        self._update_location()

    @property
    def current_longitude(self) -> Optional[float]:
        if self.current_location and isinstance(self.current_location, bytes):
            try:
                lon, _ = parse_wkb_point(self.current_location)
                return lon
            except Exception:
                pass
        return getattr(self, "_lon", None)

    @current_longitude.setter
    def current_longitude(self, val: Optional[float]):
        self._lon = val
        self._update_location()

    def _update_location(self):
        lat = getattr(self, "_lat", None)
        lon = getattr(self, "_lon", None)
        if lat is not None and lon is not None:
            from sqlalchemy import func
            self.current_location = func.ST_PointFromText(f"POINT({lon} {lat})", 4326)

    @property
    def assistant_id(self) -> int:
        return self.user_id

    @property
    def aadhaar_number(self) -> str:
        return self.aadhaar_masked or ""

    @property
    def doc_front_url(self) -> str:
        if hasattr(self, "_doc_front_url_override"):
            return self._doc_front_url_override
        for doc in self.documents:
            if doc.doc_type == "aadhaar_front":
                return doc.file_url
        return ""

    @doc_front_url.setter
    def doc_front_url(self, val: str):
        self._doc_front_url_override = val

    @property
    def doc_back_url(self) -> str:
        if hasattr(self, "_doc_back_url_override"):
            return self._doc_back_url_override
        for doc in self.documents:
            if doc.doc_type == "aadhaar_back":
                return doc.file_url
        return ""

    @doc_back_url.setter
    def doc_back_url(self, val: str):
        self._doc_back_url_override = val

    @property
    def review_notes(self) -> Optional[str]:
        return getattr(self, "_review_notes", None)

    @review_notes.setter
    def review_notes(self, val: Optional[str]):
        self._review_notes = val

    @property
    def reviewed_by(self) -> Optional[int]:
        return self.kyc_reviewed_by

    @property
    def reviewed_at(self) -> Optional[datetime]:
        return self.kyc_reviewed_at


class AssistantDocument(Base):
    __tablename__ = "assistant_documents"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    assistant_id: Mapped[int] = mapped_column(Integer, ForeignKey("assistant_profiles.id", ondelete="CASCADE"), nullable=False)
    doc_type: Mapped[str] = mapped_column(String(50), nullable=False)  # Enforces 'aadhaar_front', etc.
    file_url: Mapped[str] = mapped_column(String(500), nullable=False)
    verified: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    uploaded_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    # Relationships
    assistant: Mapped["AssistantProfile"] = relationship("AssistantProfile", back_populates="documents")


class PayoutAccount(Base):
    __tablename__ = "payout_accounts"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    assistant_id: Mapped[int] = mapped_column(Integer, ForeignKey("assistant_profiles.id", ondelete="CASCADE"), nullable=False)
    account_holder_name: Mapped[str] = mapped_column(String(150), nullable=False)
    account_number_enc: Mapped[bytes] = mapped_column(LargeBinary(255), nullable=False)
    ifsc_code: Mapped[Optional[str]] = mapped_column(String(11), nullable=True)
    upi_id: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    is_verified: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    # Relationships
    assistant: Mapped["AssistantProfile"] = relationship("AssistantProfile", back_populates="payout_accounts")


class Payout(Base):
    __tablename__ = "payouts"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    assistant_id: Mapped[int] = mapped_column(Integer, ForeignKey("assistant_profiles.id", ondelete="RESTRICT"), nullable=False)
    amount: Mapped[float] = mapped_column(Numeric(10, 2), nullable=False)
    status: Mapped[PayoutStatus] = mapped_column(Enum(PayoutStatus), default=PayoutStatus.PENDING, nullable=False)
    reference_id: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    initiated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )
    completed_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)

    # Relationships
    assistant: Mapped["AssistantProfile"] = relationship("AssistantProfile", back_populates="payouts")
