import React, { useState, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { bookingsService } from "@/services/bookings.service"
import { assistantsService } from "@/services/assistants.service"
import { sosService } from "@/services/sos.service"
import { useTrackingSocket } from "@/hooks/useTrackingSocket"
import { useTrackingStore } from "@/store/tracking.store"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Map, MapMarker, MarkerContent, MapControls, MapRoute } from "@/components/ui/map"
import {
  SirenIcon,
  PhoneCallIcon,
  ChatCircleTextIcon,
  ShieldCheckIcon,
  ClockIcon,
  MapPinIcon,
  UserCheckIcon,
  FlagIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion, animate, useAnimation } from "framer-motion"

import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar"
import { useAuth } from "@/store/auth-context"
import RideOtpCard from "@/components/booking/RideOtpCard"
import OtpVerificationCard from "@/components/booking/OtpVerificationCard"

function computeHaversineKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371
  const dLat = (lat2 - lat1) * (Math.PI / 180)
  const dLon = (lon2 - lon1) * (Math.PI / 180)
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2)
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

export function AssistantEnRoutePage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()
  const token = localStorage.getItem("access_token")

  const { latitude: assistantLat, longitude: assistantLon, etaMins, distanceKm } = useTrackingStore()
  const [booking, setBooking] = useState<any>(null)
  const [guide, setGuide] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  // Animated marker coordinates
  const [markerCoords, setMarkerCoords] = useState<{ lat: number; lon: number } | null>(null)
  const [mapCenter, setMapCenter] = useState<[number, number]>([77.2090, 28.6139])

  // SOS state
  const [sosActive, setSosActive] = useState(false)
  const [triggeringSos, setTriggeringSos] = useState(false)

  // Animation controller for pulsing ETA badge
  const etaControls = useAnimation()
  const defaultGuideAvatar = guide?.profile_photo_url || "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=80"
  
  // Real-time Haversine Distance & ETA calculation from Assistant coordinates to Pickup
  const calculatedDist = (markerCoords && booking?.pickup_latitude && booking?.pickup_longitude)
    ? computeHaversineKm(markerCoords.lat, markerCoords.lon, booking.pickup_latitude, booking.pickup_longitude)
    : null

  const resolvedDistance = distanceKm ?? (calculatedDist !== null ? calculatedDist : 0.05)
  const isArrivedAtPickup = resolvedDistance < 0.1
  const resolvedEta = etaMins ?? (isArrivedAtPickup ? 0 : Math.max(1, Math.round(resolvedDistance * 2.5)))

  // OSRM Polylines
  const [enroutePolyline, setEnroutePolyline] = useState<[number, number][]>([])
  const [destinationPolyline, setDestinationPolyline] = useState<[number, number][]>([])

  // Fetch OSRM route for Assistant -> Pickup
  useEffect(() => {
    if (!markerCoords || !booking?.pickup_latitude || !booking?.pickup_longitude || isArrivedAtPickup) {
      setEnroutePolyline([])
      return
    }

    const fetchEnrouteRoute = async () => {
      try {
        const url = `https://router.project-osrm.org/route/v1/driving/${markerCoords.lon},${markerCoords.lat};${booking.pickup_longitude},${booking.pickup_latitude}?overview=full&geometries=geojson`
        const res = await fetch(url)
        const data = await res.json()
        if (data.routes && data.routes.length > 0) {
          setEnroutePolyline(data.routes[0].geometry.coordinates)
        } else {
          setEnroutePolyline([[markerCoords.lon, markerCoords.lat], [booking.pickup_longitude, booking.pickup_latitude]])
        }
      } catch (err) {
        console.error("OSRM enroute fetch failed:", err)
        setEnroutePolyline([[markerCoords.lon, markerCoords.lat], [booking.pickup_longitude, booking.pickup_latitude]])
      }
    }

    fetchEnrouteRoute()
  }, [markerCoords?.lat, markerCoords?.lon, booking?.pickup_latitude, booking?.pickup_longitude, isArrivedAtPickup])

  // Fetch OSRM route for Pickup -> Destination
  useEffect(() => {
    if (!booking?.pickup_latitude || !booking?.pickup_longitude || !booking?.destination_latitude || !booking?.destination_longitude) {
      setDestinationPolyline([])
      return
    }

    const fetchDestinationRoute = async () => {
      try {
        const url = `https://router.project-osrm.org/route/v1/driving/${booking.pickup_longitude},${booking.pickup_latitude};${booking.destination_longitude},${booking.destination_latitude}?overview=full&geometries=geojson`
        const res = await fetch(url)
        const data = await res.json()
        if (data.routes && data.routes.length > 0) {
          setDestinationPolyline(data.routes[0].geometry.coordinates)
        } else {
          setDestinationPolyline([[booking.pickup_longitude, booking.pickup_latitude], [booking.destination_longitude, booking.destination_latitude]])
        }
      } catch (err) {
        console.error("OSRM destination fetch failed:", err)
        setDestinationPolyline([[booking.pickup_longitude, booking.pickup_latitude], [booking.destination_longitude, booking.destination_latitude]])
      }
    }

    fetchDestinationRoute()
  }, [booking?.pickup_latitude, booking?.pickup_longitude, booking?.destination_latitude, booking?.destination_longitude])

  const handleStatusChange = (newStatus: string) => {
    toast.info(`Travel update: ${newStatus}`)
    const st = newStatus.toUpperCase()
    if (st === "STARTED" || st === "TRACKING" || st === "IN_PROGRESS" || st === "GUEST_PICKED_UP") {
      navigate(`/trip/${bookingId}/tracking`)
    } else if (st === "COMPLETED") {
      navigate(`/ride/completed/${bookingId}`, { replace: true })
    } else if (st === "CANCELLED" || st === "EXPIRED" || st === "NO_SHOW") {
      navigate(`/ride/cancelled/${bookingId}`, { replace: true })
    }
  }

  // Subscribe to tracking WebSockets
  useTrackingSocket(
    bookingId ? parseInt(bookingId) : null,
    token,
    handleStatusChange
  )

  // Handle smooth coordinate interpolation on GPS ping updates
  useEffect(() => {
    if (assistantLat && assistantLon) {
      if (!markerCoords) {
        setMarkerCoords({ lat: assistantLat, lon: assistantLon })
        setMapCenter([assistantLon, assistantLat])
      } else {
        const startLat = markerCoords.lat
        const startLon = markerCoords.lon

        // Animate marker transition over 1.5 seconds to smooth out GPS increments
        animate(0, 1, {
          duration: 1.5,
          ease: "easeInOut",
          onUpdate: (latest) => {
            const currentLat = startLat + (assistantLat - startLat) * latest
            const currentLon = startLon + (assistantLon - startLon) * latest
            setMarkerCoords({ lat: currentLat, lon: currentLon })
          }
        })
      }
    }
  }, [assistantLat, assistantLon])

  // Trigger pulse animation on ETA updates
  useEffect(() => {
    if (etaMins !== null) {
      etaControls.start({
        scale: [1, 1.1, 1],
        transition: { duration: 0.5, ease: "easeInOut" }
      })
    }
  }, [etaMins, etaControls])

  useEffect(() => {
    if (!bookingId) {
      navigate("/bookings")
      return
    }

    const fetchBookingDetails = async () => {
      setLoading(true)
      try {
        const b = await bookingsService.getBooking(parseInt(bookingId))

        // Navigation guards based on status
        const st = (b.status || "").toLowerCase()
        if (st === "completed") {
          navigate(`/ride/completed/${bookingId}`, { replace: true })
          return
        }
        if (st === "cancelled" || st === "no_show" || st === "expired") {
          navigate(`/ride/cancelled/${bookingId}`, { replace: true })
          return
        }
        if (b.payment_status === "pending" && b.payment_method === "online") {
          navigate(`/ride/payment/${bookingId}`, { replace: true })
          return
        }

        if (st === "started" || st === "tracking" || st === "in_progress" || st === "guest_picked_up") {
          navigate(`/trip/${bookingId}/tracking`)
          return
        }

        setBooking(b)

        if (b.assistant_id) {
          try {
            const g = await assistantsService.getAssistantProfile(b.assistant_id)
            setGuide(g)
            if (g.current_latitude && g.current_longitude) {
              setMarkerCoords({ lat: g.current_latitude, lon: g.current_longitude })
            } else if (b.pickup_latitude && b.pickup_longitude) {
              setMarkerCoords({
                lat: b.pickup_latitude,
                lon: b.pickup_longitude
              })
            }
          } catch (e) {
            setGuide({
              id: b.assistant_id,
              name: b.assistant_name || b.assistant?.name || "Assigned Escort Guide",
              avg_rating: 0.0,
              total_trips: 1,
              experience_years: 2,
            })
            if (b.pickup_latitude && b.pickup_longitude) {
              setMarkerCoords({
                lat: b.pickup_latitude,
                lon: b.pickup_longitude
              })
            }
          }
        } else if (b.pickup_latitude && b.pickup_longitude) {
          setMarkerCoords({
            lat: b.pickup_latitude,
            lon: b.pickup_longitude
          })
        }
      } catch (err) {
        console.error(err)
        toast.error("Failed to load enroute details.")
        navigate("/bookings")
      } finally {
        setLoading(false)
      }
    }

    fetchBookingDetails()
  }, [bookingId, navigate])

  const handleSos = async () => {
    if (!bookingId || !booking) return
    const lat = assistantLat || booking.pickup_latitude
    const lon = assistantLon || booking.pickup_longitude

    setTriggeringSos(true)
    try {
      await sosService.triggerSos(parseInt(bookingId), lat, lon)
      setSosActive(true)
      toast.error("SOS Alert triggered! Secure dispatchers and emergency services notified.")
    } catch (err) {
      console.error(err)
      toast.error("Failed to trigger SOS alert automatically.")
    } finally {
      setTriggeringSos(false)
    }
  }

  if (loading || !booking) {
    return (
      <div className="h-[400px] w-full flex flex-col items-center justify-center gap-4 bg-background text-foreground">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-muted-foreground">Initializing live tracking metrics...</span>
      </div>
    )
  }

  const { user } = useAuth()
  const isRoleAssistant = (user?.role || "").toLowerCase() === "assistant" || (user?.role || "").toLowerCase() === "guide"

  const passengerName = booking?.guest_name || booking?.guest?.full_name || booking?.guest?.name || "Passenger"
  const passengerPhone = booking?.guest_phone || booking?.guest?.phone_number || ""
  
  const displayName = isRoleAssistant ? passengerName : (guide?.name || "Assigned Escort Guide")
  const displayInitials = displayName.trim().split(" ").filter(Boolean).map((w: string) => w[0]).join("").toUpperCase().slice(0, 2) || "U"
  const avatarSrc = isRoleAssistant
    ? (booking?.guest_avatar || booking?.guest?.profile_photo_url || null)
    : (guide?.profile_photo_url || booking?.assistant_avatar || guide?.avatar_url || null)

  const cleanPickupAddress = (booking.pickup_address || "Passenger Pickup Point").replace(/\s*\(Type:.*?\)/gi, "")

  return (
    <div className="h-[calc(100vh-80px)] w-full relative flex flex-col overflow-hidden bg-background">
      {/* Full Width Dynamic Map Area */}
      <div className="flex-1 w-full relative z-0">
        <Map
          viewport={{ center: mapCenter, zoom: 14.5 }}
          className="w-full h-full"
        >
          <MapControls />
          
          {/* Passenger Pickup Pin */}
          <MapMarker latitude={booking.pickup_latitude} longitude={booking.pickup_longitude}>
            <MarkerContent>
              <div className="flex flex-col items-center -translate-y-1/2">
                <div className="bg-primary text-primary-foreground p-2 rounded-full shadow-lg border-2 border-background">
                  <MapPinIcon size={18} weight="fill" />
                </div>
                <div className="w-1.5 h-1.5 bg-primary rounded-full mt-0.5 border border-background" />
              </div>
            </MarkerContent>
          </MapMarker>

          {/* Animating Assistant EnRoute Pin */}
          {markerCoords && (
            <MapMarker latitude={markerCoords.lat} longitude={markerCoords.lon}>
              <MarkerContent>
                <div className="flex flex-col items-center -translate-y-1/2">
                  <div className="bg-emerald-500 text-white p-2 rounded-full shadow-lg border-2 border-background animate-pulse">
                    <UserCheckIcon size={18} weight="fill" />
                  </div>
                  <div className="w-1.5 h-1.5 bg-emerald-500 rounded-full mt-0.5 border border-background animate-ping" />
                </div>
              </MarkerContent>
            </MapMarker>
          )}

          {/* Passenger Destination Pin */}
          {booking.destination_latitude && booking.destination_longitude && (
            <MapMarker latitude={booking.destination_latitude} longitude={booking.destination_longitude}>
              <MarkerContent>
                <div className="flex flex-col items-center -translate-y-1/2">
                  <div className="bg-rose-500 text-white p-2 rounded-full shadow-lg border-2 border-background">
                    <FlagIcon size={18} weight="fill" />
                  </div>
                  <div className="w-1.5 h-1.5 bg-rose-500 rounded-full mt-0.5 border border-background" />
                </div>
              </MarkerContent>
            </MapMarker>
          )}

          {/* Enroute Polyline connecting Assistant location to Passenger Pickup */}
          {enroutePolyline.length >= 2 && !isArrivedAtPickup && (
            <MapRoute
              id="enroute-assistant-to-pickup"
              coordinates={enroutePolyline}
              color="#10b981"
              width={4}
            />
          )}

          {/* Trip Polyline connecting Pickup to Destination */}
          {destinationPolyline.length >= 2 && (
            <MapRoute
              id="enroute-pickup-to-destination"
              coordinates={destinationPolyline}
              color="#f43f5e"
              width={4}
              opacity={0.8}
            />
          )}
        </Map>

        {/* Pulse ETA badge floating at top left */}
        <motion.div
          animate={etaControls}
          className="absolute top-4 left-4 z-10 max-w-[calc(100vw-120px)] sm:max-w-xs"
        >
          <Badge className="bg-background/95 border border-border text-foreground px-3.5 py-2 rounded-2xl shadow-xl flex items-center gap-2 font-bold backdrop-blur-md">
            <ClockIcon size={16} className="text-primary animate-spin-slow shrink-0" />
            <div className="text-left leading-tight min-w-0">
              <span className="text-[9px] uppercase tracking-wider text-muted-foreground block truncate">
                {isRoleAssistant
                  ? (isArrivedAtPickup ? "Pickup Status" : "En Route to Pickup")
                  : (isArrivedAtPickup ? "Guide Status" : "Guide Arriving in")}
              </span>
              <span className={`text-sm font-black block truncate ${isArrivedAtPickup ? "text-emerald-500" : "text-primary"}`}>
                {isArrivedAtPickup ? "Arrived at Pickup" : `${resolvedEta} mins`}
              </span>
              <span className="text-[10px] text-muted-foreground block font-semibold truncate">
                {isArrivedAtPickup
                  ? (isRoleAssistant ? "(At passenger location)" : "(Guide is at your location)")
                  : `(${resolvedDistance.toFixed(2)} km away)`}
              </span>
            </div>
          </Badge>
        </motion.div>

        {/* Persistent SOS Trigger Button Floating top right */}
        <div className="absolute top-4 right-4 z-10">
          <Button
            onClick={handleSos}
            disabled={triggeringSos || sosActive}
            className={`rounded-2xl py-5 px-3.5 sm:py-6 sm:px-4 shadow-xl border cursor-pointer hover:scale-[1.03] transition-transform ${
              sosActive
                ? "bg-destructive text-destructive-foreground border-destructive"
                : "bg-background text-destructive border-destructive/20 hover:bg-destructive/10"
            }`}
          >
            <SirenIcon size={20} weight="fill" className={sosActive ? "animate-bounce" : "animate-pulse"} />
            <span className="font-extrabold text-xs ml-1">{sosActive ? "SOS ACTIVE" : "SOS"}</span>
          </Button>
        </div>
      </div>

      {/* Professional Bottom Sheet Panel */}
      <Card className="absolute bottom-4 left-4 right-4 z-10 border border-border/80 shadow-2xl rounded-3xl bg-card/95 backdrop-blur-xl p-4 sm:p-5 space-y-4 max-w-4xl mx-auto overflow-hidden">
        <CardContent className="p-0 space-y-4 overflow-hidden">
          
          {/* Top Row: Info & Actions */}
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-border/50 pb-3.5">
            <div className="flex gap-3.5 items-center text-left min-w-0 flex-1">
              <Avatar className="w-13 h-13 sm:w-14 sm:h-14 rounded-2xl border border-border/80 shadow-sm shrink-0">
                {avatarSrc && <AvatarImage src={avatarSrc} alt={displayName} className="rounded-2xl object-cover" />}
                <AvatarFallback className="rounded-2xl bg-gradient-to-tr from-emerald-600 to-teal-500 text-white font-black text-base sm:text-lg">
                  {displayInitials}
                </AvatarFallback>
              </Avatar>

              <div className="min-w-0 space-y-1 flex-1">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="font-black text-base text-foreground truncate max-w-[180px] sm:max-w-[280px]">
                    {displayName}
                  </span>
                  <Badge className="bg-emerald-500/15 text-emerald-500 border-0 text-[10px] px-2 py-0.5 rounded-full font-bold flex items-center gap-1 shrink-0">
                    <ShieldCheckIcon size={12} weight="fill" />
                    Verified
                  </Badge>
                  {!isRoleAssistant && (
                    <span className="text-xs text-amber-500 font-extrabold flex items-center gap-0.5 shrink-0">
                      ★ {guide?.rating && Number(guide.rating) > 0 ? Number(guide.rating).toFixed(1) : (guide?.avg_rating && Number(guide.avg_rating) > 0 ? Number(guide.avg_rating).toFixed(1) : "New")}
                    </span>
                  )}
                </div>

                <div className="flex items-center gap-2 text-xs text-muted-foreground flex-wrap">
                  <span className="font-semibold truncate">
                    {isRoleAssistant ? "Passenger • Pickup Request" : `${guide?.total_trips ?? 1} Completed Trips`}
                  </span>
                  <span className="hidden sm:inline">•</span>
                  <span className="text-emerald-500 font-bold flex items-center gap-1 shrink-0">
                    <span className="w-2 h-2 rounded-full bg-emerald-500 animate-ping" />
                    {isArrivedAtPickup ? "Arrived at pickup" : "En Route to pickup"}
                  </span>
                </div>
              </div>
            </div>

            {/* Call / Message Quick Action Buttons */}
            <div className="flex items-center gap-2.5 w-full sm:w-auto shrink-0">
              <Button
                variant="outline"
                onClick={() => {
                  if (isRoleAssistant && passengerPhone) {
                    window.location.href = `tel:${passengerPhone}`
                  } else {
                    toast.success(isRoleAssistant ? `Calling passenger (${passengerPhone || "Encrypted"})...` : "Calling guide...")
                  }
                }}
                className="flex-1 sm:flex-initial rounded-xl py-3.5 px-4 font-bold text-xs gap-1.5 flex items-center justify-center cursor-pointer border-border hover:bg-emerald-500/10 hover:border-emerald-500/30 text-emerald-500"
              >
                <PhoneCallIcon size={16} weight="bold" />
                {isRoleAssistant ? "Call Passenger" : "Call Guide"}
              </Button>
              <Button
                variant="outline"
                onClick={() => toast.success("Live chat opened...")}
                className="flex-1 sm:flex-initial rounded-xl py-3.5 px-4 font-bold text-xs gap-1.5 flex items-center justify-center cursor-pointer border-border hover:bg-accent"
              >
                <ChatCircleTextIcon size={16} weight="bold" />
                Chat
              </Button>
            </div>
          </div>

          {/* Bottom Row: Ride Start OTP Card & ETA Details */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5 pt-1 items-stretch">
            {/* Start OTP Display Box for Guest / Verification Input for Assistant */}
            <div className="flex flex-col justify-between space-y-1.5 min-w-0">
              {isRoleAssistant ? (
                <OtpVerificationCard
                  bookingId={parseInt(bookingId!)}
                  onSuccess={() => navigate(`/trip/${bookingId}/tracking`)}
                  className="w-full shadow-sm"
                />
              ) : (
                <RideOtpCard otp={booking.otp_start || "123456"} className="w-full shadow-sm" />
              )}
              <span className="text-[10px] text-muted-foreground font-semibold text-left pl-1 block">
                {isRoleAssistant
                  ? "Ask passenger for their 6-digit OTP code upon arrival to verify and start the ride."
                  : "Share this 6-digit OTP code with your assistant upon arrival to start the ride."}
              </span>
            </div>

            {/* Estimated Arrival / Location Status Banner */}
            <div className="bg-muted/40 border border-border/80 rounded-xl p-3 px-3.5 flex items-center justify-between gap-3 text-left overflow-hidden min-w-0">
              <div className="space-y-0.5 min-w-0 flex-1">
                <span className="text-[9px] uppercase tracking-wider text-muted-foreground font-extrabold block truncate">
                  {isArrivedAtPickup ? "Location Status" : "Estimated Arrival"}
                </span>
                <span className={`text-sm font-black block truncate ${isArrivedAtPickup ? "text-emerald-500" : "text-foreground"}`}>
                  {isArrivedAtPickup ? "Arrived at Pickup Point" : `~${resolvedEta} mins (${resolvedDistance.toFixed(2)} km)`}
                </span>
                <span className="text-[10px] text-muted-foreground font-medium block truncate" title={cleanPickupAddress}>
                  Pickup: {cleanPickupAddress}
                </span>
              </div>
              <div className="w-9 h-9 rounded-xl bg-primary/10 border border-primary/20 text-primary flex items-center justify-center shrink-0">
                <ClockIcon size={20} weight="fill" />
              </div>
            </div>
          </div>

        </CardContent>
      </Card>
    </div>
  )
}
export default AssistantEnRoutePage
