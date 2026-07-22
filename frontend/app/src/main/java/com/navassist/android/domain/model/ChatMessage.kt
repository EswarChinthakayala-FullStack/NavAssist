package com.navassist.android.domain.model

data class ChatMessage(
    val id: String,
    val bookingId: String,
    val senderId: String,
    val senderName: String,
    val messageText: String,
    val timestamp: String,
    val isFromMe: Boolean
)
