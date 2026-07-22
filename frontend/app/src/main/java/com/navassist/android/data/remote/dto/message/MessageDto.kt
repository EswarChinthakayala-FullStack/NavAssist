package com.navassist.android.data.remote.dto.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequestDto(
    @SerialName("message_type") val messageType: String = "TEXT",
    @SerialName("content") val content: String? = null,
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("latitude") val latitude: String? = null,
    @SerialName("longitude") val longitude: String? = null
)

@Serializable
data class MessageSenderDto(
    @SerialName("id") val id: Int,
    @SerialName("full_name") val fullName: String,
    @SerialName("role") val role: String,
    @SerialName("profile_photo_url") val profilePhotoUrl: String? = null
)

@Serializable
data class ChatMessageDto(
    @SerialName("id") val id: Int,
    @SerialName("booking_id") val bookingId: Int,
    @SerialName("sender_id") val senderId: Int,
    @SerialName("message_type") val messageType: String = "TEXT",
    @SerialName("content") val content: String? = null,
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("latitude") val latitude: String? = null,
    @SerialName("longitude") val longitude: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("sender") val sender: MessageSenderDto? = null
)

@Serializable
data class UploadAttachmentResponseDto(
    @SerialName("url") val url: String,
    @SerialName("filename") val filename: String
)
