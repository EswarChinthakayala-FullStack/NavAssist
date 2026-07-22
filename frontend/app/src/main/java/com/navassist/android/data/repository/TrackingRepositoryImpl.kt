package com.navassist.android.data.repository

import com.navassist.android.core.websocket.SocketEvent
import com.navassist.android.core.websocket.SocketManager
import com.navassist.android.core.websocket.SocketState
import com.navassist.android.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepositoryImpl @Inject constructor(
    private val socketManager: SocketManager
) : TrackingRepository {

    override val socketState: StateFlow<SocketState> = socketManager.socketState
    override val socketEvents: Flow<SocketEvent> = socketManager.socketEvents

    override fun startTrackingSession(bookingId: Int) {
        socketManager.startTrackingSession(bookingId)
    }

    override fun sendLocationUpdate(
        bookingId: Int,
        latitude: Double,
        longitude: Double,
        heading: Double,
        speed: Double
    ) {
        socketManager.sendLocationUpdate(bookingId, latitude, longitude, heading, speed)
    }

    override fun sendChatMessage(bookingId: Int, messageText: String) {
        socketManager.sendChatMessage(bookingId, messageText)
    }

    override fun stopTrackingSession() {
        socketManager.stopTrackingSession()
    }
}
