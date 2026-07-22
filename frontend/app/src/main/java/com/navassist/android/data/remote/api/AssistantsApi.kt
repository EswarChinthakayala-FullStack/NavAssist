package com.navassist.android.data.remote.api

import com.navassist.android.data.remote.dto.assistant.AssistantDashboardStatsDto
import com.navassist.android.data.remote.dto.assistant.AssistantLocationDto
import com.navassist.android.data.remote.dto.assistant.AssistantMyProfileDto
import com.navassist.android.data.remote.dto.assistant.AssistantNearbyDto
import com.navassist.android.data.remote.dto.assistant.AssistantStatusToggleDto
import com.navassist.android.data.remote.dto.assistant.FullEarningsDashboardDto
import com.navassist.android.data.remote.dto.assistant.TodayEarningsDto
import com.navassist.android.data.remote.dto.assistant.UpdateAssistantProfileRequestDto
import com.navassist.android.data.remote.dto.booking.BookingResponseDto
import com.navassist.android.data.remote.dto.user.AssistantProfileDto
import retrofit2.http.*

interface AssistantsApi {
    @GET("assistants/nearby")
    suspend fun getNearbyAssistants(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("lat") lat: Double = latitude,
        @Query("lng") lng: Double = longitude,
        @Query("radius_km") radiusKm: Double = 5.0
    ): List<AssistantNearbyDto>

    @GET("assistants/me/profile")
    suspend fun getMyProfile(): AssistantMyProfileDto

    @PATCH("assistants/me/profile")
    suspend fun updateMyAssistantProfile(@Body request: UpdateAssistantProfileRequestDto): AssistantMyProfileDto

    @GET("assistants/{id}")
    suspend fun getAssistantProfile(@Path("id") id: Int): AssistantProfileDto

    @POST("assistants/me/status")
    suspend fun toggleOnlineStatus(@Body request: AssistantStatusToggleDto): AssistantMyProfileDto

    @GET("assistants/me/incoming-bookings")
    suspend fun getIncomingBookings(): List<BookingResponseDto>

    @GET("assistants/me/earnings/today")
    suspend fun getTodayEarnings(): TodayEarningsDto

    @GET("assistants/me/earnings")
    suspend fun getFullEarningsSummary(@Query("filter_period") filterPeriod: String = "this_week"): FullEarningsDashboardDto

    @GET("assistants/me/dashboard")
    suspend fun getDashboardStats(): AssistantDashboardStatsDto

    @PATCH("assistants/me/location")
    suspend fun updateLocation(@Body request: AssistantLocationDto): Unit
}
