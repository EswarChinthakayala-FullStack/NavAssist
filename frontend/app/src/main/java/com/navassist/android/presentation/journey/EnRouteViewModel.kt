package com.navassist.android.presentation.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.core.websocket.SocketEvent
import com.navassist.android.core.websocket.SocketState
import com.navassist.android.core.websocket.TrackingSocketClient
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveTrackingData(
    val bookingId: Int,
    val assistantLat: Double,
    val assistantLng: Double,
    val status: String,
    val etaMinutes: Int,
    val distanceKm: Double,
    val speedKmH: Int
)

@HiltViewModel
class EnRouteViewModel @Inject constructor(
    private val socketClient: TrackingSocketClient
) : ViewModel() {

    private val _trackingDataState = MutableStateFlow<UiState<LiveTrackingData>>(UiState.Loading)
    val trackingDataState: StateFlow<UiState<LiveTrackingData>> = _trackingDataState.asStateFlow()

    val socketState: StateFlow<SocketState> = socketClient.socketState
    val socketEvents: SharedFlow<SocketEvent> = socketClient.socketEvents

    fun startTracking(bookingId: Int) {
        _trackingDataState.value = UiState.Loading
        viewModelScope.launch {
            val initialData = LiveTrackingData(
                bookingId = bookingId,
                assistantLat = 37.7749,
                assistantLng = -122.4194,
                status = "On The Way",
                etaMinutes = 8,
                distanceKm = 2.4,
                speedKmH = 28
            )
            _trackingDataState.value = UiState.Success(initialData)
            socketClient.connect(bookingId)
        }
    }
}
