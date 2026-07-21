from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from pydantic import BaseModel, Field
from typing import Optional, List

from app.api import deps
from app.repositories import booking_repository as crud_booking
from app.repositories.assistant_repository import assistant_repository as crud_assistant
from app.models import User, UserRole
from app.models.booking import BookingStatus, Booking
from app.schemas.booking import BookingCreate, BookingResponse, BookingStatusUpdate, FareEstimateOut
from app.services.booking_service import BookingService
from app.services.pricing_service import PricingService
from app.services.geo_service import GeoService

router = APIRouter()


class CancelRequest(BaseModel):
    reason: Optional[str] = Field(None, description="Explanation for booking cancellation")


class StatusRequest(BaseModel):
    status: str = Field(..., description="Target status transition")
    otp: Optional[str] = Field(None, description="Guest OTP (required for starting/picked_up transition)")


@router.post("/", response_model=BookingResponse, status_code=status.HTTP_201_CREATED)
async def create_booking(
    request: BookingCreate,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Creates a new booking (immediate or scheduled).
    Calculates estimated distance and fares, saving as pending.
    """
    if current_user.role != UserRole.GUEST:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only guests can request bookings"
        )
        
    booking = await BookingService.create_booking_request(
        db=db,
        guest_id=current_user.id,
        pickup_latitude=request.pickup_latitude,
        pickup_longitude=request.pickup_longitude,
        pickup_address=request.pickup_address,
        destination_latitude=request.destination_latitude,
        destination_longitude=request.destination_longitude,
        destination_address=request.destination_address,
        assistant_id=request.assistant_id
    )
    await db.commit()
    
    return BookingResponse(
        id=booking.id,
        guest_id=booking.guest_id,
        assistant_id=booking.assistant_id,
        status=booking.status,
        pickup_latitude=request.pickup_latitude,
        pickup_longitude=request.pickup_longitude,
        pickup_address=booking.pickup_address,
        destination_latitude=request.destination_latitude,
        destination_longitude=request.destination_longitude,
        destination_address=booking.destination_address,
        fare_amount=booking.fare_estimate or booking.final_fare or 0.0,
        otp_start=booking.otp_start,
        created_at=booking.created_at,
        updated_at=booking.updated_at
    )


# Backward compatibility compatibility alias
@router.post("/request", response_model=BookingResponse, status_code=status.HTTP_201_CREATED, include_in_schema=False)
async def request_booking_legacy(request: BookingCreate, current_user: User = Depends(deps.get_current_user), db: AsyncSession = Depends(deps.get_db)):
    return await create_booking(request, current_user, db)


from sqlalchemy.orm import selectinload

def serialize_booking_response(booking: Booking, current_user: User) -> BookingResponse:
    p_lat = getattr(booking, "_pickup_lat", 0.0)
    p_lon = getattr(booking, "_pickup_lon", 0.0)
    d_lat = getattr(booking, "_dest_lat", 0.0)
    d_lon = getattr(booking, "_dest_lon", 0.0)
    if hasattr(booking, "pickup_coordinates") and isinstance(booking.pickup_coordinates, bytes):
        try:
            from app.models.booking import parse_wkb_point
            p_lon, p_lat = parse_wkb_point(booking.pickup_coordinates)
        except Exception:
            pass
    if hasattr(booking, "destination_coordinates") and isinstance(booking.destination_coordinates, bytes):
        try:
            from app.models.booking import parse_wkb_point
            d_lon, d_lat = parse_wkb_point(booking.destination_coordinates)
        except Exception:
            pass

    history_list = []
    try:
        if "status_history" in booking.__dict__ and booking.status_history:
            for h in booking.status_history:
                history_list.append({
                    "status": h.status if isinstance(h.status, str) else h.status.value,
                    "changed_at": h.changed_at,
                    "changed_by": h.changed_by
                })
    except Exception:
        pass

    g_name = None
    g_phone = None
    g_avatar = None
    try:
        if "guest" in booking.__dict__ and booking.guest:
            g_name = getattr(booking.guest, "full_name", None) or getattr(booking.guest, "name", None) or getattr(booking.guest, "email", None)
            g_phone = getattr(booking.guest, "phone_number", None) or getattr(booking.guest, "phone", None)
            g_avatar = getattr(booking.guest, "profile_photo_url", None)
    except Exception:
        pass

    a_name = None
    a_phone = None
    a_avatar = None
    try:
        if "assistant" in booking.__dict__ and booking.assistant:
            a_name = getattr(booking.assistant, "full_name", None) or getattr(booking.assistant, "name", None) or getattr(booking.assistant, "email", None)
            a_phone = getattr(booking.assistant, "phone_number", None) or getattr(booking.assistant, "phone", None)
            a_avatar = getattr(booking.assistant, "profile_photo_url", None)
            try:
                if not a_avatar and "assistant_profile" in booking.assistant.__dict__ and booking.assistant.assistant_profile:
                    a_avatar = getattr(booking.assistant.assistant_profile, "profile_photo_url", None)
            except Exception:
                pass
    except Exception:
        pass

    return BookingResponse(
        id=booking.id,
        guest_id=booking.guest_id,
        assistant_id=booking.assistant_id,
        status=booking.status,
        pickup_latitude=p_lat,
        pickup_longitude=p_lon,
        pickup_address=booking.pickup_address,
        destination_latitude=d_lat,
        destination_longitude=d_lon,
        destination_address=booking.destination_address,
        fare_amount=booking.fare_estimate or booking.final_fare or 0.0,
        otp_start=booking.otp_start if current_user.role != UserRole.ASSISTANT else "",
        guest_name=g_name,
        guest_phone=g_phone,
        guest_avatar=g_avatar,
        assistant_name=a_name,
        assistant_phone=a_phone,
        assistant_avatar=a_avatar,
        created_at=booking.created_at,
        updated_at=booking.updated_at,
        distance_km=booking.distance_km,
        estimated_duration_min=booking.estimated_duration_min,
        status_history=history_list
    )


@router.get("/active/me", response_model=Optional[BookingResponse])
async def get_my_active_booking(
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Returns the single active booking for the authenticated user (guest or assistant).
    Returns null if no active ride exists.
    """
    if current_user.role == UserRole.GUEST:
        booking = await crud_booking.get_active_booking_by_guest(db, guest_id=current_user.id)
    elif current_user.role == UserRole.ASSISTANT:
        booking = await crud_booking.get_active_booking_by_assistant(db, assistant_id=current_user.id)
    else:
        return None

    if not booking:
        return None

    return serialize_booking_response(booking, current_user)


@router.get("/", response_model=List[BookingResponse])
async def list_own_bookings(
    status: Optional[BookingStatus] = None,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Lists own bookings, filterable by status."""
    query = select(Booking).options(
        selectinload(Booking.status_history),
        selectinload(Booking.guest),
        selectinload(Booking.assistant)
    )
    if current_user.role == UserRole.GUEST:
        query = query.filter(Booking.guest_id == current_user.id)
    elif current_user.role == UserRole.ASSISTANT:
        query = query.filter(Booking.assistant_id == current_user.id)
        
    if status:
        query = query.filter(Booking.status == status)
        
    result = await db.execute(query.order_by(Booking.created_at.desc()))
    bookings = result.scalars().all()
    return [serialize_booking_response(b, current_user) for b in bookings]


@router.get("/{booking_id}", response_model=BookingResponse)
async def get_booking_details(
    booking_id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Fetches details of a specific booking."""
    booking = await crud_booking.get_booking(db, booking_id=booking_id)
    if not booking:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Booking not found"
        )
        
    if current_user.role != UserRole.ADMIN and current_user.id != booking.guest_id and current_user.id != booking.assistant_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Unauthorized to access this booking"
        )
    return serialize_booking_response(booking, current_user)


@router.post("/{booking_id}/estimate", response_model=FareEstimateOut)
async def get_booking_fare_estimate(
    booking_id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Fare + ETA estimate for an active booking before confirming."""
    booking = await crud_booking.get_booking(db, booking_id=booking_id)
    if not booking:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Booking not found"
        )
        
    route = await GeoService.get_route(
        pickup_lat=booking.pickup_latitude,
        pickup_lon=booking.pickup_longitude,
        dest_lat=booking.destination_latitude,
        dest_lon=booking.destination_longitude
    )
    
    distance_km = route["distance_meters"] / 1000.0
    duration_min = route["duration_seconds"] / 60.0
    
    from app.models.location import ServicePointType
    breakdown = await PricingService.calculate_fare_breakdown(
        db,
        service_point_type=ServicePointType.GENERAL,
        distance_km=distance_km,
        duration_minutes=duration_min,
        discount_amount=float(booking.discount_amount or 0.0)
    )
    
    return FareEstimateOut(
        pickup_address=booking.pickup_address,
        destination_address=booking.destination_address,
        distance_km=round(distance_km, 2),
        duration_minutes=int(round(duration_min)),
        base_fare=breakdown["base_fare"],
        distance_fare=breakdown["distance_fare"],
        time_fare=breakdown["time_fare"],
        waiting_charges=breakdown["waiting_charges"],
        booking_fee=breakdown["booking_fee"],
        subtotal=breakdown["subtotal"],
        surge_multiplier=breakdown["surge_multiplier"],
        surge_amount=breakdown["surge_amount"],
        taxes=breakdown["taxes"],
        discount_amount=breakdown["discount_amount"],
        total_fare=breakdown["total_fare"],
        estimated_fare=breakdown["total_fare"]
    )


@router.patch("/{booking_id}/cancel", response_model=BookingResponse)
async def cancel_booking(
    booking_id: int,
    request: CancelRequest,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Cancels a booking with reason notes."""
    booking = await BookingService.transition_booking(
        db=db,
        booking_id=booking_id,
        new_status=BookingStatus.CANCELLED,
        changed_by_user_id=current_user.id,
        cancellation_reason=request.reason
    )
    await db.commit()
    return serialize_booking_response(booking, current_user)


@router.patch("/{booking_id}/accept", response_model=BookingResponse)
async def accept_booking(
    booking_id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Allows an assistant to accept an assigned booking request."""
    if current_user.role != UserRole.ASSISTANT:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only assistants can accept bookings"
        )
        
    booking = await BookingService.transition_booking(
        db=db,
        booking_id=booking_id,
        new_status=BookingStatus.ACCEPTED,
        changed_by_user_id=current_user.id,
        assistant_id=current_user.id
    )
    await db.commit()
    return serialize_booking_response(booking, current_user)


@router.patch("/{booking_id}/reject", response_model=BookingResponse)
async def reject_booking(
    booking_id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Assistant declines, triggers re-matching (reverts to PENDING)."""
    if current_user.role != UserRole.ASSISTANT:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only assistants can reject bookings"
        )
        
    booking = await BookingService.transition_booking(
        db=db,
        booking_id=booking_id,
        new_status=BookingStatus.PENDING,
        changed_by_user_id=current_user.id,
        assistant_id=None
    )
    await db.commit()
    return serialize_booking_response(booking, current_user)


@router.patch("/{booking_id}/status")
async def advance_booking_status(
    booking_id: int,
    request: StatusRequest,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Advances lifecycle status (enroute -> picked up -> completed)."""
    booking = await BookingService.transition_booking(
        db=db,
        booking_id=booking_id,
        new_status=request.status,
        changed_by_user_id=current_user.id,
        otp=request.otp
    )
    await db.commit()
    
    status_str = booking.status.value if hasattr(booking.status, "value") else str(booking.status)
    return {
        "status": "success",
        "booking_id": booking.id,
        "booking_status": status_str,
        "otp_start": booking.otp_start
    }


class BookingLocationRequest(BaseModel):
    latitude: float = Field(..., description="Current latitude")
    longitude: float = Field(..., description="Current longitude")


@router.post("/{booking_id}/location")
async def update_booking_location_alias(
    booking_id: int,
    request: BookingLocationRequest,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Alias for location updates on active booking."""
    booking = await crud_booking.get_booking(db, booking_id=booking_id)
    if not booking:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Booking request not found"
        )
    
    from app.core import redis_client
    assistant_id = booking.assistant_id or current_user.id
    try:
        await redis_client.update_assistant_location(assistant_id, request.latitude, request.longitude)
    except Exception:
        pass

    return {"status": "success", "booking_id": booking_id, "latitude": request.latitude, "longitude": request.longitude}


@router.get("/{booking_id}/invoice")
async def get_booking_invoice_pdf(
    booking_id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Generates and returns print-quality TAX invoice PDF for a completed booking journey.
    Validates authenticated user roles and ride status parameters.
    """
    booking = await crud_booking.get_booking(db, booking_id=booking_id)
    if not booking:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Booking record not found"
        )

    # 1. Enforce Role & Ownership Permissions Check
    is_guest = current_user.role == UserRole.GUEST and booking.guest_id == current_user.id
    is_assigned_assistant = current_user.role == UserRole.ASSISTANT and booking.assistant_id == current_user.id
    is_admin = current_user.role == UserRole.ADMIN

    if not (is_guest or is_assigned_assistant or is_admin):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Unauthorized access attempt to download this invoice."
        )

    # 2. Check Ride Status is Completed
    # Status can be BookingStatus.COMPLETED or string equivalent
    status_val = booking.status.value if hasattr(booking.status, "value") else str(booking.status)
    if status_val.lower() != "completed":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invoices can only be downloaded for completed rides. Current status: {status_val.upper()}"
        )

    # 3. Check Payment Status (Completed / Received) with self-healing fallback
    if booking.payment_status.lower() != "completed":
        from app.models.payment import Payment, PaymentStatus
        payment_query = await db.execute(
            select(Payment).filter(
                Payment.booking_id == booking.id,
                Payment.status.in_([PaymentStatus.CAPTURED, PaymentStatus.COMPLETED])
            )
        )
        successful_payment = payment_query.scalars().first()

        if successful_payment:
            booking.payment_status = "completed"
            booking.payment_id = successful_payment.id
            db.add(booking)
            await db.flush()
        else:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invoices can only be downloaded after payment is confirmed received."
            )

    # 4. Generate or fetch cached PDF path via InvoiceService
    import os
    from app.services.invoice_service import InvoiceService
    from fastapi.responses import FileResponse

    pdf_path = await InvoiceService.get_invoice_pdf_path(db, booking)
    
    # Extract filename from path
    filename = os.path.basename(pdf_path)

    return FileResponse(
        path=pdf_path,
        media_type="application/pdf",
        filename=filename
    )

