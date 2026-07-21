import logging
from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import func

from app.models.engagement import RatingReview
from app.models.booking import Booking, BookingStatus
from app.models.assistant import AssistantProfile
from app.core.exceptions import ValidationError, NotFoundError

logger = logging.getLogger(__name__)


class RatingService:
    @staticmethod
    async def submit_rating(
        db: AsyncSession,
        booking_id: int,
        rated_by: int,
        rating: int,
        review_text: Optional[str] = None
    ) -> RatingReview:
        """
        Submits a rating review for a completed journey booking.
        Calculates and updates assistant's average rating metrics.
        """
        if rating < 1 or rating > 5:
            raise ValidationError("Rating must be an integer between 1 and 5")
            
        # 1. Fetch Booking and verify eligibility
        booking_result = await db.execute(select(Booking).filter(Booking.id == booking_id))
        booking = booking_result.scalars().first()
        if not booking:
            raise NotFoundError("Booking request not found")
            
        if booking.status != BookingStatus.COMPLETED:
            raise ValidationError("Ratings can only be submitted for completed journeys")
            
        if booking.guest_id != rated_by:
            raise ValidationError("Only the guest who made the booking can submit a rating")
            
        if not booking.assistant_id:
            raise ValidationError("No assistant was assigned to this booking")
            
        # 2. Check duplicate submission
        existing_result = await db.execute(
            select(RatingReview).filter(RatingReview.booking_id == booking_id)
        )
        if existing_result.scalars().first():
            raise ValidationError("You have already submitted a rating for this journey")
            
        # 3. Create RatingReview record
        review = RatingReview(
            booking_id=booking_id,
            rated_by=rated_by,
            rated_assistant_id=booking.assistant_id,
            rating=rating,
            review_text=review_text
        )
        db.add(review)
        await db.flush()
        
        # 4. Recompute assistant avg_rating and count
        stats_result = await db.execute(
            select(
                func.coalesce(func.avg(RatingReview.rating), 0.0),
                func.count(RatingReview.id)
            ).filter(RatingReview.rated_assistant_id == booking.assistant_id)
        )
        avg_rating, total_reviews = stats_result.first() or (0.0, 0)
        
        # Fetch assistant profile by user_id or id
        profile_result = await db.execute(
            select(AssistantProfile).filter(AssistantProfile.user_id == booking.assistant_id)
        )
        profile = profile_result.scalars().first()
        if not profile:
            profile_result = await db.execute(
                select(AssistantProfile).filter(AssistantProfile.id == booking.assistant_id)
            )
            profile = profile_result.scalars().first()

        if profile:
            profile.avg_rating = round(float(avg_rating), 1)
            db.add(profile)
            
        await db.flush()

        try:
            from app.services.audit_service import AuditService
            from app.services.notification_service import NotificationService
            from app.models.engagement import NotificationType

            await AuditService.log_event(
                db=db,
                action="RATING_SUBMITTED",
                entity_name="RatingReview",
                entity_id=review.id,
                user_id=rated_by,
                details={"booking_id": booking_id, "rating": rating, "assistant_id": booking.assistant_id}
            )

            reviewer_name = "Guest"
            if booking.guest and booking.guest.full_name:
                reviewer_name = booking.guest.full_name
            elif rated_by:
                from app.models.user import User
                guest_res = await db.execute(select(User).filter(User.id == rated_by))
                guest_user = guest_res.scalars().first()
                if guest_user and guest_user.full_name:
                    reviewer_name = guest_user.full_name

            comment_snippet = f' "{review_text}"' if review_text else ""

            await NotificationService.dispatch_user_notification(
                db=db,
                user_id=booking.assistant_id,
                title=f"New {rating}★ Rating from {reviewer_name}",
                body=f"{reviewer_name} rated your escort guidance {rating} stars!{comment_snippet} (Updated average rating: {float(avg_rating):.1f})",
                type=NotificationType.SYSTEM,
                data={
                    "booking_id": booking_id,
                    "rating": rating,
                    "reviewer_name": reviewer_name,
                    "review_text": review_text,
                    "route": f"/trip/{booking_id}/tracking"
                }
            )
        except Exception as e:
            logger.error(f"Rating audit/notification dispatch error: {e}")

        logger.info(f"Submitted rating {rating} for assistant {booking.assistant_id}. New average: {avg_rating}")
        return review
