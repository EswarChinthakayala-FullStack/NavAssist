package com.navassist.android.domain.repository

import com.navassist.android.domain.model.ChatMessage

interface MessageRepository {
    suspend fun getMessages(bookingId: String): Result<List<ChatMessage>>
    suspend fun sendMessage(
        bookingId: String,
        text: String? = null,
        messageType: String = "TEXT",
        mediaUrl: String? = null,
        latitude: String? = null,
        longitude: String? = null
    ): Result<ChatMessage>
    suspend fun uploadAttachment(fileUri: android.net.Uri, context: android.content.Context): Result<String>
}
