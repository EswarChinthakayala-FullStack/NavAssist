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

sealed interface UserManagementEffect {
    data class ShowToast(val message: String) : UserManagementEffect
    data class ShowSnackbar(val message: String) : UserManagementEffect
}

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _usersState = MutableStateFlow<UiState<List<AdminUserDto>>>(UiState.Loading)
    val usersState: StateFlow<UiState<List<AdminUserDto>>> = _usersState.asStateFlow()

    private var allUsers: List<AdminUserDto> = emptyList()

    private val _effects = MutableSharedFlow<UserManagementEffect>()
    val effects: SharedFlow<UserManagementEffect> = _effects.asSharedFlow()

    init {
        loadUsers()
    }

    fun loadUsers(roleFilter: String? = null) {
        _usersState.value = UiState.Loading
        viewModelScope.launch {
            val res = adminRepository.getUsers(roleFilter)
            res.onSuccess { list ->
                allUsers = list
                _usersState.value = UiState.Success(list)
            }.onFailure { err ->
                _usersState.value = UiState.Error(err.message ?: "Failed to load user list")
            }
        }
    }

    fun filterUsers(query: String?) {
        if (query.isNullOrBlank()) {
            _usersState.value = UiState.Success(allUsers)
            return
        }
        val q = query.lowercase().trim()
        val filtered = allUsers.filter {
            (it.fullName ?: "").lowercase().contains(q) ||
            it.phone.contains(q) ||
            (it.email ?: "").lowercase().contains(q) ||
            it.id.toString().contains(q)
        }
        _usersState.value = UiState.Success(filtered)
    }

    fun suspendUser(userId: Int, name: String) {
        viewModelScope.launch {
            _effects.emit(UserManagementEffect.ShowToast("Updating status for $name..."))
            val res = adminRepository.suspendUser(userId)
            res.onSuccess {
                _effects.emit(UserManagementEffect.ShowToast("Account status updated for $name ✓"))
                loadUsers()
            }.onFailure { err ->
                _effects.emit(UserManagementEffect.ShowSnackbar("Failed to update status: ${err.message}"))
            }
        }
    }
}
