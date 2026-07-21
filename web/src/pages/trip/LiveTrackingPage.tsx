import React, { useState, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { bookingsService } from "@/services/bookings.service"
import { trackingService } from "@/services/tracking.service"
import { assistantsService } from "@/services/assistants.service"
import { useTrackingSocket } from "@/hooks/useTrackingSocket"
import { useTrackingStore } from "@/store/tracking.store"
import { useAuth } from "@/store/auth-context"
import { RideOtpCard } from "@/components/booking/RideOtpCard"
import { OtpVerificationCard } from "@/components/booking/OtpVerificationCard"
import { RateAssistantDialog } from "@/components/booking/RateAssistantDialog"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Map, MapMarker, MarkerContent, MapRoute, MapControls } from "@/components/ui/map"
import { Button } from "@/components/ui/button"
import {
  Drawer,
  DrawerContent,
  DrawerHeader,
  DrawerTitle,
  DrawerDescription,
  DrawerTrigger
} from "@/components/ui/drawer"
import {
  MapPinIcon,
  ClockIcon,
  ShieldCheckIcon,
  CircleIcon,
  CheckCircleIcon,
  BroadcastIcon,
  WarningCircleIcon,
  UserCheckIcon,
  NavigationArrowIcon,
  CaretUpIcon,
  StarIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion, animate } from "framer-motion"

export function LiveTrackingPage() {
  const { user } = useAuth()
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()
  const token = localStorage.getItem("access_token")

  // WebSockets tracking states
  const {
    latitude: wsLat,
    longitude: wsLon,
    connectionStatus,
    updateCoordinates
  } = useTrackingStore()

  const [booking, setBooking] = useState<any>(null)
  const [guide, setGuide] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  // Current active coordinates to plot (interpolated smoothly)
  const [guestCoords, setGuestCoords] = useState<{ lat: number; lon: number } | null>(null)
  const [assistantCoords, setAssistantCoords] = useState<{ lat: number; lon: number } | null>(null)
  const [mobileDrawerOpen, setMobileDrawerOpen] = useState(false)
  const [viewport, setViewport] = useState({
    center: [77.2090, 28.6139] as [number, number],
    zoom: 13.5
  })

  // Auto-close mobile drawer on expanding window to desktop layout (lg: >= 1024px)
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth >= 1024) {
        setMobileDrawerOpen(false)
      }
    }

    window.addEventListener("resize", handleResize)
    return () => window.removeEventListener("resize", handleResize)
  }, [])

  // Poll fallback on WebSocket disconnect
  useEffect(() => {
    if (connectionStatus !== "disconnected" || !bookingId) return

    const pollInterval = setInterval(async () => {
      try {
        const data = await trackingService.getLiveCoordinates(parseInt(bookingId))
        if (data.latitude && data.longitude) {
          updateCoordinates(Number(data.latitude), Number(data.longitude))
        }
      } catch (err) {
        console.warn("REST fallback coordinates poll failed:", err)
      }
    }, 5000)

    return () => clearInterval(pollInterval)
  }, [connectionStatus, bookingId, updateCoordinates])

  const [showRatingDialog, setShowRatingDialog] = useState(false)

  // Setup sockets
  const handleStatusChange = (newStatus: string) => {
    toast.info(`Status update: ${newStatus}`)
    if (booking) {
      setBooking((prev: any) => prev ? { ...prev, status: newStatus } : null)
    }
    const st = newStatus.toUpperCase()
    if (st === "COMPLETED") {
      toast.success("Trip completed! Redirecting...")
      navigate(`/ride/completed/${bookingId}`, { replace: true })
    } else if (st === "CANCELLED" || st === "EXPIRED" || st === "NO_SHOW") {
      toast.error("Trip cancelled.")
      navigate(`/ride/cancelled/${bookingId}`, { replace: true })
    }
  }

  useTrackingSocket(
    bookingId ? parseInt(bookingId) : null,
    token,
    handleStatusChange
  )

  // Sync / Smoothly Animate Assistant marker position
  useEffect(() => {
    if (wsLat && wsLon) {
      if (!assistantCoords) {
        setAssistantCoords({ lat: wsLat, lon: wsLon })
      } else {
        const startLat = assistantCoords.lat
        const startLon = assistantCoords.lon

        animate(0, 1, {
          duration: 1.5,
          ease: "easeInOut",
          onUpdate: (latest) => {
            const currentLat = startLat + (wsLat - startLat) * latest
            const currentLon = startLon + (wsLon - startLon) * latest
            setAssistantCoords({ lat: currentLat, lon: currentLon })
          }
        })
      }
    }
  }, [wsLat, wsLon])

  // Auto-fit viewport bounds containing both markers
  useEffect(() => {
    if (guestCoords && assistantCoords) {
      const minLat = Math.min(guestCoords.lat, assistantCoords.lat)
      const maxLat = Math.max(guestCoords.lat, assistantCoords.lat)
      const minLng = Math.min(guestCoords.lon, assistantCoords.lon)
      const maxLng = Math.max(guestCoords.lon, assistantCoords.lon)

      const centerLon = (minLng + maxLng) / 2
      const centerLat = (minLat + maxLat) / 2

      const latDiff = Math.abs(maxLat - minLat)
      const lngDiff = Math.abs(maxLng - minLng)
      const maxDiff = Math.max(latDiff, lngDiff)

      let zoom = 14
      if (maxDiff > 0.05) zoom = 11
      else if (maxDiff > 0.02) zoom = 12
      else if (maxDiff > 0.01) zoom = 13

      setViewport({
        center: [centerLon, centerLat],
        zoom
      })
    }
  }, [guestCoords, assistantCoords])

  useEffect(() => {
    if (!bookingId) {
      navigate("/bookings")
      return
    }

    const fetchContext = async () => {
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

        setBooking(b)

        const guestLat = b.pickup_latitude
        const guestLon = b.pickup_longitude
        setGuestCoords({ lat: guestLat, lon: guestLon })

        // Initialize assistant coordinates & fetch real ratings profile
        if (b.assistant_id) {
          try {
            const g = await assistantsService.getAssistantProfile(b.assistant_id)
            const realRating = g.avg_rating ?? g.rating ?? 5.0
            setGuide({
              ...g,
              name: g.name || g.user?.full_name || b.assistant?.full_name || "Assigned Guide",
              rating: typeof realRating === "number" ? realRating.toFixed(1) : String(realRating)
            })
          } catch (e) {
            setGuide({
              id: b.assistant_id,
              name: b.assistant?.full_name || "Assigned Guide",
              rating: "5.0",
              total_trips: 0
            })
          }

          // Fetch last coordinates or fallback to guide approaching pickup point
          try {
            const loc = await trackingService.getLiveCoordinates(b.id)
            if (loc.latitude && loc.longitude) {
              setAssistantCoords({ lat: Number(loc.latitude), lon: Number(loc.longitude) })
              updateCoordinates(Number(loc.latitude), Number(loc.longitude))
            } else {
              // Guide initial position approaching pickup location
              const approachLat = Number(b.pickup_latitude) + 0.006
              const approachLon = Number(b.pickup_longitude) + 0.006
              setAssistantCoords({ lat: approachLat, lon: approachLon })
            }
          } catch (e) {
            const approachLat = Number(b.pickup_latitude) + 0.006
            const approachLon = Number(b.pickup_longitude) + 0.006
            setAssistantCoords({ lat: approachLat, lon: approachLon })
          }
        }
      } catch (err) {
        console.error(err)
        toast.error("Failed to load tracking coordinates.")
        navigate("/bookings")
      } finally {
        setLoading(false)
      }
    }

    fetchContext()
  }, [bookingId, navigate])

  const [destRoutePoints, setDestRoutePoints] = useState<[number, number][]>([])
  const [guideRoutePoints, setGuideRoutePoints] = useState<[number, number][]>([])

  // 1. Always fetch Main Journey Route: Pickup Point -> Destination Point
  useEffect(() => {
    if (!booking?.pickup_latitude || !booking?.destination_latitude) return

    const fetchDestRoute = async () => {
      try {
        const url = `https://router.project-osrm.org/route/v1/driving/${booking.pickup_longitude},${booking.pickup_latitude};${booking.destination_longitude},${booking.destination_latitude}?overview=full&geometries=geojson`
        const res = await fetch(url)
        const data = await res.json()
        if (data.routes && data.routes[0]?.geometry?.coordinates) {
          setDestRoutePoints(data.routes[0].geometry.coordinates)
        } else {
          setDestRoutePoints([
            [booking.pickup_longitude, booking.pickup_latitude],
            [booking.destination_longitude, booking.destination_latitude]
          ])
        }
      } catch (err) {
        setDestRoutePoints([
          [booking.pickup_longitude, booking.pickup_latitude],
          [booking.destination_longitude, booking.destination_latitude]
        ])
      }
    }

    fetchDestRoute()
  }, [booking?.pickup_latitude, booking?.pickup_longitude, booking?.destination_latitude, booking?.destination_longitude])

  // 2. Fetch Guide Approach Route: Guide Position -> Pickup Point (Pre-ride)
  useEffect(() => {
    if (!booking?.pickup_latitude || !assistantCoords) return
    const currentUpper = (booking.status || "").toUpperCase()
    const isStarted = ["STARTED", "IN_PROGRESS", "GUEST_PICKED_UP"].includes(currentUpper)
    if (isStarted) {
      setGuideRoutePoints([])
      return
    }

    const fetchGuideRoute = async () => {
      try {
        const url = `https://router.project-osrm.org/route/v1/driving/${assistantCoords.lon},${assistantCoords.lat};${booking.pickup_longitude},${booking.pickup_latitude}?overview=full&geometries=geojson`
        const res = await fetch(url)
        const data = await res.json()
        if (data.routes && data.routes[0]?.geometry?.coordinates) {
          setGuideRoutePoints(data.routes[0].geometry.coordinates)
        } else {
          setGuideRoutePoints([
            [assistantCoords.lon, assistantCoords.lat],
            [booking.pickup_longitude, booking.pickup_latitude]
          ])
        }
      } catch (err) {
        setGuideRoutePoints([
          [assistantCoords.lon, assistantCoords.lat],
          [booking.pickup_longitude, booking.pickup_latitude]
        ])
      }
    }

    fetchGuideRoute()
  }, [booking?.status, booking?.pickup_latitude, booking?.pickup_longitude, assistantCoords?.lat, assistantCoords?.lon])

  const handleFocusGuide = () => {
    if (assistantCoords) {
      setViewport({
        center: [assistantCoords.lon, assistantCoords.lat],
        zoom: 16
      })
      toast.info("Centered map on Guide's current location")
    } else {
      toast.error("Guide position not available yet.")
    }
  }

  const handleBroadcastCurrentLocation = () => {
    if (!navigator.geolocation) {
      toast.error("Geolocation is not supported by your browser.")
      return
    }

    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const lat = pos.coords.latitude
        const lon = pos.coords.longitude
        setAssistantCoords({ lat, lon })
        updateCoordinates(lat, lon)
        setViewport({
          center: [lon, lat],
          zoom: 16
        })

        if (bookingId) {
          try {
            await trackingService.updateCoordinates(parseInt(bookingId), lat, lon)
          } catch (err) {
            console.warn("GPS location pushed locally:", err)
          }
          toast.success("Updated map to your device's current GPS position!")
        }
      },
      (err) => {
        console.error("Geolocation error:", err)
        toast.error("Could not retrieve GPS location. Please allow location permissions in your browser.")
      },
      { enableHighAccuracy: true }
    )
  }

  // Auto-watch GPS position if user is the assigned Assistant
  useEffect(() => {
    const isAssistant = (user?.role || "").toLowerCase() === "assistant"
    if (!isAssistant || !bookingId || !navigator.geolocation) return

    const watchId = navigator.geolocation.watchPosition(
      async (pos) => {
        const lat = pos.coords.latitude
        const lon = pos.coords.longitude
        setAssistantCoords({ lat, lon })
        updateCoordinates(lat, lon)
        try {
          await trackingService.updateCoordinates(parseInt(bookingId), lat, lon)
        } catch (e) {
          // Silent catch
        }
      },
      (err) => console.warn("GPS watch position warning:", err),
      { enableHighAccuracy: true, maximumAge: 5000, timeout: 10000 }
    )

    return () => navigator.geolocation.clearWatch(watchId)
  }, [user?.role, bookingId])

  // Auto-fit viewport bounds containing Pickup, Destination, and Assistant
  useEffect(() => {
    if (booking?.pickup_latitude && booking?.destination_latitude) {
      const lats = [booking.pickup_latitude, booking.destination_latitude]
      const lons = [booking.pickup_longitude, booking.destination_longitude]
      if (assistantCoords) {
        lats.push(assistantCoords.lat)
        lons.push(assistantCoords.lon)
      }

      const minLat = Math.min(...lats)
      const maxLat = Math.max(...lats)
      const minLon = Math.min(...lons)
      const maxLon = Math.max(...lons)

      const centerLon = (minLon + maxLon) / 2
      const centerLat = (minLat + maxLat) / 2

      const latDiff = Math.abs(maxLat - minLat)
      const lngDiff = Math.abs(maxLon - minLon)
      const maxDiff = Math.max(latDiff, lngDiff)

      let zoom = 13
      if (maxDiff > 0.1) zoom = 10
      else if (maxDiff > 0.05) zoom = 11
      else if (maxDiff > 0.02) zoom = 12
      else if (maxDiff > 0.01) zoom = 13

      setViewport({
        center: [centerLon, centerLat],
        zoom
      })
    }
  }, [booking?.pickup_latitude, booking?.pickup_longitude, booking?.destination_latitude, booking?.destination_longitude, assistantCoords?.lat, assistantCoords?.lon])

  if (loading || !booking) {
    return (
      <div className="h-[400px] w-full flex flex-col items-center justify-center gap-4 bg-background text-foreground">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-muted-foreground">Initializing live tracking metrics...</span>
      </div>
    )
  }

  // Define steps for Timeline
  const steps = [
    { key: "PENDING", label: "Booking Request Made", desc: "Awaiting dispatcher confirmation" },
    { key: "ACCEPTED", label: "Guide Assigned", desc: "Assistant is arriving at pickup location" },
    { key: "STARTED", label: "Trip Started", desc: "Live navigation is active" },
    { key: "COMPLETED", label: "Journey Completed", desc: "Successfully navigated" }
  ]

  const getStepStatus = (stepKey: string) => {
    const statusOrder = ["PENDING", "ACCEPTED", "STARTED", "COMPLETED"]
    const currentUpper = (booking.status || "").toUpperCase()

    let normalizedCurrent = "PENDING"
    if (["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE", "ARRIVED_PICKUP"].includes(currentUpper)) {
      normalizedCurrent = "ACCEPTED"
    } else if (["GUEST_PICKED_UP", "IN_PROGRESS", "STARTED", "TRACKING"].includes(currentUpper)) {
      normalizedCurrent = "STARTED"
    } else if (currentUpper === "COMPLETED") {
      normalizedCurrent = "COMPLETED"
    }

    const currentIndex = statusOrder.indexOf(normalizedCurrent)
    const stepIndex = statusOrder.indexOf(stepKey)

    if (stepIndex < currentIndex) return "completed"
    if (stepIndex === currentIndex) return "active"
    return "pending"
  }

  return (
    <div className="h-[calc(100vh-80px)] w-full flex flex-col lg:flex-row overflow-hidden rounded-2xl border border-border/80 bg-background text-foreground shadow-xl relative">
      
      {/* Map Content Column (Full height on mobile and desktop) */}
      <div className="w-full h-full lg:flex-1 relative shrink-0 overflow-hidden bg-background">
        <Map
          viewport={viewport}
          onViewportChange={setViewport}
          className="w-full h-full"
        >
          <MapControls />

          {/* 1. Main Trip Route: Pickup -> Destination (Solid Primary Blue) */}
          {destRoutePoints.length > 0 && (
            <MapRoute id="main-dest-route" coordinates={destRoutePoints} color="#2563eb" width={5} opacity={0.9} />
          )}

          {/* 2. Guide Approach Route: Guide -> Pickup (Dashed Emerald Green) */}
          {guideRoutePoints.length > 0 && (
            <MapRoute id="guide-approach-route" coordinates={guideRoutePoints} color="#10b981" width={4} opacity={0.85} dashArray={[2, 2]} />
          )}
          
          {/* Guest Pickup Pin */}
          {guestCoords && (
            <MapMarker latitude={guestCoords.lat} longitude={guestCoords.lon}>
              <MarkerContent>
                <div className="flex flex-col items-center -translate-y-1/2">
                  <div className="bg-primary text-primary-foreground p-2.5 rounded-full shadow-xl border-2 border-background">
                    <MapPinIcon size={20} weight="fill" />
                  </div>
                  <span className="text-[10px] font-black bg-background/90 text-foreground px-2 py-0.5 rounded-md shadow border border-border mt-1 whitespace-nowrap">
                    Pickup Point
                  </span>
                </div>
              </MarkerContent>
            </MapMarker>
          )}

          {/* Destination Pin */}
          {booking.destination_latitude && booking.destination_longitude && (
            <MapMarker latitude={booking.destination_latitude} longitude={booking.destination_longitude}>
              <MarkerContent>
                <div className="flex flex-col items-center -translate-y-1/2">
                  <div className="bg-destructive text-destructive-foreground p-2.5 rounded-full shadow-xl border-2 border-background">
                    <MapPinIcon size={20} weight="fill" />
                  </div>
                  <span className="text-[10px] font-black bg-background/90 text-foreground px-2 py-0.5 rounded-md shadow border border-border mt-1 whitespace-nowrap">
                    Destination
                  </span>
                </div>
              </MarkerContent>
            </MapMarker>
          )}

          {/* Assistant Pin */}
          {assistantCoords && (
            <MapMarker latitude={assistantCoords.lat} longitude={assistantCoords.lon}>
              <MarkerContent>
                <div className="flex flex-col items-center -translate-y-1/2">
                  <div className="bg-emerald-600 text-white p-2.5 rounded-full shadow-xl border-2 border-background ring-4 ring-emerald-600/30">
                    <UserCheckIcon size={20} weight="fill" />
                  </div>
                  <span className="text-[10px] font-black bg-emerald-600 text-white px-2 py-0.5 rounded-md shadow border border-background mt-1 whitespace-nowrap">
                    Escort Guide
                  </span>
                </div>
              </MarkerContent>
            </MapMarker>
          )}

          {/* Floating Map Status Badge (Top Left) */}
          <div className="absolute top-3 left-3 z-30 flex items-center gap-1.5">
            {connectionStatus === "connecting" && (
              <div
                title="Reconnecting to GPS Server..."
                className="w-8 h-8 rounded-full bg-amber-500/20 border border-amber-500/30 text-amber-400 shadow-md backdrop-blur-md flex items-center justify-center cursor-help shrink-0"
              >
                <WarningCircleIcon size={16} className="animate-spin" />
              </div>
            )}
            {connectionStatus === "connected" && (
              <div
                title="Live Sync Connected"
                className="w-8 h-8 rounded-full bg-success/20 border border-success/30 text-success shadow-md backdrop-blur-md flex items-center justify-center cursor-help shrink-0"
              >
                <BroadcastIcon size={16} />
              </div>
            )}
          </div>

          {/* Floating Map Quick Actions (Top Right Glassmorphic Action Pill) */}
          <div className="absolute top-3 right-3 sm:right-[195px] z-30 flex items-center gap-1 p-1 rounded-full border border-border/80 bg-background/90 shadow-md backdrop-blur-md">
            <Button
              variant="ghost"
              size="icon"
              onClick={handleFocusGuide}
              className="w-7 h-7 sm:w-8 sm:h-8 rounded-full text-foreground hover:bg-muted/80 cursor-pointer transition-all active:scale-95 shrink-0"
              title="Center map on Guide"
            >
              <UserCheckIcon size={16} className="text-emerald-500" />
              <span className="sr-only">Center Guide</span>
            </Button>

            <div className="w-[1px] h-4 bg-border/80 shrink-0" />

            <Button
              variant="default"
              size="icon"
              onClick={handleBroadcastCurrentLocation}
              className="w-7 h-7 sm:w-8 sm:h-8 rounded-full bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs cursor-pointer transition-all active:scale-95 shrink-0"
              title="Snap to device GPS location"
            >
              <NavigationArrowIcon size={15} />
              <span className="sr-only">Snap GPS</span>
            </Button>
          </div>
        </Map>

        {/* Mobile Bottom Bar Overlay (lg:hidden) */}
        <div className="absolute bottom-0 left-0 right-0 z-30 bg-card/95 backdrop-blur-xl border-t border-border/80 p-3 space-y-2 rounded-t-2xl shadow-2xl lg:hidden">
          <div className="w-10 h-1 bg-muted-foreground/30 rounded-full mx-auto mb-1" />

          {/* Assigned Guide & Rating Header */}
          <div className="w-full flex items-center gap-2.5 text-left px-0.5">
            <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-emerald-600 to-teal-500 text-white font-black text-xs flex items-center justify-center shadow-xs border border-white/20 shrink-0">
              {(guide?.name || "Eswar")[0]}
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-1.5">
                <span className="text-xs font-extrabold text-foreground truncate">{guide?.name || "Eswar Chinthakayala"}</span>
                <Badge className="bg-amber-500/15 text-amber-400 border border-amber-500/30 rounded-md text-[9px] font-black px-1.5 py-0.2 flex items-center gap-0.5 shrink-0">
                  <StarIcon size={10} weight="fill" />
                  <span>{guide?.rating && Number(guide.rating) > 0 ? Number(guide.rating).toFixed(1) : (guide?.avg_rating && Number(guide.avg_rating) > 0 ? Number(guide.avg_rating).toFixed(1) : "New")}</span>
                </Badge>
              </div>
              <span className="text-[9px] uppercase tracking-wider text-muted-foreground font-semibold block">Assigned Escort Guide</span>
            </div>
          </div>

          {/* OTP Card on Mobile Bottom Bar */}
          {(user?.role || "").toLowerCase() !== "assistant" &&
            !["STARTED", "IN_PROGRESS", "COMPLETED", "CANCELLED"].includes((booking.status || "").toUpperCase()) && (
              <RideOtpCard otp={booking.otp_start || "123456"} className="w-full" />
          )}

          {(user?.role || "").toLowerCase() === "assistant" &&
            !["STARTED", "IN_PROGRESS", "COMPLETED", "CANCELLED"].includes((booking.status || "").toUpperCase()) && (
              <OtpVerificationCard bookingId={booking.id} onSuccess={() => navigate("/bookings")} className="w-full" />
          )}

          {/* Drawer Trigger Button on Mobile */}
          <Drawer open={mobileDrawerOpen} onOpenChange={setMobileDrawerOpen}>
            <DrawerTrigger className="w-full h-8 text-[10px] font-black rounded-xl border border-border/80 bg-muted/20 hover:bg-muted/40 text-foreground flex items-center justify-center gap-1.5 cursor-pointer shadow-xs transition-colors">
              <CaretUpIcon size={12} weight="bold" />
              <span>View Booking Progress & Timeline</span>
            </DrawerTrigger>

            <DrawerContent className="p-4 space-y-4 max-h-[85vh] bg-card border-t border-border/80 rounded-t-3xl overflow-y-auto">
              <DrawerHeader className="p-0 pb-2 border-b border-border/50 text-left">
                <DrawerTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
                  <CheckCircleIcon size={18} className="text-primary" />
                  Booking Progress Lifecycle
                </DrawerTitle>
                <DrawerDescription className="text-xs">Live tracking milestone updates</DrawerDescription>
              </DrawerHeader>

              {/* Drawer Timeline Steps */}
              <div className="relative pl-6 space-y-4 py-2">
                <div className="absolute left-[11px] top-1 bottom-1 w-0.5 bg-border border-dashed" />
                
                {steps.map((step) => {
                  const status = getStepStatus(step.key)
                  return (
                    <div key={step.key} className="relative text-left space-y-0.5">
                      <div className="absolute -left-[20px] top-0.5 bg-card shrink-0">
                        {status === "completed" ? (
                          <CheckCircleIcon size={18} className="text-success" weight="fill" />
                        ) : status === "active" ? (
                          <div className="w-4 h-4 rounded-full bg-primary ring-4 ring-primary/30 border-2 border-background" />
                        ) : (
                          <CircleIcon size={14} className="text-muted-foreground/40" />
                        )}
                      </div>
                      <h5 className={`text-xs font-bold ${
                        status === "active" ? "text-primary" : status === "completed" ? "text-foreground" : "text-muted-foreground/60"
                      }`}>
                        {step.label}
                      </h5>
                      <p className="text-[10px] text-muted-foreground">{step.desc}</p>
                    </div>
                  )
                })}
              </div>

              <div className="border-t border-border/50 pt-3">
                <div className="w-full flex items-center gap-2.5 text-left mb-2.5">
                  <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-emerald-600 to-teal-500 text-white font-black text-xs flex items-center justify-center shadow-xs border border-white/20 shrink-0">
                    {(guide?.name || "Eswar")[0]}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-1.5">
                      <span className="text-xs font-extrabold text-foreground truncate">{guide?.name || "Eswar Chinthakayala"}</span>
                      <Badge className="bg-amber-500/15 text-amber-400 border border-amber-500/30 rounded-md text-[9px] font-black px-1.5 py-0.2 flex items-center gap-0.5 shrink-0">
                        <StarIcon size={10} weight="fill" />
                        <span>{guide?.rating ?? guide?.avg_rating ?? "5.0"}</span>
                      </Badge>
                    </div>
                    <span className="text-[9px] uppercase tracking-wider text-muted-foreground font-semibold block">Assigned Escort Guide</span>
                  </div>
                </div>

                {(user?.role || "").toLowerCase() !== "assistant" &&
                  !["STARTED", "IN_PROGRESS", "COMPLETED", "CANCELLED"].includes((booking.status || "").toUpperCase()) && (
                    <RideOtpCard otp={booking.otp_start || "123456"} className="w-full" />
                )}

                {(user?.role || "").toLowerCase() === "assistant" &&
                  !["STARTED", "IN_PROGRESS", "COMPLETED", "CANCELLED"].includes((booking.status || "").toUpperCase()) && (
                    <OtpVerificationCard bookingId={booking.id} onSuccess={() => navigate("/bookings")} className="w-full" />
                )}
              </div>
            </DrawerContent>
          </Drawer>
        </div>
      </div>

      {/* Desktop Sidebar Console Column (hidden on mobile, visible lg:flex) */}
      <Card className="hidden lg:flex w-80 h-full shrink-0 border-l border-border/80 bg-card rounded-r-2xl overflow-y-auto flex-col">
        <CardHeader className="border-b border-border/50 p-3.5 bg-muted/20 shrink-0">
          <CardTitle className="text-xs font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
            <CheckCircleIcon size={16} className="text-primary" />
            Booking Progress
          </CardTitle>
          <CardDescription className="text-[10px]">Live tracking lifecycle</CardDescription>
        </CardHeader>

        <CardContent className="p-4 flex-1 space-y-3.5 overflow-y-auto">
          {/* Vertical Progress Timeline */}
          <div className="relative pl-5 space-y-3.5">
            <div className="absolute left-[9px] top-1 bottom-1 w-0.5 bg-border border-dashed" />
            
            {steps.map((step) => {
              const status = getStepStatus(step.key)
              return (
                <div key={step.key} className="relative text-left space-y-0.5">
                  {/* Timeline circle icon */}
                  <div className="absolute -left-[18px] top-0.5 bg-card shrink-0">
                    {status === "completed" ? (
                      <CheckCircleIcon size={16} className="text-success" weight="fill" />
                    ) : status === "active" ? (
                      <div className="w-3.5 h-3.5 rounded-full bg-primary ring-4 ring-primary/30 border-2 border-background" />
                    ) : (
                      <CircleIcon size={12} className="text-muted-foreground/40" />
                    )}
                  </div>
                  <h5 className={`text-[11px] font-bold ${
                    status === "active" ? "text-primary" : status === "completed" ? "text-foreground" : "text-muted-foreground/60"
                  }`}>
                    {step.label}
                  </h5>
                  <p className="text-[9px] text-muted-foreground">{step.desc}</p>
                </div>
              )
            })}
          </div>
        </CardContent>

        {/* Role-based Bottom Panel */}
        <CardFooter className="border-t border-border/50 p-3.5 bg-muted/10 flex flex-col gap-2.5 shrink-0">
          <div className="w-full flex items-center gap-2.5 text-left">
            <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-emerald-600 to-teal-500 text-white font-black text-xs flex items-center justify-center shadow-xs border border-white/20 shrink-0">
              {(guide?.name || "Eswar")[0]}
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-1.5">
                <span className="text-xs font-extrabold text-foreground truncate">{guide?.name || "Eswar Chinthakayala"}</span>
                <Badge className="bg-amber-500/15 text-amber-400 border border-amber-500/30 rounded-md text-[9px] font-black px-1.5 py-0.2 flex items-center gap-0.5 shrink-0">
                  <StarIcon size={10} weight="fill" />
                  <span>{guide?.rating && Number(guide.rating) > 0 ? Number(guide.rating).toFixed(1) : (guide?.avg_rating && Number(guide.avg_rating) > 0 ? Number(guide.avg_rating).toFixed(1) : "New")}</span>
                </Badge>
              </div>
              <span className="text-[9px] uppercase tracking-wider text-muted-foreground font-semibold block">Assigned Escort Guide</span>
            </div>
          </div>

          {/* Passenger View: Ride OTP Display */}
          {(user?.role || "").toLowerCase() !== "assistant" &&
            !["STARTED", "IN_PROGRESS", "COMPLETED", "CANCELLED"].includes((booking.status || "").toUpperCase()) && (
              <RideOtpCard otp={booking.otp_start || "123456"} className="w-full" />
          )}

          {/* Assistant View: OTP Verification Card */}
          {(user?.role || "").toLowerCase() === "assistant" &&
            !["STARTED", "IN_PROGRESS", "COMPLETED", "CANCELLED"].includes((booking.status || "").toUpperCase()) && (
              <OtpVerificationCard bookingId={booking.id} onSuccess={() => navigate("/bookings")} className="w-full" />
          )}
        </CardFooter>
      </Card>

      {booking && (
        <RateAssistantDialog
          open={showRatingDialog}
          onOpenChange={setShowRatingDialog}
          bookingId={booking.id}
          guideName={guide?.name}
          guideAvatar={guide?.profile_photo_url}
          onSuccess={() => navigate("/bookings")}
        />
      )}
    </div>
  )
}
export default LiveTrackingPage
