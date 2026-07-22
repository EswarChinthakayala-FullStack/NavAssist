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

enum class LoginMethod {
    PASSWORD,
    OTP
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val loginState: StateFlow<UiState<User>> = _loginState.asStateFlow()

    private val _otpSendState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val otpSendState: StateFlow<UiState<Unit>> = _otpSendState.asStateFlow()

    private val _loginMethod = MutableStateFlow(LoginMethod.PASSWORD)
    val loginMethod: StateFlow<LoginMethod> = _loginMethod.asStateFlow()

    private val _resendTimer = MutableStateFlow(0)
    val resendTimer: StateFlow<Int> = _resendTimer.asStateFlow()

    private var timerJob: Job? = null

    fun setLoginMethod(method: LoginMethod) {
        _loginMethod.value = method
        _loginState.value = UiState.Idle
        _otpSendState.value = UiState.Idle
    }

    fun loginWithPassword(phone: String, password: String) {
        val cleanPhone = phone.trim()
        val cleanPassword = password.trim()

        if (cleanPhone.isBlank()) {
            _loginState.value = UiState.Error("Phone number is required.")
            return
        }
        if (cleanPassword.isBlank()) {
            _loginState.value = UiState.Error("Password is required.")
            return
        }

        _loginState.value = UiState.Loading
        viewModelScope.launch {
            val result = authRepository.login(cleanPhone, cleanPassword)
            result.onSuccess { user ->
                _loginState.value = UiState.Success(user)
            }.onFailure { error ->
                _loginState.value = UiState.Error(error.message ?: "Authentication failed. Please check your credentials.")
            }
        }
    }

    fun sendOtp(phone: String) {
        val cleanPhone = phone.trim()
        if (cleanPhone.isBlank()) {
            _otpSendState.value = UiState.Error("Phone number is required to send OTP.")
            return
        }

        _otpSendState.value = UiState.Loading
        viewModelScope.launch {
            val result = authRepository.sendOtp(cleanPhone)
            result.onSuccess {
                _otpSendState.value = UiState.Success(Unit)
                startResendTimer()
            }.onFailure { error ->
                _otpSendState.value = UiState.Error(error.message ?: "Failed to send OTP. Please try again.")
            }
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        val cleanPhone = phone.trim()
        val cleanOtp = otp.trim()

        if (cleanPhone.isBlank()) {
            _loginState.value = UiState.Error("Phone number is required.")
            return
        }
        if (cleanOtp.length < 6) {
            _loginState.value = UiState.Error("Please enter complete 6-digit OTP.")
            return
        }

        _loginState.value = UiState.Loading
        viewModelScope.launch {
            val result = authRepository.verifyOtp(cleanPhone, cleanOtp)
            result.onSuccess { user ->
                _loginState.value = UiState.Success(user)
            }.onFailure { error ->
                _loginState.value = UiState.Error(error.message ?: "Invalid OTP. Please check and try again.")
            }
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        _resendTimer.value = 60
        timerJob = viewModelScope.launch {
            while (_resendTimer.value > 0) {
                delay(1000)
                _resendTimer.value -= 1
            }
        }
    }
}
