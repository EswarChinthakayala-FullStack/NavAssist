package com.navassist.android.domain.usecase.notification

import com.navassist.android.domain.model.NotificationItem
import com.navassist.android.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(): Flow<List<NotificationItem>> {
        return notificationRepository.getNotifications()
    }
}
