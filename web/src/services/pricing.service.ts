import api from "./api-client"

export const pricingService = {
  async estimateFare(pickupLat: number, pickupLon: number, dropoffLat: number, dropoffLon: number, couponCode?: string) {
    const res = await api.post("/pricing/estimate", {
      pickup_latitude: pickupLat,
      pickup_longitude: pickupLon,
      destination_latitude: dropoffLat,
      destination_longitude: dropoffLon,
      coupon_code: couponCode
    })
    return res.data
  }
}
export default pricingService
