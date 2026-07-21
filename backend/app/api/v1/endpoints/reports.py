from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy.orm import selectinload
from typing import List
from datetime import datetime, timezone
import uuid
import logging

from app.api import deps
from app.models.user import User, UserRole
from app.models.booking import Booking
from app.models.booking_report import BookingReport, ReportCategory, ReportSeverity, ReportStatus
from app.models.support import SupportTicket, TicketStatus, TicketPriority
from app.schemas.booking_report import (
    BookingReportCreate, BookingReportResponse, ReportResolutionRequest
)

logger = logging.getLogger(__name__)

router = APIRouter()

@router.post("/bookings/{bookingId}/report", response_model=BookingReportResponse, status_code=status.HTTP_201_CREATED)
async def create_booking_report(
    bookingId: int,
    request: BookingReportCreate,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Open a dispute report against a booking. Validates ownership and details.
    """
    res_b = await db.execute(select(Booking).filter(Booking.id == bookingId))
    booking = res_b.scalars().first()
    if not booking:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Booking not found.")

    is_guest = booking.guest_id == current_user.id
    is_assistant = booking.assistant_id == current_user.id

    if not (is_guest or is_assistant):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied.")

    # Prevent duplicate reports by the same user for this booking
    res_dup = await db.execute(
        select(BookingReport).filter(
            BookingReport.booking_id == bookingId,
            BookingReport.reporter_id == current_user.id
        )
    )
    if res_dup.scalars().first():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="You have already submitted a report for this booking."
        )

    # Resolve reporter and target user
    reporter_id = current_user.id
    against_user_id = booking.assistant_id if is_guest else booking.guest_id

    # Generate unique report number
    report_num = f"REP-{datetime.now(timezone.utc).year}-{uuid.uuid4().hex[:6].upper()}"

    # Build evidence dictionary from request list
    evidence_dict = {"files": request.evidence or []}

    # Save booking report
    report = BookingReport(
        report_number=report_num,
        booking_id=bookingId,
        reporter_id=reporter_id,
        against_user_id=against_user_id,
        category=request.category,
        severity=request.severity,
        status=ReportStatus.SUBMITTED,
        description=request.description,
        evidence_json=evidence_dict
    )
    db.add(report)
    await db.flush()

    # Create associated administrative support ticket automatically to trigger alerts
    evidence_block = ""
    if request.evidence:
        evidence_block = "\n\nAttached Evidence Files:\n" + "\n".join([f"- {url}" for url in request.evidence])

    admin_ticket = SupportTicket(
        user_id=reporter_id,
        subject=f"[RIDE DISPUTE {report_num}] Booking #{bookingId}",
        description=f"Ride Report Category: {request.category.value}\nSeverity: {request.severity.value}\n\nDescription: {request.description}{evidence_block}",
        status=TicketStatus.OPEN,
        priority=TicketPriority.HIGH if request.severity in (ReportSeverity.HIGH, ReportSeverity.CRITICAL) else TicketPriority.MEDIUM
    )
    db.add(admin_ticket)

    await db.commit()
    await db.refresh(report)
    return report

@router.get("/bookings/{bookingId}/reports", response_model=List[BookingReportResponse])
async def get_booking_reports(
    bookingId: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Get user reports opened for a specific booking.
    """
    res_reports = await db.execute(
        select(BookingReport)
        .filter(
            BookingReport.booking_id == bookingId,
            BookingReport.reporter_id == current_user.id
        )
    )
    return res_reports.scalars().all()

@router.get("/admin/list", response_model=List[BookingReportResponse])
async def list_reports_admin(
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    List all ride dispute reports in the system. Admin privilege required.
    """
    res_reports = await db.execute(
        select(BookingReport).order_by(BookingReport.created_at.desc())
    )
    return res_reports.scalars().all()

@router.patch("/admin/{reportId}/resolve", response_model=BookingReportResponse)
async def resolve_report_admin(
    reportId: int,
    request: ReportResolutionRequest,
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Administratively update and resolve/close a ride dispute ticket.
    """
    res_rep = await db.execute(select(BookingReport).filter(BookingReport.id == reportId))
    report = res_rep.scalars().first()
    if not report:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Report not found.")

    report.status = request.status
    report.resolution_notes = request.notes
    report.resolution_time = datetime.now(timezone.utc)
    report.assigned_admin_id = current_user.id

    db.add(report)
    await db.commit()
    await db.refresh(report)
    return report
