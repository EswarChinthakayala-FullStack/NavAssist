from datetime import datetime, timezone
from zoneinfo import ZoneInfo
from typing import Optional

# Indian Standard Time (IST) timezone
IST = ZoneInfo("Asia/Kolkata")


def to_ist(dt: Optional[datetime]) -> Optional[datetime]:
    """Converts a naive or aware datetime object to Indian Standard Time (IST)."""
    if dt is None:
        return None
    if dt.tzinfo is None:
        # Assume naive datetimes are in UTC
        dt = dt.replace(tzinfo=timezone.utc)
    return dt.astimezone(IST)


def now_ist() -> datetime:
    """Returns the current datetime in Indian Standard Time (IST)."""
    return datetime.now(IST)


def get_ist_now() -> datetime:
    """Returns the current datetime in Indian Standard Time (IST)."""
    return now_ist()


def format_ist(dt: Optional[datetime]) -> Optional[str]:
    """Formats a datetime object to an ISO 8601 string localized to IST."""
    ist_dt = to_ist(dt)
    if ist_dt is None:
        return None
    return ist_dt.isoformat()
