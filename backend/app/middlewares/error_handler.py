import logging
import traceback
from fastapi import Request, Response
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware
from app.core.exceptions import AppException

logger = logging.getLogger(__name__)


class ErrorHandlerMiddleware(BaseHTTPMiddleware):
    """Global middleware that catches all unhandled and custom exceptions."""
    async def dispatch(self, request: Request, call_next) -> Response:
        try:
            return await call_next(request)
        except AppException as exc:
            # Domain exception, log as warning
            logger.warning(f"Domain exception: {exc.message} (Status Code: {exc.status_code})")
            return JSONResponse(
                status_code=exc.status_code,
                content={
                    "success": False,
                    "detail": exc.message
                }
            )
        except Exception as exc:
            # Unhandled server exception, log as critical with full traceback
            tb = traceback.format_exc()
            logger.critical(f"Unhandled Exception: {exc}\nTraceback:\n{tb}")
            return JSONResponse(
                status_code=500,
                content={
                    "success": False,
                    "detail": "An internal server error occurred. Please try again later."
                }
            )
