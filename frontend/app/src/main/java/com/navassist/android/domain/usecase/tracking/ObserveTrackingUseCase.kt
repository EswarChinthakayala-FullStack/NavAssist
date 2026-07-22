package com.navassist.android.domain.usecase.tracking

import com.navassist.android.core.websocket.SocketEvent
import com.navassist.android.core.websocket.SocketState
import com.navassist.android.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveTrackingUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository
) {
    val socketState: StateFlow<SocketState> = trackingRepository.socketState
    val socketEvents: Flow<SocketEvent> = trackingRepository.socketEvents

    fun startSession(bookingId: Int) {
        trackingRepository.startTrackingSession(bookingId)
    }

    fun stopSession() {
        trackingRepository.stopTrackingSession()
    }
}
