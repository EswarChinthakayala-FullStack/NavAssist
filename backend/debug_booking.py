import asyncio
from app.core.database import SessionLocal
from app.models import Booking
from sqlalchemy.future import select

async def run():
    async with SessionLocal() as db:
        res = await db.execute(select(Booking).filter(Booking.id == 10))
        b = res.scalars().first()
        if b:
            print("BOOKING 10:")
            print(f"Distance: {b.distance_km}")
            print(f"Duration: {b.estimated_duration_min}")
            print(f"Scheduled At: {b.scheduled_at}")
            print(f"Created At: {b.created_at}")

if __name__ == "__main__":
    asyncio.run(run())
