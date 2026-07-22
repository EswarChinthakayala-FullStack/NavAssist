from datetime import datetime, timezone
from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy.orm import joinedload
from sqlalchemy import delete, update, func, update as sqlalchemy_update

from app.models import AssistantProfile as Assistant, KycStatus, OnlineStatus, AssistantDocument, User
from app.repositories.base_repository import BaseRepository


class AssistantRepository(BaseRepository[Assistant]):
    def __init__(self):
        super().__init__(Assistant)

    async def get_assistant(self, db: AsyncSession, user_id: int) -> Optional[Assistant]:
        """Fetch assistant profile by user ID."""
        result = await db.execute(
            select(Assistant)
            .filter(Assistant.user_id == user_id)
            .options(joinedload(Assistant.user))
        )
        return result.scalars().first()

    async def get_assistant_by_id(self, db: AsyncSession, assistant_id: int) -> Optional[Assistant]:
        """Fetch assistant profile by its own primary key (id)."""
        result = await db.execute(
            select(Assistant)
            .filter(Assistant.id == assistant_id)
            .options(joinedload(Assistant.user))
        )
        return result.scalars().first()

    async def create_assistant_profile(self, db: AsyncSession, user_id: int, name: str) -> Assistant:
        """Creates an assistant profile and sets the user's full_name."""
        result = await db.execute(select(User).filter(User.id == user_id))
        user = result.scalars().first()
        if user:
            user.full_name = name
            db.add(user)
        
        db_assistant = Assistant(user_id=user_id)
        db.add(db_assistant)
        await db.flush()
        return db_assistant

    async def update_assistant_location(
        self, db: AsyncSession, user_id: int, latitude: float, longitude: float
    ) -> Optional[Assistant]:
        """Updates latitude and longitude coordinates of an assistant, syncing MySQL POINT geometry."""
        assistant = await self.get_assistant(db, user_id)
        if not assistant:
            assistant = Assistant(
                user_id=user_id,
                verification_status=KycStatus.APPROVED,
                is_online=True
            )
            db.add(assistant)
            await db.flush()

        # Mark assistant online and update spatial coordinates
        assistant.is_online = True
        assistant.current_latitude = latitude
        assistant.current_longitude = longitude
        
        # Update MySQL spatial POINT column with SRID 4326
        assistant.current_location = func.ST_PointFromText(f"POINT({longitude} {latitude})", 4326)
        assistant.location_updated_at = datetime.now(timezone.utc)
        db.add(assistant)
        await db.flush()
        return assistant

    async def update_assistant_online_status(
        self, db: AsyncSession, user_id: int, status: OnlineStatus
    ) -> Optional[Assistant]:
        """Updates active online presence status of an assistant."""
        assistant = await self.get_assistant(db, user_id)
        if assistant:
            assistant.online_status = status
            db.add(assistant)
            await db.flush()
        return assistant

    # Spatial Queries (Uber/Rapido Standard Engine)
    async def get_nearby_assistants(
        self, db: AsyncSession, latitude: float, longitude: float, radius_km: float = 5.0
    ) -> List[Assistant]:
        import math
        from app.models.booking import Booking, BookingStatus

        busy_assistants_subquery = (
            select(Booking.assistant_id)
            .filter(
                Booking.assistant_id.isnot(None),
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
        )

        # Query online, verified assistants who are not in busy list
        result = await db.execute(
            select(Assistant)
            .options(joinedload(Assistant.user))
            .filter(
                Assistant.is_online == True,
                Assistant.verification_status.in_([KycStatus.VERIFIED, KycStatus.APPROVED, KycStatus.NOT_SUBMITTED, KycStatus.PENDING]),
                Assistant.user_id.notin_(busy_assistants_subquery),
                Assistant.id.notin_(busy_assistants_subquery)
            )
        )
        candidates = list(result.scalars().all())

        # Spatial distance calculation & multi-tier sorting
        nearby_candidates = []
        for a in candidates:
            a_lat = a.current_latitude or 0.0
            a_lon = a.current_longitude or 0.0
            if a_lat == 0.0 and a_lon == 0.0:
                continue

            # Haversine distance in km
            dlat = math.radians(a_lat - latitude)
            dlon = math.radians(a_lon - longitude)
            haver_a = (math.sin(dlat / 2) ** 2 +
                       math.cos(math.radians(latitude)) * math.cos(math.radians(a_lat)) *
                       math.sin(dlon / 2) ** 2)
            c = 2 * math.atan2(math.sqrt(haver_a), math.sqrt(1 - haver_a))
            dist_km = 6371.0 * c

            if dist_km <= radius_km:
                setattr(a, "_calculated_distance_km", round(dist_km, 2))
                nearby_candidates.append(a)

        # Multi-Tier Sorting Strategy (Uber/Rapido standard):
        # 1. Shortest distance / ETA
        # 2. Highest trust score
        # 3. Highest rating
        # 4. Lowest active workload (fair trip distribution)
        nearby_candidates.sort(
            key=lambda x: (
                getattr(x, "_calculated_distance_km", 999.0),
                -float(getattr(x, "trust_score", 100.0) or 100.0),
                -float(getattr(x, "avg_rating", 5.0) or 5.0),
                int(getattr(x, "total_trips", 0) or 0)
            )
        )

        return nearby_candidates

    # KYC Document Operations
    async def get_kyc(self, db: AsyncSession, kyc_id: int) -> Optional[Assistant]:
        """Fetch KYC assistant profile by ID."""
        result = await db.execute(select(Assistant).filter(Assistant.id == kyc_id))
        return result.scalars().first()

    async def get_kyc_by_assistant(self, db: AsyncSession, assistant_id: int) -> Optional[Assistant]:
        """Fetch KYC assistant profile by assistant ID."""
        return await self.get_assistant(db, assistant_id)

    async def create_kyc(
        self, db: AsyncSession, assistant_id: int, aadhaar_number: str, doc_front_url: str, doc_back_url: str
    ) -> Assistant:
        """Creates a new assistant KYC document verification request."""
        assistant = await self.get_assistant(db, assistant_id)
        if not assistant:
            assistant = Assistant(user_id=assistant_id)
            db.add(assistant)
            await db.flush()
            
        # Mask Aadhaar numbers for security audit trails
        aadhaar_masked = f"XXXX-XXXX-{aadhaar_number[-4:]}"
        assistant.aadhaar_masked = aadhaar_masked
        assistant.aadhaar_number_enc = aadhaar_number.encode("utf-8")
        assistant.verification_status = KycStatus.PENDING
        
        # Clear old doc records to prevent database duplicates
        await db.execute(delete(AssistantDocument).filter(AssistantDocument.assistant_id == assistant.id))
        
        doc_front = AssistantDocument(
            assistant_id=assistant.id,
            doc_type="aadhaar_front",
            file_url=doc_front_url
        )
        doc_back = AssistantDocument(
            assistant_id=assistant.id,
            doc_type="aadhaar_back",
            file_url=doc_back_url
        )
        db.add_all([doc_front, doc_back])
        db.add(assistant)
        await db.flush()
        return assistant

    async def review_kyc(
        self, db: AsyncSession, kyc_id: int, reviewer_id: int, status: KycStatus, review_notes: Optional[str] = None
    ) -> Optional[Assistant]:
        """Processes an admin review of an Assistant's KYC application."""
        assistant = await self.get_kyc(db, kyc_id)
        if not assistant:
            return None
            
        assistant.verification_status = status
        assistant.kyc_reviewed_by = reviewer_id
        assistant.kyc_reviewed_at = datetime.now(timezone.utc)
        assistant.review_notes = review_notes
        db.add(assistant)
        
        if status == KycStatus.VERIFIED:
            await db.execute(
                sqlalchemy_update(AssistantDocument)
                .filter(AssistantDocument.assistant_id == assistant.id)
                .values(verified=True)
            )
            
        await db.flush()
        return assistant


# Module-level exports for backward compatibility
_repo = AssistantRepository()
assistant_repository = _repo
get_assistant = _repo.get_assistant
get_assistant_by_id = _repo.get_assistant_by_id
create_assistant_profile = _repo.create_assistant_profile
update_assistant_location = _repo.update_assistant_location
update_assistant_online_status = _repo.update_assistant_online_status
get_nearby_assistants = _repo.get_nearby_assistants
get_kyc = _repo.get_kyc
get_kyc_by_assistant = _repo.get_kyc_by_assistant
create_kyc = _repo.create_kyc
review_kyc = _repo.review_kyc

# BaseRepository methods
get = _repo.get
get_multi = _repo.get_multi
create = _repo.create
update = _repo.update
remove = _repo.remove
