import React, { useState, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { bookingsService } from "@/services/bookings.service"
import { assistantsService } from "@/services/assistants.service"
import { useTrackingStore } from "@/store/tracking.store"
import { useAuth } from "@/store/auth-context"
import { toast } from "sonner"
import { motion } from "framer-motion"

// Components
import AnimatedBackground from "@/components/ride/AnimatedBackground"
import SuccessBadge from "@/components/ride/SuccessBadge"
import RideCompletedHero from "@/components/ride/RideCompletedHero"
import RideCompletionIllustration from "@/components/ride/RideCompletionIllustration"
import RideSummaryCard from "@/components/ride/RideSummaryCard"
import RatingCard from "@/components/ride/RatingCard"
import BookAgainCard from "@/components/ride/BookAgainCard"
import ReceiptActions from "@/components/ride/ReceiptActions"

export function RideCompletedPage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const { resetTracking } = useTrackingStore()

  const [booking, setBooking] = useState<any>(null)
  const [guide, setGuide] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!bookingId) {
      navigate("/bookings")
      return
    }

    const verifyBooking = async () => {
      setLoading(true)
      try {
        const b = await bookingsService.getBooking(parseInt(bookingId))
        
        // Ownership guard
        if (user && b.guest_id !== user.id && b.assistant_id !== user.id && user.role !== "admin") {
          toast.error("Unauthorized to access this journey record.")
          navigate("/bookings")
          return
        }

        // Status-based navigation guards
        const st = (b.status || "").toLowerCase()
        if (st === "cancelled" || st === "no_show" || st === "expired") {
          navigate(`/ride/cancelled/${bookingId}`)
          return
        }

        // If payment is pending and is online method, redirect to payment method
        if (b.payment_status === "pending" && b.payment_method === "online") {
          navigate(`/ride/payment/${bookingId}`)
          return
        }

        // If not completed, redirect to tracking pages
        if (st !== "completed") {
          toast.info(`Active ride is in progress: ${b.status}`)
          const enrouteStatuses = ["pending", "searching", "assigned", "accepted", "assistant_enroute", "arrived_pickup"]
          if (enrouteStatuses.includes(st)) {
            navigate(`/trip/${bookingId}/enroute`)
          } else {
            navigate(`/trip/${bookingId}/tracking`)
          }
          return
        }

        // Succeeded - stop location tracking and cleanup socket states immediately
        resetTracking()
        setBooking(b)

        // Fetch guide profile if available
        if (b.assistant_id) {
          try {
            const g = await assistantsService.getAssistantProfile(b.assistant_id)
            setGuide(g)
          } catch (e) {
            setGuide(b.assistant)
          }
        }
      } catch (err) {
        console.error(err)
        toast.error("Failed to retrieve booking information.")
        navigate("/bookings")
      } finally {
        setLoading(false)
      }
    }

    verifyBooking()
  }, [bookingId, navigate, user, resetTracking])

  if (loading) {
    return (
      <div className="h-screen w-full flex flex-col items-center justify-center bg-background text-foreground gap-4">
        <div className="w-10 h-10 border-4 border-orange-500 border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-zinc-500">Verifying ride status...</span>
      </div>
    )
  }

  const guideName = guide?.name || guide?.full_name || guide?.user?.full_name || booking?.assistant_name || "Escort Guide"

  return (
    <div className="relative min-h-screen w-full overflow-x-hidden bg-background text-foreground flex flex-col items-center justify-start p-4 md:p-8 space-y-6">
      {/* Premium animated layout background */}
      <AnimatedBackground />

      {/* Top Banner Card containing greetings and right-side button */}
      <div className="relative z-10 w-full max-w-md md:max-w-4xl bg-card border border-border p-5 md:p-6 rounded-2xl shadow-xl backdrop-blur-md flex flex-col md:flex-row items-center justify-between gap-6">
        <div className="flex flex-col md:flex-row items-center gap-5 text-center md:text-left min-w-0">
          <div className="shrink-0 scale-90">
            <RideCompletionIllustration />
          </div>
          <div className="space-y-2">
            <div className="flex justify-center md:justify-start">
              <SuccessBadge />
            </div>
            <h1 className="text-2xl md:text-3xl font-black tracking-tight text-card-foreground">
              Ride Completed
            </h1>
            <p className="text-xs md:text-sm font-semibold text-muted-foreground max-w-md leading-relaxed">
              Thank you for riding with <span className="text-primary font-extrabold">NavAssist</span>. We hope you had a safe and comfortable journey.
            </p>
          </div>
        </div>
        <div className="w-full md:w-auto shrink-0 md:min-w-[200px]">
          <BookAgainCard />
        </div>
      </div>

      {/* Grid column layout for Summary Card and Ratings/Receipt actions */}
      <div className="relative z-10 w-full max-w-md md:max-w-4xl grid grid-cols-1 md:grid-cols-12 gap-6 items-start">
        {/* Left Column: Summary Card */}
        <div className="md:col-span-7 w-full">
          <RideSummaryCard booking={booking} guide={guide} />
        </div>

        {/* Right Column: Ratings Card & Secondary actions */}
        <div className="md:col-span-5 w-full flex flex-col space-y-5">
          {user?.role !== "assistant" && (
            <RatingCard bookingId={parseInt(bookingId!)} guideName={guideName} />
          )}
          <ReceiptActions bookingId={parseInt(bookingId!)} />
        </div>
      </div>
    </div>
  )
}

export default RideCompletedPage
