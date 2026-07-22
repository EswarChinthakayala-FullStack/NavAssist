package com.navassist.android.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.User
import com.navassist.android.domain.repository.AuthRepository
import com.navassist.android.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val userState: StateFlow<UiState<User>> = _userState.asStateFlow()

    fun loadProfile() {
        _userState.value = UiState.Loading
        viewModelScope.launch {
            val result = authRepository.getCurrentUser()
            result.onSuccess { u -> _userState.value = UiState.Success(u) }
                .onFailure { e -> _userState.value = UiState.Error(e.message ?: "Failed to load profile") }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
