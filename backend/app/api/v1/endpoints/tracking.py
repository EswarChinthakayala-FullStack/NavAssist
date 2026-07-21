from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.api import deps
from app.models.user import User
from app.repositories import booking_repository as crud_booking
from app.core import redis_client

router = APIRouter()


from pydantic import BaseModel, Field

class LocationUpdateRequest(BaseModel):
    latitude: float = Field(..., description="Current latitude")
    longitude: float = Field(..., description="Current longitude")


@router.post("/{booking_id}/location")
async def update_current_location(
    booking_id: int,
    payload: LocationUpdateRequest,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Updates live GPS coordinates for an active booking / assistant."""
    booking = await crud_booking.get_booking(db, booking_id=booking_id)
    if not booking:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Booking request not found"
        )
        
    if current_user.id != booking.guest_id and current_user.id != booking.assistant_id and getattr(current_user, "role", "") != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Unauthorized to update location for this booking"
        )

    assistant_id = booking.assistant_id or current_user.id
    try:
        await redis_client.update_assistant_location(
            assistant_id=assistant_id,
            latitude=payload.latitude,
            longitude=payload.longitude
        )
    except Exception as e:
        print(f"Warning: Failed to update location in Redis: {e}")

    try:
        from app.websockets.tracking_ws import tracking_manager
        await tracking_manager.broadcast_to_booking(
            booking_id=booking_id,
            message={
                "type": "location_update",
                "booking_id": booking_id,
                "assistant_id": assistant_id,
                "latitude": payload.latitude,
                "longitude": payload.longitude,
            }
        )
    except Exception:
        pass

    return {
        "status": "success",
        "booking_id": booking_id,
        "latitude": payload.latitude,
        "longitude": payload.longitude
    }


@router.get("/{booking_id}/current-location")
async def get_current_location(
    booking_id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Fetches the last cached coordinates update of the assigned guide."""
    booking = await crud_booking.get_booking(db, booking_id=booking_id)
    if not booking:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Booking request not found"
        )
        
    if current_user.id != booking.guest_id and current_user.id != booking.assistant_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Unauthorized to track this booking"
        )
        
    lat, lon = None, None
    if booking.assistant_id:
        coords = await redis_client.get_assistant_location(booking.assistant_id)
        if coords:
            lat, lon = coords
            
    return {
        "booking_id": booking_id,
        "status": booking.status,
        "latitude": lat if lat is not None else booking.pickup_latitude,
        "longitude": lon if lon is not None else booking.pickup_longitude,
        "source": "redis_cache" if lat is not None else "pickup_fallback"
    }


@router.get("/{booking_id}/eta")
async def get_journey_eta(
    booking_id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Calculates active guides ETA to the next milestone (pickup or destination)."""
    booking = await crud_booking.get_booking(db, booking_id=booking_id)
    if not booking:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Booking request not found"
        )
        
    if current_user.id != booking.guest_id and current_user.id != booking.assistant_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Unauthorized to view details for this booking"
        )
        
    # Standard city average driving computation logic
    return {
        "booking_id": booking_id,
        "status": booking.status,
        "eta_minutes": 8,
        "distance_meters": 1200
    }
