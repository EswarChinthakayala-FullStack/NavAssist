package com.navassist.android.core.websocket

sealed interface SocketEvent {
    data class ConnectionEstablished(val bookingId: Int) : SocketEvent
    data class LocationUpdate(
        val bookingId: Int,
        val latitude: Double,
        val longitude: Double,
        val heading: Double,
        val speedKmh: Double,
        val timestamp: String
    ) : SocketEvent

    data class BookingStatusChanged(
        val bookingId: Int,
        val status: String,
        val timestamp: String
    ) : SocketEvent

    data class ETAUpdated(
        val bookingId: Int,
        val etaMinutes: Int,
        val distanceRemainingKm: Double
    ) : SocketEvent

    data class SOSTriggered(
        val sosId: Int,
        val userId: Int,
        val bookingId: Int,
        val latitude: Double,
        val longitude: Double
    ) : SocketEvent

    data class ChatMessage(
        val bookingId: Int,
        val senderId: Int,
        val text: String,
        val timestamp: String
    ) : SocketEvent

    object Heartbeat : SocketEvent
    data class ConnectionClosed(val code: Int, val reason: String) : SocketEvent
    data class ErrorEvent(val cause: Throwable) : SocketEvent
}
