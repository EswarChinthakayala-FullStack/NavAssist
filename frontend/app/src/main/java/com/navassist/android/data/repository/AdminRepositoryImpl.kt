package com.navassist.android.data.repository

import com.navassist.android.data.remote.api.AdminApi
import com.navassist.android.data.remote.api.AdminBookingDto
import com.navassist.android.data.remote.api.AdminDashboardStatsDto
import com.navassist.android.data.remote.api.AdminKycItemDto
import com.navassist.android.data.remote.api.AdminUserDto
import com.navassist.android.domain.repository.AdminRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val adminApi: AdminApi
) : AdminRepository {

    override suspend fun getDashboardStats(): Result<AdminDashboardStatsDto> {
        return try {
            val stats = adminApi.getDashboardStats()
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingKycQueue(): Result<List<AdminKycItemDto>> {
        return try {
            val queue = adminApi.getPendingKycQueue()
            Result.success(queue)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approveKyc(assistantId: Int): Result<Boolean> {
        return try {
            adminApi.approveKyc(assistantId)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectKyc(assistantId: Int, reason: String): Result<Boolean> {
        return try {
            adminApi.rejectKyc(assistantId, reason)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUsers(role: String?): Result<List<AdminUserDto>> {
        return try {
            val users = adminApi.getUsers(role)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun suspendUser(userId: Int): Result<Boolean> {
        return try {
            adminApi.suspendUser(userId)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBookings(status: String?): Result<List<AdminBookingDto>> {
        return try {
            val bookings = adminApi.getBookings(status)
            Result.success(bookings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
