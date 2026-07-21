from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select

from app.models import User, EmergencyContact, UserRole
from app.repositories.base_repository import BaseRepository


class UserRepository(BaseRepository[User]):
    def __init__(self):
        super().__init__(User)

    async def get_user(self, db: AsyncSession, user_id: int) -> Optional[User]:
        """Fetch user by primary key ID."""
        return await self.get(db, user_id)

    async def get_user_by_phone(self, db: AsyncSession, phone: str) -> Optional[User]:
        """Fetch user by unique phone number."""
        result = await db.execute(select(User).filter(User.phone_number == phone))
        return result.scalars().first()

    async def get_user_by_email(self, db: AsyncSession, email: str) -> Optional[User]:
        """Fetch user by unique email."""
        result = await db.execute(select(User).filter(User.email == email))
        return result.scalars().first()

    async def create_user(
        self, 
        db: AsyncSession, 
        phone: str, 
        password_hash: str, 
        role: UserRole, 
        email: Optional[str] = None
    ) -> User:
        """Creates a basic user credential record."""
        import uuid
        db_user = User(
            public_id=str(uuid.uuid4()),
            full_name="",
            phone_number=phone,
            password_hash=password_hash,
            user_type=role,
            email=email
        )
        db.add(db_user)
        await db.flush()
        return db_user

    async def create_guest_profile(self, db: AsyncSession, user_id: int, name: str) -> User:
        """Creates a guest profile (updates the user full_name)."""
        user = await self.get_user(db, user_id)
        if user:
            user.full_name = name
            db.add(user)
            await db.flush()
        return user

    async def create_assistant_profile(self, db: AsyncSession, user_id: int, name: str):
        """Creates an assistant profile (updates user full_name and inserts AssistantProfile)."""
        from app.models.assistant import AssistantProfile
        user = await self.get_user(db, user_id)
        if user:
            user.full_name = name
            db.add(user)
            
        profile = AssistantProfile(user_id=user_id)
        db.add(profile)
        await db.flush()
        return profile

    async def get_guest(self, db: AsyncSession, user_id: int) -> Optional[User]:
        """Fetch guest profile (User with user_type guest)."""
        user = await self.get_user(db, user_id)
        if user and user.user_type == UserRole.GUEST:
            return user
        return None

    # Emergency Contacts CRUD
    async def get_emergency_contacts(self, db: AsyncSession, user_id: int) -> List[EmergencyContact]:
        """Fetch all emergency contacts registered for a user."""
        result = await db.execute(select(EmergencyContact).filter(EmergencyContact.user_id == user_id))
        return list(result.scalars().all())

    async def add_emergency_contact(
        self, 
        db: AsyncSession, 
        user_id: int, 
        name: str, 
        phone: str
    ) -> EmergencyContact:
        """Adds a new emergency contact detail."""
        contact = EmergencyContact(user_id=user_id, name=name, phone_number=phone)
        db.add(contact)
        await db.flush()
        return contact

    async def delete_emergency_contact(self, db: AsyncSession, contact_id: int, user_id: int) -> bool:
        """Deletes an emergency contact detail if owned by the user."""
        result = await db.execute(
            select(EmergencyContact).filter(
                EmergencyContact.id == contact_id, 
                EmergencyContact.user_id == user_id
            )
        )
        contact = result.scalars().first()
        if contact:
            await db.delete(contact)
            await db.flush()
            return True
        return False


# Module-level exports for backward compatibility
_repo = UserRepository()
user_repository = _repo
get_user = _repo.get_user
get_user_by_phone = _repo.get_user_by_phone
get_user_by_email = _repo.get_user_by_email
create_user = _repo.create_user
create_guest_profile = _repo.create_guest_profile
create_assistant_profile = _repo.create_assistant_profile
get_guest = _repo.get_guest
get_emergency_contacts = _repo.get_emergency_contacts
add_emergency_contact = _repo.add_emergency_contact
delete_emergency_contact = _repo.delete_emergency_contact

# BaseRepository methods
get = _repo.get
get_multi = _repo.get_multi
create = _repo.create
update = _repo.update
remove = _repo.remove
