package com.navassist.android.domain.repository

import com.navassist.android.data.remote.api.AdminBookingDto
import com.navassist.android.data.remote.api.AdminDashboardStatsDto
import com.navassist.android.data.remote.api.AdminKycItemDto
import com.navassist.android.data.remote.api.AdminUserDto

interface AdminRepository {
    suspend fun getDashboardStats(): Result<AdminDashboardStatsDto>
    suspend fun getPendingKycQueue(): Result<List<AdminKycItemDto>>
    suspend fun approveKyc(assistantId: Int): Result<Boolean>
    suspend fun rejectKyc(assistantId: Int, reason: String): Result<Boolean>
    suspend fun getUsers(role: String? = null): Result<List<AdminUserDto>>
    suspend fun suspendUser(userId: Int): Result<Boolean>
    suspend fun getBookings(status: String? = null): Result<List<AdminBookingDto>>
}
