import { useState, useEffect } from "react"
import { notificationsService } from "@/services/notifications.service"
import { toast } from "sonner"

export function useFcmToken() {
  const [token, setToken] = useState<string | null>(null)
  const [permission, setPermission] = useState<NotificationPermission>("default")

  const requestPermission = async () => {
    if (!("Notification" in window)) {
      return
    }

    try {
      const res = await Notification.requestPermission()
      setPermission(res)
      
      if (res === "granted") {
        // Mock FCM token generation
        const mockFcmToken = "fcm_token_mock_" + Math.random().toString(36).substring(7)
        setToken(mockFcmToken)
        
        // Register mock token with notifications service
        const platform = navigator.userAgent.includes("Windows") ? "windows" : "browser"
        await notificationsService.registerDeviceToken(mockFcmToken, platform)
      }
    } catch (err) {
      toast.error("Could not register device push notifications token.")
    }
  }

  useEffect(() => {
    if ("Notification" in window) {
      setPermission(Notification.permission)
    }
  }, [])

  return {
    token,
    permission,
    requestPermission
  }
}

export default useFcmToken
