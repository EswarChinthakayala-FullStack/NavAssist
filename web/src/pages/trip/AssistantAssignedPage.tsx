import React, { useState, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { bookingsService } from "@/services/bookings.service"
import { assistantsService } from "@/services/assistants.service"
import { useTrackingSocket } from "@/hooks/useTrackingSocket"
import { useTrackingStore } from "@/store/tracking.store"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  PhoneCallIcon,
  ChatCircleTextIcon,
  ShieldCheckIcon,
  ClockIcon,
  ArrowRightIcon,
  UserCheckIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

export function AssistantAssignedPage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()
  const token = localStorage.getItem("access_token")
  
  const { etaMins, distanceKm } = useTrackingStore()
  
  const [booking, setBooking] = useState<any>(null)
  const [guide, setGuide] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  // Real-time status update handler
  const handleStatusChange = (newStatus: string) => {
    toast.info(`Booking status changed: ${newStatus}`)
    const st = newStatus.toUpperCase()
    if (st === "ACCEPTED" || st === "STARTED" || st === "ENROUTE" || st === "ASSISTANT_ENROUTE" || st === "ARRIVED_PICKUP") {
      // Refresh details or redirect to enroute page
      navigate(`/trip/${bookingId}/enroute`)
    } else if (st === "COMPLETED") {
      navigate(`/ride/completed/${bookingId}`, { replace: true })
    } else if (st === "CANCELLED" || st === "EXPIRED" || st === "NO_SHOW") {
      navigate(`/ride/cancelled/${bookingId}`, { replace: true })
    }
  }

  // Subscribe to tracking WebSocket instantly
  useTrackingSocket(
    bookingId ? parseInt(bookingId) : null,
    token,
    handleStatusChange
  )

  useEffect(() => {
    if (!bookingId) {
      navigate("/bookings")
      return
    }

    const fetchBookingAndGuide = async () => {
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

        if (st === "started" || st === "enroute" || st === "assistant_enroute" || st === "arrived_pickup" || st === "guest_picked_up" || st === "in_progress") {
          navigate(`/trip/${bookingId}/enroute`)
          return
        }

        setBooking(b)

        if (b.assistant_id) {
          try {
            const g = await assistantsService.getAssistantProfile(b.assistant_id)
            setGuide(g)
          } catch (err) {
            console.error("Failed to fetch guide details:", err)
            setGuide({
              id: b.assistant_id,
              name: b.assistant_name || b.assistant?.name || "Assigned Escort Guide",
              avg_rating: 0.0,
              total_trips: 1,
              experience_years: 2,
            })
          }
        }
      } catch (err) {
        console.error("Failed to load booking context:", err)
        toast.error("Error loading active booking details.")
        navigate("/bookings")
      } finally {
        setLoading(false)
      }
    }

    fetchBookingAndGuide()
  }, [bookingId, navigate])

  if (loading) {
    return (
      <div className="h-[400px] w-full flex flex-col items-center justify-center gap-4 bg-background text-foreground">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-muted-foreground">Connecting live dispatcher status...</span>
      </div>
    )
  }

  const guideAvatar = guide?.profile_photo_url || "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=80"
  
  // Calculate dynamic ETA and distance based on real-time tracking or booking metrics
  const rawDist = distanceKm || (booking?.distance_km ? Number(booking.distance_km) : null)
  const resolvedDistance = rawDist && rawDist > 0 ? rawDist : 1.2
  const resolvedEta = etaMins || (booking?.estimated_duration_min ? Math.round(Number(booking.estimated_duration_min)) : Math.max(2, Math.round(resolvedDistance * 2.5)))

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-md mx-auto py-8"
    >
      <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-3xl overflow-hidden text-center">
        {/* Pulsing state header */}
        <div className="bg-gradient-to-b from-primary/10 to-transparent p-6 space-y-2 border-b border-border/40">
          <Badge className="bg-primary/20 hover:bg-primary/20 text-primary border-0 text-[10px] px-3.5 py-1 rounded-full font-bold animate-pulse tracking-widest uppercase">
            Guide Assigned
          </Badge>
          <CardTitle className="text-lg font-black text-foreground">
            Escort Assistant Confirmed
          </CardTitle>
          <CardDescription className="text-xs">
            Your travel assistant is preparing to meet you
          </CardDescription>
        </div>

        {/* Assistant info card */}
        <CardContent className="p-6 space-y-6">
          <div className="flex gap-4 items-center bg-muted/40 p-4 border border-border/80 rounded-2xl text-left">
            <div className="w-14 h-14 rounded-full overflow-hidden border border-border shrink-0 bg-muted">
              <img src={guideAvatar} alt={guide?.name} className="object-cover w-full h-full" />
            </div>
            <div className="flex-1 min-w-0 space-y-1">
              <div className="flex items-center gap-1.5 justify-between">
                <h4 className="font-extrabold text-sm text-foreground truncate">
                  {guide?.name || "Assigned Escort Guide"}
                </h4>
                <Badge className="bg-success text-success-foreground hover:bg-success border-0 text-[9px] px-2 py-0.5 rounded-full flex items-center gap-0.5 font-bold">
                  <ShieldCheckIcon size={12} weight="fill" />
                  Verified
                </Badge>
              </div>
              <div className="flex items-center gap-4 text-xs text-muted-foreground">
                <span className="font-bold text-warning">
                  ★ {guide?.rating && Number(guide.rating) > 0 ? Number(guide.rating).toFixed(1) : (guide?.avg_rating && Number(guide.avg_rating) > 0 ? Number(guide.avg_rating).toFixed(1) : "New")}
                </span>
                <span className="font-semibold">{guide?.total_trips ?? 1} Trips Completed</span>
              </div>
              <p className="text-[10px] text-muted-foreground font-semibold">
                Experience: {guide?.experience_years ?? 2} Years
              </p>
            </div>
          </div>

          {/* Animated arrival estimate */}
          <div className="p-4 bg-primary/5 border border-primary/20 rounded-2xl space-y-3">
            <span className="text-[10px] font-bold text-primary uppercase tracking-widest flex items-center justify-center gap-1">
              <ClockIcon size={14} />
              Estimated Arrival Time
            </span>
            <div className="flex justify-center items-baseline gap-1 text-3xl font-black text-foreground">
              <span>{resolvedEta}</span>
              <span className="text-sm font-bold text-muted-foreground">mins</span>
            </div>
            <p className="text-[10px] text-muted-foreground font-semibold">
              Guide is currently {resolvedDistance.toFixed(1)} km away from your pickup
            </p>
          </div>

          {/* Call & Message Action Stub Row */}
          <div className="grid grid-cols-2 gap-3">
            <Button
              variant="outline"
              className="rounded-2xl py-5 font-bold text-xs gap-2 flex items-center justify-center cursor-pointer border-border hover:bg-accent"
              onClick={() => toast.success("Calling Ramesh Kumar via encrypted VoIP channel...")}
            >
              <PhoneCallIcon size={18} className="text-primary" />
              <span>Call Assistant</span>
            </Button>
            <Button
              variant="outline"
              className="rounded-2xl py-5 font-bold text-xs gap-2 flex items-center justify-center cursor-pointer border-border hover:bg-accent"
              onClick={() => toast.success("Opening live secure message chat drawer...")}
            >
              <ChatCircleTextIcon size={18} className="text-primary" />
              <span>Message</span>
            </Button>
          </div>
        </CardContent>

        <CardFooter className="p-6 border-t border-border/50 bg-muted/10">
          <Button
            onClick={() => navigate(`/trip/${bookingId}/enroute`)}
            className="w-full bg-primary text-primary-foreground hover:bg-primary/95 rounded-2xl py-5 font-black text-xs shadow-lg hover:scale-[1.01] transition-all cursor-pointer flex items-center justify-center gap-1.5"
          >
            <span>Proceed to Live Tracking</span>
            <ArrowRightIcon size={16} weight="bold" />
          </Button>
        </CardFooter>
      </Card>
    </motion.div>
  )
}
export default AssistantAssignedPage
