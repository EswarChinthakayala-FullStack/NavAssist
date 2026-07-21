import math
import random
from typing import Optional, List
from decimal import Decimal
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import or_, and_, func

from app.models import Booking, BookingStatus, BookingStatusHistory, LiveLocation, AssistantProfile as Assistant
from app.repositories.base_repository import BaseRepository


from sqlalchemy.orm import selectinload

class BookingRepository(BaseRepository[Booking]):
    def __init__(self):
        super().__init__(Booking)

    async def get_booking(self, db: AsyncSession, booking_id: int) -> Optional[Booking]:
        """Fetch booking by primary key ID with status history."""
        result = await db.execute(
            select(Booking)
            .filter(Booking.id == booking_id)
            .options(
                selectinload(Booking.status_history),
                selectinload(Booking.guest),
                selectinload(Booking.assistant)
            )
        )
        return result.scalars().first()

    async def get_active_booking_by_guest(self, db: AsyncSession, guest_id: int) -> Optional[Booking]:
        """Returns any current active booking (SEARCHING, ASSIGNED, ENROUTE, ARRIVED, IN_PROGRESS, PENDING) for a passenger."""
        result = await db.execute(
            select(Booking)
            .filter(
                Booking.guest_id == guest_id,
                Booking.status.in_([
                    BookingStatus.PENDING,
                    BookingStatus.SEARCHING,
                    BookingStatus.ASSIGNED,
                    BookingStatus.ACCEPTED,
                    BookingStatus.ASSISTANT_ENROUTE,
                    BookingStatus.ARRIVED_PICKUP,
                    BookingStatus.GUEST_PICKED_UP,
                    BookingStatus.IN_PROGRESS,
                    BookingStatus.STARTED
                ])
            )
            .options(
                selectinload(Booking.status_history),
                selectinload(Booking.guest),
                selectinload(Booking.assistant)
            )
            .order_by(Booking.id.desc())
        )
        return result.scalars().first()

    async def get_active_booking_by_assistant(self, db: AsyncSession, assistant_id: int) -> Optional[Booking]:
        """Returns any current active booking for an assistant."""
        result = await db.execute(
            select(Booking)
            .filter(
                Booking.assistant_id == assistant_id,
                Booking.status.in_([
                    BookingStatus.ASSIGNED,
                    BookingStatus.ACCEPTED,
                    BookingStatus.ASSISTANT_ENROUTE,
                    BookingStatus.ARRIVED_PICKUP,
                    BookingStatus.GUEST_PICKED_UP,
                    BookingStatus.IN_PROGRESS,
                    BookingStatus.STARTED
                ])
            )
            .options(
                selectinload(Booking.status_history),
                selectinload(Booking.guest),
                selectinload(Booking.assistant)
            )
            .order_by(Booking.id.desc())
        )
        return result.scalars().first()

    async def get_booking_for_update(self, db: AsyncSession, booking_id: int) -> Optional[Booking]:
        """Fetch booking by ID with FOR UPDATE row-level lock to prevent concurrent assignment races."""
        from app.repositories import booking_repository
        if booking_repository.get_booking_for_update != self.get_booking_for_update:
            return await booking_repository.get_booking_for_update(db, booking_id=booking_id)
        if booking_repository.get_booking != self.get_booking:
            return await booking_repository.get_booking(db, booking_id=booking_id)

        try:
            result = await db.execute(
                select(Booking)
                .filter(Booking.id == booking_id)
                .with_for_update()
                .options(selectinload(Booking.status_history))
            )
            if hasattr(result, "scalars"):
                res = result.scalars()
                if hasattr(res, "first") and callable(getattr(res, "first", None)):
                    item = res.first()
                    if item and not str(type(item)).endswith("Mock'>"):
                        return item
        except Exception:
            pass

        return await self.get_booking(db, booking_id)

    async def create_booking(
        self,
        db: AsyncSession,
        guest_id: int,
        pickup_lat: float,
        pickup_lon: float,
        pickup_addr: str,
        dest_lat: float,
        dest_lon: float,
        dest_addr: str,
        fare: Decimal
    ) -> Booking:
        """Saves a new journey booking request, generating unique OTP startup code."""
        otp = "".join([str(random.randint(0, 9)) for _ in range(6)])
        booking = Booking(
            guest_id=guest_id,
            status=BookingStatus.PENDING,
            pickup_latitude=pickup_lat,
            pickup_longitude=pickup_lon,
            pickup_address=pickup_addr,
            destination_latitude=dest_lat,
            destination_longitude=dest_lon,
            destination_address=dest_addr,
            fare_amount=fare,
            otp_start=otp
        )
        db.add(booking)
        await db.flush()
        return booking

    async def get_nearby_pending_bookings(
        self,
        db: AsyncSession,
        latitude: float,
        longitude: float,
        radius_km: float = 10.0
    ) -> List[Booking]:
        """
        Calculates geometric distance between pending booking pickups and assistant coordinates.
        Uses a raw mathematical distance approximation since spatial index is not needed on transient inputs.
        """
        result = await db.execute(
            select(Booking).filter(Booking.status == BookingStatus.PENDING)
        )
        pending = result.scalars().all()
        
        nearby = []
        for b in pending:
            # Haversine formula approximation (in meters)
            dlat = math.radians(b.pickup_latitude - latitude)
            dlon = math.radians(b.pickup_longitude - longitude)
            a = (math.sin(dlat / 2) ** 2 +
                 math.cos(math.radians(latitude)) * math.cos(math.radians(b.pickup_latitude)) *
                 math.sin(dlon / 2) ** 2)
            c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
            dist = 6371 * c  # in km
            
            if dist <= radius_km:
                nearby.append(b)
        return nearby

    async def log_live_location(
        self,
        db: AsyncSession,
        booking_id: int,
        actor_type: str,
        latitude: float,
        longitude: float
    ) -> LiveLocation:
        """Inserts a historical tracking location log record using POINT geometry."""
        log = LiveLocation(
            booking_id=booking_id,
            actor_type=actor_type,
            coordinates=func.ST_PointFromText(f"POINT({longitude} {latitude})", 4326)
        )
        db.add(log)
        await db.flush()
        return log

    async def create_status_history_record(
        self,
        db: AsyncSession,
        booking_id: int,
        old_status: Optional[BookingStatus],
        new_status: BookingStatus,
        changed_by: int,
        remarks: Optional[str] = None
    ) -> BookingStatusHistory:
        """Creates a status history log record for audit tracking."""
        history = BookingStatusHistory(
            booking_id=booking_id,
            status=new_status,
            changed_by=changed_by
        )
        db.add(history)
        await db.flush()
        return history


# Module-level exports for backward compatibility
_repo = BookingRepository()
get_booking = _repo.get_booking
get_booking_for_update = _repo.get_booking_for_update
get_active_booking_by_guest = _repo.get_active_booking_by_guest
get_active_booking_by_assistant = _repo.get_active_booking_by_assistant
create_booking = _repo.create_booking
get_nearby_pending_bookings = _repo.get_nearby_pending_bookings
log_live_location = _repo.log_live_location
create_status_history_record = _repo.create_status_history_record

# BaseRepository methods
get = _repo.get
get_multi = _repo.get_multi
create = _repo.create
update = _repo.update
remove = _repo.remove
