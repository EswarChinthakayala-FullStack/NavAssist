from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import List

from app.api import deps
from app.models.user import User
from app.models.payment import WalletTransaction, WalletTransactionRefType
from app.schemas.payment import WalletTopupRequest
from app.services.wallet_service import WalletService

router = APIRouter()


@router.get("/balance")
async def get_wallet_balance(
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Fetches the caller's active wallet balance."""
    wallet = await WalletService.get_or_create_wallet(db, user_id=current_user.id)
    return {
        "balance": wallet.balance,
        "currency": wallet.currency
    }


@router.post("/topup")
async def topup_wallet(
    request: WalletTopupRequest,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Credits top-up funds to the user's wallet."""
    wallet = await WalletService.credit_wallet(
        db,
        user_id=current_user.id,
        amount=request.amount,
        reference_type=WalletTransactionRefType.TOPUP
    )
    await db.commit()
    return {
        "success": True,
        "new_balance": wallet.balance,
        "currency": wallet.currency
    }


@router.get("/transactions")
async def list_transactions(
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Lists history ledger transactions for the caller's wallet."""
    wallet = await WalletService.get_or_create_wallet(db, user_id=current_user.id)
    result = await db.execute(
        select(WalletTransaction)
        .filter(WalletTransaction.wallet_id == wallet.id)
        .order_by(WalletTransaction.created_at.desc())
    )
    return result.scalars().all()
