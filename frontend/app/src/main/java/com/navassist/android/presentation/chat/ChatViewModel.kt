package com.navassist.android.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.ChatMessage
import com.navassist.android.domain.repository.MessageRepository
import com.navassist.android.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _messagesState = MutableStateFlow<UiState<List<ChatMessage>>>(UiState.Idle)
    val messagesState: StateFlow<UiState<List<ChatMessage>>> = _messagesState.asStateFlow()

    fun loadMessages(bookingId: String = "active_booking") {
        _messagesState.value = UiState.Loading
        viewModelScope.launch {
            val result = messageRepository.getMessages(bookingId)
            result.onSuccess { list ->
                _messagesState.value = UiState.Success(list)
            }.onFailure { error ->
                _messagesState.value = UiState.Error(error.message ?: "Failed to load messages")
            }
        }
    }

    fun sendMessage(bookingId: String = "active_booking", text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val res = messageRepository.sendMessage(bookingId, text)
            res.onSuccess {
                loadMessages(bookingId)
            }
        }
    }
}
