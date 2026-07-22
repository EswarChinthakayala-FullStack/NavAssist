package com.navassist.android.core.network

object NetworkConstants {
    const val TIMEOUT_CONNECT_SECONDS = 30L
    const val TIMEOUT_READ_SECONDS = 30L
    const val TIMEOUT_WRITE_SECONDS = 30L

    const val MAX_RETRY_COUNT = 3
    const val CACHE_SIZE_BYTES = 10L * 1024L * 1024L // 10 MB

    const val MEDIA_TYPE_JSON = "application/json"
    const val API_VERSION_PREFIX = "api/v1/"
}
