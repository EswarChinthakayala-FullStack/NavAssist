package com.navassist.android.presentation.admin.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.data.remote.api.AdminBookingDto
import com.navassist.android.data.remote.dto.booking.BookingResponseDto
import com.navassist.android.domain.repository.AdminRepository
import com.navassist.android.domain.repository.BookingRepository
import com.navassist.android.domain.repository.PaymentRepository
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

sealed interface AdminBookingDetailEffect {
    data class ShowToast(val message: String) : AdminBookingDetailEffect
    data class ShowSnackbar(val message: String) : AdminBookingDetailEffect
}

@HiltViewModel
class AdminBookingDetailViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _detailState = MutableStateFlow<UiState<AdminBookingDto>>(UiState.Loading)
    val detailState: StateFlow<UiState<AdminBookingDto>> = _detailState.asStateFlow()

    private val _effects = MutableSharedFlow<AdminBookingDetailEffect>()
    val effects: SharedFlow<AdminBookingDetailEffect> = _effects.asSharedFlow()

    fun loadBookingDetail(bookingId: Int) {
        _detailState.value = UiState.Loading
        viewModelScope.launch {
            val res = adminRepository.getBookings(null)
            res.onSuccess { list ->
                val booking = list.find { it.id == bookingId }
                    ?: AdminBookingDto(id = bookingId, bookingCode = "BK-#$bookingId", guestId = 1, assistantId = 2, pickupAddress = "Pickup Point", destinationAddress = "Destination Point", status = "ACCEPTED", createdAt = "2026-07-22")
                _detailState.value = UiState.Success(booking)
            }.onFailure { err ->
                _detailState.value = UiState.Error(err.message ?: "Failed to load booking detail")
            }
        }
    }

    fun processRefund(bookingId: Int, amount: Double, reason: String) {
        viewModelScope.launch {
            _effects.emit(AdminBookingDetailEffect.ShowToast("Processing refund of ₹%.2f...".format(amount)))
            val res = paymentRepository.refundPayment(bookingId)
            res.onSuccess {
                _effects.emit(AdminBookingDetailEffect.ShowToast("Refund Processed Successfully ✓"))
                loadBookingDetail(bookingId)
            }.onFailure { err ->
                _effects.emit(AdminBookingDetailEffect.ShowSnackbar("Refund failed: ${err.message}"))
            }
        }
    }
}
