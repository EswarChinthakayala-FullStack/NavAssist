from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession
from decimal import Decimal
from pydantic import BaseModel, Field

from app.api import deps
from app.models.location import ServicePointType
from app.schemas.booking import FareEstimateOut
from app.services.geo_service import GeoService
from app.services.pricing_service import PricingService

router = APIRouter()


class StandaloneEstimateRequest(BaseModel):
    pickup_latitude: float = Field(..., ge=-90.0, le=90.0)
    pickup_longitude: float = Field(..., ge=-180.0, le=180.0)
    destination_latitude: float = Field(..., ge=-90.0, le=90.0)
    destination_longitude: float = Field(..., ge=-180.0, le=180.0)
    service_point_type: ServicePointType = Field(ServicePointType.GENERAL)


@router.post("/estimate", response_model=FareEstimateOut)
async def get_fare_estimate(
    request: StandaloneEstimateRequest,
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Standalone pre-booking endpoint to compute journey metrics and quote expected fare charges.
    """
    # 1. Fetch route details from Maps service wrapper
    route = await GeoService.get_route(
        pickup_lat=request.pickup_latitude,
        pickup_lon=request.pickup_longitude,
        dest_lat=request.destination_latitude,
        dest_lon=request.destination_longitude
    )
    
    distance_km = route["distance_meters"] / 1000.0
    duration_min = route["duration_seconds"] / 60.0
    
    # 2. Calculate dynamic fare rules & breakdown
    breakdown = await PricingService.calculate_fare_breakdown(
        db,
        service_point_type=request.service_point_type,
        distance_km=distance_km,
        duration_minutes=duration_min
    )
    
    return FareEstimateOut(
        pickup_address=route.get("pickup_address", "Pickup Coordinates"),
        destination_address=route.get("destination_address", "Destination Coordinates"),
        distance_km=round(distance_km, 2),
        duration_minutes=int(round(duration_min)),
        base_fare=breakdown["base_fare"],
        distance_fare=breakdown["distance_fare"],
        time_fare=breakdown["time_fare"],
        waiting_charges=breakdown["waiting_charges"],
        booking_fee=breakdown["booking_fee"],
        subtotal=breakdown["subtotal"],
        surge_multiplier=breakdown["surge_multiplier"],
        surge_amount=breakdown["surge_amount"],
        taxes=breakdown["taxes"],
        discount_amount=breakdown["discount_amount"],
        total_fare=breakdown["total_fare"],
        estimated_fare=breakdown["total_fare"]
    )
