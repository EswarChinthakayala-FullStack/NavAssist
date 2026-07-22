from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, Field, ConfigDict

from app.models import KycStatus
from app.schemas.user import UserSimpleOut


class AssistantApplyRequest(BaseModel):
    """Payload to submit KYC verification documents and profile details."""
    bio: Optional[str] = Field(None, description="Short bio statement")
    experience_years: int = Field(0, ge=0, description="Years of local guide experience")
    aadhaar_number: str = Field(..., min_length=12, max_length=12, description="12-digit national identity Aadhaar number")
    service_radius_km: float = Field(10.0, ge=1.0, le=50.0, description="Service range in kilometers")
    doc_front_url: str = Field(..., description="Presigned upload URL for Aadhaar front")
    doc_back_url: str = Field(..., description="Presigned upload URL for Aadhaar back")


class LocationPushRequest(BaseModel):
    """Payload for assistant to push current GPS location coordinates."""
    latitude: float = Field(..., ge=-90.0, le=90.0, description="Current latitude coordinate")
    longitude: float = Field(..., ge=-180.0, le=180.0, description="Current longitude coordinate")


class AssistantProfileOut(BaseModel):
    """Response schema mapping assistant profile details."""
    id: int
    user_id: int
    name: Optional[str] = None
    bio: Optional[str] = None
    experience_years: int
    verification_status: KycStatus
    aadhaar_masked: Optional[str] = None
    trust_score: float
    avg_rating: float
    total_trips: int
    is_online: bool
    service_radius_km: float
    current_latitude: Optional[float] = None
    current_longitude: Optional[float] = None
    profile_photo_url: Optional[str] = None
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True)


class AssistantNearbyOut(BaseModel):
    """Details of active assistants located in the vicinity of a query point."""
    assistant_id: int
    distance_km: float
    latitude: float
    longitude: float


class KycReviewRequest(BaseModel):
    """Payload for administrators to verify or reject pending assistant KYC filings."""
    status: KycStatus = Field(..., description="Review outcome choice")
    review_notes: Optional[str] = Field(None, description="Explanation or audit notes for the choice")


class KycResponse(BaseModel):
    verification_status: KycStatus
    status: Optional[KycStatus] = None
    aadhaar_number: Optional[str] = None
    current_latitude: Optional[float] = None
    current_longitude: Optional[float] = None
    message: Optional[str] = None

    model_config = ConfigDict(from_attributes=True)


class AssistantProfileUpdate(BaseModel):
    bio: Optional[str] = None
    experience_years: Optional[int] = Field(None, ge=0)
    service_radius_km: Optional[float] = Field(None, ge=1.0, le=50.0)


from app.models.user import OnlineStatus

class AssistantStatusToggle(BaseModel):
    status: Optional[OnlineStatus] = Field(None, description="Target online availability status")
    online: Optional[bool] = Field(None, description="Boolean toggle for online status")


class LocationPushRequest(BaseModel):
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)


class AssistantDocumentOut(BaseModel):
    id: int
    doc_type: str
    file_url: str
    verified: bool
    uploaded_at: datetime

    class Config:
        from_attributes = True


class AssistantKycQueueOut(BaseModel):
    id: int
    user_id: int
    bio: Optional[str] = None
    experience_years: int
    verification_status: KycStatus
    aadhaar_masked: Optional[str] = None
    trust_score: float
    avg_rating: float
    total_trips: int
    is_online: bool
    service_radius_km: float
    created_at: datetime
    updated_at: datetime
    user: Optional[UserSimpleOut] = None
    documents: List[AssistantDocumentOut] = []

    class Config:
        from_attributes = True


