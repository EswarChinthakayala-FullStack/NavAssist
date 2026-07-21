import logging
from decimal import Decimal
from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select

from app.models.payment import Wallet, WalletTransaction, WalletTransactionType, WalletTransactionRefType
from app.core.exceptions import ValidationError

logger = logging.getLogger(__name__)


class WalletService:
    @staticmethod
    async def get_or_create_wallet(db: AsyncSession, user_id: int) -> Wallet:
        """Fetches or initializes the wallet account for a user."""
        result = await db.execute(select(Wallet).filter(Wallet.user_id == user_id))
        wallet = result.scalars().first()
        if not wallet:
            wallet = Wallet(user_id=user_id, balance=0.00, currency="INR")
            db.add(wallet)
            await db.flush()
        return wallet

    @classmethod
    async def credit_wallet(
        cls,
        db: AsyncSession,
        user_id: int,
        amount: Decimal,
        reference_type: WalletTransactionRefType,
        reference_id: Optional[int] = None
    ) -> Wallet:
        """
        Credits funds to the user's wallet. Uses with_for_update() row locks for strict atomicity.
        """
        if amount <= 0:
            raise ValidationError("Credit amount must be greater than zero")
            
        # Select with row lock (SELECT ... FOR UPDATE)
        result = await db.execute(
            select(Wallet).filter(Wallet.user_id == user_id).with_for_update()
        )
        wallet = result.scalars().first()
        if not wallet:
            wallet = Wallet(user_id=user_id, balance=0.00, currency="INR")
            db.add(wallet)
            await db.flush()
            
        old_balance = Decimal(str(wallet.balance))
        new_balance = old_balance + amount
        wallet.balance = float(new_balance)
        
        # Log transaction record
        tx = WalletTransaction(
            wallet_id=wallet.id,
            type=WalletTransactionType.CREDIT,
            amount=float(amount),
            reference_type=reference_type,
            reference_id=reference_id,
            balance_after=float(new_balance)
        )
        db.add_all([wallet, tx])
        await db.flush()
        
        logger.info(f"Credited Rs.{amount:.2f} to user {user_id} wallet. New balance: Rs.{new_balance:.2f}")
        return wallet

    @classmethod
    async def debit_wallet(
        cls,
        db: AsyncSession,
        user_id: int,
        amount: Decimal,
        reference_type: WalletTransactionRefType,
        reference_id: Optional[int] = None
    ) -> Wallet:
        """
        Debits funds from user wallet. Uses with_for_update() to prevent double-spending race conditions.
        """
        if amount <= 0:
            raise ValidationError("Debit amount must be greater than zero")
            
        result = await db.execute(
            select(Wallet).filter(Wallet.user_id == user_id).with_for_update()
        )
        wallet = result.scalars().first()
        if not wallet or Decimal(str(wallet.balance)) < amount:
            raise ValidationError("Insufficient wallet balance")
            
        old_balance = Decimal(str(wallet.balance))
        new_balance = old_balance - amount
        wallet.balance = float(new_balance)
        
        tx = WalletTransaction(
            wallet_id=wallet.id,
            type=WalletTransactionType.DEBIT,
            amount=float(amount),
            reference_type=reference_type,
            reference_id=reference_id,
            balance_after=float(new_balance)
        )
        db.add_all([wallet, tx])
        await db.flush()
        
        logger.info(f"Debited Rs.{amount:.2f} from user {user_id} wallet. New balance: Rs.{new_balance:.2f}")
        return wallet
