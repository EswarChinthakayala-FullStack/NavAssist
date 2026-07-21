import asyncio
import logging
from datetime import datetime, timedelta, timezone
from sqlalchemy.future import select
from sqlalchemy import delete

from app.core.celery_app import celery_app
from app.core.database import SessionLocal
from app.models.user import OtpVerification, RefreshToken
from app.models.safety import TripShare
from app.models.booking import LiveLocation

logger = logging.getLogger(__name__)


async def _async_cleanup_expired_otps():
    async with SessionLocal() as db:
        try:
            now = datetime.now(timezone.utc)
            stmt = delete(OtpVerification).where(OtpVerification.expires_at < now)
            result = await db.execute(stmt)
            await db.commit()
            deleted_count = result.rowcount if hasattr(result, 'rowcount') else 0
            logger.info(f"Cleaned up {deleted_count} expired OTP records.")
            return deleted_count
        except Exception as e:
            await db.rollback()
            logger.error(f"Error cleaning up expired OTPs: {e}")
            return 0


@celery_app.task(name="app.tasks.cleanup_tasks.cleanup_expired_otps")
def cleanup_expired_otps():
    """Sweeps and deletes expired temporary OTP records from database."""
    logger.info("Starting cleanup_expired_otps sweep...")
    return asyncio.run(_async_cleanup_expired_otps())


async def _async_revoke_expired_refresh_tokens():
    async with SessionLocal() as db:
        try:
            now = datetime.now(timezone.utc)
            stmt = delete(RefreshToken).where(RefreshToken.expires_at < now)
            result = await db.execute(stmt)
            await db.commit()
            count = result.rowcount if hasattr(result, 'rowcount') else 0
            logger.info(f"Cleaned up {count} expired refresh tokens.")
            return count
        except Exception as e:
            await db.rollback()
            logger.error(f"Error purging expired refresh tokens: {e}")
            return 0


@celery_app.task(name="app.tasks.cleanup_tasks.revoke_expired_refresh_tokens")
def revoke_expired_refresh_tokens():
    """Sweeps and purges expired refresh tokens from database."""
    logger.info("Starting revoke_expired_refresh_tokens sweep...")
    return asyncio.run(_async_revoke_expired_refresh_tokens())


async def _async_cleanup_live_locations():
    async with SessionLocal() as db:
        try:
            cutoff = datetime.now(timezone.utc) - timedelta(days=7)
            stmt = delete(LiveLocation).where(LiveLocation.recorded_at < cutoff)
            result = await db.execute(stmt)
            await db.commit()
            count = result.rowcount if hasattr(result, 'rowcount') else 0
            logger.info(f"Cleaned up {count} old live location snapshots.")
            return count
        except Exception as e:
            await db.rollback()
            logger.error(f"Error purging live location history: {e}")
            return 0


@celery_app.task(name="app.tasks.cleanup_tasks.cleanup_live_locations")
def cleanup_live_locations():
    """Sweeps location tracking history snapshots older than 7 days."""
    logger.info("Starting cleanup_live_locations sweep...")
    return asyncio.run(_async_cleanup_live_locations())


async def _async_cleanup_expired_trip_shares():
    async with SessionLocal() as db:
        try:
            now = datetime.now(timezone.utc)
            result = await db.execute(
                select(TripShare).filter(
                    TripShare.is_active == True,
                    TripShare.expires_at < now
                )
            )
            shares = result.scalars().all()
            for share in shares:
                share.is_active = False
                db.add(share)
            await db.commit()
            return len(shares)
        except Exception as e:
            await db.rollback()
            logger.error(f"Error deactivating expired trip shares: {e}")
            return 0


@celery_app.task(name="app.tasks.cleanup_tasks.cleanup_expired_trip_shares")
def cleanup_expired_trip_shares():
    """Automatically marks expired trip share link tokens as inactive."""
    logger.info("Starting cleanup_expired_trip_shares sweep...")
    count = asyncio.run(_async_cleanup_expired_trip_shares())
    logger.info(f"Trip shares clean completed. Disabled {count} expired records.")
    return count


async def _async_log_live_location(booking_id: int, assistant_id: int, latitude: float, longitude: float):
    async with SessionLocal() as db:
        try:
            from sqlalchemy import func
            log = LiveLocation(
                booking_id=booking_id,
                actor_type="assistant",
                coordinates=func.ST_PointFromText(f"POINT({longitude} {latitude})", 4326)
            )
            db.add(log)
            
            from app.repositories.assistant_repository import assistant_repository
            await assistant_repository.update_assistant_location(
                db, 
                user_id=assistant_id, 
                latitude=latitude, 
                longitude=longitude
            )
            await db.commit()
            return True
        except Exception as e:
            await db.rollback()
            logger.error(f"Error logging live location in background: {e}")
            return False


@celery_app.task(name="app.tasks.cleanup_tasks.sync_live_location_snapshot")
def sync_live_location_snapshot(booking_id: int, assistant_id: int, latitude: float, longitude: float):
    """Asynchronously logs location history entries to offload WebSocket concurrency workloads."""
    return asyncio.run(_async_log_live_location(booking_id, assistant_id, latitude, longitude))
