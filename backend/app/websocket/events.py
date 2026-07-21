from typing import Dict, Any


def build_location_update_event(
    booking_id: int,
    lat: float,
    lng: float,
    heading: float,
    speed: float,
    ts: str
) -> Dict[str, Any]:
    """Streams the assistant's live GPS position during an active booking."""
    return {
        "event": "location:update",
        "booking_id": booking_id,
        "lat": lat,
        "lng": lng,
        "heading": heading,
        "speed": speed,
        "ts": ts
    }


def build_booking_status_event(
    booking_id: int,
    status: str,
    ts: str
) -> Dict[str, Any]:
    """Pushes every booking lifecycle transition in real time."""
    return {
        "event": "booking:status_changed",
        "booking_id": booking_id,
        "status": status,
        "ts": ts
    }


def build_eta_update_event(
    booking_id: int,
    eta_minutes: int,
    distance_remaining_km: float
) -> Dict[str, Any]:
    """Recalculated ETA as the assistant approaches pickup or destination."""
    return {
        "event": "eta:update",
        "booking_id": booking_id,
        "eta_minutes": eta_minutes,
        "distance_remaining_km": distance_remaining_km
    }


def build_sos_triggered_event(
    sos_id: int,
    user_id: int,
    booking_id: int,
    lat: float,
    lng: float
) -> Dict[str, Any]:
    """Broadcasts an SOS alert instantly to on-duty admins."""
    return {
        "event": "sos:triggered",
        "sos_id": sos_id,
        "user_id": user_id,
        "booking_id": booking_id,
        "lat": lat,
        "lng": lng
    }


def build_chat_message_event(
    booking_id: int,
    sender_id: int,
    message: str,
    ts: str
) -> Dict[str, Any]:
    """Optional in-trip text messaging channel."""
    return {
        "event": "chat:message",
        "booking_id": booking_id,
        "sender_id": sender_id,
        "message": message,
        "ts": ts
    }


def build_connection_ack_event(
    status: str,
    booking_id: int
) -> Dict[str, Any]:
    """Confirms a WebSocket subscription was established."""
    return {
        "event": "connection:ack",
        "status": status,
        "booking_id": booking_id
    }
