import asyncio
import logging
from sqlalchemy.future import select

from app.core.celery_app import celery_app
from app.core.database import SessionLocal
from app.models.assistant import AssistantProfile

logger = logging.getLogger(__name__)


async def _async_recompute_trust_scores():
    async with SessionLocal() as db:
        try:
            # Query all active assistant profiles
            result = await db.execute(select(AssistantProfile))
            profiles = result.scalars().all()
            
            for profile in profiles:
                # Trust score calculated out of 5.00 (DECIMAL(3,2) schema)
                base = 3.00
                rating_contrib = (float(profile.avg_rating or 0.0) / 5.0) * 1.50
                trip_contrib = min(0.50, (profile.total_trips or 0) * 0.05)
                
                computed_score = min(5.00, max(0.00, round(base + rating_contrib + trip_contrib, 2)))
                profile.trust_score = computed_score
                db.add(profile)
                
            await db.commit()
            return len(profiles)
        except Exception as e:
            await db.rollback()
            logger.error(f"Failed to recompute trust scores: {e}")
            return 0


@celery_app.task(name="app.tasks.scoring_tasks.recompute_assistant_trust_scores")
def recompute_assistant_trust_scores():
    """Nightly recalculation of assistant marketplace trust scores."""
    logger.info("Starting recompute_assistant_trust_scores nightly run...")
    count = asyncio.run(_async_recompute_trust_scores())
    logger.info(f"Trust score updates calculated successfully for {count} profiles.")
    return count
