package com.navassist.android.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.repository.BookingRepository
import com.navassist.android.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _bookingsState = MutableStateFlow<UiState<List<Booking>>>(UiState.Idle)
    val bookingsState: StateFlow<UiState<List<Booking>>> = _bookingsState.asStateFlow()

    fun loadBookings() {
        _bookingsState.value = UiState.Loading
        viewModelScope.launch {
            val result = bookingRepository.getBookings()
            result.onSuccess { list ->
                _bookingsState.value = UiState.Success(list)
            }.onFailure { error ->
                _bookingsState.value = UiState.Error(error.message ?: "Failed to load bookings")
            }
        }
    }
}
