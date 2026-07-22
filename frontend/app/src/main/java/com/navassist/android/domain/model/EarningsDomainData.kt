package com.navassist.android.domain.model

data class TripEarningsItem(
    val bookingId: String,
    val guestName: String,
    val date: String,
    val pickup: String,
    val destination: String,
    val fareAmount: Double,
    val netEarnings: Double,
    val paymentMethod: String,
    val status: String
)

data class FullEarningsDashboard(
    val walletBalanceInr: Double = 0.0,
    val lifetimeEarningsInr: Double = 0.0,
    val todayEarningsInr: Double = 0.0,
    val weeklyEarningsInr: Double = 0.0,
    val monthlyEarningsInr: Double = 0.0,
    val nextPayoutAmountInr: Double = 0.0,
    val nextPayoutDate: String = "",
    val payoutStatus: String = "Scheduled",
    val incentivesEarnedInr: Double = 0.0,
    val bonusesEarnedInr: Double = 0.0,
    val completedTrips: Int = 0,
    val hasPayoutAccount: Boolean = false,
    val earningsHistory: List<TripEarningsItem> = emptyList()
)
