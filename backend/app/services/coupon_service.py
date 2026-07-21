import logging
from decimal import Decimal
from datetime import datetime, timezone
from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import func

from app.models.pricing import Coupon, CouponRedemption, CouponDiscountType
from app.core.exceptions import ValidationError

logger = logging.getLogger(__name__)


class CouponService:
    @staticmethod
    async def validate_coupon(
        db: AsyncSession,
        user_id: int,
        coupon_code: str,
        booking_amount: Decimal
    ) -> Coupon:
        """
        Validates coupon active periods, user/global caps, and minimum booking spend.
        Throws ValidationError for failures.
        """
        result = await db.execute(
            select(Coupon).filter(
                Coupon.code == coupon_code,
                Coupon.is_active == True
            )
        )
        coupon = result.scalars().first()
        if not coupon:
            raise ValidationError("Invalid or inactive coupon code")
            
        now = datetime.now(timezone.utc)
        if coupon.valid_from > now or coupon.valid_to < now:
            raise ValidationError("Coupon is expired or not yet active")
            
        if booking_amount < Decimal(str(coupon.min_booking_amount)):
            raise ValidationError(
                f"Minimum booking amount of Rs.{coupon.min_booking_amount:.2f} required to use this coupon"
            )
            
        # Check global usage limits
        if coupon.total_usage_limit is not None:
            count_result = await db.execute(
                select(func.count(CouponRedemption.id)).filter(CouponRedemption.coupon_id == coupon.id)
            )
            total_redemptions = count_result.scalar() or 0
            if total_redemptions >= coupon.total_usage_limit:
                raise ValidationError("Coupon usage limit has been reached")
                
        # Check user-specific limits
        if coupon.usage_limit_per_user is not None:
            user_count_result = await db.execute(
                select(func.count(CouponRedemption.id)).filter(
                    CouponRedemption.coupon_id == coupon.id,
                    CouponRedemption.user_id == user_id
                )
            )
            user_redemptions = user_count_result.scalar() or 0
            if user_redemptions >= coupon.usage_limit_per_user:
                raise ValidationError("You have reached the usage limit for this coupon")
                
        return coupon

    @staticmethod
    def calculate_discount(coupon: Coupon, booking_amount: Decimal) -> Decimal:
        """
        Calculates campaign discount amounts based on FLAT or PERCENTAGE configurations,
        applying max_discount_amount caps where configured.
        """
        amount = float(booking_amount)
        discount_val = float(coupon.discount_value)
        
        if coupon.discount_type == CouponDiscountType.FLAT:
            discount = discount_val
        elif coupon.discount_type == CouponDiscountType.PERCENTAGE:
            discount = amount * (discount_val / 100.0)
        else:
            discount = 0.0
            
        # Apply max discount cap
        if coupon.max_discount_amount is not None:
            discount = min(discount, float(coupon.max_discount_amount))
            
        # Ensure discount doesn't exceed target booking charge
        discount = min(discount, amount)
        return Decimal(round(discount, 2))
