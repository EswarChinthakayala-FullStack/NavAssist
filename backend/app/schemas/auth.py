from typing import Optional
from pydantic import BaseModel, Field


class TokenPair(BaseModel):
    """Pydantic schema representing JWT access and refresh token pair."""
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    debug_otp: Optional[str] = None
    debug_email_code: Optional[str] = None


# Backward compatibility alias
Token = TokenPair


class TokenPayload(BaseModel):
    """Pydantic schema for JWT token claims payload decoding."""
    sub: Optional[str] = None
    type: Optional[str] = None


class OtpRequest(BaseModel):
    """Request schema to trigger a new 6-digit OTP SMS verification code."""
    phone: str = Field(..., description="Phone number with country code, e.g., +919999999999")


# Backward compatibility alias
SendOtpRequest = OtpRequest


class VerifyOtpRequest(BaseModel):
    """Request schema containing verification phone and received OTP code."""
    phone: str = Field(..., description="Phone number with country code")
    otp: str = Field(..., min_length=6, max_length=6, description="6-digit OTP string")


class SignupRequest(BaseModel):
    """Request schema for Guest or Assistant phone registration."""
    phone: str = Field(..., description="Phone number with country code")
    name: str = Field(..., description="Full Name")
    role: str = Field(..., description="Role chosen, must be 'guest' or 'assistant'")
    email: Optional[str] = None
    password: str = Field(..., min_length=6, description="Password (at least 6 characters)")


# Backward compatibility alias
RegisterUserRequest = SignupRequest


class LoginRequest(BaseModel):
    """Standard password-based authentication login request."""
    phone: str = Field(..., description="Phone number with country code")
    password: str = Field(..., description="User password")


class GoogleAuthRequest(BaseModel):
    """OAuth login credential request via Google token validation."""
    id_token: str = Field(..., description="Google OAuth ID token")


class ForgotPasswordRequest(BaseModel):
    phone: str = Field(..., description="Phone number with country code")


class ResetPasswordRequest(BaseModel):
    phone: str = Field(..., description="Phone number with country code")
    otp: str = Field(..., min_length=6, max_length=6, description="6-digit OTP string")
    new_password: str = Field(..., min_length=6, description="New password (at least 6 characters)")

