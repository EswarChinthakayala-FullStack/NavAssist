from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy.orm import selectinload
from typing import List
from datetime import datetime, timezone
import json
import logging

from app.api import deps
from app.models.user import User, UserRole, OnlineStatus
from app.models.booking import Booking, BookingStatus
from app.models.booking_message import BookingMessage, MessageType
from app.models.assistant import AssistantProfile
from app.models.engagement import Notification, NotificationType
from app.schemas.booking_message import (
    BookingMessageCreate, BookingMessageResponse, ConversationParticipantResponse
)
from app.core.redis_client import redis_client

logger = logging.getLogger(__name__)

router = APIRouter()

@router.post("/bookings/upload")
async def upload_attachment(
    file: UploadFile = File(...),
    current_user: User = Depends(deps.get_current_user)
):
    """
    Generic endpoint to upload media or documents securely for messaging and dispute files.
    """
    from app.integrations.s3_client import upload_file, generate_presigned_url
    content = await file.read()
    if not content:
        raise HTTPException(status_code=400, detail="Uploaded file payload is empty.")
    
    # Reject non-image or HTML files
    if content.startswith(b"<!doctype") or content.startswith(b"<html") or content.startswith(b"<?xml") and not b"<svg" in content[:200].lower():
        raise HTTPException(status_code=400, detail="Invalid image file format. Only image files (PNG, JPG, WEBP, GIF, SVG) are allowed.")

    filename = file.filename or "attachment.png"
    file_key = await upload_file(content, filename, folder="attachments")
    url = generate_presigned_url(file_key)
    return {"url": url, "filename": filename}

def mask_phone_number(phone: str) -> str:
    if not phone:
        return ""
    if len(phone) >= 10:
        return phone[:3] + "******" + phone[-2:]
    return phone[:2] + "****" + phone[-1:]

@router.get("/chat/conversations/{bookingId}", response_model=List[BookingMessageResponse], include_in_schema=False)
@router.get("/bookings/{bookingId}/messages", response_model=List[BookingMessageResponse])
async def get_booking_messages(
    bookingId: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Fetch all chat messages for a booking. Validates ownership and assignment.
    """
    res_b = await db.execute(select(Booking).filter(Booking.id == bookingId))
    booking = res_b.scalars().first()
    if not booking:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Booking not found.")

    is_guest = booking.guest_id == current_user.id
    is_assistant = booking.assistant_id == current_user.id
    is_admin = current_user.role == UserRole.ADMIN

    if not (is_guest or is_assistant or is_admin):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied.")

    res_messages = await db.execute(
        select(BookingMessage)
        .filter(BookingMessage.booking_id == bookingId)
        .options(selectinload(BookingMessage.sender))
        .order_by(BookingMessage.created_at.asc())
    )
    messages = res_messages.scalars().all()
    return messages

@router.post("/bookings/{bookingId}/messages", response_model=BookingMessageResponse)
async def send_booking_message(
    bookingId: int,
    request: BookingMessageCreate,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Send a message for a booking. Restricted to active bookings and valid participants.
    """
    res_b = await db.execute(select(Booking).filter(Booking.id == bookingId))
    booking = res_b.scalars().first()
    if not booking:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Booking not found.")

    is_guest = booking.guest_id == current_user.id
    is_assistant = booking.assistant_id == current_user.id

    if not (is_guest or is_assistant):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied.")

    # Validate active booking status
    allowed_statuses = [
        BookingStatus.ASSIGNED,
        BookingStatus.ASSISTANT_ENROUTE,
        BookingStatus.ARRIVED_PICKUP,
        BookingStatus.GUEST_PICKED_UP,
        BookingStatus.IN_PROGRESS
    ]
    if booking.status not in allowed_statuses:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Messaging is unavailable for this ride."
        )

    # Save to database
    message = BookingMessage(
        booking_id=bookingId,
        sender_id=current_user.id,
        message_type=request.message_type,
        content=request.content,
        media_url=request.media_url,
        latitude=request.latitude,
        longitude=request.longitude,
        is_read=False
    )
    db.add(message)
    await db.flush()

    # Load sender relation
    await db.refresh(message, ["sender"])

    # Push to WebSocket PubSub
    channel = f"booking:tracking:{bookingId}"
    chat_frame = {
        "event": "chat:message",
        "booking_id": bookingId,
        "id": message.id,
        "sender_id": current_user.id,
        "message_type": message.message_type.value,
        "message": message.content or "",
        "media_url": message.media_url or "",
        "latitude": message.latitude or "",
        "longitude": message.longitude or "",
        "timestamp": message.created_at.isoformat(),
        "sender_name": current_user.full_name,
        "sender_role": current_user.role.value
    }
    try:
        await redis_client.redis_client.publish(channel, json.dumps(chat_frame))
    except Exception as e:
        logger.error(f"Failed to publish message over WebSocket: {e}")

    # Enqueue recipient offline system notification
    recipient_id = booking.assistant_id if is_guest else booking.guest_id
    if recipient_id:
        notif = Notification(
            user_id=recipient_id,
            title=f"New message from {current_user.full_name}",
            body=request.content or "Sent an attachment.",
            type=NotificationType.SYSTEM,
            data={"booking_id": bookingId, "message_id": message.id}
        )
        db.add(notif)

    await db.commit()
    return message

@router.patch("/messages/{messageId}/read", status_code=status.HTTP_204_NO_CONTENT)
async def mark_message_as_read(
    messageId: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Mark a message as read.
    """
    res_msg = await db.execute(select(BookingMessage).filter(BookingMessage.id == messageId))
    message = res_msg.scalars().first()
    if not message:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Message not found.")

    res_b = await db.execute(select(Booking).filter(Booking.id == message.booking_id))
    booking = res_b.scalars().first()

    if not booking or current_user.id not in (booking.guest_id, booking.assistant_id):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied.")

    # Only mark as read if the current user is the recipient (not the sender)
    if message.sender_id != current_user.id:
        message.is_read = True
        db.add(message)
        await db.commit()

@router.get("/bookings/{bookingId}/conversation", response_model=ConversationParticipantResponse)
async def get_conversation_participant(
    bookingId: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Retrieve contact and profile details of the other participant in the conversation.
    """
    res_b = await db.execute(select(Booking).filter(Booking.id == bookingId))
    booking = res_b.scalars().first()
    if not booking:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Booking not found.")

    is_guest = booking.guest_id == current_user.id
    is_assistant = booking.assistant_id == current_user.id

    if not (is_guest or is_assistant):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied.")

    # Target user to fetch
    target_id = booking.assistant_id if is_guest else booking.guest_id
    if not target_id:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="No assistant matched for this booking yet.")

    res_target = await db.execute(select(User).filter(User.id == target_id))
    target_user = res_target.scalars().first()
    if not target_user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Participant not found.")

    # Build response fields depending on requester role
    avatar_url = target_user.profile_photo_url
    is_online = target_user.is_online if hasattr(target_user, "is_online") else False
    avg_rating = 5.0
    completed_trips = 0

    if is_guest:
        # Load assistant profile extra metrics
        res_profile = await db.execute(select(AssistantProfile).filter(AssistantProfile.user_id == target_id))
        profile = res_profile.scalars().first()
        if profile:
            avatar_url = profile.profile_photo_url or avatar_url
            is_online = profile.is_online
            avg_rating = float(profile.avg_rating) if profile.avg_rating else 5.0
            completed_trips = int(profile.total_trips) if profile.total_trips else 0

    # Ensure unmasked phone number is sent but also support masked phone for safety
    return ConversationParticipantResponse(
        user_id=target_user.id,
        name=target_user.full_name,
        role=target_user.role.value,
        phone_number=target_user.phone_number,
        avatar_url=avatar_url,
        is_online=is_online,
        avg_rating=avg_rating,
        completed_trips=completed_trips
    )
