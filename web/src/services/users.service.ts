import api from "./api-client"

export const usersService = {
  async getMe() {
    const res = await api.get("/users/me")
    return res.data
  },

  async updateMe(data: any) {
    const res = await api.patch("/users/me", data)
    return res.data
  },

  // Saved Locations CRUD
  async getSavedLocations() {
    const res = await api.get("/users/me/saved-locations")
    return res.data
  },

  async addSavedLocation(location: any) {
    const res = await api.post("/users/me/saved-locations", location)
    return res.data
  },

  async deleteSavedLocation(locationId: number) {
    const res = await api.delete(`/users/me/saved-locations/${locationId}`)
    return res.data
  },

  async updateSavedLocation(locationId: number, location: any) {
    const res = await api.patch(`/users/me/saved-locations/${locationId}`, location)
    return res.data
  },

  // Emergency Contacts CRUD
  async getEmergencyContacts() {
    const res = await api.get("/users/me/emergency-contacts")
    return res.data
  },

  async addEmergencyContact(contact: any) {
    const res = await api.post("/users/me/emergency-contacts", contact)
    return res.data
  },

  async updateEmergencyContact(contactId: number, contact: any) {
    const res = await api.put(`/users/me/emergency-contacts/${contactId}`, contact)
    return res.data
  },

  async deleteEmergencyContact(contactId: number) {
    const res = await api.delete(`/users/me/emergency-contacts/${contactId}`)
    return res.data
  }
}
export default usersService
