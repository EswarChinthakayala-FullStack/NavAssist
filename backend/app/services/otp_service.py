import random
import logging
from datetime import datetime, timezone, timedelta
from sqlalchemy import select, delete
from app.core import redis_client
from app.core.database import SessionLocal
from app.models.user import OtpVerification, OtpPurpose

logger = logging.getLogger(__name__)


class OtpService:
    @staticmethod
    async def send_otp(phone: str) -> str:
        """
        Generates a 6-digit OTP, stores it in Redis and the MySQL DB,
        and dispatches Twilio SMS immediately in a background task.
        """
        otp = f"{random.randint(100000, 999999)}"
        
        # Save to Redis cache
        await redis_client.set_otp(phone, otp, expire_seconds=300)
        
        # Save to MySQL database table otp_verifications
        try:
            async with SessionLocal() as db:
                await db.execute(delete(OtpVerification).filter(OtpVerification.identifier == phone))
                db_otp = OtpVerification(
                    identifier=phone,
                    otp_hash=otp,
                    purpose=OtpPurpose.LOGIN,
                    expires_at=datetime.now(timezone.utc) + timedelta(minutes=5),
                    is_verified=False
                )
                db.add(db_otp)
                await db.commit()
        except Exception as e:
            logger.error(f"Failed to save OTP to database for {phone}: {e}")

        # Dispatch SMS immediately as a background task
        from app.integrations.sms import send_sms
        import asyncio
        message = f"Your NavAssist verification code is: {otp}. Valid for 5 minutes. Do not share this code."
        asyncio.create_task(send_sms(phone, message))
        
        logger.info(f"OTP generated and dispatched to {phone}: {otp}")
        return otp

    @staticmethod
    async def verify_otp(phone: str, otp: str) -> bool:
        """
        Verifies the given OTP against Redis or the MySQL DB.
        If correct, marks it as verified in DB and flushes Redis cache.
        """
        # 1. Try Redis verification
        stored_otp = await redis_client.get_otp(phone)
        if stored_otp and stored_otp == otp:
            await redis_client.delete_otp(phone)
            # Mark verified in MySQL database if present
            try:
                async with SessionLocal() as db:
                    result = await db.execute(
                        select(OtpVerification).filter(
                            OtpVerification.identifier == phone,
                            OtpVerification.otp_hash == otp
                        )
                    )
                    db_otp = result.scalars().first()
                    if db_otp:
                        db_otp.is_verified = True
                        await db.commit()
            except Exception:
                pass
            logger.info(f"OTP verified successfully via Redis for {phone}")
            return True

        # 2. Try DB verification fallback
        try:
            async with SessionLocal() as db:
                result = await db.execute(
                    select(OtpVerification).filter(
                        OtpVerification.identifier == phone,
                        OtpVerification.otp_hash == otp
                    )
                )
                db_otp = result.scalars().first()
                if db_otp and db_otp.expires_at > datetime.now(timezone.utc):
                    db_otp.is_verified = True
                    await db.commit()
                    await redis_client.delete_otp(phone)
                    logger.info(f"OTP verified successfully via MySQL DB for {phone}")
                    return True
        except Exception as e:
            logger.error(f"Database OTP verification query failed: {e}")

        logger.warning(f"OTP verification failed for {phone}")
        return False
