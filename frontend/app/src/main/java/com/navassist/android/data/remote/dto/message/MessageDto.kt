package com.navassist.android.data.remote.dto.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequestDto(
    @SerialName("booking_id") val bookingId: Int,
    @SerialName("message") val message: String
)

@Serializable
data class ChatMessageDto(
    @SerialName("id") val id: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("sender_name") val senderName: String,
    @SerialName("text") val text: String,
    @SerialName("timestamp") val timestamp: String,
    @SerialName("is_from_me") val isFromMe: Boolean = false
)
