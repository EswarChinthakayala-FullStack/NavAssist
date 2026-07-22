package com.navassist.android.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.BookingStatus
import com.navassist.android.domain.repository.BookingRepository
import com.navassist.android.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingStats(
    val totalTrips: Int = 0,
    val completedTrips: Int = 0,
    val pendingTrips: Int = 0,
    val amountSpent: Double = 0.0
)

@HiltViewModel
class BookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _rawBookingsState = MutableStateFlow<UiState<List<Booking>>>(UiState.Idle)
    val rawBookingsState: StateFlow<UiState<List<Booking>>> = _rawBookingsState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("ALL") // ALL, COMPLETED, PENDING, CANCELLED
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _stats = MutableStateFlow(BookingStats())
    val stats: StateFlow<BookingStats> = _stats.asStateFlow()

    // Combined filtered state
    val filteredBookingsState: StateFlow<UiState<List<Booking>>> = combine(
        _rawBookingsState,
        _searchQuery,
        _statusFilter
    ) { state, query, filter ->
        if (state is UiState.Success) {
            val filtered = state.data.filter { booking ->
                // Filter by status
                val matchesStatus = when (filter.uppercase()) {
                    "COMPLETED" -> booking.status == BookingStatus.COMPLETED
                    "PENDING" -> booking.status == BookingStatus.PENDING || booking.status == BookingStatus.ACCEPTED || booking.status == BookingStatus.ONGOING
                    "CANCELLED" -> booking.status == BookingStatus.CANCELLED
                    else -> true
                }

                // Filter by search query
                val matchesQuery = if (query.isBlank()) {
                    true
                } else {
                    val q = query.trim().lowercase()
                    booking.id.lowercase().contains(q) ||
                            (booking.pickupLocation.addressName?.lowercase()?.contains(q) == true) ||
                            (booking.destinationLocation.addressName?.lowercase()?.contains(q) == true) ||
                            (booking.assistantName?.lowercase()?.contains(q) == true)
                }

                matchesStatus && matchesQuery
            }
            UiState.Success(filtered)
        } else {
            state
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, UiState.Idle)

    init {
        loadBookings()
    }

    fun loadBookings() {
        _rawBookingsState.value = UiState.Loading
        viewModelScope.launch {
            val result = bookingRepository.getBookings()
            result.onSuccess { list ->
                // Calculate stats
                val total = list.size
                val completed = list.count { it.status == BookingStatus.COMPLETED }
                val pending = list.count { it.status == BookingStatus.PENDING || it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.ONGOING }
                val spent = list.filter { it.status == BookingStatus.COMPLETED || it.status == BookingStatus.ONGOING }.sumOf { it.fare }

                _stats.value = BookingStats(
                    totalTrips = total,
                    completedTrips = completed,
                    pendingTrips = pending,
                    amountSpent = spent
                )

                _rawBookingsState.value = UiState.Success(list)
            }.onFailure { error ->
                _rawBookingsState.value = UiState.Error(error.message ?: "Failed to load bookings")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }
}
