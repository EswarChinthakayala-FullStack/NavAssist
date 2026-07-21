import enum
from datetime import datetime, timezone
from typing import Optional
from sqlalchemy import String, ForeignKey, DateTime, Integer, Enum, Boolean, Numeric
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base
from app.models.location import ServicePointType


class CouponDiscountType(str, enum.Enum):
    FLAT = "flat"
    PERCENTAGE = "percentage"


class Coupon(Base):
    __tablename__ = "coupons"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    code: Mapped[str] = mapped_column(String(30), unique=True, nullable=False)
    description: Mapped[str] = mapped_column(String(255), nullable=False)
    discount_type: Mapped[CouponDiscountType] = mapped_column(Enum(CouponDiscountType), nullable=False)
    discount_value: Mapped[float] = mapped_column(Numeric(8, 2), nullable=False)
    max_discount_amount: Mapped[Optional[float]] = mapped_column(Numeric(8, 2), nullable=True)
    min_booking_amount: Mapped[float] = mapped_column(Numeric(8, 2), default=0.00, nullable=False)
    usage_limit_per_user: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    total_usage_limit: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    valid_from: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    valid_to: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )


class CouponRedemption(Base):
    __tablename__ = "coupon_redemptions"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    coupon_id: Mapped[int] = mapped_column(Integer, ForeignKey("coupons.id", ondelete="RESTRICT"), nullable=False)
    user_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="RESTRICT"), nullable=False)
    booking_id: Mapped[int] = mapped_column(Integer, ForeignKey("bookings.id", ondelete="CASCADE"), unique=True, nullable=False)
    redeemed_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    # Relationships
    booking: Mapped["Booking"] = relationship("Booking", back_populates="redemption")
    coupon: Mapped["Coupon"] = relationship("Coupon")
    user: Mapped["User"] = relationship("User", foreign_keys=[user_id])


class FareRule(Base):
    __tablename__ = "fare_rules"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    service_point_type: Mapped[ServicePointType] = mapped_column(Enum(ServicePointType), nullable=False)
    base_fare: Mapped[float] = mapped_column(Numeric(8, 2), nullable=False)
    per_km_rate: Mapped[float] = mapped_column(Numeric(6, 2), nullable=False)
    per_min_rate: Mapped[float] = mapped_column(Numeric(6, 2), nullable=False)
    min_fare: Mapped[float] = mapped_column(Numeric(8, 2), nullable=False)
    surge_multiplier: Mapped[float] = mapped_column(Numeric(3, 2), default=1.00, nullable=False)
    effective_from: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    effective_to: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )
