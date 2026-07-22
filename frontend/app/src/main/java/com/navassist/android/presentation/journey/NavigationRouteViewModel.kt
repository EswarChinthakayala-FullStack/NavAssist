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

data class NavigationRouteData(
    val bookingId: Int,
    val destinationAddress: String,
    val currentLat: Double,
    val currentLng: Double,
    val etaMinutes: Int,
    val distanceRemainingKm: Double,
    val speedKmH: Int
)

@HiltViewModel
class NavigationRouteViewModel @Inject constructor(
    private val socketClient: TrackingSocketClient
) : ViewModel() {

    private val _routeDataState = MutableStateFlow<UiState<NavigationRouteData>>(UiState.Loading)
    val routeDataState: StateFlow<UiState<NavigationRouteData>> = _routeDataState.asStateFlow()

    val socketState: StateFlow<SocketState> = socketClient.socketState
    val socketEvents: SharedFlow<SocketEvent> = socketClient.socketEvents

    fun startNavigation(bookingId: Int) {
        _routeDataState.value = UiState.Loading
        viewModelScope.launch {
            val initial = NavigationRouteData(
                bookingId = bookingId,
                destinationAddress = "International Airport, Terminal 2 Entrance",
                currentLat = 37.7749,
                currentLng = -122.4194,
                etaMinutes = 14,
                distanceRemainingKm = 5.2,
                speedKmH = 34
            )
            _routeDataState.value = UiState.Success(initial)
            socketClient.connect(bookingId)
        }
    }
}
