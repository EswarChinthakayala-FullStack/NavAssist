import api from "./api-client"

export const locationsService = {
  async autocomplete(query: string) {
    const res = await api.get("/locations/autocomplete", { params: { q: query } })
    return res.data
  },

  async geocode(address: string) {
    const res = await api.get("/locations/geocode", { params: { address } })
    return res.data
  },

  async reverseGeocode(lat: number, lon: number) {
    const res = await api.get("/locations/reverse-geocode", { params: { latitude: lat, longitude: lon } })
    return res.data
  },

  async getServicePoints() {
    const res = await api.get("/locations/service-points")
    return res.data
  }
}
export default locationsService
