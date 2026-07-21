import api from "./api-client"

export const notificationsService = {
  async listNotifications() {
    const res = await api.get("/notifications")
    return res.data
  },

  async markAsRead(notificationId: number) {
    const res = await api.patch(`/notifications/${notificationId}/read`)
    return res.data
  },

  async registerDeviceToken(deviceToken: string, deviceType: string) {
    const res = await api.post("/notifications/device-token", { token: deviceToken, device_type: deviceType })
    return res.data
  },

  async markAllAsRead() {
    const res = await api.patch("/notifications/read-all")
    return res.data
  }
}
export default notificationsService
