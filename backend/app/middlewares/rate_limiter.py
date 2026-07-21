import logging
from fastapi import Request, HTTPException, status
from app.core.redis_client import redis_client

logger = logging.getLogger(__name__)


class RedisRateLimiter:
    """
    FastAPI dependency-compatible class providing Redis window-based rate limiting.
    """
    def __init__(self, limit: int = 10, window_seconds: int = 60):
        self.limit = limit
        self.window_seconds = window_seconds

    async def __call__(self, request: Request):
        # Deduce identifier (use user if auth token exists, fallback to host IP)
        user_id = getattr(request.state, "user_id", None)
        identifier = f"user:{user_id}" if user_id else f"ip:{request.client.host if request.client else 'unknown'}"
        
        path = request.url.path
        key = f"rate_limit:{identifier}:{path}"
        
        try:
            # Check current counter
            current = await redis_client.redis_client.get(key)
            if current and int(current) >= self.limit:
                logger.warning(f"Rate limit exceeded for key: {key}")
                raise HTTPException(
                    status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                    detail="Too many requests. Please slow down and try again later."
                )
            
            # Atomic increment and TTL set
            pipe = redis_client.redis_client.pipeline()
            await pipe.incr(key)
            await pipe.expire(key, self.window_seconds)
            await pipe.execute()
        except HTTPException:
            raise
        except Exception as e:
            # If Redis connection fails, fail open in production so we do not crash APIs
            logger.error(f"Rate Limiter Redis connection failure: {e}. Bypassing limit checks.")
            pass
