from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from pydantic import BaseModel, Field
from typing import Optional, List

from app.api import deps
from app.models.user import User
from app.models.engagement import RatingReview
from app.services.rating_service import RatingService

router = APIRouter()


class RatingSubmitRequest(BaseModel):
    booking_id: int = Field(..., description="Target journey booking ID")
    rating: int = Field(..., ge=1, le=5, description="Score between 1 and 5")
    review_text: Optional[str] = Field(None, description="Detailed review text")


@router.post("/", status_code=status.HTTP_201_CREATED)
async def submit_rating(
    request: RatingSubmitRequest,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Submits a journey rating and review score for a completed booking."""
    review = await RatingService.submit_rating(
        db,
        booking_id=request.booking_id,
        rated_by=current_user.id,
        rating=request.rating,
        review_text=request.review_text
    )
    await db.commit()
    return review


@router.get("/assistant/{assistant_id}")
async def get_assistant_ratings(
    assistant_id: int,
    db: AsyncSession = Depends(deps.get_db)
):
    """Retrieves all reviews and ratings submitted for a given assistant."""
    result = await db.execute(
        select(RatingReview)
        .filter(RatingReview.rated_assistant_id == assistant_id)
        .order_by(RatingReview.created_at.desc())
    )
    return result.scalars().all()


@router.get("/booking/{booking_id}")
async def get_booking_rating(
    booking_id: int,
    db: AsyncSession = Depends(deps.get_db)
):
    """Checks if a rating review exists for a specific booking."""
    result = await db.execute(
        select(RatingReview).filter(RatingReview.booking_id == booking_id)
    )
    review = result.scalars().first()
    if not review:
        return {"has_rated": False, "rating": None}
    return {
        "has_rated": True,
        "rating": {
            "id": review.id,
            "stars": review.rating,
            "comment": review.review_text,
            "created_at": review.created_at
        }
    }
