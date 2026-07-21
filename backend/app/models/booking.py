import enum
from datetime import datetime, timezone
from typing import List, Optional, Any
from sqlalchemy import String, ForeignKey, DateTime, Integer, Enum, Boolean, Numeric, CHAR, Index
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base
from app.models.user import MySQLPoint, parse_wkb_point


class BookingStatus(str, enum.Enum):
    PENDING = "pending"
    SEARCHING = "searching"
    ASSIGNED = "assigned"
    ASSISTANT_ENROUTE = "assistant_enroute"
    ARRIVED_PICKUP = "arrived_pickup"
    GUEST_PICKED_UP = "guest_picked_up"
    IN_PROGRESS = "in_progress"
    COMPLETED = "completed"
    CANCELLED = "cancelled"
    NO_SHOW = "no_show"

    # Backward compatibility aliases
    ACCEPTED = "assigned"
    STARTED = "in_progress"
    EXPIRED = "cancelled"


class CancelledBy(str, enum.Enum):
    GUEST = "guest"
    ASSISTANT = "assistant"
    ADMIN = "admin"
    SYSTEM = "system"


class Booking(Base):
    __tablename__ = "bookings"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    booking_code: Mapped[str] = mapped_column(String(20), unique=True, index=True, nullable=False)
    guest_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="RESTRICT"), nullable=False)
    assistant_id: Mapped[Optional[int]] = mapped_column(Integer, ForeignKey("users.id", ondelete="RESTRICT"), nullable=True)
    pickup_service_point_id: Mapped[Optional[int]] = mapped_column(Integer, ForeignKey("service_points.id", ondelete="RESTRICT"), nullable=True)
    
    pickup_address: Mapped[str] = mapped_column(String(500), nullable=False)
    pickup_coordinates: Mapped[Any] = mapped_column(MySQLPoint, nullable=False)
    
    destination_address: Mapped[str] = mapped_column(String(500), nullable=False)
    destination_coordinates: Mapped[Any] = mapped_column(MySQLPoint, nullable=False)
    
    scheduled_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    status: Mapped[BookingStatus] = mapped_column(Enum(BookingStatus), default=BookingStatus.PENDING, nullable=False)
    distance_km: Mapped[Optional[float]] = mapped_column(Numeric(6, 2), nullable=True)
    estimated_duration_min: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    fare_estimate: Mapped[Optional[float]] = mapped_column(Numeric(10, 2), nullable=True)
    final_fare: Mapped[Optional[float]] = mapped_column(Numeric(10, 2), nullable=True)
    
    coupon_id: Mapped[Optional[int]] = mapped_column(Integer, ForeignKey("coupons.id", ondelete="RESTRICT"), nullable=True)
    payment_method: Mapped[str] = mapped_column(String(20), default="online", nullable=False)
    payment_status: Mapped[str] = mapped_column(String(20), default="pending", nullable=False)
    payment_id: Mapped[Optional[int]] = mapped_column(Integer, ForeignKey("payments.id", ondelete="SET NULL"), nullable=True)

    cancellation_reason: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    cancelled_by: Mapped[Optional[CancelledBy]] = mapped_column(Enum(CancelledBy), nullable=True)
    
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

    @property
    def fare_amount(self) -> Optional[float]:
        return self.fare_estimate

    @fare_amount.setter
    def fare_amount(self, val: Optional[float]):
        self.fare_estimate = val

    @property
    def otp_start(self) -> str:
        if hasattr(self, "_otp_start") and self._otp_start:
            return self._otp_start
        if self.id:
            raw = (self.id * 7919 + 147258) % 900000 + 100000
            return str(raw)
        return "123456"

    @otp_start.setter
    def otp_start(self, val: str):
        self._otp_start = val

    @property
    def pickup_latitude(self) -> float:
        if self.pickup_coordinates and isinstance(self.pickup_coordinates, bytes):
            try:
                _, lat = parse_wkb_point(self.pickup_coordinates)
                return lat
            except Exception:
                pass
        return getattr(self, "_pickup_lat", 0.0)

    @pickup_latitude.setter
    def pickup_latitude(self, val: float):
        self._pickup_lat = val
        self._update_pickup_coordinates()

    @property
    def pickup_longitude(self) -> float:
        if self.pickup_coordinates and isinstance(self.pickup_coordinates, bytes):
            try:
                lon, _ = parse_wkb_point(self.pickup_coordinates)
                return lon
            except Exception:
                pass
        return getattr(self, "_pickup_lon", 0.0)

    @pickup_longitude.setter
    def pickup_longitude(self, val: float):
        self._pickup_lon = val
        self._update_pickup_coordinates()

    def _update_pickup_coordinates(self):
        lat = getattr(self, "_pickup_lat", None)
        lon = getattr(self, "_pickup_lon", None)
        if lat is not None and lon is not None:
            from sqlalchemy import func
            self.pickup_coordinates = func.ST_PointFromText(f"POINT({lon} {lat})", 4326)

    @property
    def destination_latitude(self) -> float:
        if self.destination_coordinates and isinstance(self.destination_coordinates, bytes):
            try:
                _, lat = parse_wkb_point(self.destination_coordinates)
                return lat
            except Exception:
                pass
        return getattr(self, "_dest_lat", 0.0)

    @destination_latitude.setter
    def destination_latitude(self, val: float):
        self._dest_lat = val
        self._update_destination_coordinates()

    @property
    def destination_longitude(self) -> float:
        if self.destination_coordinates and isinstance(self.destination_coordinates, bytes):
            try:
                lon, _ = parse_wkb_point(self.destination_coordinates)
                return lon
            except Exception:
                pass
        return getattr(self, "_dest_lon", 0.0)

    @destination_longitude.setter
    def destination_longitude(self, val: float):
        self._dest_lon = val
        self._update_destination_coordinates()

    def _update_destination_coordinates(self):
        lat = getattr(self, "_dest_lat", None)
        lon = getattr(self, "_dest_lon", None)
        if lat is not None and lon is not None:
            from sqlalchemy import func
            self.destination_coordinates = func.ST_PointFromText(f"POINT({lon} {lat})", 4326)

    __table_args__ = (
        Index("idx_booking_pickup_location", "pickup_coordinates", mysql_prefix="SPATIAL"),
        Index("idx_booking_dest_location", "destination_coordinates", mysql_prefix="SPATIAL"),
        Index("idx_booking_guest_status", "guest_id", "status"),
        Index("idx_booking_assistant_status", "assistant_id", "status"),
    )

    # Relationships
    guest: Mapped["User"] = relationship("User", foreign_keys=[guest_id])
    assistant: Mapped[Optional["User"]] = relationship("User", foreign_keys=[assistant_id])
    payments: Mapped[List["Payment"]] = relationship("Payment", back_populates="booking", foreign_keys="Payment.booking_id", cascade="all, delete-orphan")
    active_payment: Mapped[Optional["Payment"]] = relationship("Payment", foreign_keys=[payment_id])
    status_history: Mapped[List["BookingStatusHistory"]] = relationship("BookingStatusHistory", back_populates="booking", cascade="all, delete-orphan")
    live_locations: Mapped[List["LiveLocation"]] = relationship("LiveLocation", back_populates="booking", cascade="all, delete-orphan")
    redemption: Mapped[Optional["CouponRedemption"]] = relationship("CouponRedemption", back_populates="booking", cascade="all, delete-orphan", uselist=False)
    sos_alerts: Mapped[List["SosAlert"]] = relationship("SosAlert", back_populates="booking", cascade="all, delete-orphan")
    shares: Mapped[List["TripShare"]] = relationship("TripShare", back_populates="booking", cascade="all, delete-orphan")
    invoice: Mapped[Optional["Invoice"]] = relationship("Invoice", back_populates="booking", uselist=False, cascade="all, delete-orphan")


class BookingStatusHistory(Base):
    __tablename__ = "booking_status_history"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    booking_id: Mapped[int] = mapped_column(Integer, ForeignKey("bookings.id", ondelete="CASCADE"), nullable=False)
    status: Mapped[str] = mapped_column(String(30), nullable=False)
    changed_by: Mapped[Optional[int]] = mapped_column(Integer, ForeignKey("users.id", ondelete="SET NULL"), nullable=True)
    changed_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    # Relationships
    booking: Mapped["Booking"] = relationship("Booking", back_populates="status_history")
    changer: Mapped[Optional["User"]] = relationship("User", foreign_keys=[changed_by])


class LiveLocation(Base):
    __tablename__ = "live_locations"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    booking_id: Mapped[int] = mapped_column(Integer, ForeignKey("bookings.id", ondelete="CASCADE"), nullable=False)
    actor_type: Mapped[str] = mapped_column(String(20), nullable=False)  # 'guest' or 'assistant'
    coordinates: Mapped[Any] = mapped_column(MySQLPoint, nullable=False)
    heading: Mapped[Optional[float]] = mapped_column(Numeric(5, 2), nullable=True)
    recorded_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

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

    __table_args__ = (
        Index("idx_live_coordinates", "coordinates", mysql_prefix="SPATIAL"),
        Index("idx_live_booking", "booking_id", "recorded_at"),
    )

    # Relationships
    booking: Mapped["Booking"] = relationship("Booking", back_populates="live_locations")
