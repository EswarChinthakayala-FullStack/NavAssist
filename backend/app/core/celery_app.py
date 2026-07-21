from celery import Celery
from app.core.config import settings

# Initialize Celery app instance with Redis as both broker and backend
celery_app = Celery(
    "navassist",
    broker=settings.REDIS_URL,
    backend=settings.REDIS_URL,
    include=["app.tasks.tasks"]
)

# Task serialization & configuration settings
celery_app.conf.update(
    task_serializer="json",
    result_serializer="json",
    accept_content=["json"],
    timezone="Asia/Kolkata",  # Alignment with Indian Market timezone
    enable_utc=True,
    
    # Periodic / Cron Schedules (Celery Beat)
    beat_schedule={
        "expire-pending-bookings-every-minute": {
            "task": "app.tasks.tasks.expire_pending_bookings_task",
            "schedule": 60.0,  # 1 minute frequency
        },
        "cleanup-location-history-daily": {
            "task": "app.tasks.tasks.cleanup_location_history_task",
            "schedule": 86400.0,  # 24 hours frequency
        }
    }
)
