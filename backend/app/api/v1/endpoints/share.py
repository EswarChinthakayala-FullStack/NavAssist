from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from datetime import datetime, timezone, timedelta
import uuid

from app.api import deps
from app.models.user import User
from app.models.safety import TripShare
from app.repositories import booking_repository as crud_booking
from app.core import redis_client

router = APIRouter()


@router.post("/{booking_id}/generate-link")
async def generate_share_link(
    booking_id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Generates a unique tracking token and public view link for active journeys.
    Links expire automatically after 2 hours.
    """
    booking = await crud_booking.get_booking(db, booking_id=booking_id)
    if not booking:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Booking request not found"
        )
        
    if current_user.id != booking.guest_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only the guest of the booking can share the link"
        )
        
    token = uuid.uuid4().hex
    expiry = datetime.now(timezone.utc) + timedelta(hours=2)
    
    trip_share = TripShare(
        booking_id=booking_id,
        share_token=token,
        created_by=current_user.id,
        expires_at=expiry,
        is_active=True
    )
    db.add(trip_share)
    await db.flush()
    await db.commit()
    
    return {
        "share_token": token,
        "expires_at": expiry,
        "share_link": f"/api/v1/share/public/{token}"
    }


# Legacy compatibility route
@router.post("/booking/{booking_id}", include_in_schema=False)
async def generate_share_link_legacy(booking_id: int, current_user: User = Depends(deps.get_current_user), db: AsyncSession = Depends(deps.get_db)):
    return await generate_share_link(booking_id, current_user, db)


@router.get("/public/{share_token}")
async def public_tracking_view(
    share_token: str,
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Public unauthenticated endpoint for safety contacts to track active journeys in real-time.
    """
    result = await db.execute(
        select(TripShare).filter(
            TripShare.share_token == share_token,
            TripShare.is_active == True,
            TripShare.expires_at >= datetime.now(timezone.utc)
        )
    )
    share = result.scalars().first()
    if not share:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Active tracking link not found, disabled or expired"
        )
        
    booking = share.booking
    lat, lon = None, None
    if booking.assistant_id:
        coords = await redis_client.get_assistant_location(booking.assistant_id)
        if coords:
            lat, lon = coords
            
    return {
        "booking_id": booking.id,
        "status": booking.status,
        "latitude": lat if lat is not None else booking.pickup_latitude,
        "longitude": lon if lon is not None else booking.pickup_longitude,
        "source": "redis_cache" if lat is not None else "pickup_fallback"
    }


# Legacy compatibility route
@router.get("/live/{share_token}", include_in_schema=False)
async def public_tracking_view_legacy(share_token: str, db: AsyncSession = Depends(deps.get_db)):
    return await public_tracking_view(share_token, db)
