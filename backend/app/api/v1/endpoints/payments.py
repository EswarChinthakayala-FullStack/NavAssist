from fastapi import APIRouter, Depends, status, Request, Header, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.api import deps
from app.models.user import User, UserRole
from app.models.payment import PaymentMethod
from app.schemas.payment import PaymentCreate, PaymentResponse, PaymentVerify, CashConfirmRequest, PaymentFailureRecord
from app.services.payment_service import PaymentService
from app.core.config import settings

router = APIRouter()


def _inject_key(payment_response: PaymentResponse) -> PaymentResponse:
    """Attaches the Razorpay public key to the response for frontend Checkout initialization."""
    payment_response.razorpay_key_id = settings.RAZORPAY_KEY_ID
    return payment_response


@router.post("/create-order", response_model=PaymentResponse, status_code=status.HTTP_201_CREATED)
async def create_payment_order(
    request: PaymentCreate,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Creates a Razorpay Order ID (or records Cash payment request) for a booking with transaction safety.
    Only guests can initiate payments.
    """
    payment = await PaymentService.create_payment_order(
        db=db,
        guest_user=current_user,
        booking_id=request.booking_id,
        payment_method=request.payment_method or PaymentMethod.ONLINE
    )
    await db.commit()
    response = PaymentResponse.model_validate(payment)
    return _inject_key(response)


@router.post("/booking/{booking_id}/retry", response_model=PaymentResponse)
async def retry_payment_order(
    booking_id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Generates a new Razorpay Order ID for an existing unpaid/failed booking without creating duplicate bookings.
    """
    payment = await PaymentService.retry_payment_order(
        db=db,
        guest_user=current_user,
        booking_id=booking_id
    )
    await db.commit()
    response = PaymentResponse.model_validate(payment)
    return _inject_key(response)


@router.post("/verify", response_model=PaymentResponse)
async def verify_payment(
    request: PaymentVerify,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Executes strict 5-point verification (HMAC SHA256 signature, order ID, payment ID, amount, user ownership).
    """
    payment = await PaymentService.verify_and_capture_payment(
        db=db,
        guest_user=current_user,
        razorpay_order_id=request.razorpay_order_id,
        razorpay_payment_id=request.razorpay_payment_id,
        razorpay_signature=request.razorpay_signature
    )
    await db.commit()
    return payment


@router.post("/failure", response_model=PaymentResponse)
async def record_payment_failure(
    request: PaymentFailureRecord,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Records a client-side payment failure from Razorpay Checkout.
    Logs the error details and updates the payment status to FAILED.
    """
    payment = await PaymentService.record_payment_failure(
        db=db,
        guest_user=current_user,
        razorpay_order_id=request.razorpay_order_id,
        error_code=request.error_code,
        error_description=request.error_description,
        error_reason=request.error_reason
    )
    await db.commit()
    return payment


@router.post("/confirm-cash", response_model=PaymentResponse)
async def confirm_cash_payment(
    request: CashConfirmRequest,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Assistant or Admin action to confirm cash collection for a booking.
    """
    payment = await PaymentService.confirm_cash_payment(
        db=db,
        booking_id=request.booking_id,
        confirmed_by_user=current_user
    )
    await db.commit()
    return payment


@router.post("/webhook/razorpay", status_code=status.HTTP_200_OK)
async def razorpay_webhook(
    request: Request,
    x_razorpay_signature: str = Header(..., description="Razorpay event signature header"),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Public webhook listener for Razorpay transaction state updates.
    """
    body_bytes = await request.body()
    result = await PaymentService.process_webhook(
        db=db,
        body_bytes=body_bytes,
        signature=x_razorpay_signature
    )
    await db.commit()
    return result


@router.get("/{booking_id}/receipt")
async def get_payment_receipt(
    booking_id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Downloads an itemized transaction receipt/invoice copy for verified bookings."""
    return await PaymentService.generate_payment_receipt(
        db=db,
        booking_id=booking_id,
        user=current_user
    )


@router.post("/{booking_id}/refund", response_model=PaymentResponse)
async def refund_booking_payment(
    booking_id: int,
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Admin-only endpoint to process gateway refunds for cancelled journeys.
    """
    payment = await PaymentService.refund_payment(
        db=db,
        booking_id=booking_id,
        admin_user=current_user
    )
    await db.commit()
    return payment
