package com.navassist.android.presentation.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.ChatMessage
import com.navassist.android.domain.repository.MessageRepository
import com.navassist.android.presentation.common.state.UiState
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

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    fun loadMessages(bookingId: String) {
        if (bookingId.isBlank()) return
        _messagesState.value = UiState.Loading
        viewModelScope.launch {
            val result = messageRepository.getMessages(bookingId)
            result.onSuccess { list ->
                _messagesState.value = UiState.Success(list)
            }.onFailure { error ->
                _messagesState.value = UiState.Error(error.message ?: "Failed to load chat history")
            }
        }
    }

    fun sendMessage(bookingId: String, text: String) {
        if (text.isBlank() || bookingId.isBlank()) return
        viewModelScope.launch {
            val res = messageRepository.sendMessage(bookingId = bookingId, text = text, messageType = "TEXT")
            res.onSuccess {
                loadMessages(bookingId)
            }
        }
    }

    fun sendImageMessage(bookingId: String, imageUri: Uri, context: Context) {
        if (bookingId.isBlank()) return
        _actionState.value = UiState.Loading
        viewModelScope.launch {
            val uploadRes = messageRepository.uploadAttachment(imageUri, context)
            uploadRes.onSuccess { mediaUrl ->
                val sendRes = messageRepository.sendMessage(bookingId = bookingId, messageType = "IMAGE", mediaUrl = mediaUrl)
                sendRes.onSuccess {
                    _actionState.value = UiState.Success("Photo shared successfully")
                    loadMessages(bookingId)
                }.onFailure { err ->
                    _actionState.value = UiState.Error(err.message ?: "Failed to send photo message")
                }
            }.onFailure { err ->
                _actionState.value = UiState.Error(err.message ?: "Failed to upload photo attachment")
            }
        }
    }

    fun sendLocationMessage(bookingId: String, lat: Double, lng: Double) {
        if (bookingId.isBlank()) return
        viewModelScope.launch {
            val res = messageRepository.sendMessage(
                bookingId = bookingId,
                messageType = "LOCATION",
                latitude = lat.toString(),
                longitude = lng.toString(),
                text = "Live Location: ${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}"
            )
            res.onSuccess {
                loadMessages(bookingId)
            }
        }
    }
}
