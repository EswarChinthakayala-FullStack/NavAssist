package com.navassist.android.data.repository

import com.navassist.android.data.remote.api.SosApi
import com.navassist.android.data.remote.dto.sos.SosTriggerRequestDto
import com.navassist.android.domain.repository.SosRepository
import javax.inject.Inject
import javax.inject.Singleton

import com.navassist.android.data.remote.dto.sos.SosResponseDto

@Singleton
class SosRepositoryImpl @Inject constructor(
    private val sosApi: SosApi
) : SosRepository {

    override suspend fun triggerSos(bookingId: String?, latitude: Double, longitude: Double): Result<Unit> {
        return try {
            val numericBookingId = bookingId?.toIntOrNull()
            sosApi.triggerSos(SosTriggerRequestDto(latitude = latitude, longitude = longitude, bookingId = numericBookingId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActiveAlerts(): Result<List<SosResponseDto>> {
        return try {
            val alerts = sosApi.getActiveSosAlerts()
            Result.success(alerts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resolveSosAlert(sosId: Int): Result<Boolean> {
        return try {
            sosApi.resolveSosAlert(sosId)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
