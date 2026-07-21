from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from decimal import Decimal
from pydantic import BaseModel, Field
from typing import List

from app.api import deps
from app.models.user import User
from app.models.pricing import Coupon, CouponRedemption
from app.models.booking import Booking
from app.services.coupon_service import CouponService

router = APIRouter()


class ApplyCouponRequest(BaseModel):
    booking_id: int = Field(..., description="Booking ID to apply coupon on")
    code: str = Field(..., description="Promotional coupon code")


@router.get("/available")
async def list_available_coupons(
    db: AsyncSession = Depends(deps.get_db)
):
    """Lists all active promotional coupons available to apply."""
    from datetime import datetime, timezone
    now = datetime.now(timezone.utc)
    result = await db.execute(
        select(Coupon).filter(
            Coupon.is_active == True,
            Coupon.valid_from <= now,
            Coupon.valid_to >= now
        )
    )
    return result.scalars().all()


@router.post("/validate")
async def validate_coupon(
    code: str = Query(..., min_length=2),
    booking_amount: Decimal = Query(..., gt=0),
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Validates a coupon code and returns expected discount values.
    """
    coupon = await CouponService.validate_coupon(
        db,
        user_id=current_user.id,
        coupon_code=code,
        booking_amount=booking_amount
    )
    discount = CouponService.calculate_discount(coupon, booking_amount=booking_amount)
    
    return {
        "valid": True,
        "coupon_code": coupon.code,
        "discount_type": coupon.discount_type,
        "discount_value": coupon.discount_value,
        "calculated_discount": discount,
        "final_payable": booking_amount - discount
    }


@router.post("/apply")
async def apply_coupon(
    request: ApplyCouponRequest,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Validates and applies a coupon discount to a booking request.
    """
    # 1. Fetch booking
    booking_result = await db.execute(
        select(Booking).filter(Booking.id == request.booking_id)
    )
    booking = booking_result.scalars().first()
    if not booking:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Booking request not found"
        )
        
    if booking.guest_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Unauthorized to modify this booking request"
        )
        
    if booking.coupon_id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="A coupon discount has already been applied to this booking"
        )
        
    # 2. Validate coupon rules
    coupon = await CouponService.validate_coupon(
        db,
        user_id=current_user.id,
        coupon_code=request.code,
        booking_amount=Decimal(str(booking.fare_estimate or 0.0))
    )
    
    # 3. Calculate discount amount
    discount = CouponService.calculate_discount(coupon, booking_amount=Decimal(str(booking.fare_estimate or 0.0)))
    
    # 4. Save redemption log and update booking fields
    booking.coupon_id = coupon.id
    booking.discount_amount = float(discount)
    
    redemption = CouponRedemption(
        coupon_id=coupon.id,
        user_id=current_user.id,
        booking_id=booking.id
    )
    db.add_all([booking, redemption])
    await db.flush()
    await db.commit()
    
    return {
        "success": True,
        "booking_id": booking.id,
        "coupon_code": coupon.code,
        "discount_amount": float(discount),
        "final_fare": (booking.fare_estimate or 0.0) - float(discount)
    }
