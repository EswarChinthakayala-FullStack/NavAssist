package com.navassist.android.core.session

data class UserSession(
    val userId: String,
    val role: String,
    val email: String? = null,
    val name: String? = null
)

sealed interface AuthState {
    object Loading : AuthState
    data class Authenticated(val session: UserSession) : AuthState
    object Refreshing : AuthState
    object Unauthenticated : AuthState
    object Expired : AuthState
    object Locked : AuthState
}
