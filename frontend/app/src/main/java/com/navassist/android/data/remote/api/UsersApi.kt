package com.navassist.android.data.remote.api

import com.navassist.android.data.remote.dto.user.EmergencyContactDto
import com.navassist.android.data.remote.dto.user.SavedLocationDto
import com.navassist.android.data.remote.dto.user.UpdateProfileRequestDto
import com.navassist.android.data.remote.dto.user.UserResponseDto
import retrofit2.http.*

interface UsersApi {
    @GET("users/me")
    suspend fun getMyProfile(): UserResponseDto

    @PATCH("users/me")
    suspend fun updateMyProfile(@Body request: UpdateProfileRequestDto): UserResponseDto

    @DELETE("users/me")
    suspend fun deactivateAccount(): Unit

    @GET("users/me/saved-locations")
    suspend fun getSavedLocations(): List<SavedLocationDto>

    @POST("users/me/saved-locations")
    suspend fun addSavedLocation(@Body request: SavedLocationDto): SavedLocationDto

    @DELETE("users/me/saved-locations/{id}")
    suspend fun deleteSavedLocation(@Path("id") id: Int): Unit

    @GET("users/me/emergency-contacts")
    suspend fun getEmergencyContacts(): List<EmergencyContactDto>

    @POST("users/me/emergency-contacts")
    suspend fun addEmergencyContact(@Body request: EmergencyContactDto): EmergencyContactDto

    @DELETE("users/me/emergency-contacts/{id}")
    suspend fun deleteEmergencyContact(@Path("id") id: Int): Unit
}
