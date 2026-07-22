package com.navassist.android.presentation.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.core.websocket.SocketEvent
import com.navassist.android.core.websocket.SocketState
import com.navassist.android.core.websocket.TrackingSocketClient
import com.navassist.android.domain.model.Assistant
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssignedBookingData(
    val bookingId: Int,
    val assistantName: String,
    val assistantAvatarUrl: String,
    val rating: Double,
    val totalTrips: Int,
    val status: String,
    val etaMinutes: Int
)

@HiltViewModel
class AssistantAssignedViewModel @Inject constructor(
    private val socketClient: TrackingSocketClient
) : ViewModel() {

    private val _assignedState = MutableStateFlow<UiState<AssignedBookingData>>(UiState.Loading)
    val assignedState: StateFlow<UiState<AssignedBookingData>> = _assignedState.asStateFlow()

    val socketState: StateFlow<SocketState> = socketClient.socketState

    fun loadAssignedBooking(bookingId: Int) {
        _assignedState.value = UiState.Loading
        viewModelScope.launch {
            val mockData = AssignedBookingData(
                bookingId = bookingId,
                assistantName = "Vikram Sharma",
                assistantAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                rating = 4.9,
                totalTrips = 540,
                status = "Preparing",
                etaMinutes = 8
            )
            _assignedState.value = UiState.Success(mockData)
            socketClient.connect(bookingId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        socketClient.disconnect()
    }
}
