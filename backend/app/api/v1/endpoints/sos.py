from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from datetime import datetime, timezone
from typing import List

from app.api import deps
from app.repositories import booking_repository as crud_booking
from app.models.user import User
from app.models.booking import BookingStatus
from app.models.safety import SosAlert, SosStatus
from app.schemas.safety import SosTriggerRequest, SosAlertResponse
from app.services.sos_service import SosService

router = APIRouter()


@router.post("/trigger", response_model=SosAlertResponse, status_code=status.HTTP_201_CREATED)
async def trigger_sos(
    request: SosTriggerRequest,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Instantly triggers a safety SOS alert for an active booking.
    """
    booking = await crud_booking.get_booking(db, booking_id=request.booking_id)
    if not booking:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Booking request not found"
        )
        
    if current_user.id != booking.guest_id and current_user.id != booking.assistant_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Unauthorized to trigger SOS for this booking"
        )
        
    if booking.status not in [BookingStatus.ACCEPTED, BookingStatus.STARTED]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="SOS can only be triggered during active bookings"
        )
        
    db_sos = await SosService.trigger_sos(
        db,
        booking_id=request.booking_id,
        user_id=current_user.id,
        latitude=request.latitude,
        longitude=request.longitude
    )
    await db.commit()
    return db_sos


@router.patch("/{sos_id}/resolve", response_model=SosAlertResponse)
async def resolve_sos_alert(
    sos_id: int,
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Admin-only endpoint to resolve an active SOS alert incident.
    """
    result = await db.execute(select(SosAlert).filter(SosAlert.id == sos_id))
    alert = result.scalars().first()
    if not alert:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="SOS alert record not found"
        )
        
    alert.status = SosStatus.RESOLVED
    alert.resolved_at = datetime.now(timezone.utc)
    alert.resolved_by = current_user.id
    
    db.add(alert)
    await db.flush()
    await db.commit()
    return alert


@router.get("/active", response_model=List[SosAlertResponse])
async def list_active_sos_alerts(
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Admin-only endpoint listing all currently unresolved active SOS alerts.
    """
    result = await db.execute(
        select(SosAlert).filter(SosAlert.status == SosStatus.ACTIVE).order_by(SosAlert.triggered_at.desc())
    )
    return result.scalars().all()
