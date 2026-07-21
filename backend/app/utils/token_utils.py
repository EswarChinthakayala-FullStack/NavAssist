import secrets
import string


def generate_secure_token(length: int = 32) -> str:
    """Generates a secure random hex token."""
    return secrets.token_hex(length // 2)


def generate_booking_code(prefix: str = "NAV") -> str:
    """
    Generates a unique uppercase 6-digit alphanumeric booking reference code.
    Example: NAV-4B2E9F
    """
    chars = string.ascii_uppercase + string.digits
    code = "".join(secrets.choice(chars) for _ in range(6))
    return f"{prefix}-{code}"
