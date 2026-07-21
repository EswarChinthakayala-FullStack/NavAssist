import enum
from datetime import datetime, timezone
from typing import Optional, Any
from sqlalchemy import String, ForeignKey, DateTime, Integer, Enum, Boolean, Index, CHAR
from sqlalchemy.orm import Mapped, mapped_column, relationship as orm_relationship

from app.core.database import Base
from app.models.user import MySQLPoint, parse_wkb_point, User


class SosStatus(str, enum.Enum):
    ACTIVE = "active"
    RESOLVED = "resolved"
    FALSE_ALARM = "false_alarm"


class SosAlert(Base):
    __tablename__ = "sos_alerts"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    booking_id: Mapped[Optional[int]] = mapped_column(Integer, ForeignKey("bookings.id", ondelete="CASCADE"), nullable=True)
    user_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="RESTRICT"), nullable=False)
    coordinates: Mapped[Any] = mapped_column(MySQLPoint, nullable=False)
    status: Mapped[SosStatus] = mapped_column(Enum(SosStatus), default=SosStatus.ACTIVE, nullable=False)
    triggered_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )
    resolved_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    resolved_by: Mapped[Optional[int]] = mapped_column(Integer, ForeignKey("users.id", ondelete="SET NULL"), nullable=True)

    __table_args__ = (
        Index("idx_sos_coordinates", "coordinates", mysql_prefix="SPATIAL"),
    )

    # Relationships
    booking: Mapped[Optional["Booking"]] = orm_relationship("Booking", back_populates="sos_alerts")
    user: Mapped["User"] = orm_relationship("User", foreign_keys=[user_id])
    resolver: Mapped[Optional["User"]] = orm_relationship("User", foreign_keys=[resolved_by])

    @property
    def triggered_by(self) -> int:
        return self.user_id

    @triggered_by.setter
    def triggered_by(self, val: int):
        self.user_id = val

    @property
    def latitude(self) -> float:
        if self.coordinates and isinstance(self.coordinates, bytes):
            try:
                _, lat = parse_wkb_point(self.coordinates)
                return lat
            except Exception:
                pass
        return getattr(self, "_lat", 0.0)

    @latitude.setter
    def latitude(self, val: float):
        self._lat = val
        self._update_coordinates()

    @property
    def longitude(self) -> float:
        if self.coordinates and isinstance(self.coordinates, bytes):
            try:
                lon, _ = parse_wkb_point(self.coordinates)
                return lon
            except Exception:
                pass
        return getattr(self, "_lon", 0.0)

    @longitude.setter
    def longitude(self, val: float):
        self._lon = val
        self._update_coordinates()

    def _update_coordinates(self):
        lat = getattr(self, "_lat", None)
        lon = getattr(self, "_lon", None)
        if lat is not None and lon is not None:
            from sqlalchemy import func
            self.coordinates = func.ST_PointFromText(f"POINT({lon} {lat})", 4326)


class EmergencyContact(Base):
    __tablename__ = "emergency_contacts"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    name: Mapped[str] = mapped_column(String(150), nullable=False)
    phone_number: Mapped[str] = mapped_column(String(15), nullable=False)
    relationship: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    # Relationships
    user: Mapped["User"] = orm_relationship("User", back_populates="emergency_contacts", foreign_keys=[user_id])

    @property
    def phone(self) -> str:
        return self.phone_number

    @phone.setter
    def phone(self, value: str):
        self.phone_number = value


class TripShare(Base):
    __tablename__ = "trip_shares"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    booking_id: Mapped[int] = mapped_column(Integer, ForeignKey("bookings.id", ondelete="CASCADE"), nullable=False)
    share_token: Mapped[str] = mapped_column(CHAR(32), unique=True, nullable=False)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    # Relationships
    booking: Mapped["Booking"] = orm_relationship("Booking", back_populates="shares")
    creator: Mapped["User"] = orm_relationship("User", foreign_keys=[created_by])
