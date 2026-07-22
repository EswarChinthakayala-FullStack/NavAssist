package com.navassist.android.core.logger

import timber.log.Timber

object AppLogger {
    fun d(message: String, vararg args: Any?) {
        Timber.d(message, *args)
    }

    fun e(throwable: Throwable?, message: String, vararg args: Any?) {
        Timber.e(throwable, message, *args)
    }

    fun i(message: String, vararg args: Any?) {
        Timber.i(message, *args)
    }
}
