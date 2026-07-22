package com.navassist.android.core.network

import com.navassist.android.BuildConfig
import com.navassist.android.data.preferences.SessionDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = runBlocking { sessionDataStore.getAccessToken() }

        val requestBuilder = originalRequest.newBuilder()
            .header(HttpHeaders.ACCEPT, NetworkConstants.MEDIA_TYPE_JSON)
            .header(HttpHeaders.CONTENT_TYPE, NetworkConstants.MEDIA_TYPE_JSON)
            .header(HttpHeaders.ACCEPT_LANGUAGE, Locale.getDefault().language)
            .header(HttpHeaders.X_REQUEST_ID, RequestIdGenerator.generate())
            .header(HttpHeaders.X_APP_VERSION, BuildConfig.VERSION_NAME)
            .header(HttpHeaders.X_PLATFORM, "Android")
            .header(HttpHeaders.X_TIMEZONE, TimeZone.getDefault().id)

        if (!token.isNull_or_empty()) {
            requestBuilder.header(HttpHeaders.AUTHORIZATION, "${HttpHeaders.BEARER_PREFIX}$token")
        }

        return chain.proceed(requestBuilder.build())
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
