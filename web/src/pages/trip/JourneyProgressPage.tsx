import React, { useState, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { bookingsService } from "@/services/bookings.service"
import { useTrackingSocket } from "@/hooks/useTrackingSocket"
import { useTrackingStore } from "@/store/tracking.store"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  ClockIcon,
  MapPinIcon,
  NavigationArrowIcon,
  ShieldCheckIcon,
  ArrowLeftIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion, useSpring, useTransform } from "framer-motion"

export function JourneyProgressPage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()
  const token = localStorage.getItem("access_token")

  const { etaMins, distanceKm } = useTrackingStore()
  const [booking, setBooking] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  // Total distance estimation for the progress computation
  const [totalDistance, setTotalDistance] = useState(2.5) // default 2.5 km

  // Spring animation for the progress bar value
  const springProgress = useSpring(0, { stiffness: 50, damping: 15 })
  const animatedProgressWidth = useTransform(springProgress, (latest) => `${latest}%`)

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

  // Connect WebSocket tracking
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
        
        // Calculate raw distance between pickup and destination coordinates as total distance
        if (b.pickup_latitude && b.destination_latitude) {
          const latDiff = b.destination_latitude - b.pickup_latitude
          const lonDiff = b.destination_longitude - b.pickup_longitude
          const estDist = Math.max(0.5, Math.sqrt(latDiff * latDiff + lonDiff * lonDiff) * 111) // approximate km
          setTotalDistance(estDist)
        }
      } catch (err) {
        console.error(err)
        toast.error("Failed to load journey metrics.")
        navigate("/bookings")
      } finally {
        setLoading(false)
      }
    }

    fetchBookingDetails()
  }, [bookingId, navigate])

  // Recalculate progress on distance tick updates
  useEffect(() => {
    if (distanceKm !== null && totalDistance > 0) {
      const remaining = distanceKm
      const covered = Math.max(0, totalDistance - remaining)
      const ratio = Math.min(100, Math.max(0, (covered / totalDistance) * 100))
      
      // Update spring value
      springProgress.set(ratio)
    } else {
      springProgress.set(35) // mock default initial progress
    }
  }, [distanceKm, totalDistance, springProgress])

  if (loading || !booking) {
    return (
      <div className="h-[300px] w-full flex flex-col items-center justify-center gap-4 bg-background text-foreground">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-muted-foreground">Calculating progress stats...</span>
      </div>
    )
  }

  const resolvedRemainingDistance = distanceKm !== null ? distanceKm : 1.2
  const resolvedCoveredDistance = Math.max(0, totalDistance - resolvedRemainingDistance)
  const resolvedEta = etaMins !== null ? etaMins : 6

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-md mx-auto py-8"
    >
      <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-3xl overflow-hidden text-left">
        <CardHeader className="border-b border-border/40 bg-muted/20 pb-4">
          <CardTitle className="text-lg font-bold flex items-center gap-2">
            <NavigationArrowIcon size={20} className="text-primary animate-pulse" />
            Trip Progress Status
          </CardTitle>
          <CardDescription className="text-xs">
            Tracking en route progress to destination
          </CardDescription>
        </CardHeader>
        
        <CardContent className="p-6 space-y-6">
          {/* Custom animated progress bar */}
          <div className="space-y-2">
            <div className="flex justify-between items-center text-xs font-semibold text-muted-foreground">
              <span>Covered: {resolvedCoveredDistance.toFixed(2)} km</span>
              <span>Total: {totalDistance.toFixed(2)} km</span>
            </div>
            
            {/* Custom spring progress indicator track */}
            <div className="w-full h-3 bg-muted rounded-full overflow-hidden relative border border-border/60">
              <motion.div
                style={{ width: animatedProgressWidth }}
                className="h-full bg-primary rounded-full transition-colors"
              />
            </div>
            
            <p className="text-[10px] text-muted-foreground text-right font-semibold">
              {((resolvedCoveredDistance / totalDistance) * 100).toFixed(0)}% Completed
            </p>
          </div>

          {/* Timing details */}
          <div className="grid grid-cols-2 gap-4">
            <div className="p-4 bg-muted/40 border border-border/80 rounded-2xl flex flex-col items-center justify-center text-center">
              <ClockIcon size={20} className="text-primary mb-1" />
              <span className="text-[9px] uppercase tracking-wider text-muted-foreground font-bold">Time Left</span>
              <span className="text-lg font-black text-foreground mt-0.5">{resolvedEta} mins</span>
            </div>

            <div className="p-4 bg-muted/40 border border-border/80 rounded-2xl flex flex-col items-center justify-center text-center">
              <MapPinIcon size={20} className="text-primary mb-1" />
              <span className="text-[9px] uppercase tracking-wider text-muted-foreground font-bold">Remaining</span>
              <span className="text-lg font-black text-foreground mt-0.5">{resolvedRemainingDistance.toFixed(2)} km</span>
            </div>
          </div>

          {/* Verification status label */}
          <div className="bg-success/5 border border-success/20 p-4.5 rounded-2xl flex gap-3.5 items-start">
            <ShieldCheckIcon size={20} className="text-success shrink-0 mt-0.5" />
            <div className="space-y-0.5 text-xs text-left">
              <p className="font-extrabold text-success">Verified On-Trip</p>
              <p className="text-[11px] text-muted-foreground leading-relaxed">
                Your escort guide is monitoring delay anomalies and tracking metrics to ensure you reach terminal gates on time.
              </p>
            </div>
          </div>
        </CardContent>

        <CardFooter className="border-t border-border/50 p-6 flex justify-start bg-muted/10">
          <Button
            variant="outline"
            onClick={() => navigate(`/trip/${bookingId}/route`)}
            className="rounded-xl py-4 font-bold text-xs gap-1.5 flex items-center justify-center cursor-pointer"
          >
            <ArrowLeftIcon size={12} weight="bold" />
            Back to Map view
          </Button>
        </CardFooter>
      </Card>
    </motion.div>
  )
}
export default JourneyProgressPage
