package com.navassist.android.core.websocket

sealed class SocketException(override val message: String, cause: Throwable? = null) : Exception(message, cause) {
    class ConnectionFailed(msg: String = "WebSocket connection failed.") : SocketException(msg)
    class AuthenticationFailed(msg: String = "WebSocket authentication failed.") : SocketException(msg)
    class Timeout(msg: String = "WebSocket connection timed out.") : SocketException(msg)
    class ParsingError(msg: String = "Failed to parse WebSocket JSON frame.") : SocketException(msg)
    class NetworkError(msg: String = "Network connectivity lost during WebSocket session.") : SocketException(msg)
}
