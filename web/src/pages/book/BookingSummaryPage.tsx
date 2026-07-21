import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useBookingDraftStore } from "@/store/booking-draft.store"
import { pricingService } from "@/services/pricing.service"
import { bookingsService } from "@/services/bookings.service"
import { assistantsService } from "@/services/assistants.service"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Map, MapMarker, MarkerContent, MapRoute, MapControls } from "@/components/ui/map"
import { AssistantCard } from "@/components/booking/AssistantCard"
import { Badge } from "@/components/ui/badge"
import {
  MapPinIcon,
  CalendarBlankIcon,
  ClockIcon,
  ArrowLeftIcon,
  CheckCircleIcon,
  InfoIcon,
  PathIcon,
  ShieldCheckIcon,
  StarIcon,
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

// Custom hook to encapsulate booking creation as requested by prompt
function useCreateBooking() {
  const [loading, setLoading] = useState(false)

  const createBooking = async (payload: {
    pickup: { lat: number; lng: number; name: string }
    destination: { lat: number; lng: number; name: string }
    schedule: string | null
    selectedAssistant: { id: number; name: string } | null
  }) => {
    if (!payload.pickup || !payload.destination || !payload.selectedAssistant) {
      throw new Error("Missing required booking coordinates or guide selections.")
    }

    setLoading(true)
    try {
      // Retrive cached transit details if any
      let transitNotes = ""
      const cached = sessionStorage.getItem("booking_transit_details")
      if (cached) {
        try {
          const parsed = JSON.parse(cached)
          const parts = []
          if (parsed.transitType) parts.push(`Type: ${parsed.transitType}`)
          if (parsed.transitNumber) parts.push(`No: ${parsed.transitNumber}`)
          if (parsed.transitSpot) parts.push(`Spot: ${parsed.transitSpot}`)
          if (parsed.transitNotes) parts.push(`Notes: ${parsed.transitNotes}`)
          transitNotes = parts.join(" | ")
        } catch (e) {
          console.error("Failed to parse transit details", e)
        }
      }

      const finalPickupAddress = transitNotes
        ? `${payload.pickup.name} (${transitNotes})`
        : payload.pickup.name

      // Create booking payload matching the backend schema
      const res = await bookingsService.createBooking({
        pickup_latitude: payload.pickup.lat,
        pickup_longitude: payload.pickup.lng,
        pickup_address: finalPickupAddress,
        destination_latitude: payload.destination.lat,
        destination_longitude: payload.destination.lng,
        destination_address: payload.destination.name,
        assistant_id: payload.selectedAssistant.id,
        scheduled_time: payload.schedule || null
      })

      return res
    } finally {
      setLoading(false)
    }
  }

  return { createBooking, loading }
}

export function BookingSummaryPage() {
  const navigate = useNavigate()
  const { pickup, destination, schedule, selectedAssistant } = useBookingDraftStore()
  const { createBooking, loading: isSubmitting } = useCreateBooking()

  const [estimate, setEstimate] = useState<number | null>(null)
  const [loadingEstimate, setLoadingEstimate] = useState(true)
  const [distanceKm, setDistanceKm] = useState<number>(1.2)
  const [etaMins, setEtaMins] = useState<number>(6)
  const [routes, setRoutes] = useState<[number, number][][]>([])

  // Geolocation viewport config for static map preview
  const [viewport, setViewport] = useState({
    center: [77.2090, 28.6139] as [number, number],
    zoom: 12,
  })

  useEffect(() => {
    if (!pickup || !destination || !selectedAssistant) {
      toast.error("Please complete previous steps before summarizing.")
      navigate("/book/pickup")
      return
    }

    // Set viewport center to middle point between pickup and destination
    const midLon = (pickup.lng + destination.lng) / 2
    const midLat = (pickup.lat + destination.lat) / 2
    setViewport({
      center: [midLon, midLat],
      zoom: 12.5,
    })

    // Set fallback straight line coordinates
    setRoutes([[[pickup.lng, pickup.lat], [destination.lng, destination.lat]]])

    // Fetch actual road route coordinates from OSRM
    const fetchOSRMRoute = async () => {
      try {
        const url = `https://router.project-osrm.org/route/v1/driving/${pickup.lng},${pickup.lat};${destination.lng},${destination.lat}?overview=full&geometries=geojson&alternatives=true`
        const response = await fetch(url)
        if (response.ok) {
          const data = await response.json()
          if (data.routes && data.routes.length > 0) {
            const allRoutes = data.routes.map((r: any) => r.geometry.coordinates)
            setRoutes(allRoutes)
          }
        }
      } catch (err) {
        console.error("OSRM routing failed:", err)
      }
    }

    // Fetch estimated pricing snapshot
    const fetchPricing = async () => {
      setLoadingEstimate(true)
      try {
        const res = await pricingService.estimateFare(
          pickup.lat,
          pickup.lng,
          destination.lat,
          destination.lng
        )
        const rawFare = res.estimated_fare || res.total_fare || 150.00
        setEstimate(Number(rawFare))

        // Get nearby assistants to find the selected one's actual distance/ETA
        try {
          const nearby = await assistantsService.getNearbyAssistants(pickup.lat, pickup.lng, 25)
          const match = nearby.find((ast: any) => ast.assistant_id === selectedAssistant.id)
          if (match) {
            setDistanceKm(match.distance_km)
            setEtaMins(Math.max(3, Math.round(match.distance_km * 4)))
          }
        } catch (e) {
          console.error("Failed to fetch nearby assistants for summary card", e)
        }
      } catch (err) {
        console.error("Estimate failed:", err)
        setEstimate(185.00) // fallback mockup value
      } finally {
        setLoadingEstimate(false)
      }
    }

    fetchPricing()
    fetchOSRMRoute()
  }, [pickup, destination, selectedAssistant, navigate])

  const handleConfirm = async () => {
    if (!pickup || !destination || !selectedAssistant) return

    try {
      const res = await createBooking({
        pickup,
        destination,
        schedule,
        selectedAssistant,
      })

      toast.success("Booking request created successfully!")
      
      // Save created booking ID in session storage for later payment steps
      sessionStorage.setItem("active_booking_id", res.id.toString())
      
      // Navigate to price estimate screen
      navigate("/book/price-estimate", { state: { bookingId: res.id } })
    } catch (err: any) {
      console.error(err)
      const errorMsg = err.response?.data?.detail || err.message || "Failed to create booking request"
      toast.error(`Booking creation failed: ${errorMsg}`)
    }
  }

  if (!pickup || !destination || !selectedAssistant) return null

  // Format assistant object to match card expectations
  const cardAssistant = {
    id: selectedAssistant.id,
    name: selectedAssistant.name,
    rating: selectedAssistant.rating ?? 5.0,
    tripsCount: selectedAssistant.total_trips ?? 0,
    distance_km: distanceKm,
    eta_mins: etaMins,
    avatar_url: selectedAssistant.profile_photo_url,
  }

  // Format date readable
  const formattedSchedule = schedule && schedule !== "now"
    ? new Date(schedule).toLocaleString("en-IN", {
        dateStyle: "medium",
        timeStyle: "short",
      })
    : "Immediate Service (Now)"

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="w-full flex flex-col gap-6"
    >
      <div className="grid gap-6 md:grid-cols-12">
        {/* Left Column: Route & Map Details */}
        <div className="md:col-span-8 space-y-6">
          <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-2xl overflow-hidden">
            <CardHeader className="border-b border-border/50 bg-muted/20">
              <CardTitle className="text-lg font-bold flex items-center gap-2">
                <MapPinIcon size={20} className="text-primary" />
                Route Information
              </CardTitle>
              <CardDescription>Verify your travel origin and destination points</CardDescription>
            </CardHeader>
            <CardContent className="p-0">
              {/* Static Map Preview */}
              <div className="w-full h-[280px] bg-muted relative">
                <Map
                  viewport={viewport}
                  onViewportChange={setViewport}
                  className="w-full h-full"
                >
                  <MapControls />
                  <MapMarker longitude={pickup.lng} latitude={pickup.lat}>
                    <MarkerContent>
                      <div className="flex flex-col items-center -translate-y-1/2">
                        <div className="bg-primary text-primary-foreground p-2 rounded-full shadow-lg border-2 border-background">
                          <MapPinIcon size={18} weight="fill" />
                        </div>
                        <div className="w-1.5 h-1.5 bg-primary rounded-full mt-0.5 border border-background" />
                      </div>
                    </MarkerContent>
                  </MapMarker>
                  <MapMarker longitude={destination.lng} latitude={destination.lat}>
                    <MarkerContent>
                      <div className="flex flex-col items-center -translate-y-1/2">
                        <div className="bg-destructive text-destructive-foreground p-2 rounded-full shadow-lg border-2 border-background">
                          <MapPinIcon size={18} weight="fill" />
                        </div>
                        <div className="w-1.5 h-1.5 bg-destructive rounded-full mt-0.5 border border-background" />
                      </div>
                    </MarkerContent>
                  </MapMarker>
                  {routes.map((coords, index) => (
                    <MapRoute
                      key={index}
                      id={`summary-route-${index}`}
                      coordinates={coords}
                      color={index === 0 ? "#3b82f6" : "#9ca3af"}
                      width={index === 0 ? 5 : 3.5}
                      opacity={index === 0 ? 0.95 : 0.4}
                      dashArray={index === 0 ? undefined : [2, 2]}
                    />
                  ))}
                </Map>
              </div>

              {/* Text Address description list */}
              <div className="p-6 space-y-4">
                <div className="flex gap-3">
                  <div className="flex flex-col items-center gap-1 mt-0.5">
                    <div className="w-2.5 h-2.5 rounded-full bg-blue-500 ring-4 ring-blue-500/20" />
                    <div className="w-0.5 h-10 bg-border border-dashed" />
                  </div>
                  <div className="flex-1">
                    <span className="text-[10px] font-bold text-blue-400 uppercase tracking-widest">Pickup Origin</span>
                    <p className="text-sm font-semibold text-foreground mt-0.5">{pickup.name}</p>
                  </div>
                </div>

                <div className="flex gap-3">
                  <div className="flex flex-col items-center mt-0.5">
                    <div className="w-2.5 h-2.5 rounded-full bg-emerald-500 ring-4 ring-emerald-500/20" />
                  </div>
                  <div className="flex-1">
                    <span className="text-[10px] font-bold text-emerald-400 uppercase tracking-widest">Destination Target</span>
                    <p className="text-sm font-semibold text-foreground mt-0.5">{destination.name}</p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right Column: Guide & Schedule details */}
        <div className="md:col-span-4 space-y-6">
          {/* Assistant Card section */}
          <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-2xl">
            <CardHeader className="pb-3 bg-muted/20 border-b border-border/50 rounded-t-2xl">
              <CardTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground">
                Selected Guide
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-4 flex gap-4 items-center">
              {/* Avatar Image */}
              <div className="w-14 h-14 rounded-full overflow-hidden border border-border bg-muted flex-shrink-0">
                <img
                  src={cardAssistant.avatar_url || "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=80"}
                  alt={cardAssistant.name}
                  className="object-cover w-full h-full"
                />
              </div>

              {/* Details column */}
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-1.5 justify-between">
                  <h4 className="font-extrabold text-sm text-foreground truncate">{cardAssistant.name}</h4>
                  <Badge className="bg-success text-success-foreground hover:bg-success border-0 text-[10px] px-2 py-0.5 rounded-full flex items-center gap-0.5 font-bold shrink-0">
                    <ShieldCheckIcon size={12} weight="fill" />
                    Verified
                  </Badge>
                </div>
                
                <div className="flex items-center gap-4 mt-2 text-xs text-muted-foreground">
                  <span className="flex items-center gap-0.5 font-bold text-warning">
                    <StarIcon size={14} weight="fill" className="text-amber-500" />
                    {cardAssistant.rating > 0 ? cardAssistant.rating.toFixed(1) : "New"}
                  </span>
                  <span className="font-semibold">{cardAssistant.tripsCount > 0 ? `${cardAssistant.tripsCount} Completed Trips` : "No trips yet"}</span>
                </div>

                <div className="flex items-center gap-3.5 mt-2.5 text-[10px] text-muted-foreground font-bold uppercase tracking-wider">
                  <span className="flex items-center gap-1">
                    <MapPinIcon size={12} className="text-primary" />
                    {cardAssistant.distance_km.toFixed(1)} km away
                  </span>
                  <span>•</span>
                  <span className="text-primary font-extrabold">ETA: {cardAssistant.eta_mins} mins</span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Schedule card section */}
          <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-2xl">
            <CardHeader className="pb-3 bg-muted/20 border-b border-border/50 rounded-t-2xl">
              <CardTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
                <CalendarBlankIcon size={16} />
                Service Timing
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-4 space-y-3">
              <div className="flex items-center gap-2.5 text-sm text-foreground">
                <ClockIcon size={18} className="text-primary" />
                <span className="font-semibold">{formattedSchedule}</span>
              </div>
              <div className="bg-muted/40 border border-border p-3 rounded-xl flex gap-2">
                <InfoIcon size={16} className="text-muted-foreground mt-0.5 shrink-0" />
                <p className="text-[11px] text-muted-foreground leading-relaxed">
                  Escort guides will be waiting near your specified entrance or transit terminal platform 10 minutes before scheduling.
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Sticky Footer fare summary bar */}
      <div className="sticky bottom-0 z-40 bg-card/85 backdrop-blur-md border border-border/80 rounded-2xl p-4 md:p-6 shadow-xl flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mt-6">
        <div>
          <span className="text-xs text-muted-foreground">Fare Estimate Summary</span>
          <div className="text-2xl font-black text-foreground flex items-baseline gap-1 mt-0.5">
            {loadingEstimate ? (
              <span className="h-7 w-20 bg-muted animate-pulse rounded" />
            ) : (
              <span>{new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(estimate || 0)}</span>
            )}
          </div>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            onClick={() => navigate("/book/assistants")}
            disabled={isSubmitting}
            className="rounded-xl py-5 px-6 font-bold text-xs gap-2 flex-1 sm:flex-initial"
          >
            <ArrowLeftIcon size={14} weight="bold" />
            Back
          </Button>
          <Button
            onClick={handleConfirm}
            disabled={isSubmitting || loadingEstimate}
            className="bg-primary text-primary-foreground hover:bg-primary/95 rounded-xl py-5 px-8 font-extrabold text-xs shadow-lg flex-1 sm:flex-initial hover:scale-[1.02] transition-all"
          >
            {isSubmitting ? "Creating Booking..." : "Confirm Booking"}
          </Button>
        </div>
      </div>
    </motion.div>
  )
}
export default BookingSummaryPage
