package com.navassist.android.presentation.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupportTicketData(
    val ticketId: String,
    val category: String,
    val subject: String,
    val status: String = "OPEN"
)

@HiltViewModel
class FeedbackViewModel @Inject constructor() : ViewModel() {

    private val _ticketState = MutableStateFlow<UiState<SupportTicketData>>(UiState.Idle)
    val ticketState: StateFlow<UiState<SupportTicketData>> = _ticketState.asStateFlow()

    fun createSupportTicket(category: String, subject: String, description: String) {
        if (subject.isBlank() || description.length < 10) {
            _ticketState.value = UiState.Error("Please provide a subject and at least 10 characters of description.")
            return
        }

        _ticketState.value = UiState.Loading
        viewModelScope.launch {
            val ticket = SupportTicketData(
                ticketId = "SUP-2026-1054",
                category = category,
                subject = subject,
                status = "OPEN"
            )
            _ticketState.value = UiState.Success(ticket)
        }
    }
}
