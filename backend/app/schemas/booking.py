from datetime import datetime
from decimal import Decimal
from typing import Optional
from pydantic import BaseModel, Field, ConfigDict

from app.models.booking import BookingStatus


class BookingCreate(BaseModel):
    """Input payload for a Guest to request a new local escort assistance booking."""
    pickup_latitude: float = Field(..., ge=-90.0, le=90.0, description="Latitude of pickup location")
    pickup_longitude: float = Field(..., ge=-180.0, le=180.0, description="Longitude of pickup location")
    pickup_address: str = Field(..., min_length=3, description="Text address of pickup location")
    
    destination_latitude: float = Field(..., ge=-90.0, le=90.0, description="Latitude of destination")
    destination_longitude: float = Field(..., ge=-180.0, le=180.0, description="Longitude of destination")
    destination_address: str = Field(..., min_length=3, description="Text address of destination")
    assistant_id: Optional[int] = Field(None, description="Pre-selected assistant ID")
    scheduled_time: Optional[str] = Field(None, description="ISO datetime for scheduled bookings (null for immediate)")


from typing import List, Optional

class BookingStatusHistoryOut(BaseModel):
    status: str
    changed_at: datetime
    changed_by: Optional[int] = None

    model_config = ConfigDict(from_attributes=True)


class BookingOut(BaseModel):
    """Response payload containing full details of a matching journey booking."""
    id: int
    guest_id: int
    assistant_id: Optional[int] = None
    status: BookingStatus
    
    pickup_latitude: float
    pickup_longitude: float
    pickup_address: str
    
    destination_latitude: float
    destination_longitude: float
    destination_address: str
    
    fare_amount: Decimal
    otp_start: str
    
    guest_name: Optional[str] = None
    guest_phone: Optional[str] = None
    guest_avatar: Optional[str] = None
    assistant_name: Optional[str] = None
    assistant_phone: Optional[str] = None
    assistant_avatar: Optional[str] = None
    
    created_at: datetime
    updated_at: datetime
    distance_km: Optional[Decimal] = None
    estimated_duration_min: Optional[int] = None
    status_history: Optional[List[BookingStatusHistoryOut]] = []

    model_config = ConfigDict(from_attributes=True)


# Backward compatibility alias
BookingResponse = BookingOut


class BookingStatusUpdate(BaseModel):
    """Payload to request a state machine transition on a booking request."""
    status: BookingStatus = Field(..., description="Target state to transition to")
    otp: Optional[str] = Field(None, min_length=6, max_length=6, description="OTP code required to START trip")
    cancellation_reason: Optional[str] = Field(None, description="Required explanation text when status is CANCELLED")


class FareEstimateOut(BaseModel):
    """Calculated routing metrics and fare quote estimates return payload."""
    pickup_address: str
    destination_address: str
    distance_km: float
    duration_minutes: int
    base_fare: Decimal
    distance_fare: Decimal
    time_fare: Decimal
    waiting_charges: Decimal = Decimal("0.00")
    booking_fee: Decimal = Decimal("15.00")
    subtotal: Decimal
    surge_multiplier: Decimal = Decimal("1.00")
    surge_amount: Decimal = Decimal("0.00")
    taxes: Decimal = Decimal("0.00")
    discount_amount: Decimal = Decimal("0.00")
    total_fare: Decimal
    estimated_fare: Decimal
