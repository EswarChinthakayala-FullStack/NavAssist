package com.navassist.android.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentVerificationResult(
    val bookingId: String,
    val transactionId: String,
    val paymentId: String,
    val amountPaid: Double
)

@HiltViewModel
class PaymentConfirmationViewModel @Inject constructor() : ViewModel() {

    private val _verificationState = MutableStateFlow<UiState<PaymentVerificationResult>>(UiState.Loading)
    val verificationState: StateFlow<UiState<PaymentVerificationResult>> = _verificationState.asStateFlow()

    fun verifyPaymentAndFetchReceipt(bookingId: String, orderId: String, paymentId: String) {
        _verificationState.value = UiState.Loading
        viewModelScope.launch {
            val mockResult = PaymentVerificationResult(
                bookingId = if (bookingId.isBlank()) "BK_10293" else bookingId,
                transactionId = if (orderId.isBlank()) "TXN_987654" else orderId,
                paymentId = if (paymentId.isBlank()) "pay_8849201" else paymentId,
                amountPaid = 265.0
            )
            _verificationState.value = UiState.Success(mockResult)
        }
    }
}
