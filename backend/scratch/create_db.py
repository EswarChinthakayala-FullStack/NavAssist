import asyncio
from sqlalchemy.ext.asyncio import create_async_engine
from sqlalchemy import text
from app.core.config import settings

async def main():
    # Build engine URI without database name
    password_part = f":{settings.MYSQL_PASSWORD}" if settings.MYSQL_PASSWORD else ""
    base_uri = f"mysql+aiomysql://{settings.MYSQL_USER}{password_part}@{settings.MYSQL_SERVER}:{settings.MYSQL_PORT}"
    
    print(f"Connecting to MySQL server at {base_uri}...")
    engine = create_async_engine(base_uri)
    async with engine.connect() as conn:
        print("Creating database if not exists 'navassist'...")
        await conn.execute(text("CREATE DATABASE IF NOT EXISTS navassist CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"))
        await conn.commit()
    print("Database created or already exists.")
    await engine.dispose()

if __name__ == "__main__":
    asyncio.run(main())
