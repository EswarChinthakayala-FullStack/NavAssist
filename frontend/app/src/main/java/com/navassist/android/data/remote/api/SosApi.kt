package com.navassist.android.data.remote.api

import com.navassist.android.data.remote.dto.sos.SosResponseDto
import com.navassist.android.data.remote.dto.sos.SosTriggerRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SosApi {
    @POST("sos/trigger")
    suspend fun triggerSos(@Body request: SosTriggerRequestDto): SosResponseDto

    @GET("sos/active")
    suspend fun getActiveSosAlerts(): List<SosResponseDto>

    @retrofit2.http.PATCH("sos/{sos_id}/resolve")
    suspend fun resolveSosAlert(@retrofit2.http.Path("sos_id") sosId: Int): SosResponseDto
}
