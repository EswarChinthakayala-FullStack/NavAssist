import asyncio
import logging
from app.core.celery_app import celery_app
from app.core.database import SessionLocal
from app.models.assistant import Payout, PayoutStatus

logger = logging.getLogger(__name__)


async def _async_process_payment_webhook(body_bytes: bytes, signature: str):
    async with SessionLocal() as db:
        try:
            from app.services.payment_service import PaymentService
            res = await PaymentService.process_webhook(db, body_bytes, signature)
            await db.commit()
            return res
        except Exception as e:
            await db.rollback()
            logger.error(f"Failed to process payment webhook asynchronously: {e}")
            return False


@celery_app.task(name="app.tasks.payment_tasks.process_payment_webhook")
def process_payment_webhook(body_bytes: bytes, signature: str):
    """Processes incoming payment webhooks from gateways asynchronously."""
    logger.info("Processing gateway payment webhook event payload asynchronously...")
    return asyncio.run(_async_process_payment_webhook(body_bytes, signature))


async def _async_process_assistant_payouts():
    async with SessionLocal() as db:
        try:
            from sqlalchemy.future import select
            res = await db.execute(select(Payout).filter(Payout.status == PayoutStatus.PENDING))
            pending_payouts = res.scalars().all()
            if not pending_payouts:
                return 0
                
            from app.services.payout_service import PayoutService
            processed_count = 0
            for payout in pending_payouts:
                import uuid
                ref_id = f"PAYOUT-BATCH-{uuid.uuid4().hex[:10].upper()}"
                await PayoutService.process_payout(
                    db=db,
                    payout_id=payout.id,
                    admin_user_id=1,
                    reference_id=ref_id,
                    approve=True
                )
                processed_count += 1
                
            await db.commit()
            return processed_count
        except Exception as e:
            await db.rollback()
            logger.error(f"Failed to process batch assistant payouts: {e}")
            return 0


@celery_app.task(name="app.tasks.payment_tasks.process_assistant_payouts")
def process_assistant_payouts():
    """Batch processes guides weekly payouts and logs ledger sheets."""
    logger.info("Processing weekly payouts disbursements queue...")
    return asyncio.run(_async_process_assistant_payouts())
