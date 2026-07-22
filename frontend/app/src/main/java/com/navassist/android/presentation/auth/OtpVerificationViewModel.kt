package com.navassist.android.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.User
import com.navassist.android.domain.repository.AuthRepository
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpVerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _otpVerifyState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val otpVerifyState: StateFlow<UiState<User>> = _otpVerifyState.asStateFlow()

    private val _otpSendState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val otpSendState: StateFlow<UiState<Unit>> = _otpSendState.asStateFlow()

    private val _resendTimer = MutableStateFlow(30)
    val resendTimer: StateFlow<Int> = _resendTimer.asStateFlow()

    private val _timerProgress = MutableStateFlow(100)
    val timerProgress: StateFlow<Int> = _timerProgress.asStateFlow()

    private var timerJob: Job? = null
    private val defaultCooldownSeconds = 30

    init {
        startResendTimer(defaultCooldownSeconds)
    }

    fun sendOtp(phone: String) {
        val cleanPhone = phone.trim()
        if (cleanPhone.isBlank()) {
            _otpSendState.value = UiState.Error("Phone number is required.")
            return
        }

        _otpSendState.value = UiState.Loading
        viewModelScope.launch {
            val result = authRepository.sendOtp(cleanPhone)
            result.onSuccess {
                _otpSendState.value = UiState.Success(Unit)
                startResendTimer(defaultCooldownSeconds)
            }.onFailure { error ->
                _otpSendState.value = UiState.Error(error.message ?: "Failed to resend OTP. Please try again.")
            }
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        val cleanPhone = phone.trim()
        val cleanOtp = otp.trim()

        if (cleanPhone.isBlank()) {
            _otpVerifyState.value = UiState.Error("Phone number is required.")
            return
        }
        if (cleanOtp.length < 6) {
            _otpVerifyState.value = UiState.Error("Please enter complete 6-digit verification code.")
            return
        }

        _otpVerifyState.value = UiState.Loading
        viewModelScope.launch {
            val result = authRepository.verifyOtp(cleanPhone, cleanOtp)
            result.onSuccess { user ->
                _otpVerifyState.value = UiState.Success(user)
            }.onFailure { error ->
                _otpVerifyState.value = UiState.Error(error.message ?: "Invalid or expired OTP code. Please check and try again.")
            }
        }
    }

    fun startResendTimer(totalSeconds: Int = 30) {
        timerJob?.cancel()
        _resendTimer.value = totalSeconds
        _timerProgress.value = 100

        timerJob = viewModelScope.launch {
            while (_resendTimer.value > 0) {
                delay(1000)
                _resendTimer.value -= 1
                val progress = ((_resendTimer.value.toFloat() / totalSeconds.toFloat()) * 100).toInt()
                _timerProgress.value = progress.coerceIn(0, 100)
            }
        }
    }

    fun resetVerifyState() {
        _otpVerifyState.value = UiState.Idle
    }
}
