package com.navassist.android.core.websocket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionAckPayload(
    @SerialName("event") val event: String,
    @SerialName("status") val status: String,
    @SerialName("booking_id") val bookingId: Int
)

@Serializable
data class LocationUpdatePayload(
    @SerialName("event") val event: String = SocketConstants.EVENT_LOCATION_UPDATE,
    @SerialName("booking_id") val bookingId: Int,
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double,
    @SerialName("heading") val heading: Double = 0.0,
    @SerialName("speed") val speed: Double = 0.0,
    @SerialName("ts") val ts: String
)

@Serializable
data class BookingStatusPayload(
    @SerialName("event") val event: String = SocketConstants.EVENT_BOOKING_STATUS_CHANGED,
    @SerialName("booking_id") val bookingId: Int,
    @SerialName("status") val status: String,
    @SerialName("ts") val ts: String
)

@Serializable
data class EtaUpdatePayload(
    @SerialName("event") val event: String = SocketConstants.EVENT_ETA_UPDATE,
    @SerialName("booking_id") val bookingId: Int,
    @SerialName("eta_minutes") val etaMinutes: Int,
    @SerialName("distance_remaining_km") val distanceRemainingKm: Double
)

@Serializable
data class SosTriggeredPayload(
    @SerialName("event") val event: String = SocketConstants.EVENT_SOS_TRIGGERED,
    @SerialName("sos_id") val sosId: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("booking_id") val bookingId: Int,
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double
)

@Serializable
data class ChatMessagePayload(
    @SerialName("event") val event: String = SocketConstants.EVENT_CHAT_MESSAGE,
    @SerialName("booking_id") val bookingId: Int,
    @SerialName("sender_id") val senderId: Int,
    @SerialName("message") val message: String,
    @SerialName("ts") val ts: String
)
