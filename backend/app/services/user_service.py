import logging
from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from fastapi import HTTPException, status

from app.repositories import user_repository
from app.models.user import User
from app.models import EmergencyContact
from app.models.location import SavedLocation, LocationLabel
from app.schemas.user import UserUpdate, SavedLocationIn
from app.core.exceptions import ValidationError, NotFoundError

logger = logging.getLogger(__name__)


class UserService:
    @staticmethod
    async def get_user_profile(db: AsyncSession, user_id: int) -> User:
        """Fetches base user profile details."""
        user = await user_repository.get_user(db, user_id)
        if not user:
            raise NotFoundError("User profile not found")
        return user

    @staticmethod
    async def update_user_profile(db: AsyncSession, user_id: int, update_data: UserUpdate) -> User:
        """Updates user demographic details (name, email, profile photo)."""
        user = await user_repository.get_user(db, user_id)
        if not user:
            raise NotFoundError("User not found")
            
        if update_data.full_name is not None:
            user.full_name = update_data.full_name
        if update_data.email is not None:
            user.email = update_data.email
        if update_data.profile_photo_url is not None:
            user.profile_photo_url = update_data.profile_photo_url
            
        db.add(user)
        await db.flush()

        try:
            from app.services.audit_service import AuditService
            await AuditService.log_event(
                db=db,
                action="USER_PROFILE_UPDATED",
                entity_name="User",
                entity_id=user_id,
                user_id=user_id,
                details={"full_name": user.full_name, "email": user.email}
            )
        except Exception as e:
            logger.error(f"User update audit error: {e}")

        logger.info(f"User {user_id} profile updated successfully")
        return user

    @staticmethod
    async def delete_user_account(db: AsyncSession, user_id: int) -> bool:
        """Soft-deletes a user account by setting is_active to False."""
        user = await user_repository.get_user(db, user_id)
        if not user:
            raise NotFoundError("User not found")
            
        user.is_active = False
        db.add(user)
        await db.flush()
        logger.info(f"User {user_id} account disabled/soft-deleted")
        return True

    # Emergency Contacts
    @staticmethod
    async def list_emergency_contacts(db: AsyncSession, user_id: int) -> List[EmergencyContact]:
        """Lists emergency contacts registered to a user."""
        return await user_repository.get_emergency_contacts(db, user_id=user_id)

    @staticmethod
    async def register_emergency_contact(
        db: AsyncSession, 
        user_id: int, 
        name: str, 
        phone: str
    ) -> EmergencyContact:
        """Registers a new emergency contact for security notifications."""
        # Limit contacts check
        contacts = await user_repository.get_emergency_contacts(db, user_id=user_id)
        if len(contacts) >= 5:
            raise ValidationError("Maximum emergency contact limit (5) reached")
        return await user_repository.add_emergency_contact(db, user_id=user_id, name=name, phone=phone)

    @staticmethod
    async def remove_emergency_contact(db: AsyncSession, contact_id: int, user_id: int) -> bool:
        """Removes an emergency contact mapping."""
        success = await user_repository.delete_emergency_contact(db, contact_id=contact_id, user_id=user_id)
        if not success:
            raise NotFoundError("Emergency contact not found or unauthorized")
        return True

    # Saved Locations
    @staticmethod
    async def list_saved_locations(db: AsyncSession, user_id: int) -> List[SavedLocation]:
        """Fetch all bookmarked addresses for a user."""
        result = await db.execute(select(SavedLocation).filter(SavedLocation.user_id == user_id))
        return list(result.scalars().all())

    @staticmethod
    async def add_saved_location(db: AsyncSession, user_id: int, loc_in: SavedLocationIn) -> SavedLocation:
        """Creates a new saved location bookmark."""
        from sqlalchemy import func
        
        # Validate category
        try:
            label_enum = LocationLabel(loc_in.label)
        except ValueError:
            raise ValidationError("Invalid location label option")
            
        location = SavedLocation(
            user_id=user_id,
            label=label_enum,
            custom_label=loc_in.custom_label,
            address=loc_in.address,
            coordinates=func.ST_PointFromText(f"POINT({loc_in.longitude} {loc_in.latitude})", 4326),
            place_id=loc_in.place_id
        )
        db.add(location)
        await db.flush()
        logger.info(f"Saved location '{loc_in.label}' bookmarked for user {user_id}")
        return location

    @staticmethod
    async def remove_saved_location(db: AsyncSession, location_id: int, user_id: int) -> bool:
        """Deletes a saved location bookmark."""
        result = await db.execute(
            select(SavedLocation).filter(
                SavedLocation.id == location_id,
                SavedLocation.user_id == user_id
            )
        )
        location = result.scalars().first()
        if not location:
            raise NotFoundError("Saved location bookmark not found")
            
        await db.delete(location)
        await db.flush()
        logger.info(f"Saved location bookmark {location_id} deleted")
        return True

    @staticmethod
    async def edit_saved_location(db: AsyncSession, location_id: int, user_id: int, loc_in: SavedLocationIn) -> SavedLocation:
        """Modifies an existing saved location bookmark."""
        result = await db.execute(
            select(SavedLocation).filter(
                SavedLocation.id == location_id,
                SavedLocation.user_id == user_id
            )
        )
        location = result.scalars().first()
        if not location:
            raise NotFoundError("Saved location bookmark not found")
            
        from sqlalchemy import func
        from app.models.location import LocationLabel
        
        try:
            label_enum = LocationLabel(loc_in.label)
        except ValueError:
            raise ValidationError("Invalid location label option")
            
        location.label = label_enum
        location.custom_label = loc_in.custom_label
        location.address = loc_in.address
        location.coordinates = func.ST_PointFromText(f"POINT({loc_in.longitude} {loc_in.latitude})", 4326)
        location.place_id = loc_in.place_id
        
        db.add(location)
        await db.flush()
        logger.info(f"Saved location bookmark {location_id} updated")
        return location
