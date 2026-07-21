import enum
from datetime import datetime, timezone
from typing import List, Optional
from sqlalchemy import String, ForeignKey, DateTime, Integer, Enum, Numeric, CHAR
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class PaymentMethod(str, enum.Enum):
    ONLINE = "online"
    CASH = "cash"
    UPI = "upi"
    CARD = "card"
    NETBANKING = "netbanking"
    WALLET = "wallet"


class PaymentStatus(str, enum.Enum):
    NOT_STARTED = "not_started"
    ORDER_CREATED = "order_created"
    PAYMENT_PENDING = "payment_pending"
    AUTHORIZED = "authorized"
    CAPTURED = "captured"
    COMPLETED = "completed"
    FAILED = "failed"
    CANCELLED = "cancelled"
    REFUNDED = "refunded"

    # Backward compatibility alias
    CREATED = "order_created"


class WalletTransactionType(str, enum.Enum):
    CREDIT = "credit"
    DEBIT = "debit"


class WalletTransactionRefType(str, enum.Enum):
    BOOKING = "booking"
    REFUND = "refund"
    TOPUP = "topup"
    PAYOUT = "payout"


class Payment(Base):
    __tablename__ = "payments"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    booking_id: Mapped[int] = mapped_column(Integer, ForeignKey("bookings.id", ondelete="CASCADE"), nullable=False)
    user_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="RESTRICT"), nullable=False)
    amount: Mapped[float] = mapped_column(Numeric(10, 2), nullable=False)
    currency: Mapped[str] = mapped_column(CHAR(3), default="INR", nullable=False)
    payment_method: Mapped[Optional[PaymentMethod]] = mapped_column(Enum(PaymentMethod), default=PaymentMethod.ONLINE, nullable=True)
    gateway: Mapped[str] = mapped_column(String(30), default="razorpay", nullable=False)
    gateway_order_id: Mapped[Optional[str]] = mapped_column(String(100), unique=True, nullable=True)
    gateway_payment_id: Mapped[Optional[str]] = mapped_column(String(100), unique=True, nullable=True)
    gateway_signature: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    status: Mapped[PaymentStatus] = mapped_column(Enum(PaymentStatus), default=PaymentStatus.NOT_STARTED, nullable=False)

    payment_time: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    payment_reference: Mapped[Optional[str]] = mapped_column(String(100), unique=True, nullable=True)
    receipt_number: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    invoice_number: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    idempotency_key: Mapped[Optional[str]] = mapped_column(String(100), unique=True, nullable=True)

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

    # Relationships
    booking: Mapped["Booking"] = relationship("Booking", back_populates="payments", foreign_keys=[booking_id])
    user: Mapped["User"] = relationship("User", foreign_keys=[user_id])

    @property
    def razorpay_order_id(self) -> Optional[str]:
        return self.gateway_order_id

    @razorpay_order_id.setter
    def razorpay_order_id(self, val: Optional[str]):
        self.gateway_order_id = val

    @property
    def razorpay_payment_id(self) -> Optional[str]:
        return self.gateway_payment_id

    @razorpay_payment_id.setter
    def razorpay_payment_id(self, val: Optional[str]):
        self.gateway_payment_id = val

    @property
    def razorpay_signature(self) -> Optional[str]:
        return self.gateway_signature

    @razorpay_signature.setter
    def razorpay_signature(self, val: Optional[str]):
        self.gateway_signature = val


class Wallet(Base):
    __tablename__ = "wallets"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    balance: Mapped[float] = mapped_column(Numeric(10, 2), default=0.00, nullable=False)
    currency: Mapped[str] = mapped_column(CHAR(3), default="INR", nullable=False)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    # Relationships
    transactions: Mapped[List["WalletTransaction"]] = relationship("WalletTransaction", back_populates="wallet", cascade="all, delete-orphan")


class WalletTransaction(Base):
    __tablename__ = "wallet_transactions"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    wallet_id: Mapped[int] = mapped_column(Integer, ForeignKey("wallets.id", ondelete="CASCADE"), nullable=False)
    type: Mapped[WalletTransactionType] = mapped_column(Enum(WalletTransactionType), nullable=False)
    amount: Mapped[float] = mapped_column(Numeric(10, 2), nullable=False)
    reference_type: Mapped[WalletTransactionRefType] = mapped_column(Enum(WalletTransactionRefType), nullable=False)
    reference_id: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    balance_after: Mapped[float] = mapped_column(Numeric(10, 2), nullable=False)
    idempotency_key: Mapped[Optional[str]] = mapped_column(String(100), unique=True, nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    # Relationships
    wallet: Mapped["Wallet"] = relationship("Wallet", back_populates="transactions")
