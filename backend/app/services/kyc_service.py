import logging
from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from fastapi import HTTPException, status, UploadFile

from app.repositories.assistant_repository import assistant_repository
from app.models.assistant import AssistantProfile, KycStatus
from app.integrations import s3

logger = logging.getLogger(__name__)


class KycService:
    @staticmethod
    async def submit_kyc(
        db: AsyncSession,
        assistant_id: int,
        aadhaar_number: str,
        doc_front: UploadFile,
        doc_back: UploadFile
    ) -> AssistantProfile:
        """
        Processes multipart uploads, stores files in S3, and persists masked KYC files in the DB.
        """
        try:
            # Read files into memory bytes
            front_bytes = await doc_front.read()
            back_bytes = await doc_back.read()
            
            # S3 storage dispatches
            doc_front_key = await s3.upload_file(front_bytes, doc_front.filename, folder="kyc_front")
            doc_back_key = await s3.upload_file(back_bytes, doc_back.filename, folder="kyc_back")
            
            # Database creation mapping
            kyc_record = await assistant_repository.create_kyc(
                db, 
                assistant_id=assistant_id, 
                aadhaar_number=aadhaar_number, 
                doc_front_url=doc_front_key, 
                doc_back_url=doc_back_key
            )
            return kyc_record
        except Exception as e:
            logger.error(f"Error submitting assistant KYC records: {e}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"KYC document processing failed: {str(e)}"
            )

    @staticmethod
    async def review_kyc(
        db: AsyncSession,
        kyc_id: int,
        reviewer_id: int,
        review_status: KycStatus,
        review_notes: Optional[str] = None
    ) -> AssistantProfile:
        """
        Updates the verification status of assistant KYC files, generating presigned S3 document viewer URLs.
        """
        kyc = await assistant_repository.review_kyc(
            db,
            kyc_id=kyc_id,
            reviewer_id=reviewer_id,
            status=review_status,
            review_notes=review_notes
        )
        if not kyc:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="KYC application record not found"
            )
            
        # Map presigned viewable URLs for admin review validation
        kyc.doc_front_url = s3.generate_presigned_url(kyc.doc_front_url)
        kyc.doc_back_url = s3.generate_presigned_url(kyc.doc_back_url)

        try:
            from app.services.audit_service import AuditService
            from app.services.notification_service import NotificationService
            from app.models.engagement import NotificationType

            await AuditService.log_event(
                db=db,
                action=f"KYC_{review_status.value.upper()}",
                entity_name="AssistantProfile",
                entity_id=kyc.assistant_id,
                user_id=reviewer_id,
                details={"status": review_status.value, "notes": review_notes}
            )

            status_text = "Approved" if review_status in [KycStatus.APPROVED, KycStatus.VERIFIED] else "Rejected"
            await NotificationService.dispatch_user_notification(
                db=db,
                user_id=kyc.assistant_id,
                title=f"KYC Verification {status_text}",
                body=f"Your escort assistant KYC application has been {status_text.lower()}. Notes: {review_notes or 'None'}",
                type=NotificationType.SYSTEM,
                data={"status": review_status.value}
            )
        except Exception as e:
            logger.error(f"Audit/Notification dispatch failed for KYC review: {e}")

        return kyc
