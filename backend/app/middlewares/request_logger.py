import uuid
import time
import logging
from fastapi import Request, Response
from starlette.middleware.base import BaseHTTPMiddleware

logger = logging.getLogger(__name__)


class RequestLoggerMiddleware(BaseHTTPMiddleware):
    """
    Middleware logging processing metrics for incoming requests, injecting correlation IDs.
    """
    async def dispatch(self, request: Request, call_next):
        correlation_id = request.headers.get("X-Correlation-ID") or str(uuid.uuid4())
        request.state.correlation_id = correlation_id
        
        start_time = time.time()
        logger.info(f"Incoming request: {request.method} {request.url.path} | Correlation-ID: {correlation_id}")
        
        try:
            response: Response = await call_next(request)
        except Exception as e:
            process_time = time.time() - start_time
            logger.error(f"Request failed: {request.method} {request.url.path} | Error: {e} | Process Time: {process_time:.4f}s | Correlation-ID: {correlation_id}")
            raise
            
        process_time = time.time() - start_time
        logger.info(f"Outgoing response: status={response.status_code} | Process Time: {process_time:.4f}s | Correlation-ID: {correlation_id}")
        
        response.headers["X-Correlation-ID"] = correlation_id
        return response
