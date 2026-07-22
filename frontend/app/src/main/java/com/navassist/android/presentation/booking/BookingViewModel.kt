package com.navassist.android.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.Assistant
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.domain.repository.BookingRepository
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _pickupLocation = MutableStateFlow<LocationPoint?>(null)
    val pickupLocation: StateFlow<LocationPoint?> = _pickupLocation.asStateFlow()

    private val _destinationLocation = MutableStateFlow<LocationPoint?>(null)
    val destinationLocation: StateFlow<LocationPoint?> = _destinationLocation.asStateFlow()

    private val _scheduledAt = MutableStateFlow<Long?>(null)
    val scheduledAt: StateFlow<Long?> = _scheduledAt.asStateFlow()

    private val _selectedAssistant = MutableStateFlow<Assistant?>(null)
    val selectedAssistant: StateFlow<Assistant?> = _selectedAssistant.asStateFlow()

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _estimatedFare = MutableStateFlow(0.0)
    val estimatedFare: StateFlow<Double> = _estimatedFare.asStateFlow()

    private val _bookingState = MutableStateFlow<UiState<Booking>>(UiState.Idle)
    val bookingState: StateFlow<UiState<Booking>> = _bookingState.asStateFlow()

    fun resetBooking() {
        _pickupLocation.value = null
        _destinationLocation.value = null
        _scheduledAt.value = null
        _selectedAssistant.value = null
        _currentStep.value = 1
        _estimatedFare.value = 0.0
        _bookingState.value = UiState.Idle
    }

    fun setPickup(lat: Double, lng: Double, address: String) {
        _pickupLocation.value = LocationPoint(lat, lng, address)
    }

    fun setDestination(lat: Double, lng: Double, address: String) {
        _destinationLocation.value = LocationPoint(lat, lng, address)
    }

    fun setScheduledAt(timestamp: Long?) {
        _scheduledAt.value = timestamp
    }

    fun selectAssistant(assistant: Assistant) {
        _selectedAssistant.value = assistant
    }

    fun setStep(step: Int) {
        _currentStep.value = step
    }

    fun createBooking() {
        val pickup = _pickupLocation.value ?: return
        val dest = _destinationLocation.value ?: return

        _bookingState.value = UiState.Loading
        viewModelScope.launch {
            val result = bookingRepository.createBooking(pickup, dest, _estimatedFare.value)
            result.onSuccess { booking ->
                _bookingState.value = UiState.Success(booking)
            }.onFailure { error ->
                _bookingState.value = UiState.Error(error.message ?: "Failed to complete booking")
            }
        }
    }
}
