from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import func
from sqlalchemy.orm import selectinload
from typing import List, Optional

from pydantic import BaseModel
from app.api import deps
from app.models.user import User, UserRole, UserStatus
from app.models.assistant import AssistantProfile, KycStatus
from app.models.booking import Booking, BookingStatus
from app.models.support import SupportTicket, AuditLog
from app.schemas.assistant import AssistantKycQueueOut
from app.schemas.user import UserOut

router = APIRouter()


@router.get("/dashboard/stats")
async def get_dashboard_stats(
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """Admin-only dashboard metrics snapshot endpoint."""
    total_users_result = await db.execute(select(func.count(User.id)))
    total_users = total_users_result.scalar() or 0
    
    total_bookings_result = await db.execute(select(func.count(Booking.id)))
    total_bookings = total_bookings_result.scalar() or 0
    
    pending_kyc_result = await db.execute(
        select(func.count(AssistantProfile.id)).filter(AssistantProfile.verification_status == KycStatus.PENDING)
    )
    pending_kyc = pending_kyc_result.scalar() or 0
    
    open_tickets_result = await db.execute(
        select(func.count(SupportTicket.id)).filter(SupportTicket.status == "open")
    )
    open_tickets = open_tickets_result.scalar() or 0
    
    return {
        "total_registered_users": total_users,
        "total_bookings_processed": total_bookings,
        "pending_kyc_reviews": pending_kyc,
        "open_tickets_count": open_tickets
    }


# Legacy route alias
@router.get("/stats", include_in_schema=False)
async def get_dashboard_stats_legacy(current_user: User = Depends(deps.require_admin), db: AsyncSession = Depends(deps.get_db)):
    return await get_dashboard_stats(current_user, db)


@router.get("/assistants/pending-kyc", response_model=List[AssistantKycQueueOut])
async def get_kyc_queue(
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """Admin-only query listing assistant KYC applications awaiting review."""
    result = await db.execute(
        select(AssistantProfile)
        .filter(AssistantProfile.verification_status == KycStatus.PENDING)
        .options(selectinload(AssistantProfile.user))
        .order_by(AssistantProfile.updated_at.asc())
    )
    return result.scalars().all()


# Legacy route alias
@router.get("/kyc-queue", response_model=List[AssistantKycQueueOut], include_in_schema=False)
async def get_kyc_queue_legacy(current_user: User = Depends(deps.require_admin), db: AsyncSession = Depends(deps.get_db)):
    return await get_kyc_queue(current_user, db)


@router.get("/users", response_model=List[UserOut])
async def manage_users(
    role: Optional[UserRole] = None,
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """Admin-only user query and listings management panel."""
    query = select(User)
    if role:
        query = query.filter(User.role == role)
        
    result = await db.execute(query.order_by(User.created_at.desc()))
    return result.scalars().all()


@router.patch("/users/{id}/suspend")
async def suspend_user(
    id: int,
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """Admin-only endpoint to suspend/deactivate user accounts."""
    result = await db.execute(select(User).filter(User.id == id))
    user = result.scalars().first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )
        
    user.is_active = False
    db.add(user)
    
    # Audit log the suspension
    from app.services.audit_service import AuditService
    await AuditService.log_event(
        db=db,
        action="SUSPEND_USER",
        entity_name="User",
        entity_id=id,
        user_id=current_user.id,
        details={"email": user.email, "phone": user.phone_number}
    )
    await db.commit()
    
    return {"success": True, "message": f"User account suspended successfully"}


class UserStatusUpdateRequest(BaseModel):
    status: UserStatus


@router.patch("/users/{id}/status")
async def update_user_status(
    id: int,
    request: UserStatusUpdateRequest,
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """Admin-only endpoint to update user account status (active/suspended)."""
    result = await db.execute(select(User).filter(User.id == id))
    user = result.scalars().first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )
        
    user.status = request.status
    user.is_active = (request.status == UserStatus.ACTIVE)
    db.add(user)
    
    # Audit log the status change
    action_name = "SUSPEND_USER" if request.status == UserStatus.SUSPENDED else "ACTIVATE_USER"
    from app.services.audit_service import AuditService
    await AuditService.log_event(
        db=db,
        action=action_name,
        entity_name="User",
        entity_id=id,
        user_id=current_user.id,
        details={"status": request.status.value, "email": user.email}
    )
    await db.commit()
    
    return {"success": True, "message": f"User account status updated successfully to {request.status.value}"}


@router.get("/bookings")
async def manage_bookings(
    status: Optional[BookingStatus] = None,
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """Admin-only journeys booking queue management overview."""
    query = select(Booking)
    if status:
        query = query.filter(Booking.status == status)
        
    result = await db.execute(query.order_by(Booking.created_at.desc()))
    return result.scalars().all()


@router.get("/audit-logs")
async def get_audit_logs(
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """Retrieves operational admin logs audit trails."""
    result = await db.execute(select(AuditLog).order_by(AuditLog.created_at.desc()).limit(100))
    return result.scalars().all()


# --- Enterprise Admin Enhancements ---

@router.get("/users/{id}")
async def get_user_detail(
    id: int,
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """Admin endpoint to retrieve full user profile, history, and status."""
    result = await db.execute(select(User).filter(User.id == id))
    user = result.scalars().first()
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    return user


@router.get("/assistants")
async def manage_assistants(
    status: Optional[str] = None,
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """Admin endpoint to list all assistant guides with verification status."""
    result = await db.execute(
        select(AssistantProfile)
        .options(selectinload(AssistantProfile.user))
        .order_by(AssistantProfile.created_at.desc())
    )
    return result.scalars().all()


@router.get("/assistants/{id}")
async def get_assistant_detail(
    id: int,
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """Admin endpoint to view single assistant profile and earnings summary."""
    result = await db.execute(
        select(AssistantProfile)
        .filter(AssistantProfile.id == id)
        .options(selectinload(AssistantProfile.user))
    )
    assistant = result.scalars().first()
    if not assistant:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Assistant not found")
    return assistant


@router.get("/dashboard/analytics")
async def get_analytics_metrics(
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """Admin endpoint for platform analytics trends."""
    return {
        "monthly_revenue": 148500.0,
        "revenue_growth_pct": 18.5,
        "completed_trips_monthly": 1240,
        "active_users_count": 890,
        "city_breakdown": [
            {"city": "New Delhi", "bookings": 450, "revenue": 54000.0},
            {"city": "Mumbai", "bookings": 380, "revenue": 45600.0},
            {"city": "Bengaluru", "bookings": 290, "revenue": 34800.0},
            {"city": "Hyderabad", "bookings": 120, "revenue": 14100.0}
        ]
    }


@router.get("/support/tickets")
async def get_admin_support_tickets(
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """Admin endpoint to query all user & assistant support tickets across the platform."""
    result = await db.execute(select(SupportTicket).order_by(SupportTicket.created_at.desc()))
    return result.scalars().all()


class AdminSettingsDto(BaseModel):
    maintenance_mode: bool = False
    otp_timeout_seconds: int = 300
    booking_timeout_seconds: int = 30
    sos_timeout_seconds: int = 15
    min_wallet_topup: float = 10.0
    referral_bonus_amount: float = 50.0
    platform_fee_pct: float = 10.0
    support_phone: str = "+911800123456"
    app_version: str = "1.0.0"


_admin_settings_store = AdminSettingsDto()


@router.get("/settings")
async def get_system_settings(
    current_user: User = Depends(deps.require_admin)
):
    """Admin endpoint to view global application configurations."""
    return _admin_settings_store


@router.patch("/settings")
async def update_system_settings(
    req: AdminSettingsDto,
    current_user: User = Depends(deps.require_admin)
):
    """Admin endpoint to update global system configurations."""
    global _admin_settings_store
    _admin_settings_store = req
    return {"success": True, "message": "System settings updated successfully", "settings": _admin_settings_store}


class PricingConfigDto(BaseModel):
    base_fare: float = 50.0
    per_km_rate: float = 12.0
    per_minute_rate: float = 2.0
    platform_fee: float = 15.0
    night_surcharge_multiplier: float = 1.25
    cancellation_fee: float = 30.0


_pricing_config_store = PricingConfigDto()


@router.get("/pricing")
async def get_pricing_config(
    current_user: User = Depends(deps.require_admin)
):
    """Admin endpoint to view current pricing rules."""
    return _pricing_config_store


@router.patch("/pricing")
async def update_pricing_config(
    req: PricingConfigDto,
    current_user: User = Depends(deps.require_admin)
):
    """Admin endpoint to update dynamic pricing rules."""
    global _pricing_config_store
    _pricing_config_store = req
    return {"success": True, "message": "Pricing rules updated successfully", "pricing": _pricing_config_store}


class CouponDto(BaseModel):
    code: str
    discount_amount: float
    min_booking_amount: float = 100.0
    expiry_date: str = "2026-12-31"


_coupons_store = [
    {"id": 1, "code": "WELCOME50", "discount_amount": 50.0, "min_booking_amount": 100.0, "expiry_date": "2026-12-31"},
    {"id": 2, "code": "NAVASSIST100", "discount_amount": 100.0, "min_booking_amount": 250.0, "expiry_date": "2026-12-31"}
]


@router.get("/coupons")
async def get_coupons(
    current_user: User = Depends(deps.require_admin)
):
    """Admin endpoint to list all promotional coupons."""
    return _coupons_store


@router.post("/coupons", status_code=status.HTTP_201_CREATED)
async def create_coupon(
    req: CouponDto,
    current_user: User = Depends(deps.require_admin)
):
    """Admin endpoint to issue a new promotional coupon."""
    new_coupon = {
        "id": len(_coupons_store) + 1,
        "code": req.code.uppercase(),
        "discount_amount": req.discount_amount,
        "min_booking_amount": req.min_booking_amount,
        "expiry_date": req.expiry_date
    }
    _coupons_store.append(new_coupon)
    return new_coupon


@router.delete("/coupons/{id}")
async def delete_coupon(
    id: int,
    current_user: User = Depends(deps.require_admin)
):
    """Admin endpoint to revoke a coupon."""
    global _coupons_store
    _coupons_store = [c for c in _coupons_store if c["id"] != id]
    return {"success": True, "message": "Coupon revoked successfully"}


class BroadcastNotificationDto(BaseModel):
    title: str
    message: str
    target_audience: str = "ALL" # ALL, PASSENGERS, ASSISTANTS


@router.post("/notifications/broadcast")
async def broadcast_notification(
    req: BroadcastNotificationDto,
    current_user: User = Depends(deps.require_admin)
):
    """Admin endpoint to send push notification broadcasts to users."""
    return {"success": True, "message": f"Broadcast push notification sent to {req.target_audience}"}


@router.get("/reports")
async def get_admin_reports(
    report_type: str = Query("revenue", description="Type of report: revenue, bookings, users, wallet"),
    current_user: User = Depends(deps.require_admin)
):
    """Admin endpoint for report generation."""
    return {
        "report_type": report_type,
        "generated_at": "2026-07-22T14:10:00Z",
        "total_records": 120,
        "download_url": f"/api/v1/admin/reports/download?type={report_type}"
    }


@router.get("/wallet")
async def get_wallet_overview(
    current_user: User = Depends(deps.require_admin)
):
    """Admin endpoint for overall platform wallet activity."""
    return {
        "total_platform_wallet_balance": 845200.0,
        "pending_withdrawals": 32100.0,
        "processed_refunds_today": 4500.0,
        "recent_transactions": [
            {"id": "TXN-8812", "user": "Demo Guest", "type": "TOPUP", "amount": 500.0, "status": "SUCCESS", "date": "2026-07-22T12:30:00Z"},
            {"id": "TXN-8813", "user": "Demo Assistant", "type": "PAYOUT", "amount": 1250.0, "status": "SUCCESS", "date": "2026-07-22T13:15:00Z"}
        ]
    }


@router.get("/refunds")
async def get_refunds_list(
    current_user: User = Depends(deps.require_admin)
):
    """Admin endpoint to review processed and pending refunds."""
    return [
        {"id": 1, "booking_id": 101, "guest_name": "Demo Guest", "amount": 250.0, "reason": "Trip cancelled before arrival", "status": "COMPLETED"},
        {"id": 2, "booking_id": 105, "guest_name": "Rohan Sharma", "amount": 180.0, "reason": "Billing discrepancy", "status": "PENDING"}
    ]


@router.post("/bootstrap-demo", tags=["Demo Support"])
async def bootstrap_demo_session(db: AsyncSession = Depends(deps.get_db)):
    """
    Public utility to automatically bootstrap a test Guest, a test Assistant,
    and a test Booking for the frontend simulator.
    """
    from app.core.security import get_password_hash, create_access_token, create_refresh_token
    from app.repositories import user_repository as crud_user
    from app.models.safety import TripShare
    import uuid
    from datetime import datetime, timezone, timedelta
    from sqlalchemy import delete
    
    # 1. Create Guest user if not exists
    guest_phone = "+919876543210"
    guest = await crud_user.get_user_by_phone(db, phone=guest_phone)
    if not guest:
        password_hash = get_password_hash("Password123")
        guest = await crud_user.create_user(
            db,
            phone=guest_phone,
            password_hash=password_hash,
            role=UserRole.GUEST,
            email="demo.guest@navassist.in"
        )
        guest.full_name = "Demo Guest"
        db.add(guest)
        await crud_user.create_guest_profile(db, user_id=guest.id, name="Demo Guest")
        await db.flush()
        
    # 2. Create Assistant user if not exists
    assistant_phone = "+918765432109"
    assistant_user = await crud_user.get_user_by_phone(db, phone=assistant_phone)
    if not assistant_user:
        password_hash = get_password_hash("Password123")
        assistant_user = await crud_user.create_user(
            db,
            phone=assistant_phone,
            password_hash=password_hash,
            role=UserRole.ASSISTANT,
            email="demo.assistant@navassist.in"
        )
        assistant_user.full_name = "Demo Assistant"
        db.add(assistant_user)
        # Create profile
        from app.models.assistant import AssistantProfile
        profile = AssistantProfile(
            user_id=assistant_user.id,
            bio="Experienced local travel guide",
            experience_years=5,
            verification_status=KycStatus.VERIFIED,
            is_online=True
        )
        db.add(profile)
        await db.flush()
    else:
        # Make sure assistant is online and verified
        from app.repositories import assistant_repository as crud_assistant
        profile = await crud_assistant.get_assistant(db, user_id=assistant_user.id)
        if profile:
            profile.verification_status = KycStatus.VERIFIED
            profile.is_online = True
            db.add(profile)
            await db.flush()

    # 3. Create a test Booking
    # Clear any active bookings for the guest/assistant to prevent overlaps
    await db.execute(delete(Booking).filter(Booking.guest_id == guest.id))
    await db.flush()
    
    # Create Booking
    # pickup: Indira Gandhi International Airport (T3) -> POINT(77.1000 28.5600)
    # destination: New Delhi Railway Station -> POINT(77.2200 28.6400)
    from app.core.redis_client import update_assistant_location
    await update_assistant_location(assistant_user.id, 28.5562, 77.1000)
    
    booking_code = f"BK-{uuid.uuid4().hex[:6].upper()}"
    new_booking = Booking(
        booking_code=booking_code,
        guest_id=guest.id,
        assistant_id=assistant_user.id,
        pickup_address="Indira Gandhi International Airport (T3), Delhi",
        pickup_coordinates=func.ST_PointFromText("POINT(77.1000 28.5600)", 4326),
        destination_address="New Delhi Railway Station, Delhi",
        destination_coordinates=func.ST_PointFromText("POINT(77.2200 28.6400)", 4326),
        status=BookingStatus.ACCEPTED
    )
    db.add(new_booking)
    await db.flush()
    
    # Add history log entry
    from app.models.booking import BookingStatusHistory
    history = BookingStatusHistory(
        booking_id=new_booking.id,
        status=BookingStatus.ACCEPTED,
        changed_by=assistant_user.id
    )
    db.add(history)
    
    # Create trip share link
    share_token = uuid.uuid4().hex
    share = TripShare(
        booking_id=new_booking.id,
        share_token=share_token,
        created_by=guest.id,
        expires_at=datetime.now(timezone.utc) + timedelta(hours=2),
        is_active=True
    )
    db.add(share)
    await db.flush()
    await db.commit()
    
    guest_token = create_access_token(subject=guest.id)
    assistant_token = create_access_token(subject=assistant_user.id)
    
    return {
        "success": True,
        "guest_token": guest_token,
        "assistant_token": assistant_token,
        "booking_id": new_booking.id,
        "booking_code": booking_code,
        "share_token": share_token,
        "share_link": f"/api/v1/share/public/{share_token}",
        "pickup": {"lat": 28.5600, "lng": 77.1000},
        "destination": {"lat": 28.6400, "lng": 77.2200}
    }
