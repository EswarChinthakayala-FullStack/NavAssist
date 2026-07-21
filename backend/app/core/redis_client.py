from typing import List, Optional, Tuple, Dict, Any
import redis.asyncio as aioredis
from app.core.config import settings

# Configure Redis Connection Pool for high concurrency
pool = aioredis.ConnectionPool.from_url(
    settings.REDIS_URL,
    max_connections=1000,
    encoding="utf-8",
    decode_responses=True
)

# Global async Redis client using pool
redis_client = aioredis.Redis(connection_pool=pool)


async def get_redis():
    """Dependency injection helper for FastAPI endpoints."""
    yield redis_client


# OTP-specific helpers
async def set_otp(phone: str, otp: str, expire_seconds: int = 300) -> None:
    """Stores a temporary 6-digit OTP for a phone number in Redis with an expiry timer."""
    key = f"otp:{phone}"
    await redis_client.set(key, otp, ex=expire_seconds)


async def get_otp(phone: str) -> Optional[str]:
    """Retrieves the OTP for a phone number from Redis."""
    key = f"otp:{phone}"
    return await redis_client.get(key)


async def delete_otp(phone: str) -> None:
    """Removes the OTP from Redis after successful verification."""
    key = f"otp:{phone}"
    await redis_client.delete(key)


# Geolocation-specific helpers for Assistants
async def update_assistant_location(assistant_id: int, latitude: float, longitude: float) -> None:
    """
    Updates the live GPS coordinates of an assistant in a Redis spatial index.
    Key used: 'assistants:locations'
    Member: assistant_id
    """
    # Redis GEOADD takes: key, longitude, latitude, member
    await redis_client.geoadd(
        "assistants:locations",
        (longitude, latitude, str(assistant_id))
    )


async def remove_assistant_location(assistant_id: int) -> None:
    """Removes an assistant from the live location spatial index (e.g. when going offline)."""
    await redis_client.zrem("assistants:locations", str(assistant_id))


async def get_nearby_assistants(
    latitude: float,
    longitude: float,
    radius_km: float = 5.0
) -> List[Tuple[str, float]]:
    """
    Finds assistant IDs located within a given radius in kilometers.
    Returns a list of tuples containing: (assistant_id_str, distance_in_km)
    """
    # GEORADIUS query using current client. georadius query is represented by geosearch in newer Redis
    # But for compatibility we can run geosearch or georadius.
    # In redis-py, geoquery can use: geosearch(key, longitude, latitude, radius, unit, withdist)
    try:
        results = await redis_client.geosearch(
            "assistants:locations",
            longitude=longitude,
            latitude=latitude,
            radius=radius_km,
            unit="km",
            withdist=True
        )
        # results format: [[member, distance], ...]
        return [(item[0], item[1]) for item in results]
    except Exception:
        # Fallback for older Redis versions that don't support GEOSEARCH
        # We try GEORADIUS (takes: key, longitude, latitude, radius, unit, withdist)
        results = await redis_client.georadius(
            "assistants:locations",
            longitude,
            latitude,
            radius_km,
            unit="km",
            withdist=True
        )
        return [(item[0], item[1]) for item in results]


async def get_assistant_location(assistant_id: int) -> Optional[Tuple[float, float]]:
    """
    Retrieves the current coordinates (latitude, longitude) of an assistant from the Redis GEO index.
    Returns (latitude, longitude) or None if not found.
    """
    try:
        # geopos returns a list of coordinates tuples [(longitude, latitude)] or [None]
        pos = await redis_client.geopos("assistants:locations", str(assistant_id))
        if pos and pos[0]:
            # Redis stores as (longitude, latitude), so return as (latitude, longitude)
            lon, lat = pos[0]
            return float(lat), float(lon)
    except Exception:
        pass
    return None
