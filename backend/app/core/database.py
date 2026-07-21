from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from sqlalchemy.orm import DeclarativeBase
from app.core.config import settings

# Create async engine for MySQL
# pool_pre_ping ensures stale connections are recycled
engine = create_async_engine(
    settings.SQLALCHEMY_DATABASE_URI,
    echo=False,  # Set to True for verbose SQL logging during debugging
    pool_pre_ping=True,
    pool_size=100,
    max_overflow=50,
    pool_recycle=300,
    pool_timeout=30
)

# Async session class factory
SessionLocal = async_sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False,
    autocommit=False,
    autoflush=False
)


# Declarative base class for models
class Base(DeclarativeBase):
    pass
