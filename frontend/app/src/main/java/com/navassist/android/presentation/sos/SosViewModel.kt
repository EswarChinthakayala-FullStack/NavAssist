package com.navassist.android.presentation.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.repository.SosRepository
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SosViewModel @Inject constructor(
    private val sosRepository: SosRepository
) : ViewModel() {

    private val _sosState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val sosState: StateFlow<UiState<Unit>> = _sosState.asStateFlow()

    fun triggerSos(lat: Double, lng: Double) {
        _sosState.value = UiState.Loading
        viewModelScope.launch {
            val res = sosRepository.triggerSos(null, lat, lng)
            res.onSuccess { _sosState.value = UiState.Success(Unit) }
                .onFailure { e -> _sosState.value = UiState.Error(e.message ?: "Failed to dispatch SOS signal") }
        }
    }
}
