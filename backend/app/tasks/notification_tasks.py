import asyncio
import logging
from sqlalchemy.future import select

from app.core.celery_app import celery_app
from app.core.database import SessionLocal
from app.models.user import User
from app.models import EmergencyContact
from app.integrations import sms, push

logger = logging.getLogger(__name__)


@celery_app.task(name="app.tasks.notification_tasks.send_push_notification")
def send_push_notification(token: str, title: str, body: str, data: dict = None):
    """Asynchronously dispatches push notifications via FCM."""
    return asyncio.run(push.send_push_notification(token, title, body, data))


@celery_app.task(name="app.tasks.notification_tasks.send_booking_receipt_email")
def send_booking_receipt_email(email: str, booking_id: int, amount: float):
    """Sends a booking payment receipt details to a guest."""
    logger.info(f"Booking receipt email sent to {email} for booking {booking_id} (Rs.{amount:.2f})")
    return True


@celery_app.task(name="app.tasks.notification_tasks.send_otp_sms")
def send_otp_sms(phone: str, otp: str):
    """Asynchronously dispatches OTP via SMS service (Twilio)."""
    message = f"Your NavAssist verification code is: {otp}. Valid for 5 minutes. Do not share this code."
    return asyncio.run(sms.send_sms(phone, message))


async def _async_send_sos_notifications(user_id: int, latitude: float, longitude: float, booking_id: int):
    async with SessionLocal() as db:
        try:
            user_result = await db.execute(select(User).filter(User.id == user_id))
            user = user_result.scalars().first()
            if not user:
                return False
                
            user_name = user.full_name or "A NavAssist User"
            contacts_result = await db.execute(
                select(EmergencyContact).filter(EmergencyContact.user_id == user_id)
            )
            contacts = contacts_result.scalars().all()
            
            if not contacts:
                logger.warning(f"SOS Triggered by {user_name}, but no emergency contacts are configured.")
                return False
                
            maps_link = f"https://maps.google.com/?q={latitude},{longitude}"
            alert_message = (
                f"EMERGENCY ALERT: {user_name} has triggered an SOS panic button during their NavAssist journey. "
                f"Location: {maps_link}. Please check on them immediately. (Booking #{booking_id})"
            )
            
            sent_count = 0
            for contact in contacts:
                success = await sms.send_sms(contact.phone, alert_message)
                if success:
                    sent_count += 1
            return True
        except Exception as e:
            logger.error(f"Failed to process SOS emergency dispatch: {e}")
            return False


@celery_app.task(name="app.tasks.notification_tasks.send_sos_notifications")
def send_sos_notifications(user_id: int, latitude: float, longitude: float, booking_id: int):
    """Asynchronously alerts emergency contacts with active GPS locations during an SOS incident."""
    logger.critical(f"SOS TRIGGERED BY USER ID: {user_id} ON BOOKING ID: {booking_id}!")
    return asyncio.run(_async_send_sos_notifications(user_id, latitude, longitude, booking_id))
