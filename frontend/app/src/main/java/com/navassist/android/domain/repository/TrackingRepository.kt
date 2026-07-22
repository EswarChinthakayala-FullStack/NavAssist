package com.navassist.android.domain.repository

import com.navassist.android.core.websocket.SocketEvent
import com.navassist.android.core.websocket.SocketState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TrackingRepository {
    val socketState: StateFlow<SocketState>
    val socketEvents: Flow<SocketEvent>
    fun startTrackingSession(bookingId: Int)
    fun sendLocationUpdate(bookingId: Int, latitude: Double, longitude: Double, heading: Double = 0.0, speed: Double = 0.0)
    fun sendChatMessage(bookingId: Int, messageText: String)
    fun stopTrackingSession()
}
