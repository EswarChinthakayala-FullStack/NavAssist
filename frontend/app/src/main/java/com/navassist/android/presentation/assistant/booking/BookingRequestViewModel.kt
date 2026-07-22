package com.navassist.android.presentation.assistant.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.data.remote.api.BookingsApi
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.BookingStatus
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BookingRequestEffect {
    data class ShowToast(val message: String) : BookingRequestEffect
    data class ShowSnackbar(val message: String) : BookingRequestEffect
    data class NavigateToJourney(val bookingId: String) : BookingRequestEffect
    object DismissBottomSheet : BookingRequestEffect
}

@HiltViewModel
class BookingRequestViewModel @Inject constructor(
    private val bookingsApi: BookingsApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _bookingState = MutableStateFlow<UiState<Booking>>(UiState.Loading)
    val bookingState: StateFlow<UiState<Booking>> = _bookingState.asStateFlow()

    private val _isExpired = MutableStateFlow(false)
    val isExpired: StateFlow<Boolean> = _isExpired.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _effects = MutableSharedFlow<BookingRequestEffect>()
    val effects: SharedFlow<BookingRequestEffect> = _effects.asSharedFlow()

    private var currentBookingId: String? = savedStateHandle.get<String>("booking_id")

    init {
        currentBookingId?.let { id ->
            loadBookingDetails(id)
        }
    }

    fun loadBookingDetails(bookingId: String) {
        currentBookingId = bookingId
        _bookingState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val idInt = bookingId.toIntOrNull() ?: 0
                val dto = bookingsApi.getBookingById(idInt)
                val booking = Booking(
                    id = dto.id.toString(),
                    guestId = dto.guestId.toString(),
                    assistantId = dto.assistantId?.toString(),
                    guestName = dto.guestName ?: "Passenger",
                    guestPhoto = dto.guestAvatar,
                    guestPhone = dto.guestPhone,
                    pickupLocation = LocationPoint(dto.pickupLatitude, dto.pickupLongitude, dto.pickupAddress),
                    destinationLocation = LocationPoint(dto.destinationLatitude, dto.destinationLongitude, dto.destinationAddress),
                    status = BookingStatus.PENDING,
                    fare = dto.fareAmount,
                    currency = "INR",
                    createdAt = dto.createdAt
                )
                _bookingState.value = UiState.Success(booking)
            } catch (e: Exception) {
                _bookingState.value = UiState.Error(e.message ?: "Failed to load booking details")
            }
        }
    }

    fun acceptBooking() {
        val id = currentBookingId ?: return
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val idInt = id.toIntOrNull() ?: 0
                bookingsApi.acceptBooking(idInt)
                _isProcessing.value = false
                _effects.emit(BookingRequestEffect.ShowToast("Ride Accepted! Transitioning to Journey..."))
                _effects.emit(BookingRequestEffect.NavigateToJourney(id))
            } catch (e: Exception) {
                _isProcessing.value = false
                _effects.emit(BookingRequestEffect.ShowSnackbar("Error accepting ride: ${e.message}"))
            }
        }
    }

    fun rejectBooking() {
        val id = currentBookingId ?: return
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val idInt = id.toIntOrNull() ?: 0
                bookingsApi.rejectBooking(idInt)
                _isProcessing.value = false
                _effects.emit(BookingRequestEffect.ShowToast("Booking request declined"))
                _effects.emit(BookingRequestEffect.DismissBottomSheet)
            } catch (e: Exception) {
                _isProcessing.value = false
                _effects.emit(BookingRequestEffect.DismissBottomSheet)
            }
        }
    }

    fun onCountdownExpired() {
        _isExpired.value = true
        viewModelScope.launch {
            _effects.emit(BookingRequestEffect.ShowToast("Booking request expired"))
        }
    }
}
