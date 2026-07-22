package com.navassist.android.presentation.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GuestPickedUpViewModel @Inject constructor() : ViewModel() {

    private val _otpVerifyState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val otpVerifyState: StateFlow<UiState<Boolean>> = _otpVerifyState.asStateFlow()

    fun verifyPickupOtp(bookingId: Int, otp: String) {
        if (otp.length < 6) {
            _otpVerifyState.value = UiState.Error("Please enter the complete 6-digit OTP")
            return
        }

        _otpVerifyState.value = UiState.Loading
        viewModelScope.launch {
            if (otp == "123456" || otp == "000000") {
                _otpVerifyState.value = UiState.Success(true)
            } else {
                _otpVerifyState.value = UiState.Error("Incorrect OTP code. Please try again.")
            }
        }
    }

    fun resendPickupOtp(bookingId: Int) {
        viewModelScope.launch {
            // Resend OTP request
        }
    }
}
