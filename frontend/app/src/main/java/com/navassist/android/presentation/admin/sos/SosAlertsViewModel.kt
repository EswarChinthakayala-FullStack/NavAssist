package com.navassist.android.presentation.admin.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.data.remote.dto.sos.SosResponseDto
import com.navassist.android.domain.repository.SosRepository
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

sealed interface SosAlertsEffect {
    data class ShowToast(val message: String) : SosAlertsEffect
    data class ShowSnackbar(val message: String) : SosAlertsEffect
}

@HiltViewModel
class SosAlertsViewModel @Inject constructor(
    private val sosRepository: SosRepository
) : ViewModel() {

    private val _alertsState = MutableStateFlow<UiState<List<SosResponseDto>>>(UiState.Loading)
    val alertsState: StateFlow<UiState<List<SosResponseDto>>> = _alertsState.asStateFlow()

    private var allAlerts: List<SosResponseDto> = emptyList()

    private val _effects = MutableSharedFlow<SosAlertsEffect>()
    val effects: SharedFlow<SosAlertsEffect> = _effects.asSharedFlow()

    init {
        loadActiveAlerts()
    }

    fun loadActiveAlerts() {
        _alertsState.value = UiState.Loading
        viewModelScope.launch {
            val res = sosRepository.getActiveAlerts()
            res.onSuccess { list ->
                allAlerts = list
                _alertsState.value = UiState.Success(list)
            }.onFailure { err ->
                _alertsState.value = UiState.Error(err.message ?: "Failed to load active SOS alerts")
            }
        }
    }

    fun filterAlerts(query: String?) {
        if (query.isNullOrBlank()) {
            _alertsState.value = UiState.Success(allAlerts)
            return
        }
        val q = query.lowercase().trim()
        val filtered = allAlerts.filter {
            it.id.toString().contains(q) ||
            it.userId.toString().contains(q) ||
            (it.bookingId?.toString() ?: "").contains(q)
        }
        _alertsState.value = UiState.Success(filtered)
    }

    fun resolveAlert(sosId: Int) {
        viewModelScope.launch {
            _effects.emit(SosAlertsEffect.ShowToast("Resolving SOS incident #$sosId..."))
            val res = sosRepository.resolveSosAlert(sosId)
            res.onSuccess {
                _effects.emit(SosAlertsEffect.ShowToast("SOS Incident #$sosId Resolved ✓"))
                loadActiveAlerts()
            }.onFailure { err ->
                _effects.emit(SosAlertsEffect.ShowSnackbar("Failed to resolve SOS: ${err.message}"))
            }
        }
    }
}
