package com.navassist.android.data.remote.dto.assistant

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssistantNearbyDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("rating") val rating: Double = 5.0,
    @SerialName("is_online") val isOnline: Boolean = true
)

@Serializable
data class AssistantLocationDto(
    @SerialName("assistant_id") val assistantId: Int,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("heading") val heading: Double = 0.0,
    @SerialName("speed") val speed: Double = 0.0
)

@Serializable
data class AssistantStatusToggleDto(
    @SerialName("online") val online: Boolean
)

@Serializable
data class UpdateAssistantProfileRequestDto(
    @SerialName("bio") val bio: String? = null,
    @SerialName("experience_years") val experienceYears: Int? = null,
    @SerialName("service_radius_km") val serviceRadiusKm: Double? = null
)

@Serializable
data class AssistantMyProfileDto(
    @SerialName("id") val id: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("experience_years") val experienceYears: Int = 0,
    @SerialName("verification_status") val verificationStatus: String = "VERIFIED",
    @SerialName("aadhaar_masked") val aadhaarMasked: String? = null,
    @SerialName("trust_score") val trustScore: Double = 98.0,
    @SerialName("avg_rating") val avgRating: Double = 5.0,
    @SerialName("total_trips") val totalTrips: Int = 0,
    @SerialName("is_online") val isOnline: Boolean = false,
    @SerialName("service_radius_km") val serviceRadiusKm: Double = 10.0,
    @SerialName("profile_photo_url") val profilePhotoUrl: String? = null
)

@Serializable
data class AssistantDashboardStatsDto(
    @SerialName("today_trips") val todayTrips: Int = 0,
    @SerialName("today_earnings") val todayEarnings: Double = 0.0,
    @SerialName("rating") val rating: Double = 5.0,
    @SerialName("acceptance_rate") val acceptanceRate: Double = 95.0,
    @SerialName("completion_rate") val completionRate: Double = 98.0,
    @SerialName("online_time_hours") val onlineTimeHours: Double = 0.0
)

@Serializable
data class TodayEarningsDto(
    @SerialName("today_earnings_inr") val todayEarningsInr: Double = 0.0,
    @SerialName("completed_trips_today") val completedTripsToday: Int = 0,
    @SerialName("average_fare_inr") val averageFareInr: Double = 0.0,
    @SerialName("weekly_progress_pct") val weeklyProgressPct: Double = 0.0
)

@Serializable
data class EarningsHistoryItemDto(
    @SerialName("booking_id") val bookingId: Int,
    @SerialName("guest_name") val guestName: String = "Passenger",
    @SerialName("date") val date: String = "",
    @SerialName("pickup") val pickup: String = "",
    @SerialName("destination") val destination: String = "",
    @SerialName("fare_amount") val fareAmount: Double = 0.0,
    @SerialName("net_earnings") val netEarnings: Double = 0.0,
    @SerialName("payment_method") val paymentMethod: String = "Online UPI",
    @SerialName("status") val status: String = "COMPLETED"
)

@Serializable
data class FullEarningsDashboardDto(
    @SerialName("wallet_balance_inr") val walletBalanceInr: Double = 0.0,
    @SerialName("lifetime_earnings_inr") val lifetimeEarningsInr: Double = 0.0,
    @SerialName("today_earnings_inr") val todayEarningsInr: Double = 0.0,
    @SerialName("weekly_earnings_inr") val weeklyEarningsInr: Double = 0.0,
    @SerialName("monthly_earnings_inr") val monthlyEarningsInr: Double = 0.0,
    @SerialName("next_payout_amount_inr") val nextPayoutAmountInr: Double = 0.0,
    @SerialName("next_payout_date") val nextPayoutDate: String = "",
    @SerialName("payout_status") val payoutStatus: String = "Scheduled",
    @SerialName("incentives_earned_inr") val incentivesEarnedInr: Double = 0.0,
    @SerialName("bonuses_earned_inr") val bonusesEarnedInr: Double = 0.0,
    @SerialName("completed_trips") val completedTrips: Int = 0,
    @SerialName("has_payout_account") val hasPayoutAccount: Boolean = false,
    @SerialName("earnings_history") val earningsHistory: List<EarningsHistoryItemDto> = emptyList()
)
