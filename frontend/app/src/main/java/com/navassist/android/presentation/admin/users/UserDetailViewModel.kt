package com.navassist.android.presentation.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.data.remote.api.AdminUserDto
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

sealed interface UserDetailEffect {
    data class ShowToast(val message: String) : UserDetailEffect
    data class ShowSnackbar(val message: String) : UserDetailEffect
}

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _detailState = MutableStateFlow<UiState<AdminUserDto>>(UiState.Loading)
    val detailState: StateFlow<UiState<AdminUserDto>> = _detailState.asStateFlow()

    private val _effects = MutableSharedFlow<UserDetailEffect>()
    val effects: SharedFlow<UserDetailEffect> = _effects.asSharedFlow()

    fun loadUserDetail(userId: Int) {
        _detailState.value = UiState.Loading
        viewModelScope.launch {
            val res = adminRepository.getUsers(null)
            res.onSuccess { list ->
                val user = list.find { it.id == userId }
                    ?: AdminUserDto(id = userId, fullName = "User #$userId", phone = "+919876543210", email = "user$userId@navassist.in", role = "PASSENGER", isActive = true)
                _detailState.value = UiState.Success(user)
            }.onFailure { err ->
                _detailState.value = UiState.Error(err.message ?: "Failed to load user detail")
            }
        }
    }

    fun toggleUserStatus(userId: Int) {
        viewModelScope.launch {
            _effects.emit(UserDetailEffect.ShowToast("Toggling user status..."))
            val res = adminRepository.suspendUser(userId)
            res.onSuccess {
                _effects.emit(UserDetailEffect.ShowToast("Account status updated ✓"))
                loadUserDetail(userId)
            }.onFailure { err ->
                _effects.emit(UserDetailEffect.ShowSnackbar("Failed to update status: ${err.message}"))
            }
        }
    }
}
