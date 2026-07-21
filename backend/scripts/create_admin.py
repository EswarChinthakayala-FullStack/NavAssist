import asyncio
import argparse
import sys
import uuid
from sqlalchemy.future import select

from app.core.database import SessionLocal
from app.core.security import get_password_hash
from app.models.user import User, UserRole


async def create_admin(name: str, email: str, phone: str, password: str):
    """
    Saves a new administrator user to the database after verifying email/phone uniqueness.
    """
    async with SessionLocal() as db:
        # Check uniqueness constraints
        result = await db.execute(
            select(User).filter((User.email == email) | (User.phone_number == phone))
        )
        if result.scalars().first():
            print(f"Error: User account with email '{email}' or phone '{phone}' already registered.")
            sys.exit(1)
            
        admin = User(
            public_id=str(uuid.uuid4()),
            full_name=name,
            email=email,
            phone_number=phone,
            password_hash=get_password_hash(password),
            role=UserRole.ADMIN,
            is_phone_verified=True,
            is_email_verified=True
        )
        db.add(admin)
        await db.commit()
        print(f"Success: Administrator account '{email}' bootstrapped successfully.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="CLI tool to bootstrap admin accounts.")
    parser.add_argument("--name", required=True, help="Full Name of the Administrator")
    parser.add_argument("--email", required=True, help="Unique Email Address")
    parser.add_argument("--phone", required=True, help="Unique E.164 Phone Number")
    parser.add_argument("--password", required=True, help="Access Password")
    
    args = parser.parse_args()
    asyncio.run(create_admin(args.name, args.email, args.phone, args.password))
