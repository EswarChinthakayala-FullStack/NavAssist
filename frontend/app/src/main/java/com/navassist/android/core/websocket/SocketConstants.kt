package com.navassist.android.core.websocket

object SocketConstants {
    const val WS_PATH_PREFIX = "ws/tracking/"
    const val PING_INTERVAL_SECONDS = 30L
    const val CONNECT_TIMEOUT_SECONDS = 15L

    const val INITIAL_RETRY_DELAY_MS = 1000L
    const val MAX_RETRY_DELAY_MS = 30000L
    const val MAX_RETRY_COUNT = 5

    // Backend Event Keys
    const val EVENT_CONNECTION_ACK = "connection:ack"
    const val EVENT_LOCATION_UPDATE = "location:update"
    const val EVENT_BOOKING_STATUS_CHANGED = "booking:status_changed"
    const val EVENT_ETA_UPDATE = "eta:update"
    const val EVENT_SOS_TRIGGERED = "sos:triggered"
    const val EVENT_CHAT_MESSAGE = "chat:message"
    const val EVENT_PING = "ping"
    const val EVENT_PONG = "pong"
}
