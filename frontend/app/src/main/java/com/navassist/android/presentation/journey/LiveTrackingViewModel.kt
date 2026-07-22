package com.navassist.android.presentation.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.core.websocket.SocketEvent
import com.navassist.android.core.websocket.SocketState
import com.navassist.android.core.websocket.TrackingSocketClient
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackingStateData(
    val bookingId: Int,
    val assistantLat: Double,
    val assistantLng: Double,
    val passengerLat: Double,
    val passengerLng: Double,
    val etaMinutes: Int,
    val distanceKm: Double,
    val speedKmH: Int
)

@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    private val socketClient: TrackingSocketClient
) : ViewModel() {

    private val _trackingState = MutableStateFlow<UiState<TrackingStateData>>(UiState.Loading)
    val trackingState: StateFlow<UiState<TrackingStateData>> = _trackingState.asStateFlow()

    val socketState: StateFlow<SocketState> = socketClient.socketState
    val socketEvents: SharedFlow<SocketEvent> = socketClient.socketEvents

    private var pollingJob: Job? = null

    fun startLiveTracking(bookingId: Int) {
        _trackingState.value = UiState.Loading
        viewModelScope.launch {
            val initial = TrackingStateData(
                bookingId = bookingId,
                assistantLat = 37.7749,
                assistantLng = -122.4194,
                passengerLat = 37.7739,
                passengerLng = -122.4180,
                etaMinutes = 8,
                distanceKm = 2.4,
                speedKmH = 28
            )
            _trackingState.value = UiState.Success(initial)
            socketClient.connect(bookingId)
            observeSocketState(bookingId)
        }
    }

    private fun observeSocketState(bookingId: Int) {
        viewModelScope.launch {
            socketState.collect { state ->
                if (state is SocketState.Disconnected || state is SocketState.Error) {
                    startRestPollingFallback(bookingId)
                } else if (state is SocketState.Connected) {
                    stopRestPollingFallback()
                }
            }
        }
    }

    private fun startRestPollingFallback(bookingId: Int) {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(10000)
                // Rest fallback fetch
            }
        }
    }

    private fun stopRestPollingFallback() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopRestPollingFallback()
        socketClient.disconnect()
    }
}
