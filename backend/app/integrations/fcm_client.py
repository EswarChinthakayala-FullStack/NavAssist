import logging
from typing import List, Optional

logger = logging.getLogger(__name__)


firebase_app = None


def is_mock_mode() -> bool:
    """Always returns True as FCM push notifications are disabled/removed."""
    return True


async def send_push_notification(device_token: str, title: str, body: str, data: dict = None) -> bool:
    """
    Logs push notification parameters to console/logger as FCM is disabled.
    """
    print(f"\n================ [MOCK PUSH NOTIFICATION] ================")
    print(f"DEVICE TOKEN: {device_token}")
    print(f"TITLE:        {title}")
    print(f"BODY:         {body}")
    print(f"DATA:         {data}")
    print(f"==========================================================\n")
    logger.info(f"Mock notification dispatched: {title} - {body}")
    return True


async def send_multicast_push_notification(device_tokens: List[str], title: str, body: str, data: dict = None) -> bool:
    """
    Logs multicast push notification parameters to console/logger.
    """
    if not device_tokens:
        return False
        
    print(f"\n================ [MOCK MULTICAST PUSH] ================")
    print(f"DEVICES ({len(device_tokens)}): {', '.join(device_tokens[:3])}...")
    print(f"TITLE:        {title}")
    print(f"BODY:         {body}")
    print(f"DATA:         {data}")
    print(f"=======================================================\n")
    logger.info(f"Mock Multicast dispatched to {len(device_tokens)} devices: {title}")
    return True
