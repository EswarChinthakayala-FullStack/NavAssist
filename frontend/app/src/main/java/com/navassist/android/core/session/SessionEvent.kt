package com.navassist.android.core.session

sealed interface SessionEvent {
    data class LoginSuccess(val session: UserSession) : SessionEvent
    object Logout : SessionEvent
    object SessionExpired : SessionEvent
    object TokenRefreshed : SessionEvent
    object RequireAuthentication : SessionEvent
    object RequireBiometric : SessionEvent
}
