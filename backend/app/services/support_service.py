import logging
from datetime import datetime, timezone
from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from fastapi import HTTPException, status

from app.models.support import SupportTicket, TicketMessage, TicketStatus, TicketPriority
from app.models.engagement import NotificationType
from app.core.exceptions import NotFoundError, ValidationError

logger = logging.getLogger(__name__)


class SupportService:
    @staticmethod
    async def create_ticket(
        db: AsyncSession,
        user_id: int,
        subject: str,
        description: str,
        booking_id: Optional[int] = None,
        category: str = "GENERAL",
        priority: TicketPriority = TicketPriority.MEDIUM
    ) -> SupportTicket:
        """Creates a customer support query ticket and initial message."""
        ticket = SupportTicket(
            user_id=user_id,
            booking_id=booking_id,
            subject=subject,
            category=category,
            priority=priority,
            status=TicketStatus.OPEN
        )
        db.add(ticket)
        await db.flush()

        initial_msg = TicketMessage(
            ticket_id=ticket.id,
            sender_id=user_id,
            message=description
        )
        db.add(initial_msg)
        await db.flush()

        try:
            from app.services.audit_service import AuditService
            from app.services.notification_service import NotificationService

            await AuditService.log_event(
                db=db,
                action="SUPPORT_TICKET_CREATED",
                entity_name="SupportTicket",
                entity_id=ticket.id,
                user_id=user_id,
                details={"subject": subject, "category": category}
            )

            await NotificationService.dispatch_user_notification(
                db=db,
                user_id=user_id,
                title="Support Ticket Received",
                body=f"Your ticket #{ticket.id} ('{subject}') has been submitted. A support representative will respond shortly.",
                type=NotificationType.SYSTEM,
                data={"ticket_id": ticket.id}
            )
        except Exception as e:
            logger.error(f"Support ticket audit error: {e}")

        logger.info(f"Created support ticket #{ticket.id} for user #{user_id}")
        return ticket

    @staticmethod
    async def add_message(
        db: AsyncSession,
        ticket_id: int,
        sender_id: int,
        message: str
    ) -> TicketMessage:
        """Appends a reply message to a support ticket thread."""
        result = await db.execute(select(SupportTicket).filter(SupportTicket.id == ticket_id))
        ticket = result.scalars().first()
        if not ticket:
            raise NotFoundError("Support ticket not found")

        msg = TicketMessage(
            ticket_id=ticket.id,
            sender_id=sender_id,
            message=message
        )
        db.add(msg)
        
        # If user replied, mark ticket IN_PROGRESS
        if sender_id == ticket.user_id and ticket.status == TicketStatus.RESOLVED:
            ticket.status = TicketStatus.IN_PROGRESS
            db.add(ticket)

        await db.flush()
        logger.info(f"Added message to ticket #{ticket_id} by user #{sender_id}")
        return msg

    @staticmethod
    async def resolve_ticket(
        db: AsyncSession,
        ticket_id: int,
        resolver_id: int,
        resolution_notes: Optional[str] = None
    ) -> SupportTicket:
        """Resolves an open customer support ticket."""
        result = await db.execute(select(SupportTicket).filter(SupportTicket.id == ticket_id))
        ticket = result.scalars().first()
        if not ticket:
            raise NotFoundError("Support ticket not found")

        ticket.status = TicketStatus.RESOLVED
        ticket.assigned_to = resolver_id
        db.add(ticket)
        await db.flush()

        try:
            from app.services.audit_service import AuditService
            from app.services.notification_service import NotificationService

            await AuditService.log_event(
                db=db,
                action="SUPPORT_TICKET_RESOLVED",
                entity_name="SupportTicket",
                entity_id=ticket.id,
                user_id=resolver_id,
                details={"notes": resolution_notes}
            )

            await NotificationService.dispatch_user_notification(
                db=db,
                user_id=ticket.user_id,
                title="Support Ticket Resolved",
                body=f"Your ticket #{ticket.id} ('{ticket.subject}') has been marked as resolved.",
                type=NotificationType.SYSTEM,
                data={"ticket_id": ticket.id}
            )
        except Exception as e:
            logger.error(f"Support resolution audit error: {e}")

        logger.info(f"Support ticket #{ticket_id} resolved by user #{resolver_id}")
        return ticket
