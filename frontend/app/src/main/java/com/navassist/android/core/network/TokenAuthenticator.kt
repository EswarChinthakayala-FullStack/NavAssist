package com.navassist.android.core.network

import com.navassist.android.data.preferences.SessionDataStore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite refresh loops if auth/refresh itself returned 401
        if (response.request.url.encodedPath.contains("auth/refresh") || response.request.url.encodedPath.contains("auth/login")) {
            return null
        }

        return runBlocking {
            mutex.withLock {
                val currentToken = sessionDataStore.getAccessToken()
                val requestToken = response.request.header(HttpHeaders.AUTHORIZATION)
                    ?.removePrefix(HttpHeaders.BEARER_PREFIX)

                // If token was already refreshed by another concurrent request, retry with new token
                if (currentToken != null && currentToken != requestToken) {
                    return@withLock response.request.newBuilder()
                        .header(HttpHeaders.AUTHORIZATION, "${HttpHeaders.BEARER_PREFIX}$currentToken")
                        .build()
                }

                // Call refresh token endpoint (transparent background refresh)
                val refreshedToken = performTokenRefresh()
                if (refreshedToken != null) {
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
            // Returns refreshed token or null if refresh token expired
            sessionDataStore.getAccessToken()
        } catch (e: Exception) {
            null
        }
    }
}
