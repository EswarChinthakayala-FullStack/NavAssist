from datetime import datetime
from decimal import Decimal
from typing import Optional
from pydantic import BaseModel, Field, ConfigDict

from app.models.payment import PaymentStatus, PaymentMethod


class PaymentOrderCreate(BaseModel):
    """Input payload to initialize a payment transaction for a booking."""
    booking_id: int = Field(..., description="Booking ID to make payment for")
    payment_method: Optional[PaymentMethod] = Field(default=PaymentMethod.ONLINE, description="Payment method: online vs cash")


# Backward compatibility alias
PaymentCreate = PaymentOrderCreate


class PaymentResponse(BaseModel):
    """Response payload detailing a payment order status."""
    id: int
    booking_id: int
    gateway_order_id: Optional[str] = None
    gateway_payment_id: Optional[str] = None
    razorpay_order_id: Optional[str] = None
    razorpay_payment_id: Optional[str] = None
    razorpay_key_id: Optional[str] = None
    payment_method: Optional[PaymentMethod] = None
    status: PaymentStatus
    amount: Decimal
    currency: Optional[str] = "INR"
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True)


class PaymentVerify(BaseModel):
    """HMAC signature verification verification data returned by Razorpay checkout."""
    razorpay_order_id: str = Field(..., description="Razorpay Order ID returned from backend")
    razorpay_payment_id: str = Field(..., description="Razorpay Payment ID returned by checkout form")
    razorpay_signature: str = Field(..., description="HMAC SHA256 checkout verify signature")


class PaymentFailureRecord(BaseModel):
    """Input payload to record a failed payment attempt from the checkout client."""
    razorpay_order_id: str = Field(..., description="Razorpay Order ID for the failed payment")
    error_code: Optional[str] = Field(None, description="Razorpay error code")
    error_description: Optional[str] = Field(None, description="Human-readable error description")
    error_reason: Optional[str] = Field(None, description="Technical error reason")


class CashConfirmRequest(BaseModel):
    """Input payload to confirm cash collection for a booking."""
    booking_id: int = Field(..., description="Booking ID for cash confirmation")


class WalletTopupRequest(BaseModel):
    """Request payload to credit local balance funds to Guest wallet."""
    amount: Decimal = Field(..., gt=0, description="Top-up credit amount in INR")
