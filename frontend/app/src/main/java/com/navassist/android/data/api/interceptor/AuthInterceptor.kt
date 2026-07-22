package com.navassist.android.data.api.interceptor

import com.navassist.android.data.preferences.SessionDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = runBlocking { sessionDataStore.getAccessToken() }

        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNull_or_empty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
