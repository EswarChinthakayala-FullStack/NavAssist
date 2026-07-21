import logging
from typing import Optional, Dict, Any, List
from decimal import Decimal
from datetime import datetime, date
from sqlalchemy.future import select
from sqlalchemy.ext.asyncio import AsyncSession
from app.models.engagement import Notification, NotificationType
from app.models.user import DeviceToken

logger = logging.getLogger(__name__)


def _sanitize_data(obj: Any) -> Any:
    """Recursively converts non-serializable objects (Decimal, datetime, etc.) to JSON primitives."""
    if isinstance(obj, Decimal):
        return float(obj)
    if isinstance(obj, (datetime, date)):
        return obj.isoformat()
    if isinstance(obj, dict):
        return {str(k): _sanitize_data(v) for k, v in obj.items()}
    if isinstance(obj, (list, tuple, set)):
        return [_sanitize_data(x) for x in obj]
    return obj


class NotificationService:
    @staticmethod
    async def dispatch_user_notification(
        db: AsyncSession, 
        user_id: int, 
        title: str, 
        body: str,
        type: NotificationType = NotificationType.SYSTEM,
        data: Optional[Dict[str, Any]] = None
    ) -> Notification:
        """Dispatches an in-app notification feed item and triggers push dispatches."""
        sanitized_data = _sanitize_data(data) if data else None
        
        async with db.begin_nested():
            notification = Notification(
                user_id=user_id,
                title=title,
                body=body,
                type=type,
                data=sanitized_data,
                is_read=False
            )
            db.add(notification)
            await db.flush()
        
        try:
            tokens_result = await db.execute(
                select(DeviceToken).filter(DeviceToken.user_id == user_id)
            )
            if hasattr(tokens_result, "scalars"):
                tokens = tokens_result.scalars().all()
                if tokens:
                    from app.tasks.tasks import send_push_notification_task
                    for t in tokens:
                        send_push_notification_task.delay(t.fcm_token, title, body, sanitized_data)
        except Exception as e:
            logger.error(f"Failed to enqueue push notifications for user {user_id}: {e}")
            
        return notification

    @staticmethod
    async def list_user_notifications(db: AsyncSession, user_id: int, limit: int = 50) -> List[Notification]:
        """Fetches notifications feed for a user ordered by newest first."""
        res = await db.execute(
            select(Notification)
            .filter(Notification.user_id == user_id)
            .order_by(Notification.created_at.desc())
            .limit(limit)
        )
        return list(res.scalars().all())

    @staticmethod
    async def mark_notification_as_read(db: AsyncSession, notification_id: int, user_id: int) -> bool:
        """Marks a notification item as read."""
        res = await db.execute(
            select(Notification).filter(
                Notification.id == notification_id,
                Notification.user_id == user_id
            )
        )
        notification = res.scalars().first()
        if notification:
            notification.is_read = True
            db.add(notification)
            await db.flush()
            return True
        return False
