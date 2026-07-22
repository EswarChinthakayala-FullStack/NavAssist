package com.navassist.android.core.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val client: TrackingSocketClient
) {
    val socketState: StateFlow<SocketState> = client.socketState
    val socketEvents: Flow<SocketEvent> = client.socketEvents

    fun startTrackingSession(bookingId: Int) {
        client.connect(bookingId)
    }

    fun sendLocationUpdate(bookingId: Int, lat: Double, lng: Double, heading: Double = 0.0, speed: Double = 0.0) {
        client.sendLocation(bookingId, lat, lng, heading, speed)
    }

    fun sendChatMessage(bookingId: Int, text: String) {
        client.sendMessage(bookingId, text)
    }

    fun stopTrackingSession() {
        client.disconnect()
    }
}
