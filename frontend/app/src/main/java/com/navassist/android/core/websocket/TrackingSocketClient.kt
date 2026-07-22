package com.navassist.android.core.websocket

import com.navassist.android.BuildConfig
import com.navassist.android.data.preferences.SessionDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val socketParser: SocketParser,
    private val reconnectPolicy: SocketReconnectPolicy,
    private val heartbeat: SocketHeartbeat,
    private val sessionDataStore: SessionDataStore
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private var webSocket: WebSocket? = null
    private var currentBookingId: Int? = null

    private val _socketState = MutableStateFlow<SocketState>(SocketState.Disconnected)
    val socketState: StateFlow<SocketState> = _socketState.asStateFlow()

    private val _socketEvents = MutableSharedFlow<SocketEvent>(replay = 0)
    val socketEvents: SharedFlow<SocketEvent> = _socketEvents.asSharedFlow()

    fun connect(bookingId: Int) {
        currentBookingId = bookingId
        scope.launch {
            val token = sessionDataStore.getAccessToken()
            if (token.isNull_or_empty()) {
                _socketState.value = SocketState.AuthenticationFailed
                return@launch
            }

            _socketState.value = SocketState.Connecting

            val wsUrl = "${BuildConfig.BASE_URL.replace("http", "ws")}${SocketConstants.WS_PATH_PREFIX}$bookingId?token=$token"
            val request = Request.Builder().url(wsUrl).build()

            webSocket = okHttpClient.newWebSocket(request, createWebSocketListener(bookingId))
        }
    }

    fun sendLocation(bookingId: Int, lat: Double, lng: Double, heading: Double = 0.0, speed: Double = 0.0) {
        val payload = """{"event":"location:update","booking_id":$bookingId,"lat":$lat,"lng":$lng,"heading":$heading,"speed":$speed,"ts":"${System.currentTimeMillis()}"}"""
        webSocket?.send(payload)
    }

    fun sendMessage(bookingId: Int, messageText: String) {
        val payload = """{"event":"chat:message","booking_id":$bookingId,"message":"$messageText"}"""
        webSocket?.send(payload)
    }

    fun disconnect() {
        heartbeat.stop()
        webSocket?.close(1000, "Normal Closure")
        webSocket = null
        currentBookingId = null
        reconnectPolicy.reset()
        _socketState.value = SocketState.Disconnected
    }

    private fun createWebSocketListener(bookingId: Int): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectPolicy.reset()
                _socketState.value = SocketState.Connected(bookingId)
                heartbeat.start(webSocket, scope)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val event = socketParser.parse(text)
                if (event != null) {
                    scope.launch { _socketEvents.emit(event) }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                heartbeat.stop()
                _socketState.value = SocketState.Error(t)
                attemptReconnect(bookingId)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                heartbeat.stop()
                _socketState.value = SocketState.Disconnected
                scope.launch { _socketEvents.emit(SocketEvent.ConnectionClosed(code, reason)) }
            }
        }
    }

    private fun attemptReconnect(bookingId: Int) {
        val delayMs = reconnectPolicy.nextDelayMs()
        if (delayMs != null) {
            _socketState.value = SocketState.Reconnecting(reconnectPolicy.getRetryCount())
            scope.launch {
                kotlinx.coroutines.delay(delayMs)
                connect(bookingId)
            }
        } else {
            _socketState.value = SocketState.Disconnected
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
