package com.navassist.android.core.websocket

sealed interface SocketState {
    object Connecting : SocketState
    data class Connected(val bookingId: Int) : SocketState
    object Disconnected : SocketState
    data class Reconnecting(val attempt: Int) : SocketState
    object AuthenticationFailed : SocketState
    object NetworkUnavailable : SocketState
    data class Error(val exception: Throwable) : SocketState
}
