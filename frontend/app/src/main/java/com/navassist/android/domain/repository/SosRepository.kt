package com.navassist.android.domain.repository

import com.navassist.android.data.remote.dto.sos.SosResponseDto

interface SosRepository {
    suspend fun triggerSos(bookingId: String?, latitude: Double, longitude: Double): Result<Unit>
    suspend fun getActiveAlerts(): Result<List<SosResponseDto>>
    suspend fun resolveSosAlert(sosId: Int): Result<Boolean>
}
