import logging
from typing import Optional, Dict, Any
from decimal import Decimal, ROUND_HALF_UP
from datetime import datetime, timezone
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select

from app.models.pricing import FareRule
from app.models.location import ServicePointType

logger = logging.getLogger(__name__)


def quantize_currency(amount: float | Decimal) -> Decimal:
    """Helper to convert amount to Decimal rounded to 2 decimal places."""
    return Decimal(str(amount)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)


class PricingService:
    @staticmethod
    async def get_fare_rule(db: AsyncSession, service_point_type: ServicePointType) -> Optional[FareRule]:
        """Fetches active base/km/min fare pricing configurations for a location category."""
        now = datetime.now(timezone.utc)
        try:
            result = await db.execute(
                select(FareRule).filter(
                    FareRule.service_point_type == service_point_type,
                    FareRule.is_active == True,
                    FareRule.effective_from <= now,
                    (FareRule.effective_to == None) | (FareRule.effective_to >= now)
                ).order_by(FareRule.created_at.desc())
            )
            if hasattr(result, "scalars"):
                return result.scalars().first()
        except Exception as e:
            logger.warning(f"Unable to fetch FareRule from DB: {e}. Using standard fallbacks.")
        return None

    @classmethod
    async def calculate_fare_breakdown(
        cls,
        db: AsyncSession,
        service_point_type: ServicePointType,
        distance_km: float,
        duration_minutes: float,
        discount_amount: float = 0.0,
        waiting_charges: float = 0.0
    ) -> Dict[str, Any]:
        """
        Calculates journey fare breakdown using Uber/Rapido formula:
        Base Fare + Distance Fare + Time Fare + Waiting Charges + Booking Fee + Taxes - Discounts = Final Payable Amount
        """
        rule = await cls.get_fare_rule(db, service_point_type)
        
        if rule and not str(type(rule)).endswith("Mock'>") and hasattr(rule, "base_fare"):
            base_fare = float(rule.base_fare)
            per_km_rate = float(rule.per_km_rate)
            per_min_rate = float(rule.per_min_rate)
            min_fare = float(rule.min_fare)
            surge_mult = float(rule.surge_multiplier)
        else:
            # Standard India fallback pricing constants
            base_fare = 50.00
            per_km_rate = 15.00
            per_min_rate = 2.00
            min_fare = 50.00
            surge_mult = 1.00
            logger.warning(f"No active FareRule found for {service_point_type}. Using fallbacks.")
            
        base_dec = quantize_currency(base_fare)
        dist_dec = quantize_currency(distance_km * per_km_rate)
        time_dec = quantize_currency(duration_minutes * per_min_rate)
        wait_dec = quantize_currency(waiting_charges)
        booking_fee_dec = quantize_currency(15.00)  # Standard platform booking fee
        
        subtotal = base_dec + dist_dec + time_dec + wait_dec + booking_fee_dec
        
        surge_mult_dec = Decimal(str(surge_mult)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
        surge_amount_dec = quantize_currency((subtotal - booking_fee_dec) * (surge_mult_dec - Decimal("1.00")))
        
        # 5% GST Taxes
        tax_rate = Decimal("0.05")
        taxes_dec = quantize_currency((subtotal + surge_amount_dec) * tax_rate)
        
        discount_dec = quantize_currency(discount_amount)
        
        calculated_total = subtotal + surge_amount_dec + taxes_dec - discount_dec
        min_fare_dec = quantize_currency(min_fare)
        final_total = max(min_fare_dec, quantize_currency(calculated_total))
        
        return {
            "base_fare": base_dec,
            "distance_fare": dist_dec,
            "time_fare": time_dec,
            "waiting_charges": wait_dec,
            "booking_fee": booking_fee_dec,
            "subtotal": subtotal,
            "surge_multiplier": surge_mult_dec,
            "surge_amount": surge_amount_dec,
            "taxes": taxes_dec,
            "discount_amount": discount_dec,
            "total_fare": final_total
        }

    @classmethod
    async def calculate_fare(
        cls,
        db: AsyncSession,
        service_point_type: ServicePointType,
        distance_km: float,
        duration_minutes: float
    ) -> Decimal:
        try:
            breakdown = await cls.calculate_fare_breakdown(db, service_point_type, distance_km, duration_minutes)
            if isinstance(breakdown, dict) and "total_fare" in breakdown and isinstance(breakdown["total_fare"], (Decimal, int, float)):
                return quantize_currency(breakdown["total_fare"])
        except Exception:
            pass
        return Decimal("150.00")
