package com.navassist.android.presentation.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CompletedTripData(
    val bookingId: Int,
    val pickupAddress: String,
    val destinationAddress: String,
    val assistantName: String,
    val fareTotal: String,
    val paymentStatus: String = "PAID"
)

@HiltViewModel
class JourneyCompletedViewModel @Inject constructor() : ViewModel() {

    private val _tripState = MutableStateFlow<UiState<CompletedTripData>>(UiState.Loading)
    val tripState: StateFlow<UiState<CompletedTripData>> = _tripState.asStateFlow()

    fun loadCompletedTrip(bookingId: Int) {
        _tripState.value = UiState.Loading
        viewModelScope.launch {
            val mockTrip = CompletedTripData(
                bookingId = bookingId,
                pickupAddress = "Central Station, Main Entrance",
                destinationAddress = "International Airport, Terminal 2",
                assistantName = "Vikram Sharma",
                fareTotal = "$48.50"
            )
            _tripState.value = UiState.Success(mockTrip)
        }
    }
}
