# Unified enums and system constants configuration

from app.models.user import UserRole, UserStatus, AuthProvider, DeviceType, OnlineStatus
from app.models.otp import OtpPurpose
from app.models.assistant import KycStatus, PayoutStatus
from app.models.location import LocationLabel, ServicePointType
from app.models.booking import BookingStatus, CancelledBy
from app.models.promo import CouponDiscountType
from app.models.payment import (
    PaymentMethod, 
    PaymentStatus, 
    WalletTransactionType, 
    WalletTransactionRefType
)
from app.models.safety import SosStatus
from app.models.engagement import NotificationType
from app.models.support import TicketStatus, TicketPriority
