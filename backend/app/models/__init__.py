# Expose all models under the app.models namespace

from app.models.base import Base, TimestampMixin
from app.models.user import (
    User, DeviceToken, RefreshToken, AppSetting, OtpVerification,
    UserRole, UserStatus, OnlineStatus, AuthProvider, DeviceType, OtpPurpose
)
from app.models.assistant import (
    AssistantProfile, AssistantDocument, PayoutAccount, Payout, 
    KycStatus, PayoutStatus
)
from app.models.location import (
    SavedLocation, ServicePoint, LocationLabel, ServicePointType
)
from app.models.booking import (
    Booking, BookingStatusHistory, LiveLocation, BookingStatus, CancelledBy
)
from app.models.invoice import Invoice, InvoiceStatus
from app.models.pricing import (
    FareRule, Coupon, CouponRedemption, CouponDiscountType
)
from app.models.payment import (
    Payment, Wallet, WalletTransaction, PaymentMethod, PaymentStatus, 
    WalletTransactionType, WalletTransactionRefType
)
from app.models.safety import (
    SosAlert, EmergencyContact, TripShare, SosStatus
)
from app.models.engagement import (
    RatingReview, Notification, NotificationType
)
from app.models.support import (
    SupportTicket, SupportTicketMessage, Faq, AuditLog, TicketStatus, TicketPriority
)
from app.models.booking_message import BookingMessage, MessageType
from app.models.booking_report import BookingReport, ReportCategory, ReportSeverity, ReportStatus

