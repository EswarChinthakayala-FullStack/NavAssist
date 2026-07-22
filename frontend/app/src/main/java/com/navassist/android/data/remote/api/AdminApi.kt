package com.navassist.android.data.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.*

@Serializable
data class AdminDashboardStatsDto(
    @SerialName("total_registered_users") val totalRegisteredUsers: Int = 0,
    @SerialName("total_bookings_processed") val totalBookingsProcessed: Int = 0,
    @SerialName("pending_kyc_reviews") val pendingKycReviews: Int = 0,
    @SerialName("open_tickets_count") val openTicketsCount: Int = 0
)

@Serializable
data class AdminUserDto(
    @SerialName("id") val id: Int,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("phone_number") val phone: String,
    @SerialName("email") val email: String? = null,
    @SerialName("role") val role: String,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class AdminBookingDto(
    @SerialName("id") val id: Int,
    @SerialName("booking_code") val bookingCode: String,
    @SerialName("guest_id") val guestId: Int,
    @SerialName("assistant_id") val assistantId: Int? = null,
    @SerialName("pickup_address") val pickupAddress: String,
    @SerialName("destination_address") val destinationAddress: String,
    @SerialName("status") val status: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class AdminKycItemDto(
    @SerialName("id") val id: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("full_name") val fullName: String? = "Applicant Assistant",
    @SerialName("verification_status") val verificationStatus: String,
    @SerialName("aadhaar_front_url") val aadhaarFrontUrl: String? = null,
    @SerialName("aadhaar_back_url") val aadhaarBackUrl: String? = null
)

interface AdminApi {
    @GET("admin/dashboard/stats")
    suspend fun getDashboardStats(): AdminDashboardStatsDto

    @GET("admin/assistants/pending-kyc")
    suspend fun getPendingKycQueue(): List<AdminKycItemDto>

    @PATCH("kyc/admin/{assistant_id}/approve")
    suspend fun approveKyc(@Path("assistant_id") assistantId: Int): Map<String, Boolean>

    @PATCH("kyc/admin/{assistant_id}/reject")
    suspend fun rejectKyc(
        @Path("assistant_id") assistantId: Int,
        @Query("reason") reason: String
    ): Map<String, Boolean>

    @GET("admin/users")
    suspend fun getUsers(@Query("role") role: String? = null): List<AdminUserDto>

    @PATCH("admin/users/{id}/suspend")
    suspend fun suspendUser(@Path("id") id: Int): Map<String, Any>

    @GET("admin/bookings")
    suspend fun getBookings(@Query("status") status: String? = null): List<AdminBookingDto>
}
