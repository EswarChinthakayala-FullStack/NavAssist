package com.navassist.android.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.notifications.adapter.NotificationItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor() : ViewModel() {

    private val _notificationsState = MutableStateFlow<UiState<List<NotificationItem>>>(UiState.Loading)
    val notificationsState: StateFlow<UiState<List<NotificationItem>>> = _notificationsState.asStateFlow()

    private val notifList = mutableListOf(
        NotificationItem("1", "Assistant Assigned", "Vikram Sharma has been assigned to your booking #BK_10293.", "10 min ago", false, "BOOKING"),
        NotificationItem("2", "Payment Confirmed", "Payment of $48.50 verified successfully via Apple Pay.", "35 min ago", false, "PAYMENT"),
        NotificationItem("3", "Discount Applied", "Promo code 'DISCOUNT20' saved $12.00 on your trip.", "2 hours ago", true, "OFFERS"),
        NotificationItem("4", "Booking Confirmed", "Your ride from MG Road to Airport has been confirmed.", "Yesterday", true, "BOOKING")
    )

    fun loadNotifications() {
        _notificationsState.value = UiState.Success(notifList.toList())
    }

    fun markAsRead(id: String) {
        val index = notifList.indexOfFirst { it.id == id }
        if (index != -1) {
            notifList[index] = notifList[index].copy(isRead = true)
            _notificationsState.value = UiState.Success(notifList.toList())
        }
    }

    fun markAllAsRead() {
        for (i in notifList.indices) {
            notifList[i] = notifList[i].copy(isRead = true)
        }
        _notificationsState.value = UiState.Success(notifList.toList())
    }
}
