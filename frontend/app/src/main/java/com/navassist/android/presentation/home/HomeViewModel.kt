package com.navassist.android.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.domain.model.SavedLocation
import com.navassist.android.domain.model.User
import com.navassist.android.domain.model.UserRole
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
import org.maplibre.android.geometry.LatLng
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

    // Live location state
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    private val _currentLocationName = MutableStateFlow("Detecting location…")
    val currentLocationName: StateFlow<String> = _currentLocationName.asStateFlow()

    private val _currentLocationAccuracy = MutableStateFlow<Float?>(null)
    val currentLocationAccuracy: StateFlow<Float?> = _currentLocationAccuracy.asStateFlow()

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
                // Fallback to Traveler guest profile when unauthenticated
                _userProfile.value = UiState.Success(
                    User(
                        id = "guest",
                        fullName = "Traveler",
                        email = "guest@navassist.com",
                        phone = "",
                        role = UserRole.GUEST,
                        profilePictureUrl = null,
                        rating = 5.0f
                    )
                )
            }
        }
    }

    fun updateLocation(latLng: LatLng, address: String, accuracy: Float?) {
        _currentLocation.value = latLng
        _currentLocationName.value = address
        _currentLocationAccuracy.value = accuracy
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
