from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession
from typing import List, Dict, Any

from app.api import deps
from app.services.geo_service import GeoService

router = APIRouter()


@router.get("/autocomplete")
async def autocomplete(
    q: str = Query(..., min_length=2, description="Place search query prefix")
):
    """Suggests address predictions based on search string prefixes."""
    return await GeoService.autocomplete_place(q)


@router.get("/geocode")
async def geocode(
    address: str = Query(..., description="Full descriptive address to geocode")
):
    """Converts a descriptive address search text into latitude/longitude coordinates."""
    return await GeoService.geocode_address(address)


@router.get("/reverse-geocode")
async def reverse_geocode(
    latitude: float = Query(..., ge=-90.0, le=90.0),
    longitude: float = Query(..., ge=-180.0, le=180.0)
):
    """Converts physical coordinates back into a human-readable address description."""
    return await GeoService.reverse_geocode(latitude, longitude)


@router.get("/service-points")
async def get_service_points(
    db: AsyncSession = Depends(deps.get_db)
):
    """Retrieves all active configured railway stations, airports, and bus stands."""
    from sqlalchemy.future import select
    from app.models.location import ServicePoint
    result = await db.execute(select(ServicePoint).filter(ServicePoint.is_active == True))
    points = result.scalars().all()
    return [
        {
            "id": p.id,
            "name": p.name,
            "type": p.type,
            "city": p.city,
            "state": p.state,
            "code": p.code,
            "latitude": p.latitude,
            "longitude": p.longitude
        }
        for p in points
    ]
