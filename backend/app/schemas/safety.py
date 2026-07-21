from datetime import datetime
from pydantic import BaseModel, Field, ConfigDict

from app.models import SosStatus


class SosTriggerRequest(BaseModel):
    """Input payload to trigger an SOS alert broadcast to emergency contacts."""
    booking_id: int = Field(..., description="ID of the booking this SOS alert relates to")
    latitude: float = Field(..., ge=-90.0, le=90.0, description="Current latitude of the user")
    longitude: float = Field(..., ge=-180.0, le=180.0, description="Current longitude of the user")


class SosAlertResponse(BaseModel):
    """Return payload representing a logged SOS trigger incident."""
    id: int
    booking_id: int
    triggered_by: int
    latitude: float
    longitude: float
    status: SosStatus
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


class TripShareOut(BaseModel):
    """Journey link share metadata for family safety monitoring."""
    share_token: str
    booking_id: int
    created_by: int
    expires_at: datetime
    is_active: bool
    created_at: datetime
    share_link: str

    model_config = ConfigDict(from_attributes=True)
