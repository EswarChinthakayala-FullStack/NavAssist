from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File, Form
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from pydantic import BaseModel, Field

from app.api import deps
from app.models import User, UserRole, KycStatus, AssistantProfile
from app.repositories import assistant_repository as crud_assistant
from app.schemas.kyc import KycResponse
from app.services.kyc_service import KycService

router = APIRouter()


class RejectKycRequest(BaseModel):
    reason: str = Field(..., min_length=3, description="Audit reason for KYC rejection")


@router.post("/documents", response_model=KycResponse, status_code=status.HTTP_201_CREATED)
async def upload_documents(
    aadhaar_number: str = Form(..., min_length=12, max_length=12, pattern="^[0-9]{12}$", description="12-digit Aadhaar Number"),
    doc_front: UploadFile = File(..., description="Front side photograph of Aadhaar card"),
    doc_back: UploadFile = File(..., description="Back side photograph of Aadhaar card"),
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Uploads Aadhaar card and police verification photos securely to S3.
    """
    if current_user.role != UserRole.ASSISTANT:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only assistants can submit KYC documents"
        )
        
    kyc_record = await KycService.submit_kyc(
        db, 
        assistant_id=current_user.id, 
        aadhaar_number=aadhaar_number, 
        doc_front=doc_front, 
        doc_back=doc_back
    )
    await db.commit()
    return kyc_record


@router.get("/status", response_model=KycResponse)
async def get_kyc_status(
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Fetches the KYC status of the logged-in assistant user."""
    if current_user.role != UserRole.ASSISTANT:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only assistant users have KYC profiles"
        )
        
    assistant = await crud_assistant.get_assistant(db, user_id=current_user.id)
    if not assistant:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="KYC details profile not found"
        )
    return {
        "verification_status": assistant.verification_status,
        "message": f"KYC status is {assistant.verification_status.value}"
    }


@router.patch("/admin/{assistant_id}/approve", response_model=KycResponse)
async def approve_kyc(
    assistant_id: int,
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Admin-only endpoint to approve assistant KYC verification applications.
    """
    profile = await crud_assistant.get_assistant(db, user_id=assistant_id)
    if not profile:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Assistant profile not found"
        )
        
    kyc = await KycService.review_kyc(
        db,
        kyc_id=profile.id,
        reviewer_id=current_user.id,
        review_status=KycStatus.APPROVED,
        review_notes="Approved by Administrator via CLI / Admin Panel"
    )
    await db.commit()
    return kyc


@router.patch("/admin/{assistant_id}/reject", response_model=KycResponse)
async def reject_kyc(
    assistant_id: int,
    request: RejectKycRequest,
    current_user: User = Depends(deps.require_admin),
    db: AsyncSession = Depends(deps.get_db)
):
    """
    Admin-only endpoint to reject assistant KYC verification applications with reasons.
    """
    profile = await crud_assistant.get_assistant(db, user_id=assistant_id)
    if not profile:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Assistant profile not found"
        )
        
    kyc = await KycService.review_kyc(
        db,
        kyc_id=profile.id,
        reviewer_id=current_user.id,
        review_status=KycStatus.REJECTED,
        review_notes=request.reason
    )
    await db.commit()
    return kyc
