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

data class ShareLinkData(
    val shareId: String,
    val shareUrl: String,
    val expiresInSeconds: Long = 7200,
    val isActive: Boolean = true
)

@HiltViewModel
class ShareTripViewModel @Inject constructor() : ViewModel() {

    private val _shareState = MutableStateFlow<UiState<ShareLinkData>>(UiState.Loading)
    val shareState: StateFlow<UiState<ShareLinkData>> = _shareState.asStateFlow()

    fun generateShareLink(bookingId: Int) {
        _shareState.value = UiState.Loading
        viewModelScope.launch {
            val mockData = ShareLinkData(
                shareId = "TRIP123ABC",
                shareUrl = "https://navassist.app/live/TRIP123ABC",
                expiresInSeconds = 7200,
                isActive = true
            )
            _shareState.value = UiState.Success(mockData)
        }
    }
}
