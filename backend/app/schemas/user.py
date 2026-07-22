from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, Field, EmailStr, ConfigDict

from app.models import UserRole, KycStatus, OnlineStatus, UserStatus


class EmergencyContactBase(BaseModel):
    name: str = Field(..., description="Contact's full name")
    phone: str = Field(..., description="Contact's phone number with country code")
    relationship: Optional[str] = Field("Guardian", description="Relationship type: Father, Mother, Spouse, Friend, Guardian, etc.")
    is_primary: Optional[bool] = Field(False, description="Whether contact is the primary emergency contact")


class EmergencyContactIn(EmergencyContactBase):
    """Pydantic input schema for registering an emergency contact."""
    pass


# Backward compatibility alias
EmergencyContactCreate = EmergencyContactIn


class EmergencyContactOut(EmergencyContactBase):
    """Pydantic response schema representing an emergency contact."""
    id: int
    user_id: int
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


# Backward compatibility alias
EmergencyContactResponse = EmergencyContactOut


class SavedLocationIn(BaseModel):
    """Input payload to create or update a saved bookmark location."""
    label: str = Field(..., description="Location category: home, office, favorite, other")
    custom_label: Optional[str] = Field(None, description="Custom label if category is other")
    address: str = Field(..., description="Descriptive text address")
    latitude: float = Field(..., ge=-90.0, le=90.0, description="Latitude coordinate")
    longitude: float = Field(..., ge=-180.0, le=180.0, description="Longitude coordinate")
    place_id: Optional[str] = Field(None, description="Google maps place unique identifier")


class SavedLocationOut(BaseModel):
    """Response payload for a saved bookmark location."""
    id: int
    user_id: int
    label: str
    custom_label: Optional[str]
    address: str
    latitude: float
    longitude: float
    place_id: Optional[str]
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


class GuestProfileResponse(BaseModel):
    user_id: int
    name: str
    profile_picture_url: Optional[str] = None
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


class AssistantProfileResponse(BaseModel):
    user_id: int
    name: str
    profile_picture_url: Optional[str] = None
    kyc_status: KycStatus
    online_status: OnlineStatus
    current_latitude: Optional[float] = None
    current_longitude: Optional[float] = None
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True)


class UserSimpleOut(BaseModel):
    id: int
    phone: str
    email: Optional[str] = None
    role: UserRole
    is_active: bool
    status: UserStatus
    full_name: str
    profile_photo_url: Optional[str] = None
    is_phone_verified: bool
    is_email_verified: bool
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True)


class UserOut(UserSimpleOut):
    """Unified user registration or query return details."""
    # Nested profile information
    guest: Optional[GuestProfileResponse] = None
    assistant: Optional[AssistantProfileResponse] = None


# Backward compatibility alias
UserResponse = UserOut


class UserUpdate(BaseModel):
    """Input payload schema to update base profile fields."""
    full_name: Optional[str] = Field(None, description="User's new full name")
    email: Optional[EmailStr] = Field(None, description="User's new email address")
    profile_photo_url: Optional[str] = Field(None, description="S3 uploaded photo URL")
