from typing import List
from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.api import deps
from app.models.user import User
from app.schemas.user import (
    UserOut,
    UserUpdate,
    SavedLocationIn,
    SavedLocationOut,
    EmergencyContactIn,
    EmergencyContactOut
)
from app.services.user_service import UserService

router = APIRouter()


@router.get("/me", response_model=UserOut)
async def get_my_profile(
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Fetches full profile information of the currently authenticated user."""
    return await UserService.get_user_profile(db, user_id=current_user.id)


@router.patch("/me", response_model=UserOut)
async def update_my_profile(
    request: UserUpdate,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Updates profile attributes for the logged-in user."""
    user = await UserService.update_user_profile(db, user_id=current_user.id, update_data=request)
    await db.commit()
    return user


@router.delete("/me", status_code=status.HTTP_200_OK)
async def deactivate_my_account(
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Soft-deletes/deactivates the caller's active user account."""
    await UserService.delete_user_account(db, user_id=current_user.id)
    await db.commit()
    return {"success": True, "message": "User account deactivated successfully"}


# Saved Locations Bookmarks
@router.get("/saved-locations", response_model=List[SavedLocationOut], include_in_schema=False)
@router.get("/me/saved-locations", response_model=List[SavedLocationOut])
async def list_my_saved_locations(
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Lists saved Home/Office/Favorite locations."""
    return await UserService.list_saved_locations(db, user_id=current_user.id)


@router.post("/saved-locations", response_model=SavedLocationOut, status_code=status.HTTP_201_CREATED, include_in_schema=False)
@router.post("/me/saved-locations", response_model=SavedLocationOut, status_code=status.HTTP_201_CREATED)
async def create_my_saved_location(
    request: SavedLocationIn,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Adds a saved location bookmark."""
    location = await UserService.add_saved_location(db, user_id=current_user.id, loc_in=request)
    await db.commit()
    await db.refresh(location)
    return location


@router.patch("/saved-locations/{id}", response_model=SavedLocationOut, include_in_schema=False)
@router.patch("/me/saved-locations/{id}", response_model=SavedLocationOut)
async def edit_my_saved_location(
    id: int,
    request: SavedLocationIn,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Edits an existing saved location bookmark."""
    location = await UserService.edit_saved_location(db, location_id=id, user_id=current_user.id, loc_in=request)
    await db.commit()
    await db.refresh(location)
    return location


@router.delete("/saved-locations/{id}", status_code=status.HTTP_200_OK, include_in_schema=False)
@router.delete("/me/saved-locations/{id}", status_code=status.HTTP_200_OK)
async def remove_my_saved_location(
    id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Removes a saved location bookmark."""
    await UserService.remove_saved_location(db, location_id=id, user_id=current_user.id)
    await db.commit()
    return {"success": True, "message": "Saved location bookmark deleted"}


# Emergency Contacts
@router.get("/emergency-contacts", response_model=List[EmergencyContactOut], include_in_schema=False)
@router.get("/me/emergency-contacts", response_model=List[EmergencyContactOut])
async def list_my_emergency_contacts(
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Lists registered emergency safety contacts."""
    return await UserService.list_emergency_contacts(db, user_id=current_user.id)


@router.post("/emergency-contacts", response_model=EmergencyContactOut, status_code=status.HTTP_201_CREATED, include_in_schema=False)
@router.post("/me/emergency-contacts", response_model=EmergencyContactOut, status_code=status.HTTP_201_CREATED)
async def create_my_emergency_contact(
    request: EmergencyContactIn,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Adds a new emergency contact."""
    contact = await UserService.register_emergency_contact(
        db, 
        user_id=current_user.id, 
        name=request.name, 
        phone=request.phone
    )
    await db.commit()
    return contact


@router.delete("/emergency-contacts/{id}", status_code=status.HTTP_200_OK, include_in_schema=False)
@router.delete("/me/emergency-contacts/{id}", status_code=status.HTTP_200_OK)
async def remove_my_emergency_contact(
    id: int,
    current_user: User = Depends(deps.get_current_user),
    db: AsyncSession = Depends(deps.get_db)
):
    """Removes an emergency contact."""
    await UserService.remove_emergency_contact(db, contact_id=id, user_id=current_user.id)
    await db.commit()
    return {"success": True, "message": "Emergency contact deleted successfully"}
