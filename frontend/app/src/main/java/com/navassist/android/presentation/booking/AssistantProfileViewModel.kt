package com.navassist.android.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.Assistant
import com.navassist.android.domain.model.Review
import com.navassist.android.domain.repository.AssistantRepository
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssistantProfileViewModel @Inject constructor(
    private val assistantRepository: AssistantRepository
) : ViewModel() {

    private val _assistantProfileState = MutableStateFlow<UiState<Assistant>>(UiState.Loading)
    val assistantProfileState: StateFlow<UiState<Assistant>> = _assistantProfileState.asStateFlow()

    private val _reviewsState = MutableStateFlow<UiState<List<Review>>>(UiState.Loading)
    val reviewsState: StateFlow<UiState<List<Review>>> = _reviewsState.asStateFlow()

    fun loadAssistantProfile(assistantId: String) {
        _assistantProfileState.value = UiState.Loading
        _reviewsState.value = UiState.Loading

        viewModelScope.launch {
            val idInt = assistantId.toIntOrNull() ?: 1
            val result = assistantRepository.getAssistantProfile(idInt)
            result.onSuccess { assistant ->
                _assistantProfileState.value = UiState.Success(assistant)
            }.onFailure { error ->
                _assistantProfileState.value = UiState.Error(error.message ?: "Failed to load assistant profile")
            }

            val mockReviews = listOf(
                Review("1", "Aarav Mehta", 5.0f, "Extremely professional, arrived early and helped with all luggage.", "Oct 18, 2026"),
                Review("2", "Priya Sharma", 4.8f, "Great navigator and smooth drive. Highly recommend!", "Oct 12, 2026"),
                Review("3", "Vikram Patel", 5.0f, "Friendly, polite, and very safe driver.", "Sep 29, 2026")
            )
            _reviewsState.value = UiState.Success(mockReviews)
        }
    }
}
