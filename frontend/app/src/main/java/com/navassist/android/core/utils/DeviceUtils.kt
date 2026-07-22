package com.navassist.android.core.utils

import android.os.Build

object DeviceUtils {
    fun getDeviceModel(): String = "${Build.MANUFACTURER} ${Build.MODEL}"
    fun getAndroidVersion(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
}
