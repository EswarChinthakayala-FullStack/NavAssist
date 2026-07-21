import api from "./api-client"

export const paymentsService = {
  async createOrder(bookingId: number, paymentMethod: "online" | "cash" = "online") {
    const res = await api.post("/payments/create-order", {
      booking_id: bookingId,
      payment_method: paymentMethod
    })
    return res.data
  },

  async retryOrder(bookingId: number) {
    const res = await api.post(`/payments/booking/${bookingId}/retry`)
    return res.data
  },

  async confirmCashPayment(bookingId: number) {
    const res = await api.post("/payments/confirm-cash", { booking_id: bookingId })
    return res.data
  },

  async verifyPayment(paymentDetails: { razorpay_order_id: string; razorpay_payment_id: string; razorpay_signature: string }) {
    const res = await api.post("/payments/verify", paymentDetails)
    return res.data
  },

  async recordFailure(failureDetails: {
    razorpay_order_id: string
    error_code?: string
    error_description?: string
    error_reason?: string
  }) {
    const res = await api.post("/payments/failure", failureDetails)
    return res.data
  },

  async getReceipt(bookingId: number) {
    const res = await api.get(`/payments/${bookingId}/receipt`)
    return res.data
  }
}
export default paymentsService
