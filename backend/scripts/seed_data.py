import asyncio
import logging
from datetime import datetime, timezone
import uuid
from sqlalchemy import func
from sqlalchemy.future import select

from app.core.database import SessionLocal
from app.core.security import get_password_hash
from app.models.user import User, UserRole
from app.models.location import ServicePoint, ServicePointType
from app.models.pricing import FareRule

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


async def seed_master_data():
    """Seeds initial master service points, fare rules configuration, and admin profile."""
    async with SessionLocal() as db:
        # 1. Seed demo admin user
        admin_email = "admin@navassist.in"
        result = await db.execute(select(User).filter(User.email == admin_email))
        admin = result.scalars().first()
        if not admin:
            admin = User(
                public_id=str(uuid.uuid4()),
                full_name="NavAssist Master Admin",
                email=admin_email,
                phone_number="+919999999999",
                password_hash=get_password_hash("Admin@123"),
                role=UserRole.ADMIN,
                is_phone_verified=True,
                is_email_verified=True
            )
            db.add(admin)
            logger.info("Admin account seeded: admin@navassist.in / Admin@123")
            
        # 2. Seed location Service Points
        sp_result = await db.execute(select(ServicePoint).limit(1))
        if not sp_result.scalars().first():
            delhi_airport = ServicePoint(
                name="Indira Gandhi International Airport (T3)",
                type=ServicePointType.AIRPORT,
                city="New Delhi",
                state="Delhi",
                code="DEL",
                coordinates=func.ST_PointFromText("POINT(77.1000 28.5600)", 4326),
                is_active=True
            )
            ndls_station = ServicePoint(
                name="New Delhi Railway Station",
                type=ServicePointType.RAILWAY_STATION,
                city="New Delhi",
                state="Delhi",
                code="NDLS",
                coordinates=func.ST_PointFromText("POINT(77.2200 28.6400)", 4326),
                is_active=True
            )
            db.add_all([delhi_airport, ndls_station])
            logger.info("Service points (Airport & NDLS Railway station) seeded.")
            
        # 3. Seed Fare Rules
        fr_result = await db.execute(select(FareRule).limit(1))
        if not fr_result.scalars().first():
            airport_rule = FareRule(
                service_point_type=ServicePointType.AIRPORT,
                base_fare=150.00,
                per_km_rate=20.00,
                per_min_rate=3.00,
                min_fare=150.00,
                surge_multiplier=1.00,
                effective_from=datetime.now(timezone.utc),
                is_active=True
            )
            station_rule = FareRule(
                service_point_type=ServicePointType.RAILWAY_STATION,
                base_fare=80.00,
                per_km_rate=15.00,
                per_min_rate=2.00,
                min_fare=80.00,
                surge_multiplier=1.00,
                effective_from=datetime.now(timezone.utc),
                is_active=True
            )
            general_rule = FareRule(
                service_point_type=ServicePointType.GENERAL,
                base_fare=50.00,
                per_km_rate=12.00,
                per_min_rate=2.00,
                min_fare=50.00,
                surge_multiplier=1.00,
                effective_from=datetime.now(timezone.utc),
                is_active=True
            )
            db.add_all([airport_rule, station_rule, general_rule])
            logger.info("Marketplace Fare rules seeded successfully.")
            
        await db.commit()
        logger.info("Master database seeding operations completed successfully.")


if __name__ == "__main__":
    asyncio.run(seed_master_data())
