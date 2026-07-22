package com.navassist.android.domain.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val profilePictureUrl: String? = null,
    val isVerified: Boolean = false,
    val rating: Float = 5.0f
)

enum class UserRole {
    GUEST,
    ASSISTANT,
    ADMIN
}
