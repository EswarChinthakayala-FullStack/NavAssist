package com.navassist.android.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.data.remote.api.AdminDashboardStatsDto
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

sealed interface AdminEffect {
    data class ShowToast(val message: String) : AdminEffect
    data class ShowSnackbar(val message: String) : AdminEffect
    object NavigateToKycQueue : AdminEffect
    object NavigateToUsers : AdminEffect
    object NavigateToBookings : AdminEffect
    object NavigateToSos : AdminEffect
}

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _statsState = MutableStateFlow<UiState<AdminDashboardStatsDto>>(UiState.Loading)
    val statsState: StateFlow<UiState<AdminDashboardStatsDto>> = _statsState.asStateFlow()

    private val _effects = MutableSharedFlow<AdminEffect>()
    val effects: SharedFlow<AdminEffect> = _effects.asSharedFlow()

    init {
        loadDashboardStats()
    }

    fun loadDashboardStats() {
        _statsState.value = UiState.Loading
        viewModelScope.launch {
            val res = adminRepository.getDashboardStats()
            res.onSuccess { stats ->
                _statsState.value = UiState.Success(stats)
            }.onFailure { err ->
                _statsState.value = UiState.Error(err.message ?: "Failed to load admin stats")
            }
        }
    }

    fun onKycQueueClicked() {
        viewModelScope.launch { _effects.emit(AdminEffect.NavigateToKycQueue) }
    }

    fun onUsersClicked() {
        viewModelScope.launch { _effects.emit(AdminEffect.NavigateToUsers) }
    }

    fun onBookingsClicked() {
        viewModelScope.launch { _effects.emit(AdminEffect.NavigateToBookings) }
    }

    fun onSosClicked() {
        viewModelScope.launch { _effects.emit(AdminEffect.NavigateToSos) }
    }
}
