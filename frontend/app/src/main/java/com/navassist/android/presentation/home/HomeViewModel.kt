package com.navassist.android.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.domain.model.SavedLocation
import com.navassist.android.domain.model.User
import com.navassist.android.domain.repository.BookingRepository
import com.navassist.android.domain.repository.UserRepository
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UiState<User>>(UiState.Loading)
    val userProfile: StateFlow<UiState<User>> = _userProfile.asStateFlow()

    val savedLocations: StateFlow<List<SavedLocation>> = userRepository.getSavedLocations()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _bookingState = MutableStateFlow<UiState<Booking>>(UiState.Idle)
    val bookingState: StateFlow<UiState<Booking>> = _bookingState.asStateFlow()

    init {
        loadUserProfile()
        refreshSavedLocations()
    }

    fun loadUserProfile() {
        _userProfile.value = UiState.Loading
        viewModelScope.launch {
            val result = userRepository.getMyProfile()
            result.onSuccess { user ->
                _userProfile.value = UiState.Success(user)
            }.onFailure { error ->
                _userProfile.value = UiState.Error(error.message ?: "Failed to load user profile")
            }
        }
    }

    fun refreshSavedLocations() {
        viewModelScope.launch {
            userRepository.refreshSavedLocations()
        }
    }

    fun requestAssistance(
        pickupLat: Double,
        pickupLng: Double,
        pickupAddress: String,
        destLat: Double,
        destLng: Double,
        destAddress: String,
        estimatedFare: Double
    ) {
        _bookingState.value = UiState.Loading
        viewModelScope.launch {
            val pickup = LocationPoint(pickupLat, pickupLng, pickupAddress)
            val destination = LocationPoint(destLat, destLng, destAddress)
            val result = bookingRepository.createBooking(pickup, destination, estimatedFare)
            result.onSuccess { booking ->
                _bookingState.value = UiState.Success(booking)
            }.onFailure { error ->
                _bookingState.value = UiState.Error(error.message ?: "Failed to request assistance")
            }
        }
    }
}
