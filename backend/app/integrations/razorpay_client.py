import logging
import hmac
import hashlib
from typing import Dict, Any, Optional
import razorpay
from fastapi import HTTPException, status
from app.core.config import settings

logger = logging.getLogger(__name__)

# Initialize Razorpay Client with Test Mode credentials
razorpay_client = None
if settings.RAZORPAY_KEY_ID and settings.RAZORPAY_KEY_SECRET:
    try:
        razorpay_client = razorpay.Client(auth=(settings.RAZORPAY_KEY_ID, settings.RAZORPAY_KEY_SECRET))
        logger.info(f"Razorpay client initialized (key: {settings.RAZORPAY_KEY_ID[:12]}...)")
    except Exception as e:
        logger.error(f"Failed to initialize Razorpay Client: {e}")


async def create_razorpay_order(amount_inr: float, booking_id: int) -> Dict[str, Any]:
    """
    Creates a Razorpay Order transaction.
    Takes amount in INR and converts to Paise (Razorpay expects integer amount in lowest currency unit).
    """
    if not razorpay_client:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Payment gateway is not configured. Please contact support."
        )

    amount_paise = int(round(amount_inr * 100))
    receipt_id = f"receipt_booking_{booking_id}"

    try:
        data = {
            "amount": amount_paise,
            "currency": "INR",
            "receipt": receipt_id,
            "notes": {
                "booking_id": str(booking_id)
            }
        }
        order = razorpay_client.order.create(data=data)
        return {
            "order_id": order["id"],
            "amount": order["amount"],  # Amount in paise from Razorpay
            "amount_inr": amount_inr,   # Original amount in INR
            "currency": order["currency"],
            "status": order["status"],
            "provider": "razorpay"
        }
    except Exception as e:
        logger.error(f"Razorpay Order creation API error: {e}")
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Payment gateway order creation failed: {str(e)}"
        )


def verify_payment_signature(
    razorpay_order_id: str,
    razorpay_payment_id: str,
    razorpay_signature: str
) -> bool:
    """
    Validates payment success signatures returned by checkout clients using HMAC SHA256.
    """
    if not razorpay_client:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Payment gateway is not configured for signature verification."
        )

    try:
        params_dict = {
            'razorpay_order_id': razorpay_order_id,
            'razorpay_payment_id': razorpay_payment_id,
            'razorpay_signature': razorpay_signature
        }
        razorpay_client.utility.verify_payment_signature(params_dict)
        return True
    except razorpay.errors.SignatureVerificationError:
        logger.warning(f"Razorpay signature verification failed for order {razorpay_order_id}")
        return False
    except Exception as e:
        logger.error(f"Razorpay signature verification error: {e}")
        return False


def verify_webhook_signature(payload_bytes: bytes, signature_header: str, webhook_secret: str) -> bool:
    """
    Verifies that the incoming webhook payload was digitally signed by Razorpay.
    """
    if not razorpay_client:
        logger.warning("Webhook signature check skipped — Razorpay client not initialized.")
        return False

    try:
        expected_signature = hmac.new(
            webhook_secret.encode('utf-8'),
            payload_bytes,
            hashlib.sha256
        ).hexdigest()

        return hmac.compare_digest(expected_signature, signature_header)
    except Exception as e:
        logger.error(f"Error validating webhook signature: {e}")
        return False


async def refund_payment(payment_id: str, amount_inr: float) -> Dict[str, Any]:
    """
    Refunds a transaction through Razorpay API.
    """
    if not razorpay_client:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Payment gateway is not configured for refund processing."
        )

    amount_paise = int(round(amount_inr * 100))
    try:
        refund = razorpay_client.payment.refund(payment_id, {
            "amount": amount_paise
        })
        return {
            "refund_id": refund["id"],
            "status": refund["status"],
            "amount": refund["amount"] / 100.0,
            "provider": "razorpay"
        }
    except Exception as e:
        logger.error(f"Razorpay refund failed for payment {payment_id}: {e}")
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Payment gateway refund failed: {str(e)}"
        )
