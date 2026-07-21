from contextlib import asynccontextmanager
from fastapi import FastAPI, Depends, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from slowapi import _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded

from app.api.v1.router import api_router
from app.core.config import settings
from app.core.database import engine
from app.core.redis_client import redis_client
from app.core.limiter import limiter


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Lifespan context manager that handles startup and shutdown operations,
    ensuring database pools and cache clients initialize and close cleanly.
    """
    # Startup operations
    # Setup structured JSON logging
    from app.core.logging_config import setup_logging
    setup_logging()
    
    # Verify Redis connection
    try:
        await redis_client.ping()
        print("Connected to Redis cache successfully.")
    except Exception as e:
        print(f"Warning: Failed to connect to Redis cache on startup: {e}")

    # Ensure schema migrations for payment columns
    try:
        from migrate_db import run_migrations
        await run_migrations()
    except Exception as e:
        print(f"Startup schema migration check notice: {e}")

    yield
    
    # Shutdown operations
    # Dispose SQLAlchemy connections
    await engine.dispose()
    # Close Redis connections
    await redis_client.close()
    print("Database and Cache connections closed.")


from typing import Any
from fastapi.responses import JSONResponse
from fastapi.encoders import jsonable_encoder
from datetime import datetime
from app.utils.timezone import to_ist

class ISTJSONResponse(JSONResponse):
    def render(self, content: Any) -> bytes:
        encoded = jsonable_encoder(
            content,
            custom_encoder={
                datetime: lambda dt: to_ist(dt).isoformat()
            }
        )
        return super().render(encoded)

app = FastAPI(
    title=settings.PROJECT_NAME,
    version="1.0.0",
    description="Backend API services for NavAssist, connecting guests with assistants.",
    lifespan=lifespan,
    default_response_class=ISTJSONResponse
)

# Register Request Logger Middleware
from app.middlewares.request_logger import RequestLoggerMiddleware
app.add_middleware(RequestLoggerMiddleware)

# Register custom error handler middleware
from app.middlewares.error_handler import ErrorHandlerMiddleware
app.add_middleware(ErrorHandlerMiddleware)

# Configure CORS Middleware
# Essential for allowing external Guest and Assistant mobile client requests
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://localhost:8000",
        "http://127.0.0.1:8000",
    ],
    allow_origin_regex=r"https?://(localhost|127\.0\.0\.1)(:\d+)?",
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register slowapi rate limiting configuration and exception handlers
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# Mount uploads folder for static uploads
from fastapi.staticfiles import StaticFiles
import os
uploads_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "uploads")
os.makedirs(uploads_path, exist_ok=True)
app.mount("/static", StaticFiles(directory=uploads_path), name="static")

# Mount web directory for static frontend tracking and simulation demo
web_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "web")
os.makedirs(web_path, exist_ok=True)
app.mount("/web", StaticFiles(directory=web_path, html=True), name="web")

# Include v1 endpoints
app.include_router(api_router, prefix=settings.API_V1_STR)


@app.get("/", tags=["General"])
async def root():
    """Welcome and meta information endpoint."""
    return {
        "app": settings.PROJECT_NAME,
        "version": "1.0.0",
        "status": "healthy",
        "documentation": "/docs"
    }


@app.get("/health", tags=["General"])
async def health_check():
    """Active diagnostic check verifying MySQL and Redis health states."""
    mysql_healthy = False
    redis_healthy = False
    
    # Check MySQL
    try:
        from sqlalchemy import text
        async with engine.connect() as conn:
            await conn.execute(text("SELECT 1"))
        mysql_healthy = True
    except Exception as e:
        print(f"Database health query failed: {e}")
        
    # Check Redis
    try:
        await redis_client.ping()
        redis_healthy = True
    except Exception as e:
        print(f"Redis health check ping failed: {e}")
        
    return {
        "status": "online" if (mysql_healthy and redis_healthy) else "degraded",
        "services": {
            "database": "online" if mysql_healthy else "offline",
            "cache": "online" if redis_healthy else "offline"
        }
    }


# Include WebSocket tracking endpoints
from app.websocket import tracking_ws
app.include_router(tracking_ws.router, prefix="/ws", tags=["WebSockets"])
