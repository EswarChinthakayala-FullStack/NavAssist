import re
from typing import Optional


EMAIL_REGEX = re.compile(r"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$")
PHONE_REGEX = re.compile(r"^\+?[1-9]\d{9,14}$")  # E.164 phone format
AADHAAR_REGEX = re.compile(r"^\d{12}$")


def validate_email(email: str) -> bool:
    """Validates if an email matches standard formats."""
    return bool(EMAIL_REGEX.match(email))


def validate_phone_number(phone: str) -> bool:
    """Validates E.164 formatting requirements on phone numbers."""
    return bool(PHONE_REGEX.match(phone))


def validate_aadhaar_number(aadhaar: str) -> bool:
    """Validates if an Aadhaar number is a 12-digit numeric code."""
    return bool(AADHAAR_REGEX.match(aadhaar))
