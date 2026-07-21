from typing import Generic, TypeVar, Optional
from pydantic import BaseModel, Field

T = TypeVar("T")


class PaginationParams(BaseModel):
    """Standard pagination query parameters."""
    page: int = Field(1, ge=1, description="Page number (1-indexed)")
    limit: int = Field(20, ge=1, le=100, description="Number of items per page")


class ApiResponse(BaseModel, Generic[T]):
    """Generic API response envelope to standardize all REST controller returns."""
    success: bool = Field(True, description="Indicates if the request was successfully processed")
    message: Optional[str] = Field(None, description="Optional informational message")
    data: Optional[T] = Field(None, description="Response payload data")
