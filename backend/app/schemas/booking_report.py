from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, Field, ConfigDict
from app.models.booking_report import ReportCategory, ReportSeverity, ReportStatus

class BookingReportCreate(BaseModel):
    category: ReportCategory
    severity: ReportSeverity = Field(ReportSeverity.MEDIUM)
    description: str = Field(..., min_length=20, max_length=1000)
    evidence: Optional[List[str]] = Field(None, description="Optional uploaded file URLs/filenames")
    against_user_id: Optional[int] = Field(None)
    gps_coordinates: Optional[str] = Field(None)

class BookingReportResponse(BaseModel):
    id: int
    report_number: str
    booking_id: int
    reporter_id: int
    against_user_id: Optional[int] = None
    category: ReportCategory
    severity: ReportSeverity
    status: ReportStatus
    description: str
    evidence_json: Optional[dict] = None
    assigned_admin_id: Optional[int] = None
    resolution_notes: Optional[str] = None
    resolution_time: Optional[datetime] = None
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True)

class ReportResolutionRequest(BaseModel):
    status: ReportStatus
    notes: str = Field(..., min_length=5, max_length=1000)
