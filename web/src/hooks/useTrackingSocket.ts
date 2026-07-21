import { useEffect, useRef } from "react"
import { useTrackingStore } from "@/store/tracking.store"

interface LocationUpdateEvent {
  event: string
  booking_id: number
  latitude: number
  longitude: number
  heading?: number
  speed?: number
  timestamp: string
}

export function useTrackingSocket(
  bookingId: number | null,
  token: string | null,
  onStatusChange?: (newStatus: string) => void
) {
  const { updateCoordinates, updateEta, setConnectionStatus, resetTracking } = useTrackingStore()
  const socketRef = useRef<WebSocket | null>(null)
  const reconnectTimeoutRef = useRef<any>(null)
  const reconnectAttemptsRef = useRef(0)
  
  const statusCallbackRef = useRef(onStatusChange)
  useEffect(() => {
    statusCallbackRef.current = onStatusChange
  }, [onStatusChange])

  const connect = () => {
    if (!bookingId || !token) return

    if (socketRef.current) {
      socketRef.current.close()
    }

    setConnectionStatus("connecting")
    // Build WebSocket URL
    const wsProto = window.location.protocol === "https:" ? "wss:" : "ws:"
    // Fallback to backend port 8000
    const host = window.location.hostname === "localhost" ? "localhost:8000" : window.location.host
    const wsUrl = `${wsProto}//${host}/ws/tracking/${bookingId}?token=${encodeURIComponent(token)}`

    try {
      const ws = new WebSocket(wsUrl)
      socketRef.current = ws

      ws.onopen = () => {
        setConnectionStatus("connected")
        reconnectAttemptsRef.current = 0
      }

      ws.onmessage = (event) => {
        try {
          const payload = JSON.parse(event.data)
          const eventType = payload.event || payload.action
          
          if (eventType === "location:update" || eventType === "update_location") {
            const lat = payload.latitude || payload.lat
            const lng = payload.longitude || payload.lng
            if (lat !== undefined && lng !== undefined) {
              updateCoordinates(lat, lng)
            }
          } else if (eventType === "booking:status_changed" || eventType === "status_changed") {
            if (payload.status && statusCallbackRef.current) {
              statusCallbackRef.current(payload.status)
            }
          } else if (eventType === "eta:update" || eventType === "update_eta") {
            const eta = payload.eta_minutes || payload.eta
            const dist = payload.distance_remaining_km || payload.distance
            if (eta !== undefined && dist !== undefined) {
              updateEta(Number(eta), Number(dist))
            }
          } else if (eventType === "connection:ack") {
            // Handshake confirmation
            setConnectionStatus("connected")
          }
        } catch (err) {
          // Ignore JSON parsing errors for malformed events
        }
      }

      ws.onclose = () => {
        setConnectionStatus("disconnected")
        attemptReconnect()
      }

      ws.onerror = () => {
        setConnectionStatus("disconnected")
      }
    } catch (err) {
      setConnectionStatus("disconnected")
      attemptReconnect()
    }
  }

  const attemptReconnect = () => {
    if (reconnectAttemptsRef.current >= 5) {
      // Limit to 5 attempts to prevent CPU churn
      return
    }

    const backoffDelay = Math.min(1000 * Math.pow(2, reconnectAttemptsRef.current), 10000)
    reconnectAttemptsRef.current += 1

    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current)
    }

    reconnectTimeoutRef.current = setTimeout(() => {
      connect()
    }, backoffDelay)
  }

  useEffect(() => {
    connect()

    return () => {
      if (socketRef.current) {
        socketRef.current.close()
      }
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current)
      }
      resetTracking()
    }
  }, [bookingId, token])

  return {
    socket: socketRef.current
  }
}

export default useTrackingSocket
