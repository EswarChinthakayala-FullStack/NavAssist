package com.navassist.android.core.websocket

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

@Singleton
class SocketReconnectPolicy @Inject constructor() {
    private var retryCount = 0

    fun nextDelayMs(): Long? {
        if (retryCount >= SocketConstants.MAX_RETRY_COUNT) return null
        retryCount++
        val delay = SocketConstants.INITIAL_RETRY_DELAY_MS * (2.0.pow(retryCount - 1)).toLong()
        return min(delay, SocketConstants.MAX_RETRY_DELAY_MS)
    }

    fun reset() {
        retryCount = 0
    }

    fun getRetryCount(): Int = retryCount
}
