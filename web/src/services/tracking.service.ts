import api from "./api-client"

export const trackingService = {
  async getLiveCoordinates(bookingId: number) {
    try {
      const res = await api.get(`/tracking/${bookingId}/current-location`)
      return res.data
    } catch (err) {
      return { latitude: null, longitude: null }
    }
  },

  async updateCoordinates(bookingId: number, lat: number, lon: number) {
    try {
      // Primary location push to assistant location manager
      const res = await api.patch(`/assistants/me/location`, { latitude: lat, longitude: lon })
      return res.data
    } catch (err: any) {
      try {
        const fallback = await api.post(`/tracking/${bookingId}/location`, { latitude: lat, longitude: lon })
        return fallback.data
      } catch (fErr) {
        return { status: "local_sync", latitude: lat, longitude: lon }
      }
    }
  },

  async generateShareLink(bookingId: number) {
    const res = await api.post(`/share/${bookingId}/generate-link`)
    return res.data
  },

  async getPublicTracking(token: string) {
    const res = await api.get(`/share/public/${token}`)
    return res.data
  }
}
export default trackingService
