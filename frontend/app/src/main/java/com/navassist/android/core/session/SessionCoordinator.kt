package com.navassist.android.core.session

import com.navassist.android.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionCoordinator @Inject constructor(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository
) {
    suspend fun login(userId: String, role: String, accessToken: String, refreshToken: String) {
        sessionManager.onLoginSuccess(userId, role, accessToken, refreshToken)
    }

    suspend fun logout() {
        authRepository.logout()
        sessionManager.logout()
    }

    suspend fun handleForcedLogout() {
        sessionManager.onSessionExpired()
    }
}
