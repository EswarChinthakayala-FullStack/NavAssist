package com.navassist.android.data.remote.dto.sos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SosTriggerRequestDto(
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("booking_id") val bookingId: Int? = null
)

@Serializable
data class SosResponseDto(
    @SerialName("id") val id: Int = 1,
    @SerialName("sos_id") val sosId: Int = 1,
    @SerialName("status") val status: String = "ACTIVE",
    @SerialName("message") val message: String = "SOS Alert Active",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("user_id") val userId: Int = 1,
    @SerialName("booking_id") val bookingId: Int? = null,
    @SerialName("latitude") val latitude: Double = 28.6139,
    @SerialName("longitude") val longitude: Double = 77.2090,
    @SerialName("triggered_at") val triggeredAt: String = ""
)
