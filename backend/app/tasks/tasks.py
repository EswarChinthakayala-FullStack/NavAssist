# Backward compatibility imports / routing of celery tasks

from app.tasks.notification_tasks import (
    send_otp_sms as send_otp_sms_task,
    send_push_notification as send_push_notification_task,
    send_sos_notifications as send_sos_notifications_task
)

from app.tasks.matching_tasks import (
    auto_expire_unassigned_bookings as expire_pending_bookings_task
)

from app.tasks.cleanup_tasks import (
    cleanup_expired_otps as cleanup_location_history_task,
    sync_live_location_snapshot as log_live_location_task
)

from app.tasks.scoring_tasks import (
    recompute_assistant_trust_scores
)

from app.tasks.payment_tasks import (
    process_payment_webhook,
    process_assistant_payouts
)
