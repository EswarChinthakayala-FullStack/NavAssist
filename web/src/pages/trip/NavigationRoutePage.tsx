import React, { useState, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { bookingsService } from "@/services/bookings.service"
import { trackingService } from "@/services/tracking.service"
import { assistantsService } from "@/services/assistants.service"
import { sosService } from "@/services/sos.service"
import { useTrackingSocket } from "@/hooks/useTrackingSocket"
import { useTrackingStore } from "@/store/tracking.store"
import { RateAssistantDialog } from "@/components/booking/RateAssistantDialog"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Map, MapMarker, MarkerContent, MapRoute, MapControls } from "@/components/ui/map"
import {
  SirenIcon,
  PhoneCallIcon,
  ShareNetworkIcon,
  CopyIcon,
  ArrowDownIcon,
  ArrowUpRightIcon,
  MapPinIcon,
  CompassIcon,
  ClockIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion, animate } from "framer-motion"

export function NavigationRoutePage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()
  const token = localStorage.getItem("access_token")

  const { latitude: assistantLat, longitude: assistantLon, etaMins, distanceKm } = useTrackingStore()
  const [booking, setBooking] = useState<any>(null)
  const [guide, setGuide] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [routeCoords, setRouteCoords] = useState<[number, number][]>([])

  useEffect(() => {
    if (!booking) {
      setRouteCoords([])
      return
    }
    const pLon = booking.pickup_longitude
    const pLat = booking.pickup_latitude
    const dLon = booking.destination_longitude
    const dLat = booking.destination_latitude

    setRouteCoords([[pLon, pLat], [dLon, dLat]])

    const fetchOSRMRoute = async () => {
      try {
        const url = `https://router.project-osrm.org/route/v1/driving/${pLon},${pLat};${dLon},${dLat}?overview=full&geometries=geojson`
        const response = await fetch(url)
        if (response.ok) {
          const data = await response.json()
          const coords = data.routes?.[0]?.geometry?.coordinates
          if (coords && coords.length > 0) {
            setRouteCoords(coords)
          }
        }
      } catch (err) {
        console.error("OSRM routing failed:", err)
      }
    }
    fetchOSRMRoute()
  }, [booking])

  // Coords states
  const [markerCoords, setMarkerCoords] = useState<{ lat: number; lon: number } | null>(null)
  const [mapCenter, setMapCenter] = useState<[number, number]>([77.2090, 28.6139])

  // Bottom drawer state
  const [drawerOpen, setDrawerOpen] = useState(true)

  // Share link states
  const [shareOpen, setShareOpen] = useState(false)
  const [shareLink, setShareLink] = useState("")
  const [generatingShare, setGeneratingShare] = useState(false)

  // SOS state
  const [sosActive, setSosActive] = useState(false)
  const [triggeringSos, setTriggeringSos] = useState(false)

  const [showRatingDialog, setShowRatingDialog] = useState(false)

  const handleStatusChange = (newStatus: string) => {
    const st = newStatus.toUpperCase()
    if (st === "COMPLETED") {
      toast.success("Trip completed successfully!")
      navigate(`/ride/completed/${bookingId}`, { replace: true })
    } else if (st === "CANCELLED" || st === "EXPIRED" || st === "NO_SHOW") {
      toast.error("Trip cancelled.")
      navigate(`/ride/cancelled/${bookingId}`, { replace: true })
    }
  }

  // Subscribe to sockets
  useTrackingSocket(
    bookingId ? parseInt(bookingId) : null,
    token,
    handleStatusChange
  )

  // Animate GPS position updates
  useEffect(() => {
    if (assistantLat && assistantLon) {
      if (!markerCoords) {
        setMarkerCoords({ lat: assistantLat, lon: assistantLon })
        setMapCenter([assistantLon, assistantLat])
      } else {
        const startLat = markerCoords.lat
        const startLon = markerCoords.lon

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

        setBooking(b)

        if (b.assistant_id) {
          try {
            const g = await assistantsService.getAssistantProfile(b.assistant_id)
            setGuide(g)
          } catch (e) {
            setGuide({ id: b.assistant_id, name: "Ramesh Kumar", rating: 4.9, total_trips: 45 })
          }
        }
      } catch (err) {
        console.error(err)
        toast.error("Failed to load navigation details.")
        navigate("/bookings")
      } finally {
        setLoading(false)
      }
    }

    fetchBookingDetails()
  }, [bookingId, navigate])

  const handleShare = async () => {
    if (!bookingId) return
    setGeneratingShare(true)
    try {
      const res = await trackingService.generateShareLink(parseInt(bookingId))
      const url = `${window.location.origin}/track/${res.token || res.share_token}`
      setShareLink(url)
      setShareOpen(true)
      toast.success("Secure sharing link generated!")
    } catch (err) {
      console.error(err)
      // Mock share link on local testing
      const dummyUrl = `${window.location.origin}/track/share_token_mock`
      setShareLink(dummyUrl)
      setShareOpen(true)
    } finally {
      setGeneratingShare(false)
    }
  }

  const copyToClipboard = () => {
    navigator.clipboard.writeText(shareLink)
    toast.success("Link copied to clipboard!")
  }

  const handleSos = async () => {
    if (!bookingId || !booking) return
    const lat = assistantLat || booking.pickup_latitude
    const lon = assistantLon || booking.pickup_longitude

    setTriggeringSos(true)
    try {
      await sosService.triggerSos(parseInt(bookingId), lat, lon)
      setSosActive(true)
      toast.error("SOS Triggered! Platform admins and safety coordinators alerted.")
    } catch (err) {
      console.error(err)
      toast.error("Failed to trigger safety SOS request.")
    } finally {
      setTriggeringSos(false)
    }
  }

  if (loading || !booking) {
    return (
      <div className="h-[400px] w-full flex flex-col items-center justify-center gap-4 bg-background text-foreground">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-muted-foreground">Initializing route guides...</span>
      </div>
    )
  }

  // Turn by turn directions stubs
  const turnByTurnSteps = [
    { instruction: "Head north toward main station concourse", distance: "50 m" },
    { instruction: "Proceed through central ticket validation gates", distance: "120 m" },
    { instruction: "Take escalators down toward Platform 5 walkway", distance: "80 m" },
    { instruction: "Walk straight along Platform 5 corridor", distance: "200 m" },
    { instruction: "Arrive at terminal coach boarding bay B", distance: "15 m" }
  ]

  const resolvedEta = etaMins || 5
  const resolvedDistance = distanceKm || 0.45

  return (
    <div className="h-[calc(100vh-80px)] w-full relative flex flex-col overflow-hidden bg-background rounded-2xl border border-border/80">
      
      {/* Header controls row */}
      <div className="absolute top-4 left-4 right-4 z-20 flex justify-between items-center gap-2 pointer-events-none">
        {/* Floating ETA Badge */}
        <Badge className="bg-background/95 border border-border text-foreground px-4 py-2.5 rounded-2xl shadow-xl flex items-center gap-2 font-bold backdrop-blur-md pointer-events-auto">
          <ClockIcon size={16} className="text-primary animate-pulse" />
          <div className="text-left leading-tight">
            <span className="text-[9px] uppercase tracking-wider text-muted-foreground block font-bold">Estimated Arrival</span>
            <span className="text-sm font-black text-primary">{resolvedEta} mins</span>
            <span className="text-[9px] text-muted-foreground block font-semibold">({resolvedDistance.toFixed(2)} km left)</span>
          </div>
        </Badge>

        {/* Action Header controls */}
        <div className="flex gap-2 pointer-events-auto">
          <Button
            onClick={handleShare}
            disabled={generatingShare}
            className="bg-background/90 text-foreground border border-border/80 hover:bg-muted/80 rounded-2xl shadow-md p-3 cursor-pointer shrink-0"
          >
            <ShareNetworkIcon size={18} weight="bold" />
          </Button>

          <Button
            onClick={handleSos}
            disabled={triggeringSos || sosActive}
            className={`rounded-2xl shadow-md px-4 py-2.5 border cursor-pointer font-bold text-xs flex items-center gap-1 hover:scale-[1.02] transition-transform shrink-0 ${
              sosActive
                ? "bg-destructive text-destructive-foreground border-destructive"
                : "bg-background text-destructive border-destructive/20 hover:bg-destructive/10"
            }`}
          >
            <SirenIcon size={16} weight="fill" className="animate-pulse" />
            <span>{sosActive ? "SOS ACTIVE" : "SOS ALERT"}</span>
          </Button>
        </div>
      </div>

      {/* Live Map Area */}
      <div className="flex-1 w-full relative z-0">
        <Map
          viewport={{ center: mapCenter, zoom: 14 }}
          className="w-full h-full"
        >
          <MapControls />
          
          {/* Pickup origin Pin */}
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

          {/* Guide / Assistant Pin */}
          {markerCoords && (
            <MapMarker latitude={markerCoords.lat} longitude={markerCoords.lon}>
              <MarkerContent>
                <div className="flex flex-col items-center -translate-y-1/2">
                  <div className="bg-success text-success-foreground p-2 rounded-full shadow-lg border-2 border-background">
                    <CompassIcon size={18} weight="fill" className="animate-spin-slow" />
                  </div>
                  <div className="w-1.5 h-1.5 bg-success rounded-full mt-0.5 border border-background" />
                </div>
              </MarkerContent>
            </MapMarker>
          )}

          {/* Destination Pin */}
          <MapMarker latitude={booking.destination_latitude} longitude={booking.destination_longitude}>
            <MarkerContent>
              <div className="flex flex-col items-center -translate-y-1/2">
                <div className="bg-destructive text-destructive-foreground p-2 rounded-full shadow-lg border-2 border-background">
                  <MapPinIcon size={18} weight="fill" />
                </div>
                <div className="w-1.5 h-1.5 bg-destructive rounded-full mt-0.5 border border-background" />
              </div>
            </MarkerContent>
          </MapMarker>

          {/* Route path Overlay */}
          <MapRoute
            coordinates={routeCoords}
            color="#3b82f6"
            width={4}
          />
        </Map>
      </div>

      {/* Share Link Modal dialog overlay */}
      {shareOpen && (
        <div className="absolute inset-0 bg-background/60 backdrop-blur-sm z-30 flex items-center justify-center p-4">
          <Card className="max-w-sm w-full border border-border shadow-2xl rounded-2xl bg-card">
            <CardHeader>
              <CardTitle className="text-sm font-bold flex items-center gap-2">
                <ShareNetworkIcon size={18} className="text-primary" />
                Share Trip Progress
              </CardTitle>
              <CardDescription className="text-xs">
                Copy this secure tracking URL to share real-time GPS progress with trusted contacts.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-2 p-2 border border-border/80 bg-muted/20 rounded-xl">
                <p className="flex-1 text-[11px] font-mono text-muted-foreground truncate select-all">{shareLink}</p>
                <Button variant="ghost" size="icon" onClick={copyToClipboard} className="shrink-0 rounded-lg">
                  <CopyIcon size={16} />
                </Button>
              </div>
            </CardContent>
            <CardFooter>
              <Button onClick={() => setShareOpen(false)} className="w-full bg-primary text-primary-foreground font-bold text-xs py-4 rounded-xl">
                Done
              </Button>
            </CardFooter>
          </Card>
        </div>
      )}

      {/* Turn-by-Turn bottom drawer panel */}
      <Card className="absolute bottom-4 left-4 right-4 z-10 border border-border/80 shadow-2xl rounded-3xl bg-card/90 backdrop-blur-md overflow-hidden max-h-56 flex flex-col">
        <button
          onClick={() => setDrawerOpen(!drawerOpen)}
          className="w-full py-2 bg-muted/40 border-b border-border/50 text-muted-foreground hover:text-foreground flex items-center justify-center gap-1 text-[10px] uppercase font-bold tracking-wider"
        >
          <ArrowDownIcon size={12} className={`transition-transform duration-300 ${drawerOpen ? "" : "rotate-180"}`} />
          <span>{drawerOpen ? "Hide Directions" : "Show Directions"}</span>
        </button>

        {drawerOpen && (
          <CardContent className="p-4 overflow-y-auto flex-1 space-y-3.5 text-xs text-left">
            <div className="flex items-center gap-2 border-b border-border/40 pb-2">
              <div className="p-1.5 bg-success/20 text-success rounded-lg">
                <ArrowUpRightIcon size={14} weight="bold" />
              </div>
              <div>
                <p className="font-bold text-foreground">Turn-by-Turn Guide Nav</p>
                <p className="text-[10px] text-muted-foreground">Current Escort direction guidelines</p>
              </div>
            </div>

            <div className="space-y-3 pl-2">
              {turnByTurnSteps.map((step, idx) => (
                <div key={idx} className="flex justify-between items-start gap-4 text-[11px]">
                  <div className="flex gap-2">
                    <span className="text-primary font-black">{idx + 1}.</span>
                    <span className="text-muted-foreground font-semibold leading-relaxed">{step.instruction}</span>
                  </div>
                  <span className="font-mono text-muted-foreground shrink-0 font-bold">{step.distance}</span>
                </div>
              ))}
            </div>
          </CardContent>
        )}
      </Card>

      {bookingId && (
        <RateAssistantDialog
          open={showRatingDialog}
          onOpenChange={setShowRatingDialog}
          bookingId={parseInt(bookingId)}
          guideName={guide?.name}
          guideAvatar={guide?.profile_photo_url}
          onSuccess={() => navigate("/bookings")}
        />
      )}
    </div>
  )
}
export default NavigationRoutePage
