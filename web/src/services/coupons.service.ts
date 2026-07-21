import api from "./api-client"

export const couponsService = {
  async getAvailableCoupons() {
    const res = await api.get("/coupons/available")
    return res.data
  },

  async validateCoupon(code: string, fareAmount: number) {
    const res = await api.post("/coupons/validate", null, {
      params: {
        code,
        booking_amount: fareAmount
      }
    })
    return res.data
  },

  async applyCoupon(code: string, bookingId: number) {
    const res = await api.post(`/coupons/apply`, { code, booking_id: bookingId })
    return res.data
  }
}
export default couponsService
