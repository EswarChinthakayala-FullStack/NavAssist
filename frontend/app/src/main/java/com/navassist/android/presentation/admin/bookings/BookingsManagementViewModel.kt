package com.navassist.android.presentation.admin.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.data.remote.api.AdminBookingDto
import com.navassist.android.domain.repository.AdminRepository
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

sealed interface BookingsEffect {
    data class ShowToast(val message: String) : BookingsEffect
    data class ShowSnackbar(val message: String) : BookingsEffect
}

@HiltViewModel
class BookingsManagementViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _bookingsState = MutableStateFlow<UiState<List<AdminBookingDto>>>(UiState.Loading)
    val bookingsState: StateFlow<UiState<List<AdminBookingDto>>> = _bookingsState.asStateFlow()

    private var allBookings: List<AdminBookingDto> = emptyList()

    private val _effects = MutableSharedFlow<BookingsEffect>()
    val effects: SharedFlow<BookingsEffect> = _effects.asSharedFlow()

    init {
        loadBookings()
    }

    fun loadBookings(statusFilter: String? = null) {
        _bookingsState.value = UiState.Loading
        viewModelScope.launch {
            val res = adminRepository.getBookings(statusFilter)
            res.onSuccess { list ->
                allBookings = list
                _bookingsState.value = UiState.Success(list)
            }.onFailure { err ->
                _bookingsState.value = UiState.Error(err.message ?: "Failed to load bookings")
            }
        }
    }

    fun filterBookings(query: String?) {
        if (query.isNullOrBlank()) {
            _bookingsState.value = UiState.Success(allBookings)
            return
        }
        val q = query.lowercase().trim()
        val filtered = allBookings.filter {
            it.bookingCode.lowercase().contains(q) ||
            it.pickupAddress.lowercase().contains(q) ||
            it.destinationAddress.lowercase().contains(q) ||
            it.guestId.toString().contains(q)
        }
        _bookingsState.value = UiState.Success(filtered)
    }
}
