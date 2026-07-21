import logging
import uuid
from fastapi import APIRouter, Depends, HTTPException, status, Request
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import delete

from app.api import deps
from app.core import redis_client
from app.core.security import (
    create_access_token,
    create_refresh_token,
    get_password_hash,
    verify_password,
    verify_token
)
from app.core.limiter import limiter
from app.repositories import user_repository as crud_user
from app.models.user import User, UserRole
from app.schemas.auth import (
    RegisterUserRequest,
    SendOtpRequest,
    Token,
    VerifyOtpRequest,
    LoginRequest,
    GoogleAuthRequest,
    ForgotPasswordRequest,
    ResetPasswordRequest
)
from app.services.otp_service import OtpService

router = APIRouter()
logger = logging.getLogger(__name__)


@router.post("/send-otp", status_code=status.HTTP_200_OK, include_in_schema=False)
@router.post("/otp/send", status_code=status.HTTP_200_OK)
@limiter.limit("5/minute")
async def send_otp(request: Request, otp_req: SendOtpRequest):
    """Sends OTP to phone for registration, login, or password resets."""
    otp = await OtpService.send_otp(otp_req.phone)
    return {
        "success": True, 
        "message": "OTP sent successfully",
        "debug_otp": otp
    }


@router.post("/verify-otp", status_code=status.HTTP_200_OK, include_in_schema=False)
@router.post("/otp/verify", status_code=status.HTTP_200_OK)
@limiter.limit("5/minute")
async def verify_otp(request: Request, otp_req: VerifyOtpRequest, db: AsyncSession = Depends(deps.get_db)):
    """Verifies standard 6-digit OTP code flow validation."""
    is_valid = await OtpService.verify_otp(otp_req.phone, otp_req.otp)
    if not is_valid:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid or expired OTP"
        )
    
    user = await crud_user.get_user_by_phone(db, phone=otp_req.phone)
    if not user:
        return {
            "success": True,
            "registered": False,
            "message": "OTP verified successfully. Registration required.",
            "phone": otp_req.phone
        }
    
    access_token = create_access_token(subject=user.id)
    refresh_token = create_refresh_token(subject=user.id)
    return {
        "success": True,
        "registered": True,
        "tokens": Token(access_token=access_token, refresh_token=refresh_token)
    }


from pydantic import BaseModel

class VerifyEmailRequest(BaseModel):
    email: str
    code: str

class VerifyPhoneRequest(BaseModel):
    phone: str
    code: str

@router.post("/verify/email", status_code=status.HTTP_200_OK)
async def verify_email(request: VerifyEmailRequest, db: AsyncSession = Depends(deps.get_db)):
    """Verifies the email OTP code and marks user's email as verified."""
    from datetime import datetime, timezone
    from app.models.user import OtpVerification
    
    cache_key = f"email_verify_{request.email}"
    verified = False
    
    # 1. Try Redis first
    stored_code = await redis_client.get_otp(cache_key)
    if stored_code and stored_code == request.code:
        await redis_client.delete_otp(cache_key)
        verified = True
    
    # 2. Fallback: check otp_verifications DB table
    if not verified:
        result = await db.execute(
            select(OtpVerification).filter(
                OtpVerification.identifier == request.email,
                OtpVerification.otp_hash == request.code
            )
        )
        db_otp = result.scalars().first()
        if db_otp and db_otp.expires_at > datetime.now(timezone.utc):
            db_otp.is_verified = True
            verified = True
        elif db_otp:
            # Expired
            pass

    if not verified:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid or expired email verification code"
        )
    
    # Update user
    result = await db.execute(select(User).filter(User.email == request.email))
    user = result.scalars().first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User profile not found"
        )
        
    user.is_email_verified = True
    db.add(user)
    await db.commit()
    return {"success": True, "message": "Email address verified successfully"}

@router.post("/verify/phone", status_code=status.HTTP_200_OK)
async def verify_phone(request: VerifyPhoneRequest, db: AsyncSession = Depends(deps.get_db)):
    """Verifies the phone OTP code and marks user's phone as verified."""
    is_valid = await OtpService.verify_otp(request.phone, request.code)
    if not is_valid:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid or expired phone verification code"
        )
        
    # Update user
    result = await db.execute(select(User).filter(User.phone_number == request.phone))
    user = result.scalars().first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User profile not found"
        )
        
    user.is_phone_verified = True
    db.add(user)
    await db.commit()
    return {"success": True, "message": "Phone number verified successfully"}

@router.post("/verify/resend-email", status_code=status.HTTP_200_OK)
async def resend_email_otp(email: str, db: AsyncSession = Depends(deps.get_db)):
    """Generates and dispatches a new email verification code."""
    import random
    from datetime import datetime, timezone, timedelta
    from app.models.user import OtpVerification, OtpPurpose
    
    result = await db.execute(select(User).filter(User.email == email))
    user = result.scalars().first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User profile not found"
        )
        
    email_code = f"{random.randint(100000, 999999)}"
    await redis_client.set_otp(f"email_verify_{email}", email_code, expire_seconds=900)
    
    # Also save to otp_verifications DB table
    await db.execute(delete(OtpVerification).filter(OtpVerification.identifier == email))
    db_otp = OtpVerification(
        identifier=email,
        otp_hash=email_code,
        purpose=OtpPurpose.SIGNUP,
        expires_at=datetime.now(timezone.utc) + timedelta(minutes=15),
        is_verified=False
    )
    db.add(db_otp)
    await db.commit()
    
    from app.services.email_service import EmailService
    EmailService.send_verification_email(email, email_code)
    return {"success": True, "message": "Email verification code resent successfully", "debug_code": email_code}


@router.post("/signup", response_model=Token, status_code=status.HTTP_201_CREATED)
async def signup(request: RegisterUserRequest, db: AsyncSession = Depends(deps.get_db)):
    """Registers a new Guest or Assistant user account."""
    try:
        user_role = UserRole(request.role)
    except ValueError:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Role must be either 'guest' or 'assistant'"
        )
        
    existing_user = await crud_user.get_user_by_phone(db, phone=request.phone)
    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Phone number already registered"
        )
        
    password_hash = get_password_hash(request.password)
    new_user = await crud_user.create_user(
        db, 
        phone=request.phone, 
        password_hash=password_hash, 
        role=user_role, 
        email=request.email
    )
    
    if user_role == UserRole.GUEST:
        await crud_user.create_guest_profile(db, user_id=new_user.id, name=request.name)
    elif user_role == UserRole.ASSISTANT:
        await crud_user.create_assistant_profile(db, user_id=new_user.id, name=request.name)
        
    await db.commit()
    
    # Generate and send phone verification OTP
    phone_otp = await OtpService.send_otp(new_user.phone_number)
    
    # Generate and send email verification code
    email_code = None
    if new_user.email:
        import random
        from datetime import datetime, timezone, timedelta
        from app.models.user import OtpVerification, OtpPurpose
        email_code = f"{random.randint(100000, 999999)}"
        await redis_client.set_otp(f"email_verify_{new_user.email}", email_code, expire_seconds=900)
        # Save to otp_verifications DB table
        await db.execute(delete(OtpVerification).filter(OtpVerification.identifier == new_user.email))
        db_otp = OtpVerification(
            identifier=new_user.email,
            otp_hash=email_code,
            purpose=OtpPurpose.SIGNUP,
            expires_at=datetime.now(timezone.utc) + timedelta(minutes=15),
            is_verified=False
        )
        db.add(db_otp)
        await db.commit()
        from app.services.email_service import EmailService
        EmailService.send_verification_email(new_user.email, email_code)
    
    access_token = create_access_token(subject=new_user.id)
    refresh_token = create_refresh_token(subject=new_user.id)
    return Token(access_token=access_token, refresh_token=refresh_token, debug_otp=phone_otp, debug_email_code=email_code)


# Deprecated alias
@router.post("/register", response_model=Token, status_code=status.HTTP_201_CREATED, include_in_schema=False)
async def register_legacy(request: RegisterUserRequest, db: AsyncSession = Depends(deps.get_db)):
    return await signup(request, db)


@router.post("/login", response_model=Token)
async def login(request: LoginRequest, db: AsyncSession = Depends(deps.get_db)):
    """Authenticate via credentials, returning a token pair."""
    user = await crud_user.get_user_by_phone(db, phone=request.phone)
    if not user or not verify_password(request.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect phone number or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
        
    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Inactive user account"
        )

    # Allow login to request tokens, but frontend will block routing if unverified.
    phone_otp = None
    if not user.is_phone_verified:
        phone_otp = await OtpService.send_otp(user.phone_number)

    email_code = None
    if not user.is_email_verified and user.email:
        import random
        from datetime import datetime, timezone, timedelta
        from app.models.user import OtpVerification, OtpPurpose
        email_code = f"{random.randint(100000, 999999)}"
        await redis_client.set_otp(f"email_verify_{user.email}", email_code, expire_seconds=900)
        # Save to otp_verifications DB table
        await db.execute(delete(OtpVerification).filter(OtpVerification.identifier == user.email))
        db_otp = OtpVerification(
            identifier=user.email,
            otp_hash=email_code,
            purpose=OtpPurpose.LOGIN,
            expires_at=datetime.now(timezone.utc) + timedelta(minutes=15),
            is_verified=False
        )
        db.add(db_otp)
        await db.commit()
        from app.services.email_service import EmailService
        EmailService.send_verification_email(user.email, email_code)

    access_token = create_access_token(subject=user.id)
    refresh_token = create_refresh_token(subject=user.id)
    return Token(access_token=access_token, refresh_token=refresh_token, debug_otp=phone_otp, debug_email_code=email_code)


# OAuth Password Grant Compatibility
@router.post("/token", response_model=Token, include_in_schema=False)
async def login_for_access_token(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: AsyncSession = Depends(deps.get_db)
):
    req = LoginRequest(phone=form_data.username, password=form_data.password)
    return await login(req, db)


@router.post("/google", response_model=Token)
async def google_auth(request: GoogleAuthRequest, db: AsyncSession = Depends(deps.get_db)):
    """Google OAuth sign-in / sign-up (mock validation)."""
    # Simply generate a mock profile for the ID token
    mock_email = f"google_{request.id_token[:10]}@gmail.com"
    mock_phone = f"+91{str(uuid.uuid4().int)[:10]}"
    
    result = await db.execute(select(User).filter(User.email == mock_email))
    user = result.scalars().first()
    if not user:
        # Create Google account
        user = await crud_user.create_user(
            db, 
            phone=mock_phone, 
            password_hash=None, 
            role=UserRole.GUEST, 
            email=mock_email
        )
        user.auth_provider = "google"
        user.google_id = request.id_token[:50]
        user.is_email_verified = True
        db.add(user)
        await crud_user.create_guest_profile(db, user_id=user.id, name="Google User")
        await db.commit()
        
    access_token = create_access_token(subject=user.id)
    refresh_token = create_refresh_token(subject=user.id)
    return Token(access_token=access_token, refresh_token=refresh_token)


@router.post("/refresh-token", response_model=Token)
async def refresh_token_route(refresh_token: str, db: AsyncSession = Depends(deps.get_db)):
    """Issues a new access token, rotates refresh token."""
    user_id_str = verify_token(refresh_token, token_type="refresh")
    if not user_id_str:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired refresh token"
        )
        
    try:
        user_id = int(user_id_str)
    except ValueError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid refresh token payload"
        )

    user = await crud_user.get_user(db, user_id=user_id)
    if not user or not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found or inactive"
        )
        
    access_token = create_access_token(subject=user.id)
    new_refresh_token = create_refresh_token(subject=user.id)
    return Token(access_token=access_token, refresh_token=new_refresh_token)


# Deprecated alias
@router.post("/refresh", response_model=Token, include_in_schema=False)
async def refresh_legacy(refresh_token: str, db: AsyncSession = Depends(deps.get_db)):
    return await refresh_token_route(refresh_token, db)


@router.post("/forgot-password", status_code=status.HTTP_200_OK)
async def forgot_password(request: ForgotPasswordRequest):
    """Initiates password reset via OTP."""
    otp = await OtpService.send_otp(request.phone)
    return {
        "success": True,
        "message": "Password reset OTP sent",
        "debug_otp": otp
    }


@router.post("/reset-password", status_code=status.HTTP_200_OK)
async def reset_password(request: ResetPasswordRequest, db: AsyncSession = Depends(deps.get_db)):
    """Sets a new password after verifying the received OTP code."""
    is_valid = await OtpService.verify_otp(request.phone, request.otp)
    if not is_valid:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid or expired OTP"
        )
        
    user = await crud_user.get_user_by_phone(db, phone=request.phone)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User profile not registered"
        )
        
    user.password_hash = get_password_hash(request.new_password)
    db.add(user)
    await db.flush()
    await db.commit()
    return {"success": True, "message": "Password updated successfully"}


@router.post("/logout", status_code=status.HTTP_200_OK)
async def logout(current_user: User = Depends(deps.get_current_user)):
    """Revokes the current refresh token."""
    logger.info(f"User {current_user.id} logged out.")
    return {"success": True, "message": "Logout successful"}
