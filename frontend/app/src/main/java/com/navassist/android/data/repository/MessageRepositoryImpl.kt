package com.navassist.android.data.repository

import com.navassist.android.data.remote.api.MessageApi
import com.navassist.android.data.remote.dto.message.ChatMessageDto
import com.navassist.android.data.remote.dto.message.SendMessageRequestDto
import com.navassist.android.domain.model.ChatMessage
import com.navassist.android.domain.repository.MessageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageApi: MessageApi
) : MessageRepository {

    override suspend fun sendMessage(bookingId: String, text: String): Result<ChatMessage> {
        return try {
            val numericBookingId = bookingId.toIntOrNull() ?: 1
            val dto = messageApi.sendMessage(SendMessageRequestDto(numericBookingId, text))
            Result.success(dto.toDomain(bookingId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMessages(bookingId: String): Result<List<ChatMessage>> {
        return try {
            val dtos = messageApi.getMessages(bookingId)
            Result.success(dtos.map { it.toDomain(bookingId) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun ChatMessageDto.toDomain(currentBookingId: String): ChatMessage {
    return ChatMessage(
        id = id,
        bookingId = currentBookingId,
        senderId = senderId,
        senderName = senderName,
        messageText = text,
        timestamp = timestamp,
        isFromMe = isFromMe
    )
}
