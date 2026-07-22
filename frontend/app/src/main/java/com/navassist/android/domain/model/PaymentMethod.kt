package com.navassist.android.domain.model

data class PaymentMethod(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconResId: Int,
    val isRecommended: Boolean = false,
    val isSelected: Boolean = false,
    val isAvailable: Boolean = true
)
