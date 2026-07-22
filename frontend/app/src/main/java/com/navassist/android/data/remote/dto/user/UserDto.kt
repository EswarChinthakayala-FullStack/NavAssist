package com.navassist.android.data.remote.dto.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GuestProfileDto(
    @SerialName("user_id") val userId: Int,
    @SerialName("name") val name: String,
    @SerialName("profile_picture_url") val profilePictureUrl: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class AssistantProfileDto(
    @SerialName("user_id") val userId: Int,
    @SerialName("name") val name: String,
    @SerialName("profile_picture_url") val profilePictureUrl: String? = null,
    @SerialName("kyc_status") val kycStatus: String,
    @SerialName("online_status") val onlineStatus: String,
    @SerialName("current_latitude") val currentLatitude: Double? = null,
    @SerialName("current_longitude") val currentLongitude: Double? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class UserResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("phone") val phone: String,
    @SerialName("email") val email: String? = null,
    @SerialName("role") val role: String,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("status") val status: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("profile_photo_url") val profilePhotoUrl: String? = null,
    @SerialName("is_phone_verified") val isPhoneVerified: Boolean,
    @SerialName("is_email_verified") val isEmailVerified: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("guest") val guest: GuestProfileDto? = null,
    @SerialName("assistant") val assistant: AssistantProfileDto? = null
)

@Serializable
data class UpdateProfileRequestDto(
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("profile_photo_url") val profilePhotoUrl: String? = null
)

@Serializable
data class SavedLocationDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("label") val label: String,
    @SerialName("custom_label") val customLabel: String? = null,
    @SerialName("address") val address: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("place_id") val placeId: String? = null
)

@Serializable
data class EmergencyContactDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String,
    @SerialName("phone") val phone: String,
    @SerialName("relationship") val relationship: String? = "Guardian",
    @SerialName("is_primary") val isPrimary: Boolean? = false,
    @SerialName("created_at") val createdAt: String? = null
)
