package com.navassist.android.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.User
import com.navassist.android.domain.repository.AuthRepository
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val registerState: StateFlow<UiState<User>> = _registerState.asStateFlow()

    private val _selectedRole = MutableStateFlow("GUEST")
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    fun setSelectedRole(role: String) {
        _selectedRole.value = role.uppercase()
    }

    fun register(
        fullName: String,
        phone: String,
        email: String,
        password: String,
        confirmPassword: String,
        termsAccepted: Boolean
    ) {
        val cleanName = fullName.trim()
        val cleanPhone = phone.trim()
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()
        val cleanConfirm = confirmPassword.trim()
        val role = _selectedRole.value

        if (cleanName.length < 2) {
            _registerState.value = UiState.Error("Full Name must be at least 2 characters.")
            return
        }
        if (cleanPhone.length < 10) {
            _registerState.value = UiState.Error("Please enter a valid 10-digit mobile number.")
            return
        }
        if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _registerState.value = UiState.Error("Please enter a valid email address.")
            return
        }
        if (cleanPassword.length < 8) {
            _registerState.value = UiState.Error("Password must be at least 8 characters long.")
            return
        }
        if (cleanPassword != cleanConfirm) {
            _registerState.value = UiState.Error("Passwords do not match.")
            return
        }
        if (!termsAccepted) {
            _registerState.value = UiState.Error("You must agree to the Terms of Service & Privacy Policy.")
            return
        }

        _registerState.value = UiState.Loading
        viewModelScope.launch {
            val result = authRepository.register(
                fullName = cleanName,
                email = cleanEmail,
                phone = cleanPhone,
                password = cleanPassword,
                role = role
            )
            result.onSuccess { user ->
                _registerState.value = UiState.Success(user)
            }.onFailure { error ->
                _registerState.value = UiState.Error(error.message ?: "Account registration failed. Please try again.")
            }
        }
    }
}
