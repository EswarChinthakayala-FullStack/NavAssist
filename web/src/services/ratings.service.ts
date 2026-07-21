import api from "./api-client"

export const ratingsService = {
  async submitRating(bookingId: number, stars: number, comment?: string) {
    const res = await api.post("/ratings", { booking_id: bookingId, rating: stars, review_text: comment })
    return res.data
  },

  async getAssistantRatings(assistantId: number) {
    const res = await api.get(`/ratings/assistant/${assistantId}`)
    return res.data
  },

  async getBookingRating(bookingId: number) {
    const res = await api.get(`/ratings/booking/${bookingId}`)
    return res.data
  }
}
export default ratingsService
