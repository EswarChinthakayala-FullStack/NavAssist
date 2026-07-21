import enum
from datetime import datetime, timezone
from typing import Optional
from sqlalchemy import Integer, String, DateTime, ForeignKey, Enum
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class InvoiceStatus(str, enum.Enum):
    GENERATING = "generating"
    GENERATED = "generated"
    FAILED = "failed"


class Invoice(Base):
    __tablename__ = "invoices"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    booking_id: Mapped[int] = mapped_column(Integer, ForeignKey("bookings.id", ondelete="CASCADE"), unique=True, nullable=False)
    invoice_number: Mapped[str] = mapped_column(String(50), unique=True, index=True, nullable=False)
    file_path: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    generated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )
    invoice_version: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    invoice_hash: Mapped[Optional[str]] = mapped_column(String(64), nullable=True)  # SHA-256 hash of PDF
    pdf_size: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)  # size in bytes
    status: Mapped[InvoiceStatus] = mapped_column(Enum(InvoiceStatus), default=InvoiceStatus.GENERATING, nullable=False)

    # Relationships
    booking: Mapped["Booking"] = relationship("Booking", back_populates="invoice")
