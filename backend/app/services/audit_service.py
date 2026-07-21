import logging
from typing import Optional, Dict, Any
from decimal import Decimal
from datetime import datetime, date
from sqlalchemy.ext.asyncio import AsyncSession
from app.models.support import AuditLog

logger = logging.getLogger(__name__)


def _sanitize_for_json(obj: Any) -> Any:
    """Recursively converts non-serializable objects (Decimal, datetime, etc.) to JSON primitives."""
    if isinstance(obj, Decimal):
        return float(obj)
    if isinstance(obj, (datetime, date)):
        return obj.isoformat()
    if isinstance(obj, dict):
        return {str(k): _sanitize_for_json(v) for k, v in obj.items()}
    if isinstance(obj, (list, tuple, set)):
        return [_sanitize_for_json(x) for x in obj]
    return obj


class AuditService:
    @staticmethod
    async def log_event(
        db: AsyncSession,
        action: str,
        entity_name: str,
        entity_id: Optional[int] = None,
        user_id: Optional[int] = None,
        details: Optional[Dict[str, Any]] = None,
        ip_address: Optional[str] = None
    ) -> Optional[AuditLog]:
        """Persists a structured audit log entry to the audit_logs table cleanly without breaking transactions."""
        try:
            admin_id = user_id if user_id else 1
            sanitized_details = _sanitize_for_json(details) if details else None

            async with db.begin_nested():
                entry = AuditLog(
                    admin_id=admin_id,
                    action=action,
                    entity_type=entity_name,
                    entity_id=entity_id,
                    metadata_json=sanitized_details
                )
                db.add(entry)
                await db.flush()

            logger.info(f"Audit log recorded: {action} on {entity_name} #{entity_id} by User/Admin #{admin_id}")
            return entry
        except Exception as e:
            logger.error(f"Failed to record audit log entry: {e}")
            return None
