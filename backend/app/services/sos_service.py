import logging
from datetime import datetime, timezone
from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import func

from app.models.safety import SosAlert, SosStatus
from app.repositories import booking_repository as crud_booking
from app.tasks.tasks import send_sos_notifications_task
from app.core.exceptions import ValidationError, NotFoundError

logger = logging.getLogger(__name__)


class SosService:
    @staticmethod
    async def trigger_sos(
        db: AsyncSession,
        booking_id: int,
        user_id: int,
        latitude: float,
        longitude: float
    ) -> SosAlert:
        """
        Registers a high-priority safety alert in the database,
        coordinates geographical coordinates, and dispatches background contact alerts.
        """
        booking = await crud_booking.get_booking(db, booking_id=booking_id)
        if not booking:
            raise ValidationError("Invalid booking ID associated with SOS alert")
            
        # Initialize spatial location geometry
        alert = SosAlert(
            booking_id=booking_id,
            user_id=user_id,
            status=SosStatus.ACTIVE,
            coordinates=func.ST_PointFromText(f"POINT({longitude} {latitude})", 4326)
        )
        
        db.add(alert)
        await db.flush()
        
        # Enqueue SMS and push notifications fan-out tasks to workers
        send_sos_notifications_task.delay(
            user_id=user_id,
            latitude=latitude,
            longitude=longitude,
            booking_id=booking_id
        )
        
        logger.warning(f"CRITICAL: SOS Alert {alert.id} generated for booking {booking_id} by user {user_id}")

        try:
            from app.services.audit_service import AuditService
            from app.services.notification_service import NotificationService
            from app.models.engagement import NotificationType

            await AuditService.log_event(
                db=db,
                action="SOS_ALERT_TRIGGERED",
                entity_name="SosAlert",
                entity_id=alert.id,
                user_id=user_id,
                details={"booking_id": booking_id, "lat": latitude, "lon": longitude}
            )

            await NotificationService.dispatch_user_notification(
                db=db,
                user_id=user_id,
                title="EMERGENCY SOS ACTIVE",
                body=f"SOS alert has been triggered for Booking #BK-{booking_id}. Dispatchers have been alerted.",
                type=NotificationType.SAFETY,
                data={"booking_id": booking_id, "sos_id": alert.id}
            )
        except Exception as e:
            logger.error(f"SOS audit/notification dispatch error: {e}")

        return alert

    @staticmethod
    async def resolve_sos(
        db: AsyncSession,
        alert_id: int,
        resolver_id: int,
        resolution_status: SosStatus = SosStatus.RESOLVED
    ) -> SosAlert:
        """
        Marks an active emergency trigger resolved by an administrator.
        """
        result = await db.execute(select(SosAlert).filter(SosAlert.id == alert_id))
        alert = result.scalars().first()
        if not alert:
            raise NotFoundError("SOS alert record not found")
            
        alert.status = resolution_status
        alert.resolved_by = resolver_id
        alert.resolved_at = datetime.now(timezone.utc)
        
        db.add(alert)
        await db.flush()
        
        try:
            from app.services.audit_service import AuditService
            await AuditService.log_event(
                db=db,
                action="SOS_ALERT_RESOLVED",
                entity_name="SosAlert",
                entity_id=alert_id,
                user_id=resolver_id,
                details={"status": resolution_status.value}
            )
        except Exception as e:
            logger.error(f"SOS resolution audit error: {e}")

        logger.info(f"SOS Alert {alert_id} resolved by user {resolver_id}")
        return alert
