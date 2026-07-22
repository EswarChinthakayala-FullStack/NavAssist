package com.navassist.android.presentation.assistant.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.AssistantDashboardStats
import com.navassist.android.domain.model.AssistantProfileData
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.TodayEarnings
import com.navassist.android.domain.repository.AssistantRepository
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

sealed interface AssistantHomeEffect {
    data class ShowToast(val message: String) : AssistantHomeEffect
    data class ShowSnackbar(val message: String) : AssistantHomeEffect
    data class NavigateToTrip(val bookingId: String) : AssistantHomeEffect
}

@HiltViewModel
class AssistantHomeViewModel @Inject constructor(
    private val assistantRepository: AssistantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AssistantProfileData>>(UiState.Loading)
    val uiState: StateFlow<UiState<AssistantProfileData>> = _uiState.asStateFlow()

    private val _dashboardStats = MutableStateFlow(AssistantDashboardStats())
    val dashboardStats: StateFlow<AssistantDashboardStats> = _dashboardStats.asStateFlow()

    private val _todayEarnings = MutableStateFlow(TodayEarnings())
    val todayEarnings: StateFlow<TodayEarnings> = _todayEarnings.asStateFlow()

    private val _incomingBookings = MutableStateFlow<List<Booking>>(emptyList())
    val incomingBookings: StateFlow<List<Booking>> = _incomingBookings.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _effects = MutableSharedFlow<AssistantHomeEffect>()
    val effects: SharedFlow<AssistantHomeEffect> = _effects.asSharedFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val profileRes = assistantRepository.getMyAssistantProfile()
            profileRes.onSuccess { profile ->
                _uiState.value = UiState.Success(profile)
                _isOnline.value = profile.isOnline
            }.onFailure { err ->
                _uiState.value = UiState.Error(err.message ?: "Failed to load assistant profile")
            }

            fetchStats()
            fetchEarnings()
            fetchIncomingBookings()
        }
    }

    fun toggleOnlineStatus(targetOnline: Boolean) {
        val previousState = _isOnline.value
        _isOnline.value = targetOnline
        viewModelScope.launch {
            val result = assistantRepository.toggleOnlineStatus(targetOnline)
            result.onSuccess { actualOnline ->
                _isOnline.value = actualOnline
                val msg = if (actualOnline) "You are now ONLINE. Ready for requests!" else "You are now OFFLINE."
                _effects.emit(AssistantHomeEffect.ShowToast(msg))
                if (actualOnline) {
                    fetchIncomingBookings()
                } else {
                    _incomingBookings.value = emptyList()
                }
            }.onFailure { err ->
                _isOnline.value = previousState
                _effects.emit(AssistantHomeEffect.ShowSnackbar("Failed to update status: ${err.message}"))
            }
        }
    }

    fun fetchIncomingBookings() {
        if (!_isOnline.value) return
        viewModelScope.launch {
            val result = assistantRepository.getIncomingBookings()
            result.onSuccess { list ->
                _incomingBookings.value = list
            }
        }
    }

    fun fetchStats() {
        viewModelScope.launch {
            val result = assistantRepository.getDashboardStats()
            result.onSuccess { stats ->
                _dashboardStats.value = stats
            }
        }
    }

    fun fetchEarnings() {
        viewModelScope.launch {
            val result = assistantRepository.getTodayEarnings()
            result.onSuccess { earnings ->
                _todayEarnings.value = earnings
            }
        }
    }

    fun acceptBooking(booking: Booking) {
        viewModelScope.launch {
            val bookingIdInt = booking.id.toIntOrNull() ?: 0
            val result = assistantRepository.acceptBooking(bookingIdInt)
            result.onSuccess { accepted ->
                _effects.emit(AssistantHomeEffect.ShowToast("Booking Accepted! Navigating to Pickup..."))
                _incomingBookings.value = _incomingBookings.value.filter { it.id != booking.id }
                _effects.emit(AssistantHomeEffect.NavigateToTrip(accepted.id))
            }.onFailure { err ->
                _effects.emit(AssistantHomeEffect.ShowSnackbar("Could not accept booking: ${err.message}"))
            }
        }
    }

    fun rejectBooking(booking: Booking) {
        viewModelScope.launch {
            val bookingIdInt = booking.id.toIntOrNull() ?: 0
            val result = assistantRepository.rejectBooking(bookingIdInt)
            result.onSuccess {
                _effects.emit(AssistantHomeEffect.ShowToast("Booking request declined"))
                _incomingBookings.value = _incomingBookings.value.filter { it.id != booking.id }
            }.onFailure { err ->
                _effects.emit(AssistantHomeEffect.ShowSnackbar("Error declining request: ${err.message}"))
            }
        }
    }
}
