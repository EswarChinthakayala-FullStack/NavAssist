package com.navassist.android.data.repository

import android.content.Context
import android.net.Uri
import com.navassist.android.data.remote.api.MessageApi
import com.navassist.android.data.remote.dto.message.ChatMessageDto
import com.navassist.android.data.remote.dto.message.SendMessageRequestDto
import com.navassist.android.domain.model.ChatMessage
import com.navassist.android.domain.repository.MessageRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageApi: MessageApi
) : MessageRepository {

    override suspend fun sendMessage(
        bookingId: String,
        text: String?,
        messageType: String,
        mediaUrl: String?,
        latitude: String?,
        longitude: String?
    ): Result<ChatMessage> {
        return try {
            val numericBookingId = bookingId.toIntOrNull() ?: 1
            val req = SendMessageRequestDto(
                messageType = messageType,
                content = text,
                mediaUrl = mediaUrl,
                latitude = latitude,
                longitude = longitude
            )
            val dto = messageApi.sendMessage(numericBookingId, req)
            Result.success(dto.toDomain(bookingId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMessages(bookingId: String): Result<List<ChatMessage>> {
        return try {
            val numericBookingId = bookingId.toIntOrNull() ?: 1
            val dtos = messageApi.getMessages(numericBookingId)
            Result.success(dtos.map { it.toDomain(bookingId) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAttachment(fileUri: Uri, context: Context): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri) ?: throw IllegalStateException("Cannot open file stream")
            val bytes = inputStream.use { it.readBytes() }
            val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", "chat_upload_${System.currentTimeMillis()}.png", requestFile)
            val res = messageApi.uploadAttachment(body)
            Result.success(res.url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun ChatMessageDto.toDomain(currentBookingId: String): ChatMessage {
    val sName = sender?.fullName ?: "Participant #$senderId"
    return ChatMessage(
        id = id.toString(),
        bookingId = currentBookingId,
        senderId = senderId.toString(),
        senderName = sName,
        messageText = content ?: "",
        messageType = messageType,
        mediaUrl = mediaUrl,
        latitude = latitude,
        longitude = longitude,
        timestamp = createdAt,
        isFromMe = false
    )
}
