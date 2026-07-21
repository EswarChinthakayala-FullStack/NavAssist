import logging
from typing import Dict, Any, Callable, List, Coroutine
from sqlalchemy.ext.asyncio import AsyncSession

logger = logging.getLogger(__name__)

# Type definition for event handler functions
EventHandler = Callable[[AsyncSession, Dict[str, Any]], Coroutine[Any, Any, None]]

class DomainEventBus:
    """
    Internal Event Bus for handling domain events asynchronously or synchronously
    to synchronize related entities across multiple tables cleanly.
    """
    _handlers: Dict[str, List[EventHandler]] = {}

    @classmethod
    def register(cls, event_name: str, handler: EventHandler):
        """Registers a handler for a given domain event."""
        if event_name not in cls._handlers:
            cls._handlers[event_name] = []
        cls._handlers[event_name].append(handler)
        logger.info(f"Registered domain event handler for '{event_name}'")

    @classmethod
    async def publish(cls, db: AsyncSession, event_name: str, payload: Dict[str, Any]):
        """Publishes an event to all registered listeners."""
        handlers = cls._handlers.get(event_name, [])
        logger.info(f"Publishing domain event '{event_name}' to {len(handlers)} handler(s)")
        for handler in handlers:
            try:
                await handler(db, payload)
            except Exception as e:
                logger.error(f"Error handling event '{event_name}': {e}", exc_info=True)


# Default Domain Event Handlers Implementation

async def handle_user_registered(db: AsyncSession, payload: Dict[str, Any]):
    """
    UserRegistered Event:
    Automatically creates default user wallet, default app settings, and records audit log.
    """
    user_id = payload.get("user_id")
    if not user_id:
        return

    from app.services.wallet_service import WalletService
    from app.models.user import AppSetting
    from app.services.audit_service import AuditService
    from sqlalchemy.future import select

    # 1. Initialize Wallet
    await WalletService.get_or_create_wallet(db, user_id=user_id)

    # 2. Initialize App Settings if missing
    res = await db.execute(select(AppSetting).filter(AppSetting.user_id == user_id))
    setting = res.scalars().first()
    if not setting:
        setting = AppSetting(
            user_id=user_id,
            language="en",
            dark_mode=False,
            notifications_enabled=True
        )
        db.add(setting)

    # 3. Log Audit Event
    await AuditService.log_event(
        db=db,
        action="USER_REGISTERED",
        entity_name="User",
        entity_id=user_id,
        user_id=user_id,
        details={"auth_provider": payload.get("auth_provider", "local")}
    )
    await db.flush()


async def handle_booking_created(db: AsyncSession, payload: Dict[str, Any]):
    """
    BookingCreated Event:
    Records coupon redemptions if coupon used, logs audit event, and dispatches initial notification.
    """
    booking_id = payload.get("booking_id")
    guest_id = payload.get("guest_id")
    coupon_id = payload.get("coupon_id")

    if not booking_id:
        return

    from app.models.pricing import CouponRedemption
    from app.services.audit_service import AuditService
    from app.services.notification_service import NotificationService
    from app.models.engagement import NotificationType
    from sqlalchemy.future import select

    # 1. Populate Coupon Redemptions table if a coupon was used
    if coupon_id and guest_id:
        res = await db.execute(select(CouponRedemption).filter(CouponRedemption.booking_id == booking_id))
        redemption = res.scalars().first()
        if not redemption:
            redemption = CouponRedemption(
                coupon_id=coupon_id,
                user_id=guest_id,
                booking_id=booking_id
            )
            db.add(redemption)

    # 2. Audit Log
    await AuditService.log_event(
        db=db,
        action="BOOKING_CREATED",
        entity_name="Booking",
        entity_id=booking_id,
        user_id=guest_id,
        details={"fare_estimate": payload.get("fare_estimate")}
    )

    # 3. Dispatch Notification to Guest
    if guest_id:
        await NotificationService.dispatch_user_notification(
            db=db,
            user_id=guest_id,
            title="Booking Confirmed",
            body=f"Your booking request #BK-{booking_id} has been created and is searching for nearby assistants.",
            type=NotificationType.BOOKING,
            data={"booking_id": booking_id}
        )
    await db.flush()


async def handle_payment_captured(db: AsyncSession, payload: Dict[str, Any]):
    """
    PaymentCaptured Event:
    Credits 80% guide earnings to assistant wallet, creates ledger transaction, logs audit trail, and dispatches push notification.
    """
    payment_id = payload.get("payment_id")
    booking_id = payload.get("booking_id")
    assistant_id = payload.get("assistant_id")
    amount = payload.get("amount", 0.0)

    if not assistant_id or amount <= 0:
        return

    from decimal import Decimal
    from app.services.wallet_service import WalletService
    from app.models.payment import WalletTransactionRefType
    from app.services.notification_service import NotificationService
    from app.models.engagement import NotificationType
    from app.services.audit_service import AuditService

    earnings = Decimal(str(amount)) * Decimal("0.80")

    # Credit Assistant Wallet
    await WalletService.credit_wallet(
        db=db,
        user_id=assistant_id,
        amount=earnings,
        reference_type=WalletTransactionRefType.BOOKING_PAYOUT,
        reference_id=booking_id
    )

    # Dispatch notification to Assistant
    await NotificationService.dispatch_user_notification(
        db=db,
        user_id=assistant_id,
        title="Earnings Credited",
        body=f"₹{float(earnings):.2f} guide earnings credited for Booking #BK-{booking_id}.",
        type=NotificationType.PAYMENT,
        data={"booking_id": booking_id, "amount": float(earnings)}
    )

    # Audit log
    await AuditService.log_event(
        db=db,
        action="PAYMENT_CAPTURED",
        entity_name="Payment",
        entity_id=payment_id,
        user_id=assistant_id,
        details={"booking_id": booking_id, "amount": amount, "assistant_earnings": float(earnings)}
    )
    await db.flush()


# Register Default Domain Event Handlers
DomainEventBus.register("UserRegistered", handle_user_registered)
DomainEventBus.register("BookingCreated", handle_booking_created)
DomainEventBus.register("PaymentCaptured", handle_payment_captured)
