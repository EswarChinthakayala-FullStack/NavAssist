package com.navassist.android.presentation.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RateAssistantViewModel @Inject constructor() : ViewModel() {

    private val _submitState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val submitState: StateFlow<UiState<Boolean>> = _submitState.asStateFlow()

    fun submitRating(bookingId: Int, rating: Int, review: String, tags: List<String>) {
        if (rating == 0) {
            _submitState.value = UiState.Error("Please select a rating star score.")
            return
        }

        _submitState.value = UiState.Loading
        viewModelScope.launch {
            _submitState.value = UiState.Success(true)
        }
    }
}
