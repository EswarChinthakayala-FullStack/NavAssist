import asyncio
from app.core.database import engine
from sqlalchemy import text

async def main():
    async with engine.begin() as conn:
        await conn.execute(text("ALTER TABLE users MODIFY COLUMN profile_photo_url MEDIUMTEXT;"))
        print("Successfully updated column type to MEDIUMTEXT!")

if __name__ == "__main__":
    asyncio.run(main())
