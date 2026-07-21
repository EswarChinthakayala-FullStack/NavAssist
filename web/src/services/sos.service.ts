import api from "./api-client"

export const sosService = {
  async triggerSos(bookingId: number, lat: number, lon: number) {
    const res = await api.post("/sos/trigger", { booking_id: bookingId, latitude: lat, longitude: lon })
    return res.data
  },

  async resolveSos(sosId: number) {
    const res = await api.patch(`/sos/${sosId}/resolve`)
    return res.data
  },

  async listActiveSosAlerts() {
    const res = await api.get("/sos/active")
    return res.data
  }
}
export default sosService
