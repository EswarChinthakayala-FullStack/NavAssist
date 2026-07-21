import json
import logging
from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.api import deps
from app.core import redis_client
from app.core.security import verify_token
from app.repositories import booking_repository as crud_booking, user_repository as crud_user
from app.models.booking import BookingStatus
from app.models.user import UserRole
from app.websocket.connection_manager import manager
from app.websocket.events import (
    build_location_update_event,
    build_connection_ack_event,
    build_chat_message_event
)
from app.utils.timezone import get_ist_now

logger = logging.getLogger(__name__)
router = APIRouter()


@router.websocket("/tracking/{booking_id}")
async def websocket_tracking(
    websocket: WebSocket,
    booking_id: int,
    token: str = Query(..., description="JWT Access Token")
):
    """
    Persistent WebSocket tunnel for real-time location updates, ETAs, and chat.
    """
    # 1. Authenticate Token
    user_id_str = verify_token(token, token_type="access")
    if not user_id_str:
        await websocket.close(code=status.WS_1008_POLICY_VIOLATION, reason="Could not validate authentication token")
        return
        
    try:
        user_id = int(user_id_str)
    except ValueError:
        await websocket.close(code=status.WS_1008_POLICY_VIOLATION, reason="Invalid token payload format")
        return

    # 2. Check Authorization & Fetch User/Booking
    from app.core.database import SessionLocal
    async with SessionLocal() as db:
        user = await crud_user.get_user(db, user_id=user_id)
        if not user or not user.is_active:
            await websocket.close(code=status.WS_1008_POLICY_VIOLATION, reason="User account is inactive or not found")
            return
            
        booking = await crud_booking.get_booking(db, booking_id=booking_id)
        if not booking:
            await websocket.close(code=status.WS_1011_INTERNAL_ERROR, reason="Booking record not found")
            return
            
        # Authorize client
        if user.role == UserRole.GUEST and booking.guest_id != user.id:
            await websocket.close(code=status.WS_1008_POLICY_VIOLATION, reason="Unauthorized to track this booking")
            return
        elif user.role == UserRole.ASSISTANT and booking.assistant_id != user.id:
            await websocket.close(code=status.WS_1008_POLICY_VIOLATION, reason="Unauthorized to send updates for this booking")
            return
            
    # 3. Connection Handshake & ACK
    channel = f"booking:tracking:{booking_id}"
    await manager.connect(websocket, channel)
    
    # Send Connection Acknowledgement Frame
    ack_frame = build_connection_ack_event(status="connected", booking_id=booking_id)
    await websocket.send_text(json.dumps(ack_frame))
    
    try:
        # Loop for receiving and processing updates
        while True:
            data = await websocket.receive_text()
            try:
                payload = json.loads(data)
            except json.JSONDecodeError:
                logger.warning(f"WebSocket payload on channel {channel} was not valid JSON.")
                continue
                
            event_type = payload.get("event") or payload.get("action")
            
            # A. Process Chat Message
            if event_type in ("chat:message", "chat_message"):
                msg_text = payload.get("message")
                if msg_text:
                    chat_frame = build_chat_message_event(
                        booking_id=booking_id,
                        sender_id=user.id,
                        message=msg_text,
                        ts=get_ist_now().isoformat()
                    )
                    await redis_client.redis_client.publish(channel, json.dumps(chat_frame))
                    
            # B. Process Location Update (Assistant Only)
            elif event_type in ("location:update", "update_location") and user.role == UserRole.ASSISTANT:
                lat = payload.get("latitude") or payload.get("lat")
                lon = payload.get("longitude") or payload.get("lng")
                speed = payload.get("speed_kmh") or payload.get("speed", 0.0)
                heading = payload.get("heading", 0.0)
                
                if lat is not None and lon is not None:
                    try:
                        lat, lon = float(lat), float(lon)
                        speed = float(speed)
                        heading = float(heading)
                    except ValueError:
                        continue
                        
                    # Update local Redis GeoIndex
                    await redis_client.update_assistant_location(user.id, lat, lon)
                    
                    # Publish location update frame to Redis Pub/Sub channel
                    location_frame = build_location_update_event(
                        booking_id=booking_id,
                        lat=lat,
                        lng=lon,
                        heading=heading,
                        speed=speed,
                        ts=get_ist_now().isoformat()
                    )
                    await redis_client.redis_client.publish(channel, json.dumps(location_frame))
                    
                    # Background job: persist location history snapshot
                    from app.tasks.tasks import log_live_location_task
                    log_live_location_task.delay(
                        booking_id=booking_id, 
                        assistant_id=user.id, 
                        latitude=lat, 
                        longitude=lon
                    )
                    
    except WebSocketDisconnect:
        await manager.disconnect(websocket, channel)
    except Exception as e:
        logger.error(f"Error in WebSocket tracking loop on channel {channel}: {e}")
        await manager.disconnect(websocket, channel)
