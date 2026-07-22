package com.navassist.android.core.session

import com.navassist.android.data.preferences.SessionDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val sessionValidator: SessionValidator
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    private val _sessionEvents = MutableSharedFlow<SessionEvent>(replay = 0)
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        scope.launch {
            combine(
                sessionDataStore.userId,
                sessionDataStore.userRole,
                sessionDataStore.accessToken
            ) { userId, role, token ->
                Triple(userId, role, token)
            }.collect { (userId, role, token) ->
                if (!userId.isNull_or_empty() && !role.isNull_or_empty() && sessionValidator.isValidToken(token)) {
                    val session = UserSession(userId = userId!!, role = role!!)
                    _userSession.value = session
                    _isAuthenticated.value = true
                    _authState.value = AuthState.Authenticated(session)
                } else {
                    _userSession.value = null
                    _isAuthenticated.value = false
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    suspend fun onLoginSuccess(userId: String, role: String, accessToken: String, refreshToken: String) {
        sessionDataStore.saveTokens(accessToken, refreshToken)
        sessionDataStore.saveUserSession(userId, role)
        val session = UserSession(userId = userId, role = role)
        _userSession.value = session
        _isAuthenticated.value = true
        _authState.value = AuthState.Authenticated(session)
        _sessionEvents.emit(SessionEvent.LoginSuccess(session))
    }

    suspend fun logout() {
        sessionDataStore.clearSession()
        _userSession.value = null
        _isAuthenticated.value = false
        _authState.value = AuthState.Unauthenticated
        _sessionEvents.emit(SessionEvent.Logout)
    }

    suspend fun onSessionExpired() {
        sessionDataStore.clearSession()
        _userSession.value = null
        _isAuthenticated.value = false
        _authState.value = AuthState.Expired
        _sessionEvents.emit(SessionEvent.SessionExpired)
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
