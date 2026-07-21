import asyncio
import pytest
from typing import AsyncGenerator
from httpx import AsyncClient, ASGITransport
from sqlalchemy.ext.asyncio import AsyncSession
from unittest.mock import AsyncMock, MagicMock

from app.main import app
from app.api.deps import get_db


@pytest.fixture(scope="session")
def event_loop():
    """Create an instance of the default event loop for the test session."""
    loop = asyncio.get_event_loop_policy().new_event_loop()
    yield loop
    loop.close()


@pytest.fixture
async def mock_db() -> AsyncMock:
    """Fixture returning an AsyncMock representing a database session."""
    db_session = AsyncMock(spec=AsyncSession)
    return db_session


@pytest.fixture
async def client(mock_db: AsyncMock) -> AsyncGenerator[AsyncClient, None]:
    """
    Fixture returning an AsyncClient.
    Overrides get_db dependency to yield a mock database session.
    """
    async def override_get_db():
        yield mock_db

    app.dependency_overrides[get_db] = override_get_db
    
    # In httpx 0.20+, we pass the app directly to ASGITransport
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        yield ac
        
    app.dependency_overrides.clear()
