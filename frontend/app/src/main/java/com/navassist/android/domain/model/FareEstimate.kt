package com.navassist.android.domain.model

data class FareEstimate(
    val baseFare: Double = 180.0,
    val distanceCharge: Double = 40.0,
    val timeCharge: Double = 20.0,
    val platformFee: Double = 15.0,
    val taxes: Double = 10.0,
    val surgeMultiplier: Double = 1.0,
    val couponDiscount: Double = 0.0,
    val totalFare: Double = 245.0
)
