package com.navassist.android.domain.model

data class NotificationItem(
    val id: Int,
    val title: String,
    val body: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: String
)
