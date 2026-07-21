import logging
from app.core.config import settings

logger = logging.getLogger(__name__)

twilio_client = None
if settings.TWILIO_ACCOUNT_SID and settings.TWILIO_AUTH_TOKEN:
    try:
        from twilio.rest import Client
        twilio_client = Client(settings.TWILIO_ACCOUNT_SID, settings.TWILIO_AUTH_TOKEN)
    except Exception as e:
        logger.warning(f"Failed to initialize Twilio client. Console fallback active: {e}")


def is_mock_mode() -> bool:
    """Checks if the Twilio client is running in console mock mode."""
    return twilio_client is None


async def send_sms(to_phone: str, body: str) -> bool:
    """
    Sends an SMS text message using Twilio or falls back to logger output in development.
    """
    if not is_mock_mode():
        try:
            twilio_client.messages.create(
                to=to_phone,
                from_=settings.TWILIO_FROM_NUMBER or "+1234567890",
                body=body
            )
            logger.info(f"SMS successfully dispatched via Twilio to: {to_phone}")
            return True
        except Exception as e:
            logger.error(f"Twilio SMS delivery failed: {e}. Falling back to console log.")

    # Local Fallback Mode
    print(f"\n================ [MOCK SMS DISPATCH] ================")
    print(f"TO:      {to_phone}")
    print(f"MESSAGE: {body}")
    print(f"======================================================\n")
    logger.info(f"Mock SMS dispatched to {to_phone}: {body}")
    return True
