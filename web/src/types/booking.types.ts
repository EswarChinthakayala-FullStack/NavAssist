export type BookingStatus = "PENDING" | "ACCEPTED" | "STARTED" | "COMPLETED" | "CANCELLED"

export interface FareEstimate {
  base_fare: number
  distance_fare: number
  time_fare: number
  surge_fare: number
  discount_amount: number
  total_fare: number
}

export interface Booking {
  id: number
  guest_id: number
  assistant_id?: number
  pickup_name: string
  pickup_latitude: number
  pickup_longitude: number
  dropoff_name: string
  dropoff_latitude: number
  dropoff_longitude: number
  status: BookingStatus
  scheduled_time?: string
  coupon_code?: string
  fare: number
  created_at: string
  updated_at: string
}
