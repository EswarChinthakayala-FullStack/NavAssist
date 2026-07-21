from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy.orm import selectinload
from pydantic import BaseModel, Field, ConfigDict
from typing import List, Optional
from datetime import datetime
import logging

logger = logging.getLogger(__name__)

from app.api import deps
from app.models.user import User
from app.models.support import SupportTicket, SupportTicketMessage, Faq, TicketStatus, TicketPriority

router = APIRouter()


class TicketCreateRequest(BaseModel):
    subject: str = Field(..., max_length=200, description="Brief summary of the issue")
    description: str = Field(..., max_length=2000, description="Detailed explanation of the request")
    priority: TicketPriority = Field(TicketPriority.MEDIUM)


class MessageCreateRequest(BaseModel):
    message: str = Field(..., max_length=2000)


# Response Schemas to isolate safe fields for serialization and avoid UnicodeDecodeError on raw bytes
class UserSupportResponse(BaseModel):
    id: int
    full_name: str
    email: Optional[str] = None
    role: str

    model_config = ConfigDict(from_attributes=True)


class TicketResponse(BaseModel):
    id: int
    user_id: int
    subject: str
    description: str
    status: TicketStatus
    priority: TicketPriority
    created_at: datetime
    updated_at: datetime
    user: Optional[UserSupportResponse] = None

    model_config = ConfigDict(from_attributes=True)


class MessageResponse(BaseModel):
    id: int
    ticket_id: int
    sender_id: int
    message: str
    created_at: datetime
    sender: Optional[UserSupportResponse] = None

    model_config = ConfigDict(from_attributes=True)


class TicketDetailsResponse(BaseModel):
    ticket: TicketResponse
    messages: List[MessageResponse]

    model_config = ConfigDict(from_attributes=True)


@router.get("/faqs")
async def get_faqs(
    category: Optional[str] = None,
    db: AsyncSession = Depends(deps.get_db)
):
    """Retrieves list of active FAQs, optionally filtered by category."""
    query = select(Faq).filter(Faq.is_active == True)
    if category:
        query = query.filter(Faq.category == category)
    query = query.order_by(Faq.display_order.asc())
    
    result = await db.execute(query)
    return result.scalars().all()


@router.post("/tickets", status_code=status.HTTP_201_CREATED, response_model=TicketResponse)
async def create_ticket(
    request: TicketCreateRequest,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Creates a new helpdesk support ticket."""
    ticket = SupportTicket(
        user_id=current_user.id,
        subject=request.subject,
        description=request.description,
        priority=request.priority,
        status=TicketStatus.OPEN
    )
    ticket.user = current_user
    db.add(ticket)
    await db.flush()
    await db.commit()
    return ticket


@router.get("/tickets", response_model=List[TicketResponse])
async def list_tickets(
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Lists helpdesk support tickets opened by the caller, or all tickets if admin."""
    query = select(SupportTicket)
    if current_user.role != "admin":
        query = query.filter(SupportTicket.user_id == current_user.id)
    else:
        query = query.options(selectinload(SupportTicket.user))
        
    result = await db.execute(query.order_by(SupportTicket.created_at.desc()))
    return result.scalars().all()


@router.get("/tickets/{id}", response_model=TicketDetailsResponse)
async def get_ticket_details(
    id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Fetches details and reply history threads of a specific ticket."""
    result = await db.execute(
        select(SupportTicket)
        .filter(SupportTicket.id == id)
        .options(selectinload(SupportTicket.user))
    )
    ticket = result.scalars().first()
    if not ticket:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Ticket not found"
        )
        
    if ticket.user_id != current_user.id and current_user.role != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Unauthorized to access this ticket"
        )
        
    msg_result = await db.execute(
        select(SupportTicketMessage)
        .filter(SupportTicketMessage.ticket_id == id)
        .options(selectinload(SupportTicketMessage.sender))
        .order_by(SupportTicketMessage.created_at.asc())
    )
    messages = msg_result.scalars().all()
    
    return {
        "ticket": ticket,
        "messages": messages
    }


# Legacy compatibility route
@router.get("/tickets/legacy/{ticket_id}", include_in_schema=False, response_model=TicketDetailsResponse)
async def get_ticket_details_legacy(ticket_id: int, current_user: User = Depends(deps.get_current_user), db: AsyncSession = Depends(deps.get_db)):
    return await get_ticket_details(ticket_id, current_user, db)


@router.post("/tickets/{id}/messages", status_code=status.HTTP_201_CREATED, response_model=MessageResponse)
async def reply_to_ticket(
    id: int,
    request: MessageCreateRequest,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Sends a chat message response in a ticket thread."""
    result = await db.execute(select(SupportTicket).filter(SupportTicket.id == id))
    ticket = result.scalars().first()
    if not ticket:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Ticket not found"
        )
        
    if ticket.user_id != current_user.id and current_user.role != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Unauthorized to reply to this ticket"
        )
        
    msg = SupportTicketMessage(
        ticket_id=id,
        sender_id=current_user.id,
        message=request.message
    )
    msg.sender = current_user
    if ticket.status in [TicketStatus.RESOLVED, TicketStatus.CLOSED]:
        ticket.status = TicketStatus.OPEN
        db.add(ticket)
        
    db.add(msg)
    await db.flush()

    # Dispatch support ticket message notification
    try:
        from app.services.notification_service import NotificationService
        from app.models.engagement import NotificationType
        
        if current_user.id == ticket.user_id:
            if ticket.assigned_to:
                await NotificationService.dispatch_user_notification(
                    db=db,
                    user_id=ticket.assigned_to,
                    title="New Support Message",
                    body=f"Ticket #{ticket.id} got a new reply from {current_user.full_name}.",
                    type=NotificationType.SYSTEM,
                    data={"ticket_id": ticket.id}
                )
        else:
            await NotificationService.dispatch_user_notification(
                db=db,
                user_id=ticket.user_id,
                title="Support Ticket Reply",
                body=f"A support agent replied to your ticket: \"{ticket.subject}\".",
                type=NotificationType.SYSTEM,
                data={"ticket_id": ticket.id}
            )
    except Exception as e:
        logger.error(f"Failed to dispatch ticket message notification: {e}")

    await db.commit()
    return msg


# Legacy compatibility route
@router.post("/tickets/legacy/{ticket_id}/messages", status_code=status.HTTP_201_CREATED, include_in_schema=False, response_model=MessageResponse)
async def reply_to_ticket_legacy(ticket_id: int, request: MessageCreateRequest, current_user: User = Depends(deps.get_current_user), db: AsyncSession = Depends(deps.get_db)):
    return await reply_to_ticket(ticket_id, request, current_user, db)


class TicketStatusUpdateRequest(BaseModel):
    status: TicketStatus


@router.patch("/tickets/{id}/status", response_model=TicketResponse)
async def update_ticket_status(
    id: int,
    request: TicketStatusUpdateRequest,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Updates a support ticket status. Only accessible by admins or the ticket creator."""
    result = await db.execute(
        select(SupportTicket)
        .filter(SupportTicket.id == id)
        .options(selectinload(SupportTicket.user))
    )
    ticket = result.scalars().first()
    if not ticket:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Ticket not found"
        )
        
    if ticket.user_id != current_user.id and current_user.role != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Unauthorized to update this ticket status"
        )
        
    ticket.status = request.status
    db.add(ticket)
    await db.flush()
    await db.commit()
    return ticket
