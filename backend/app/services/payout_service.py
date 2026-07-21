import logging
from decimal import Decimal
from datetime import datetime, timezone
from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from fastapi import HTTPException, status

from app.models.assistant import PayoutAccount, Payout, PayoutStatus, AssistantProfile
from app.models.payment import WalletTransactionRefType
from app.services.wallet_service import WalletService
from app.services.notification_service import NotificationService
from app.services.audit_service import AuditService
from app.models.engagement import NotificationType

logger = logging.getLogger(__name__)

class PayoutService:
    @staticmethod
    async def create_or_update_payout_account(
        db: AsyncSession,
        user_id: int,
        account_holder_name: str,
        account_number: str,
        ifsc_code: Optional[str] = None,
        upi_id: Optional[str] = None
    ) -> PayoutAccount:
        """
        Creates or updates an assistant's payout account (bank details / UPI).
        """
        res = await db.execute(select(AssistantProfile).filter(AssistantProfile.user_id == user_id))
        profile = res.scalars().first()
        if not profile:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Assistant profile not found"
            )

        account_res = await db.execute(select(PayoutAccount).filter(PayoutAccount.assistant_id == profile.id))
        account = account_res.scalars().first()

        account_number_bytes = account_number.encode('utf-8')

        if not account:
            account = PayoutAccount(
                assistant_id=profile.id,
                account_holder_name=account_holder_name,
                account_number_enc=account_number_bytes,
                ifsc_code=ifsc_code,
                upi_id=upi_id,
                is_verified=True
            )
            db.add(account)
        else:
            account.account_holder_name = account_holder_name
            account.account_number_enc = account_number_bytes
            account.ifsc_code = ifsc_code
            account.upi_id = upi_id
            account.is_verified = True
            db.add(account)

        await db.flush()
        await AuditService.log_event(
            db=db,
            action="PAYOUT_ACCOUNT_UPDATED",
            entity_name="PayoutAccount",
            entity_id=account.id,
            user_id=user_id,
            details={"account_holder": account_holder_name, "has_upi": bool(upi_id)}
        )
        return account

    @staticmethod
    async def get_payout_account(db: AsyncSession, user_id: int) -> Optional[PayoutAccount]:
        """Fetches registered payout account for an assistant user."""
        res = await db.execute(select(AssistantProfile).filter(AssistantProfile.user_id == user_id))
        profile = res.scalars().first()
        if not profile:
            return None
        account_res = await db.execute(select(PayoutAccount).filter(PayoutAccount.assistant_id == profile.id))
        return account_res.scalars().first()

    @staticmethod
    async def request_payout(
        db: AsyncSession,
        user_id: int,
        amount: Decimal
    ) -> Payout:
        """
        Initiates a payout request for an assistant. Checks wallet balance and records Payout entry.
        """
        if amount <= 0:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Payout amount must be greater than zero"
            )

        res = await db.execute(select(AssistantProfile).filter(AssistantProfile.user_id == user_id))
        profile = res.scalars().first()
        if not profile:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Assistant profile not found"
            )

        # Check payout account registration
        account = await PayoutService.get_payout_account(db, user_id)
        if not account or not account.is_verified:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Verified bank or UPI payout account required before requesting payouts"
            )

        # Verify wallet balance
        wallet = await WalletService.get_or_create_wallet(db, user_id)
        if Decimal(str(wallet.balance)) < amount:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Insufficient wallet balance (Current: ₹{wallet.balance:.2f})"
            )

        # Create Payout record in pending status
        payout = Payout(
            assistant_id=profile.id,
            amount=float(amount),
            status=PayoutStatus.PENDING,
            initiated_at=datetime.now(timezone.utc)
        )
        db.add(payout)
        await db.flush()

        await AuditService.log_event(
            db=db,
            action="PAYOUT_REQUESTED",
            entity_name="Payout",
            entity_id=payout.id,
            user_id=user_id,
            details={"amount": float(amount)}
        )
        return payout

    @staticmethod
    async def process_payout(
        db: AsyncSession,
        payout_id: int,
        admin_user_id: int,
        reference_id: str,
        approve: bool = True
    ) -> Payout:
        """
        Admin action to approve/process or reject a payout request.
        Debits wallet on approval and sets completed_at & status.
        """
        res = await db.execute(select(Payout).filter(Payout.id == payout_id))
        payout = res.scalars().first()
        if not payout:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Payout request record not found"
            )

        if payout.status != PayoutStatus.PENDING:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Payout already processed (Current status: {payout.status.value})"
            )

        prof_res = await db.execute(select(AssistantProfile).filter(AssistantProfile.id == payout.assistant_id))
        profile = prof_res.scalars().first()
        if not profile:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Associated assistant profile not found"
            )

        if approve:
            # Debit assistant wallet
            await WalletService.debit_wallet(
                db=db,
                user_id=profile.user_id,
                amount=Decimal(str(payout.amount)),
                reference_type=WalletTransactionRefType.PAYOUT,
                reference_id=payout.id
            )

            payout.status = PayoutStatus.COMPLETED
            payout.reference_id = reference_id
            payout.completed_at = datetime.now(timezone.utc)

            await NotificationService.dispatch_user_notification(
                db=db,
                user_id=profile.user_id,
                title="Payout Processed",
                body=f"Your payout of ₹{payout.amount:.2f} has been processed successfully (Ref: {reference_id}).",
                type=NotificationType.PAYMENT,
                data={"payout_id": payout.id, "amount": payout.amount}
            )
        else:
            payout.status = PayoutStatus.FAILED
            await NotificationService.dispatch_user_notification(
                db=db,
                user_id=profile.user_id,
                title="Payout Failed",
                body=f"Your payout request of ₹{payout.amount:.2f} could not be processed.",
                type=NotificationType.PAYMENT,
                data={"payout_id": payout.id}
            )

        db.add(payout)
        await db.flush()

        await AuditService.log_event(
            db=db,
            action="PAYOUT_PROCESSED" if approve else "PAYOUT_REJECTED",
            entity_name="Payout",
            entity_id=payout.id,
            user_id=admin_user_id,
            details={"assistant_id": profile.user_id, "amount": payout.amount, "reference_id": reference_id}
        )
        return payout

    @staticmethod
    async def list_payouts_for_assistant(db: AsyncSession, user_id: int) -> List[Payout]:
        """Lists history of payouts for an assistant."""
        prof_res = await db.execute(select(AssistantProfile).filter(AssistantProfile.user_id == user_id))
        profile = prof_res.scalars().first()
        if not profile:
            return []

        res = await db.execute(
            select(Payout).filter(Payout.assistant_id == profile.id).order_by(Payout.initiated_at.desc())
        )
        return list(res.scalars().all())
