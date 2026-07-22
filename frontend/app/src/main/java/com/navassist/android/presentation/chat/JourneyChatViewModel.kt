package com.navassist.android.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.presentation.chat.adapter.JourneyChatMessage
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JourneyChatViewModel @Inject constructor() : ViewModel() {

    private val _messagesState = MutableStateFlow<UiState<List<JourneyChatMessage>>>(UiState.Loading)
    val messagesState: StateFlow<UiState<List<JourneyChatMessage>>> = _messagesState.asStateFlow()

    private val messagesList = mutableListOf(
        JourneyChatMessage("1", "assistant_1", false, "Hello! I am Vikram, your assigned travel assistant.", "03:40 PM", true),
        JourneyChatMessage("2", "user_1", true, "Hi Vikram! I am waiting near Terminal 2 Arrival Gate.", "03:41 PM", true),
        JourneyChatMessage("3", "assistant_1", false, "Perfect. I am driving toward Terminal 2 now, ETA 6 minutes.", "03:42 PM", true)
    )

    fun loadMessages(bookingId: Int) {
        _messagesState.value = UiState.Success(messagesList.toList())
    }

    fun sendMessage(bookingId: Int, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val newMessage = JourneyChatMessage(
                id = System.currentTimeMillis().toString(),
                senderId = "user_1",
                isFromUser = true,
                text = text,
                timestamp = "03:44 PM",
                isRead = true
            )
            messagesList.add(newMessage)
            _messagesState.value = UiState.Success(messagesList.toList())
        }
    }
}
