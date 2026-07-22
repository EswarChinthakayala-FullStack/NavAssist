package com.navassist.android.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.Assistant
import com.navassist.android.domain.repository.AssistantRepository
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AvailableAssistantsViewModel @Inject constructor(
    private val assistantRepository: AssistantRepository
) : ViewModel() {

    private val _assistantsState = MutableStateFlow<UiState<List<Assistant>>>(UiState.Loading)
    val assistantsState: StateFlow<UiState<List<Assistant>>> = _assistantsState.asStateFlow()

    fun loadNearbyAssistants(pickupLat: Double, pickupLng: Double) {
        _assistantsState.value = UiState.Loading
        viewModelScope.launch {
            val result = assistantRepository.getNearbyAssistants(pickupLat, pickupLng)
            result.onSuccess { list ->
                _assistantsState.value = UiState.Success(list)
            }.onFailure { error ->
                _assistantsState.value = UiState.Error(error.message ?: "Failed to find nearby assistants")
            }
        }
    }
}
