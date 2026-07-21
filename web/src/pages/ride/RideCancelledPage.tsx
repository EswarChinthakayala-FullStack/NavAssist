import React, { useState, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { bookingsService } from "@/services/bookings.service"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  WarningCircleIcon,
  ArrowClockwiseIcon,
  HeadsetIcon,
  ClockAfternoonIcon,
  InfoIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"
import AnimatedBackground from "@/components/ride/AnimatedBackground"

export function RideCancelledPage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()
  const [booking, setBooking] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!bookingId) {
      navigate("/bookings")
      return
    }

    const fetchBooking = async () => {
      setLoading(true)
      try {
        const b = await bookingsService.getBooking(parseInt(bookingId))
        const st = (b.status || "").toLowerCase()
        if (st !== "cancelled" && st !== "no_show" && st !== "expired") {
          toast.info(`Booking status: ${b.status}`)
          navigate("/bookings")
          return
        }
        setBooking(b)
      } catch (err) {
        console.error(err)
        toast.error("Failed to load cancelled booking details.")
        navigate("/bookings")
      } finally {
        setLoading(false)
      }
    }

    fetchBooking()
  }, [bookingId, navigate])

  if (loading) {
    return (
      <div className="h-screen w-full flex flex-col items-center justify-center bg-background text-foreground gap-4">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-muted-foreground">Retrieving details...</span>
      </div>
    )
  }

  const cancellationReason = booking?.cancellation_reason || "Trip request cancelled before assignment completed."

  return (
    <div className="relative min-h-screen w-full overflow-x-hidden bg-background text-foreground flex items-center justify-center p-4">
      <AnimatedBackground />

      <div className="relative z-10 w-full max-w-md mx-auto py-8 text-center space-y-6 flex flex-col items-center">
        {/* Animated Warning Icon badge */}
        <motion.div
          initial={{ scale: 0.8, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ type: "spring", stiffness: 200, delay: 0.1 }}
          className="inline-flex items-center gap-2 bg-red-500/10 border border-red-500/30 px-4 py-1.5 rounded-full text-red-500 font-extrabold select-none uppercase tracking-wider text-[10px]"
        >
          <WarningCircleIcon size={16} weight="fill" />
          <span>Booking Cancelled</span>
        </motion.div>

        {/* Custom illustration */}
        <div className="relative w-36 h-36 mx-auto flex items-center justify-center">
          <motion.div
            className="absolute w-28 h-28 rounded-full bg-red-500/10 dark:bg-red-500/5 blur-lg"
            animate={{ scale: [0.95, 1.05, 0.95] }}
            transition={{ duration: 3, repeat: Infinity }}
          />
          <svg className="w-full h-full text-red-500/80 overflow-visible" viewBox="0 0 100 100">
            {/* Draw a crossed route path */}
            <motion.path
              d="M 25,75 Q 50,25 75,75"
              fill="none"
              stroke="#3F3F46"
              strokeWidth="4"
              strokeLinecap="round"
              className="opacity-30"
            />
            <motion.path
              d="M 25,75 Q 50,25 75,75"
              fill="none"
              stroke="currentColor"
              strokeWidth="4"
              strokeLinecap="round"
              initial={{ pathLength: 0 }}
              animate={{ pathLength: 0.5 }}
              transition={{ duration: 1.5 }}
            />
            {/* Bouncing red Cross */}
            <motion.g
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ type: "spring", stiffness: 200, delay: 0.4 }}
              transform="translate(50, 45)"
            >
              <circle cx="0" cy="0" r="16" fill="currentColor" className="text-red-500" />
              <path d="M -6,-6 L 6,6 M -6,6 L 6,-6" stroke="#FFFFFF" strokeWidth="3" strokeLinecap="round" />
            </motion.g>
          </svg>
        </div>

        {/* Text descriptions */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="space-y-2.5"
        >
          <h1 className="text-2xl font-black text-card-foreground">Journey Cancelled</h1>
          <p className="text-xs text-muted-foreground max-w-sm mx-auto leading-relaxed">
            This booking has been cancelled and is no longer active. We apologize for any inconvenience caused.
          </p>
        </motion.div>

        {/* Reason Card */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.3 }}
          className="w-full"
        >
          <Card className="border border-border bg-card p-4 rounded-xl text-left flex items-start gap-3">
            <InfoIcon size={16} className="text-muted-foreground shrink-0 mt-0.5" />
            <div className="space-y-0.5">
              <span className="text-[10px] font-black uppercase text-muted-foreground tracking-wider block">Reason Notes</span>
              <p className="text-xs text-card-foreground font-semibold italic">"{cancellationReason}"</p>
            </div>
          </Card>
        </motion.div>

        {/* Book again button CTA */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.4 }}
          className="w-full space-y-2.5"
        >
          <Button
            onClick={() => navigate("/home")}
            className="w-full bg-primary text-primary-foreground hover:bg-primary/95 font-black text-xs py-5 rounded-xl flex items-center justify-center gap-1.5 cursor-pointer shadow-md"
          >
            <ArrowClockwiseIcon size={16} weight="bold" />
            <span>Book Another Ride</span>
          </Button>

          <div className="grid grid-cols-2 gap-2 w-full">
            <Button
              variant="outline"
              onClick={() => navigate("/trips")}
              className="bg-card border-border hover:bg-muted text-card-foreground text-[10px] font-bold py-3 rounded-xl flex items-center justify-center gap-1.5 cursor-pointer h-11"
            >
              <ClockAfternoonIcon size={14} className="text-primary" />
              <span>Ride History</span>
            </Button>

            <Button
              variant="outline"
              onClick={() => navigate("/support")}
              className="bg-card border-border hover:bg-muted text-card-foreground text-[10px] font-bold py-3 rounded-xl flex items-center justify-center gap-1.5 cursor-pointer h-11"
            >
              <HeadsetIcon size={14} className="text-primary" />
              <span>Contact Support</span>
            </Button>
          </div>
        </motion.div>
      </div>
    </div>
  )
}

export default RideCancelledPage
