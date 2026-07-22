import api from "./api-client"

export const assistantsService = {
  async getNearbyAssistants(lat: number, lon: number, radius?: number) {
    const params = { latitude: lat, longitude: lon, radius_km: radius }
    const res = await api.get("/assistants/nearby", { params })
    return res.data
  },

  async getAssistantProfile(assistantId: number) {
    const res = await api.get(`/assistants/${assistantId}`)
    return res.data
  },

  async getAssistantRatings(assistantId: number) {
    const res = await api.get(`/ratings/assistant/${assistantId}`)
    return res.data
  },

  async applyAsAssistant(applicationData: any) {
    const res = await api.post("/assistants/apply", applicationData)
    return res.data
  },

  async toggleOnlineStatus(isOnline: boolean) {
    const res = await api.patch("/assistants/online-status", { is_online: isOnline })
    return res.data
  },

  async updateLocation(latitude: number, longitude: number) {
    const res = await api.patch("/assistants/me/location", { latitude, longitude })
    return res.data
  }
}
export default assistantsService
