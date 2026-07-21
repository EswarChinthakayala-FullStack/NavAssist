import { useQuery } from "@tanstack/react-query"
import { pricingService } from "@/services/pricing.service"
import { assistantsService } from "@/services/assistants.service"
import { bookingsService } from "@/services/bookings.service"

export interface LocationPoint {
  lat: number
  lng: number
}

export function useFareEstimate(pickup: LocationPoint | null, destination: LocationPoint | null, couponCode?: string) {
  return useQuery({
    queryKey: ["fare-estimate", pickup?.lat, pickup?.lng, destination?.lat, destination?.lng, couponCode],
    queryFn: async () => {
      if (!pickup || !destination) return null
      return await pricingService.estimateFare(pickup.lat, pickup.lng, destination.lat, destination.lng, couponCode)
    },
    enabled: Boolean(pickup?.lat && pickup?.lng && destination?.lat && destination?.lng),
    staleTime: 1000 * 30, // 30 seconds cache
  })
}

export function useNearbyAssistants(pickupLat?: number | null, pickupLng?: number | null, radiusKm: number = 15) {
  return useQuery({
    queryKey: ["nearby-assistants", pickupLat, pickupLng, radiusKm],
    queryFn: async () => {
      if (pickupLat == null || pickupLng == null) return []
      const res = await assistantsService.getNearbyAssistants(pickupLat, pickupLng, radiusKm)
      const mapped = await Promise.all(
        res.map(async (ast: any) => {
          try {
            const p = await assistantsService.getAssistantProfile(ast.assistant_id)
            return {
              id: ast.assistant_id,
              name: p.name || `Guide #${ast.assistant_id}`,
              rating: Number(p.avg_rating ?? 5.0),
              tripsCount: Number(p.total_trips ?? 0),
              distance_km: Number(ast.distance_km ?? 0.0),
              eta_mins: Math.max(3, Math.round(Number(ast.distance_km ?? 1.0) * 4)),
              avatar_url: p.profile_photo_url || undefined,
            }
          } catch {
            return {
              id: ast.assistant_id,
              name: `Assistant Guide #${ast.assistant_id}`,
              rating: 5.0,
              tripsCount: 0,
              distance_km: Number(ast.distance_km ?? 0.0),
              eta_mins: Math.max(3, Math.round(Number(ast.distance_km ?? 1.0) * 4)),
            }
          }
        })
      )
      return mapped
    },
    enabled: Boolean(pickupLat != null && pickupLng != null),
    staleTime: 1000 * 15, // 15 seconds refresh
  })
}

export function useActiveBooking() {
  return useQuery({
    queryKey: ["active-booking"],
    queryFn: async () => {
      const all = await bookingsService.listBookings()
      const activeStatuses = [
        "pending",
        "searching",
        "assigned",
        "assistant_enroute",
        "arrived_pickup",
        "guest_picked_up",
        "in_progress",
      ]
      return all.find((b: any) => activeStatuses.includes(b.status.toLowerCase())) || null
    },
    refetchInterval: 10000,
  })
}

export function useBookingDetails(bookingId: number | null) {
  return useQuery({
    queryKey: ["booking-details", bookingId],
    queryFn: async () => {
      if (!bookingId) return null
      return await bookingsService.getBooking(bookingId)
    },
    enabled: Boolean(bookingId),
    refetchInterval: 5000,
  })
}
