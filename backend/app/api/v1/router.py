from fastapi import APIRouter
from app.api.v1.endpoints import (
    auth,
    users,
    assistants,
    kyc,
    locations,
    bookings,
    tracking,
    pricing,
    coupons,
    payments,
    wallet,
    ratings,
    sos,
    share,
    notifications,
    support,
    admin,
    messages,
    reports
)

api_router = APIRouter()

# Register endpoint routers
api_router.include_router(auth.router, prefix="/auth", tags=["Authentication"])
api_router.include_router(users.router, prefix="/users", tags=["Users"])
api_router.include_router(assistants.router, prefix="/assistants", tags=["Assistants"])
api_router.include_router(kyc.router, prefix="/kyc", tags=["KYC Verification"])
api_router.include_router(locations.router, prefix="/locations", tags=["Geo / Locations"])
api_router.include_router(bookings.router, prefix="/bookings", tags=["Bookings"])
api_router.include_router(tracking.router, prefix="/tracking", tags=["Tracking"])
api_router.include_router(pricing.router, prefix="/pricing", tags=["Pricing"])
api_router.include_router(coupons.router, prefix="/coupons", tags=["Coupons"])
api_router.include_router(payments.router, prefix="/payments", tags=["Payments"])
api_router.include_router(wallet.router, prefix="/wallet", tags=["Wallet"])
api_router.include_router(ratings.router, prefix="/ratings", tags=["Ratings & Reviews"])
api_router.include_router(sos.router, prefix="/sos", tags=["Safety / SOS"])
api_router.include_router(share.router, prefix="/share", tags=["Trip Sharing"])
api_router.include_router(notifications.router, prefix="/notifications", tags=["Notifications"])
api_router.include_router(support.router, prefix="/support", tags=["Support & FAQs"])
api_router.include_router(admin.router, prefix="/admin", tags=["Admin Portal"])
api_router.include_router(messages.router, tags=["Bookings / Messaging"])
api_router.include_router(reports.router, tags=["Bookings / Reporting"])

