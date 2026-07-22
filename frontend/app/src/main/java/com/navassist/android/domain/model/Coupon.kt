package com.navassist.android.domain.model

data class Coupon(
    val id: String,
    val code: String,
    val title: String,
    val description: String,
    val discountText: String,
    val badgeText: String = "BEST OFFER",
    val minBookingAmount: Double = 0.0,
    val isEligible: Boolean = true,
    val expiryDate: String = "Valid till Dec 31"
)
