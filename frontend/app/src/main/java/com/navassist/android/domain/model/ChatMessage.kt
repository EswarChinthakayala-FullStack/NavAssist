package com.navassist.android.domain.model

data class ChatMessage(
    val id: String,
    val bookingId: String,
    val senderId: String,
    val senderName: String,
    val messageText: String = "",
    val messageType: String = "TEXT",
    val mediaUrl: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val timestamp: String = "",
    val isFromMe: Boolean = false
)
