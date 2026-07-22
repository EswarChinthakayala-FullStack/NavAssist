package com.navassist.android.domain.repository

import com.navassist.android.domain.model.ChatMessage

interface MessageRepository {
    suspend fun getMessages(bookingId: String): Result<List<ChatMessage>>
    suspend fun sendMessage(bookingId: String, text: String): Result<ChatMessage>
}
