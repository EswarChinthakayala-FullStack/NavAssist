package com.navassist.android.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.repository.BookingRepository
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _bookingState = MutableStateFlow<UiState<Booking>>(UiState.Idle)
    val bookingState: StateFlow<UiState<Booking>> = _bookingState.asStateFlow()

    private val _cancelState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val cancelState: StateFlow<UiState<Unit>> = _cancelState.asStateFlow()

    fun loadTripDetail(bookingId: String) {
        if (bookingId.isBlank()) {
            _bookingState.value = UiState.Error("Invalid Booking ID")
            return
        }

        _bookingState.value = UiState.Loading
        viewModelScope.launch {
            val result = bookingRepository.getBookingById(bookingId)
            result.onSuccess { booking ->
                _bookingState.value = UiState.Success(booking)
            }.onFailure { error ->
                _bookingState.value = UiState.Error(error.message ?: "Failed to load trip details")
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        _cancelState.value = UiState.Loading
        viewModelScope.launch {
            val result = bookingRepository.cancelBooking(bookingId)
            result.onSuccess { updatedBooking ->
                _cancelState.value = UiState.Success(Unit)
                _bookingState.value = UiState.Success(updatedBooking)
            }.onFailure { error ->
                _cancelState.value = UiState.Error(error.message ?: "Failed to cancel booking")
            }
        }
    }
}
