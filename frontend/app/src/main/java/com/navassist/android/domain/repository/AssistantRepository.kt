package com.navassist.android.domain.repository

import com.navassist.android.domain.model.Assistant
import com.navassist.android.domain.model.AssistantDashboardStats
import com.navassist.android.domain.model.AssistantProfileData
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.FullEarningsDashboard
import com.navassist.android.domain.model.TodayEarnings

interface AssistantRepository {
    suspend fun getNearbyAssistants(latitude: Double, longitude: Double, radiusKm: Double = 5.0): Result<List<Assistant>>
    suspend fun getAssistantProfile(assistantId: Int): Result<Assistant>
    suspend fun toggleOnlineStatus(isOnline: Boolean): Result<Boolean>
    suspend fun updateLocation(latitude: Double, longitude: Double, heading: Double = 0.0, speed: Double = 0.0): Result<Unit>
    
    suspend fun getMyAssistantProfile(): Result<AssistantProfileData>
    suspend fun updateMyAssistantProfile(bio: String?, experienceYears: Int?, serviceRadiusKm: Double?): Result<AssistantProfileData>
    suspend fun getDashboardStats(): Result<AssistantDashboardStats>
    suspend fun getTodayEarnings(): Result<TodayEarnings>
    suspend fun getFullEarningsSummary(filterPeriod: String = "this_week"): Result<FullEarningsDashboard>
    suspend fun getIncomingBookings(): Result<List<Booking>>
    suspend fun acceptBooking(bookingId: Int): Result<Booking>
    suspend fun rejectBooking(bookingId: Int): Result<Booking>
}
