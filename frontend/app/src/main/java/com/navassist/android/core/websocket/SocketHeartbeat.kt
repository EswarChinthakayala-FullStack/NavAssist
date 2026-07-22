package com.navassist.android.core.websocket

import kotlinx.coroutines.*
import okhttp3.WebSocket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketHeartbeat @Inject constructor() {
    private var pingJob: Job? = null

    fun start(webSocket: WebSocket, scope: CoroutineScope) {
        stop()
        pingJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(SocketConstants.PING_INTERVAL_SECONDS * 1000L)
                val pingFrame = """{"event":"ping"}"""
                webSocket.send(pingFrame)
            }
        }
    }

    fun stop() {
        pingJob?.cancel()
        pingJob = null
    }
}
