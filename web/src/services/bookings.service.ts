import api from "./api-client"

export const bookingsService = {
  async createBooking(bookingData: any) {
    const res = await api.post("/bookings/", bookingData)
    return res.data
  },

  async getBooking(bookingId: number) {
    const res = await api.get(`/bookings/${bookingId}`)
    return res.data
  },

  async getActiveBooking() {
    const res = await api.get("/bookings/active/me")
    return res.data
  },

  async listBookings(status?: string) {
    const res = await api.get("/bookings/", { params: status ? { status } : {} })
    return res.data
  },

  async cancelBooking(bookingId: number, reason?: string) {
    const res = await api.patch(`/bookings/${bookingId}/cancel`, { reason })
    return res.data
  },

  async estimateFare(origin: any, destination: any, couponCode?: string) {
    const res = await api.post("/bookings/estimate", {
      pickup_latitude: origin.lat,
      pickup_longitude: origin.lng,
      dropoff_latitude: destination.lat,
      dropoff_longitude: destination.lng,
      coupon_code: couponCode
    })
    return res.data
  },

  async updateStatus(bookingId: number, status: string, otp?: string) {
    const res = await api.patch(`/bookings/${bookingId}/status`, { status, otp })
    return res.data
  },

  async downloadInvoice(bookingId: number) {
    const res = await api.get(`/bookings/${bookingId}/invoice`, {
      responseType: "blob"
    })
    return res
  }
}
export default bookingsService
