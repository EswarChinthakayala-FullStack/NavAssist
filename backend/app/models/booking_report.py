import enum
from datetime import datetime, timezone
from typing import Optional
from sqlalchemy import String, ForeignKey, DateTime, Integer, Enum, Boolean, JSON
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base
from app.models.user import User

class ReportCategory(str, enum.Enum):
    SAFETY = "passenger_safety"
    BEHAVIOR = "assistant_behavior"
    LATE_ARRIVAL = "late_arrival"
    WRONG_ROUTE = "wrong_route"
    FARE_ISSUE = "fare_issue"
    PAYMENT_ISSUE = "payment_issue"
    HARASSMENT = "harassment"
    EMERGENCY = "emergency"
    VEHICLE_ISSUE = "vehicle_issue"
    LOST_ITEM = "lost_item"
    TECHNICAL_PROBLEM = "technical_problem"
    OTHER = "other"

class ReportSeverity(str, enum.Enum):
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    CRITICAL = "critical"

class ReportStatus(str, enum.Enum):
    SUBMITTED = "submitted"
    UNDER_REVIEW = "under_review"
    WAITING_RESPONSE = "waiting_response"
    RESOLVED = "resolved"
    CLOSED = "closed"
    REJECTED = "rejected"

class BookingReport(Base):
    __tablename__ = "booking_reports"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    report_number: Mapped[str] = mapped_column(String(50), unique=True, index=True, nullable=False)
    booking_id: Mapped[int] = mapped_column(Integer, ForeignKey("bookings.id", ondelete="CASCADE"), index=True, nullable=False)
    reporter_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    against_user_id: Mapped[Optional[int]] = mapped_column(Integer, ForeignKey("users.id", ondelete="SET NULL"), nullable=True)
    
    category: Mapped[ReportCategory] = mapped_column(Enum(ReportCategory), nullable=False)
    severity: Mapped[ReportSeverity] = mapped_column(Enum(ReportSeverity), default=ReportSeverity.MEDIUM, nullable=False)
    status: Mapped[ReportStatus] = mapped_column(Enum(ReportStatus), default=ReportStatus.SUBMITTED, nullable=False)
    
    description: Mapped[str] = mapped_column(String(1000), nullable=False)
    evidence_json: Mapped[Optional[dict]] = mapped_column(JSON, name="evidence", nullable=True)
    
    assigned_admin_id: Mapped[Optional[int]] = mapped_column(Integer, ForeignKey("users.id", ondelete="SET NULL"), nullable=True)
    resolution_notes: Mapped[Optional[str]] = mapped_column(String(1000), nullable=True)
    resolution_time: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    
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

    reporter: Mapped["User"] = relationship("User", foreign_keys=[reporter_id])
    against_user: Mapped[Optional["User"]] = relationship("User", foreign_keys=[against_user_id])
    assigned_admin: Mapped[Optional["User"]] = relationship("User", foreign_keys=[assigned_admin_id])
