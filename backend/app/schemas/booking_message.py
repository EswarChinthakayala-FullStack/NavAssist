from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field, ConfigDict
from app.models.booking_message import MessageType

class MessageSenderResponse(BaseModel):
    id: int
    full_name: str
    role: str
    profile_photo_url: Optional[str] = None

    model_config = ConfigDict(from_attributes=True)

class BookingMessageCreate(BaseModel):
    message_type: MessageType = Field(MessageType.TEXT)
    content: Optional[str] = Field(None, max_length=2000)
    media_url: Optional[str] = Field(None, max_length=500)
    latitude: Optional[str] = Field(None, max_length=30)
    longitude: Optional[str] = Field(None, max_length=30)

class BookingMessageResponse(BaseModel):
    id: int
    booking_id: int
    sender_id: int
    message_type: MessageType
    content: Optional[str] = None
    media_url: Optional[str] = None
    latitude: Optional[str] = None
    longitude: Optional[str] = None
    is_read: bool
    created_at: datetime
    sender: Optional[MessageSenderResponse] = None

    model_config = ConfigDict(from_attributes=True)

class ConversationParticipantResponse(BaseModel):
    user_id: int
    name: str
    role: str
    phone_number: str
    avatar_url: Optional[str] = None
    is_online: bool = False
    avg_rating: float = 5.0
    completed_trips: int = 0

    model_config = ConfigDict(from_attributes=True)
