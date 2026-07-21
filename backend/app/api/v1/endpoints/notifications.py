from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import update
from pydantic import BaseModel, Field
from typing import List

from app.api import deps
from app.models.user import User, DeviceToken, DeviceType
from app.models.engagement import Notification
from app.schemas.common import PaginationParams

router = APIRouter()


class RegisterDeviceTokenRequest(BaseModel):
    token: str = Field(..., description="FCM device push token string")
    device_type: DeviceType = Field(..., description="Device operating platform (ios, android, web)")


@router.get("/")
async def list_notifications(
    params: PaginationParams = Depends(),
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Lists recent notifications dispatch alerts for the caller."""
    skip = (params.page - 1) * params.limit
    result = await db.execute(
        select(Notification)
        .filter(Notification.user_id == current_user.id)
        .order_by(Notification.created_at.desc())
        .offset(skip)
        .limit(params.limit)
    )
    return result.scalars().all()


@router.post("/device-token", status_code=status.HTTP_201_CREATED)
async def register_device_token(
    request: RegisterDeviceTokenRequest,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Registers/refreshes an FCM device push token."""
    from datetime import datetime, timezone
    res = await db.execute(
        select(DeviceToken).filter(
            DeviceToken.fcm_token == request.token,
            DeviceToken.user_id == current_user.id
        )
    )
    existing = res.scalars().first()
    if not existing:
        device_token = DeviceToken(
            user_id=current_user.id,
            fcm_token=request.token,
            device_type=request.device_type,
            last_used_at=datetime.now(timezone.utc)
        )
        db.add(device_token)
    else:
        existing.last_used_at = datetime.now(timezone.utc)
        db.add(existing)
        
    await db.flush()
    await db.commit()
        
    return {"success": True, "message": "Device token registered successfully"}


# Legacy compatibility route
@router.post("/token", status_code=status.HTTP_201_CREATED, include_in_schema=False)
async def register_device_token_legacy(request: RegisterDeviceTokenRequest, current_user: User = Depends(deps.get_current_user), db: AsyncSession = Depends(deps.get_db)):
    return await register_device_token(request, current_user, db)


@router.patch("/{id}/read", status_code=status.HTTP_200_OK)
async def mark_as_read(
    id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Marks one specific notification as read."""
    res = await db.execute(
        select(Notification).filter(
            Notification.id == id,
            Notification.user_id == current_user.id
        )
    )
    notification = res.scalars().first()
    if not notification:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Notification not found"
        )
        
    notification.is_read = True
    db.add(notification)
    await db.flush()
    await db.commit()
    return {"success": True, "message": "Notification marked as read"}


# Legacy compatibility route
@router.post("/{notification_id}/read", status_code=status.HTTP_200_OK, include_in_schema=False)
async def mark_as_read_legacy(notification_id: int, current_user: User = Depends(deps.get_current_user), db: AsyncSession = Depends(deps.get_db)):
    return await mark_as_read(notification_id, current_user, db)


@router.patch("/read-all", status_code=status.HTTP_200_OK)
async def mark_all_as_read(
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Marks all notifications for the authenticated user as read."""
    await db.execute(
        update(Notification)
        .filter(Notification.user_id == current_user.id)
        .values(is_read=True)
    )
    await db.commit()
    return {"success": True, "message": "All notifications marked as read"}
