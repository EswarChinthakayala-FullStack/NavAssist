import asyncio
import logging
from datetime import datetime, timedelta, timezone
from sqlalchemy.future import select

from app.core.celery_app import celery_app
from app.core.database import SessionLocal
from app.models.booking import Booking, BookingStatus

logger = logging.getLogger(__name__)


async def _async_expire_pending_bookings():
    threshold = datetime.now(timezone.utc) - timedelta(minutes=5)
    async with SessionLocal() as db:
        try:
            result = await db.execute(
                select(Booking).filter(
                    Booking.status == BookingStatus.PENDING,
                    Booking.created_at <= threshold
                )
            )
            expired_bookings = result.scalars().all()
            if not expired_bookings:
                return 0
                
            for booking in expired_bookings:
                booking.status = BookingStatus.EXPIRED
                db.add(booking)
                logger.info(f"Booking ID {booking.id} has expired due to no assistant response.")
                
            await db.commit()
            return len(expired_bookings)
        except Exception as e:
            await db.rollback()
            logger.error(f"Error executing pending bookings expiry sweep: {e}")
            raise


@celery_app.task(name="app.tasks.matching_tasks.auto_expire_unassigned_bookings")
def auto_expire_unassigned_bookings():
    """Periodic sweep task looking for pending bookings not claimed in 5 minutes."""
    logger.info("Starting auto_expire_unassigned_bookings cron sweep...")
    count = asyncio.run(_async_expire_pending_bookings())
    logger.info(f"Sweep completed. Expired {count} pending bookings.")
    return count
