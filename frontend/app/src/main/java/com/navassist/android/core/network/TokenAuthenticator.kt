package com.navassist.android.core.network

import com.navassist.android.data.preferences.SessionDataStore
import com.navassist.android.data.remote.api.AuthApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val authApiProvider: Provider<AuthApi>
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite refresh loops if auth/refresh itself returned 401
        val path = response.request.url.encodedPath
        if (path.contains("auth/refresh") || path.contains("auth/login") || path.contains("auth/verify-otp")) {
            return null
        }

        // Limit retry count to 1 to strictly prevent infinite 401 loops
        if (responseCount(response) >= 2) {
            return null
        }

        return runBlocking {
            mutex.withLock {
                val currentToken = sessionDataStore.getAccessToken()
                val requestToken = response.request.header(HttpHeaders.AUTHORIZATION)
                    ?.removePrefix(HttpHeaders.BEARER_PREFIX)

                // If token was already refreshed by another concurrent request, retry with new token
                if (!currentToken.isNullOrEmpty() && currentToken != requestToken) {
                    return@withLock response.request.newBuilder()
                        .header(HttpHeaders.AUTHORIZATION, "${HttpHeaders.BEARER_PREFIX}$currentToken")
                        .build()
                }

                // Call refresh token endpoint (transparent background refresh)
                val refreshedToken = performTokenRefresh()
                if (!refreshedToken.isNullOrEmpty() && refreshedToken != requestToken) {
                    response.request.newBuilder()
                        .header(HttpHeaders.AUTHORIZATION, "${HttpHeaders.BEARER_PREFIX}$refreshedToken")
                        .build()
                } else {
                    sessionDataStore.clearSession()
                    null
                }
            }
        }
    }

    private suspend fun performTokenRefresh(): String? {
        return try {
            val refreshTok = sessionDataStore.getRefreshToken()
            if (refreshTok.isNullOrEmpty()) {
                sessionDataStore.clearSession()
                return null
            }

            val tokenResponse = authApiProvider.get().refreshToken()
            val newAccess = tokenResponse.accessToken
            val newRefresh = tokenResponse.refreshToken ?: refreshTok

            if (!newAccess.isNullOrEmpty()) {
                sessionDataStore.saveTokens(newAccess, newRefresh)
                newAccess
            } else {
                sessionDataStore.clearSession()
                null
            }
        } catch (e: Exception) {
            sessionDataStore.clearSession()
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }
}
