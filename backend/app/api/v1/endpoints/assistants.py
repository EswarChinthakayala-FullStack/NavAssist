from typing import Any, List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import func

from app.api import deps
from app.repositories import assistant_repository as crud_assistant
from app.models.user import User
from app.models.assistant import AssistantProfile, OnlineStatus
from app.models.booking import Booking, BookingStatus
from app.models.engagement import RatingReview
from app.schemas.assistant import (
    AssistantProfileOut,
    AssistantProfileUpdate,
    AssistantNearbyOut,
    AssistantStatusToggle,
    LocationPushRequest,
)
from app.core.redis_client import redis_client

router = APIRouter()


async def _sync_assistant_metrics(db: AsyncSession, profile: AssistantProfile) -> AssistantProfile:
    """Calculates actual completed bookings and rating average from DB and syncs profile stats."""
    if not profile:
        return profile
        
    completed_trips_count = await db.scalar(
        select(func.count(Booking.id)).filter(
            (Booking.assistant_id == profile.user_id) | (Booking.assistant_id == profile.id),
            Booking.status == BookingStatus.COMPLETED
        )
    ) or 0

    stats = await db.execute(
        select(
            func.coalesce(func.avg(RatingReview.rating), 5.0),
            func.count(RatingReview.id)
        ).filter(
            (RatingReview.rated_assistant_id == profile.user_id) | (RatingReview.rated_assistant_id == profile.id)
        )
    )
    avg_val, total_reviews = stats.first() or (5.0, 0)
    avg_rating_val = round(float(avg_val), 1) if total_reviews > 0 else (float(profile.avg_rating) if profile.avg_rating else 5.0)

    actual_trips = max(profile.total_trips or 0, completed_trips_count)
    if profile.total_trips != actual_trips or float(profile.avg_rating or 0.0) != avg_rating_val:
        profile.total_trips = actual_trips
        profile.avg_rating = avg_rating_val
        db.add(profile)
        await db.commit()
        await db.refresh(profile)

    return profile


@router.get("/me/profile", response_model=AssistantProfileOut)
async def get_own_profile(
    current_user: User = Depends(deps.get_current_assistant),
    db: AsyncSession = Depends(deps.get_db)
):
    """Fetches full private profile and stats for the logged-in assistant."""
    profile = await crud_assistant.get_assistant(db, user_id=current_user.id)
    if not profile:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Assistant profile stats not initialized"
        )
    profile = await _sync_assistant_metrics(db, profile)
    res = AssistantProfileOut.model_validate(profile)
    if current_user.full_name:
        res.name = current_user.full_name
    return res


@router.patch("/me/profile", response_model=AssistantProfileOut)
async def update_own_profile(
    request: AssistantProfileUpdate,
    current_user: User = Depends(deps.get_current_assistant),
    db: AsyncSession = Depends(deps.get_db)
):
    """Updates bio, experience, and service radius configurations."""
    profile = await crud_assistant.get_assistant(db, user_id=current_user.id)
    if not profile:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Assistant profile not initialized"
        )
        
    if request.bio is not None:
        profile.bio = request.bio
    if request.experience_years is not None:
        profile.experience_years = request.experience_years
    if request.service_radius_km is not None:
        profile.service_radius_km = request.service_radius_km
        
    db.add(profile)
    await db.flush()
    await db.commit()
    profile = await _sync_assistant_metrics(db, profile)
    res = AssistantProfileOut.model_validate(profile)
    if current_user.full_name:
        res.name = current_user.full_name
    return res


@router.post("/me/status", response_model=AssistantProfileOut)
async def toggle_online_status(
    request: AssistantStatusToggle,
    current_user: User = Depends(deps.get_current_assistant),
    db: AsyncSession = Depends(deps.get_db)
):
    """Toggles online/offline guides availability in the marketplace."""
    profile = await crud_assistant.update_assistant_online_status(
        db, 
        user_id=current_user.id, 
        status=request.status
    )
    await db.commit()
    
    if request.status == OnlineStatus.OFFLINE:
        await redis_client.redis_client.delete(f"assistant:location:{current_user.id}")
        
    profile = await _sync_assistant_metrics(db, profile)
    res = AssistantProfileOut.model_validate(profile)
    if current_user.full_name:
        res.name = current_user.full_name
    return res


@router.patch("/me/location", status_code=status.HTTP_200_OK)
async def push_location_coords(
    request: LocationPushRequest,
    current_user: User = Depends(deps.get_current_assistant),
    db: AsyncSession = Depends(deps.get_db)
):
    """Pushes GPS location coordinates from mobile app REST client fallbacks."""
    await crud_assistant.update_assistant_location(
        db, 
        user_id=current_user.id, 
        latitude=request.latitude, 
        longitude=request.longitude
    )
    await db.commit()
    return {"status": "success", "latitude": request.latitude, "longitude": request.longitude}


@router.get("/nearby", response_model=List[AssistantNearbyOut])
async def search_nearby_guides(
    latitude: float = Query(..., ge=-90.0, le=90.0),
    longitude: float = Query(..., ge=-180.0, le=180.0),
    radius_km: float = Query(5.0, ge=1.0, le=50.0),
    db: AsyncSession = Depends(deps.get_db)
):
    """Performs spatial geographic checks to locate online verified assistants."""
    results = await crud_assistant.get_nearby_assistants(db, latitude=latitude, longitude=longitude, radius_km=radius_km)
    
    from app.utils.geo_utils import calculate_haversine_distance
    nearby_list = []
    for assistant in results:
        dist = calculate_haversine_distance(latitude, longitude, assistant.current_latitude or 0.0, assistant.current_longitude or 0.0)
        nearby_list.append(AssistantNearbyOut(
            assistant_id=assistant.user_id,
            distance_km=round(dist, 2),
            latitude=assistant.current_latitude or 0.0,
            longitude=assistant.current_longitude or 0.0
        ))
    return nearby_list


@router.get("/me/earnings")
async def get_earnings_summary(
    current_user: User = Depends(deps.get_current_assistant),
    db: AsyncSession = Depends(deps.get_db)
):
    """Retrieves wallet balance and payout history summary metrics for the assistant."""
    from app.services.payout_service import PayoutService
    from app.services.wallet_service import WalletService
    
    wallet = await WalletService.get_or_create_wallet(db, current_user.id)
    payouts = await PayoutService.list_payouts_for_assistant(db, current_user.id)
    account = await PayoutService.get_payout_account(db, current_user.id)
    
    profile = await crud_assistant.get_assistant(db, user_id=current_user.id)
    if profile:
        profile = await _sync_assistant_metrics(db, profile)
    total_trips = profile.total_trips if profile else 0
    
    return {
        "wallet_balance_inr": float(wallet.balance),
        "completed_trips": total_trips,
        "has_payout_account": account is not None and account.is_verified,
        "payouts_history": payouts
    }


from pydantic import BaseModel, Field

class PayoutAccountIn(BaseModel):
    account_holder_name: str = Field(..., min_length=2, max_length=150)
    account_number: str = Field(..., min_length=5, max_length=50)
    ifsc_code: Optional[str] = Field(None, max_length=11)
    upi_id: Optional[str] = Field(None, max_length=100)

class PayoutRequestIn(BaseModel):
    amount: float = Field(..., gt=0.0)


@router.post("/me/payout-account", status_code=status.HTTP_201_CREATED)
async def update_payout_account(
    req: PayoutAccountIn,
    current_user: User = Depends(deps.get_current_assistant),
    db: AsyncSession = Depends(deps.get_db)
):
    """Registers or updates the assistant's bank account or UPI payout details."""
    from app.services.payout_service import PayoutService
    account = await PayoutService.create_or_update_payout_account(
        db=db,
        user_id=current_user.id,
        account_holder_name=req.account_holder_name,
        account_number=req.account_number,
        ifsc_code=req.ifsc_code,
        upi_id=req.upi_id
    )
    await db.commit()
    return {"success": True, "message": "Payout account updated successfully", "account_id": account.id}


@router.get("/me/payout-account")
async def get_payout_account(
    current_user: User = Depends(deps.get_current_assistant),
    db: AsyncSession = Depends(deps.get_db)
):
    """Fetches current payout account configuration."""
    from app.services.payout_service import PayoutService
    account = await PayoutService.get_payout_account(db, current_user.id)
    if not account:
        return {"has_account": False, "account": None}
    return {
        "has_account": True,
        "account_id": account.id,
        "account_holder_name": account.account_holder_name,
        "ifsc_code": account.ifsc_code,
        "upi_id": account.upi_id,
        "is_verified": bool(account.is_verified)
    }


@router.post("/me/payouts/request", status_code=status.HTTP_201_CREATED)
async def request_payout(
    req: PayoutRequestIn,
    current_user: User = Depends(deps.get_current_assistant),
    db: AsyncSession = Depends(deps.get_db)
):
    """Requests an earnings payout from current wallet balance."""
    from decimal import Decimal
    from app.services.payout_service import PayoutService
    payout = await PayoutService.request_payout(
        db=db,
        user_id=current_user.id,
        amount=Decimal(str(req.amount))
    )
    await db.commit()
    return {"success": True, "message": "Payout request submitted successfully", "payout_id": payout.id, "amount": payout.amount}


@router.get("/{assistant_id}", response_model=AssistantProfileOut)
async def get_public_assistant_profile(
    assistant_id: int,
    db: AsyncSession = Depends(deps.get_db)
):
    """Exposes public profile info (trips counter, ratings, verification badges)."""
    profile = await crud_assistant.get_assistant(db, user_id=assistant_id)
    if not profile:
        profile = await crud_assistant.get_assistant_by_id(db, assistant_id=assistant_id)
    if not profile:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Assistant profile not found"
        )
    
    profile = await _sync_assistant_metrics(db, profile)
    res = AssistantProfileOut.model_validate(profile)
    if profile.user and profile.user.full_name:
        res.name = profile.user.full_name
    return res
