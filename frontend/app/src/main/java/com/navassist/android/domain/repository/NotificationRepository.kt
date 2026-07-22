package com.navassist.android.domain.repository

import com.navassist.android.domain.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<NotificationItem>>
    fun getUnreadCount(): Flow<Int>
    suspend fun markAsRead(id: Int): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
}
