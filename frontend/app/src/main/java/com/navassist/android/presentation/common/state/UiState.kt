package com.navassist.android.presentation.common.state

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>
}

sealed interface UiEvent {
    object Refresh : UiEvent
    data class Submit(val payload: Any) : UiEvent
}

sealed interface UiEffect {
    data class ShowToast(val message: String) : UiEffect
    data class ShowSnackbar(val message: String) : UiEffect
    data class Navigate(val destinationId: Int) : UiEffect
}
