package com.navassist.android.data.remote.dto.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardStatsDto(
    @SerialName("total_registered_users") val totalRegisteredUsers: Int,
    @SerialName("total_bookings_processed") val totalBookingsProcessed: Int,
    @SerialName("pending_kyc_reviews") val pendingKycReviews: Int,
    @SerialName("open_tickets_count") val openTicketsCount: Int
)

@Serializable
data class UserStatusUpdateRequestDto(
    @SerialName("status") val status: String
)

@Serializable
data class AuditLogDto(
    @SerialName("id") val id: Int,
    @SerialName("action") val action: String,
    @SerialName("entity_name") val entityName: String,
    @SerialName("entity_id") val entityId: Int? = null,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("created_at") val createdAt: String
)
