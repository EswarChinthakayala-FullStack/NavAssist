package com.navassist.android.domain.model

data class AssistantProfileData(
    val id: String,
    val userId: String,
    val name: String,
    val photoUrl: String? = null,
    val rating: Float = 5.0f,
    val totalTrips: Int = 0,
    val trustScore: Float = 98.0f,
    val verificationStatus: String = "VERIFIED",
    val profileCompletionPct: Int = 100,
    val isOnline: Boolean = false,
    val bio: String? = null,
    val experienceYears: Int = 0,
    val serviceRadiusKm: Double = 10.0
)

data class AssistantDashboardStats(
    val todayTrips: Int = 0,
    val todayEarnings: Double = 0.0,
    val rating: Double = 5.0,
    val acceptanceRate: Double = 95.0,
    val completionRate: Double = 98.0,
    val onlineTimeHours: Double = 0.0
)

data class TodayEarnings(
    val todayEarningsInr: Double = 0.0,
    val completedTripsToday: Int = 0,
    val averageFareInr: Double = 0.0,
    val weeklyProgressPct: Double = 0.0
)

data class AssistantDashboardFull(
    val profile: AssistantProfileData,
    val stats: AssistantDashboardStats,
    val earnings: TodayEarnings,
    val incomingBookings: List<Booking>,
    val recentTrips: List<Booking>
)
