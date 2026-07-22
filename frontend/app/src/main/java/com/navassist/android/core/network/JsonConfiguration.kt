package com.navassist.android.core.network

import com.navassist.android.BuildConfig
import kotlinx.serialization.json.Json

object JsonConfiguration {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        isLenient = true
        prettyPrint = BuildConfig.DEBUG
    }
}
