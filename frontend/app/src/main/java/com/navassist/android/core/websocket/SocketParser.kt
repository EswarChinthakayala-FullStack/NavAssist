package com.navassist.android.core.websocket

import com.navassist.android.core.network.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketParser @Inject constructor() {
    private val json = JsonConfiguration.instance

    fun parse(text: String): SocketEvent? {
        return try {
            val jsonObject = json.decodeFromString<JsonObject>(text)
            val eventType = jsonObject["event"]?.jsonPrimitive?.content
                ?: jsonObject["action"]?.jsonPrimitive?.content

            when (eventType) {
                SocketConstants.EVENT_CONNECTION_ACK -> {
                    val payload = json.decodeFromString<ConnectionAckPayload>(text)
                    SocketEvent.ConnectionEstablished(payload.bookingId)
                }
                SocketConstants.EVENT_LOCATION_UPDATE -> {
                    val payload = json.decodeFromString<LocationUpdatePayload>(text)
                    SocketEvent.LocationUpdate(
                        bookingId = payload.bookingId,
                        latitude = payload.lat,
                        longitude = payload.lng,
                        heading = payload.heading,
                        speedKmh = payload.speed,
                        timestamp = payload.ts
                    )
                }
                SocketConstants.EVENT_BOOKING_STATUS_CHANGED -> {
                    val payload = json.decodeFromString<BookingStatusPayload>(text)
                    SocketEvent.BookingStatusChanged(
                        bookingId = payload.bookingId,
                        status = payload.status,
                        timestamp = payload.ts
                    )
                }
                SocketConstants.EVENT_ETA_UPDATE -> {
                    val payload = json.decodeFromString<EtaUpdatePayload>(text)
                    SocketEvent.ETAUpdated(
                        bookingId = payload.bookingId,
                        etaMinutes = payload.etaMinutes,
                        distanceRemainingKm = payload.distanceRemainingKm
                    )
                }
                SocketConstants.EVENT_SOS_TRIGGERED -> {
                    val payload = json.decodeFromString<SosTriggeredPayload>(text)
                    SocketEvent.SOSTriggered(
                        sosId = payload.sosId,
                        userId = payload.userId,
                        bookingId = payload.bookingId,
                        latitude = payload.lat,
                        longitude = payload.lng
                    )
                }
                SocketConstants.EVENT_CHAT_MESSAGE -> {
                    val payload = json.decodeFromString<ChatMessagePayload>(text)
                    SocketEvent.ChatMessage(
                        bookingId = payload.bookingId,
                        senderId = payload.senderId,
                        text = payload.message,
                        timestamp = payload.ts
                    )
                }
                SocketConstants.EVENT_PONG -> SocketEvent.Heartbeat
                else -> null
            }
        } catch (e: Exception) {
            SocketEvent.ErrorEvent(SocketException.ParsingError(e.message ?: "Failed to parse frame"))
        }
    }
}
