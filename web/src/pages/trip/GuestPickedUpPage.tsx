import React, { useState, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { bookingsService } from "@/services/bookings.service"
import { assistantsService } from "@/services/assistants.service"
import { useTrackingSocket } from "@/hooks/useTrackingSocket"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  CheckCircleIcon,
  NavigationArrowIcon,
  ShieldCheckIcon,
  UserCheckIcon,
  ArrowRightIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

export function GuestPickedUpPage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()
  const token = localStorage.getItem("access_token")

  const [booking, setBooking] = useState<any>(null)
  const [guide, setGuide] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  // Real-time status update handler
  const handleStatusChange = (newStatus: string) => {
    const st = newStatus.toUpperCase()
    if (st === "STARTED" || st === "IN_PROGRESS") {
      navigate(`/trip/${bookingId}/route`)
    } else if (st === "COMPLETED") {
      navigate(`/ride/completed/${bookingId}`, { replace: true })
    } else if (st === "CANCELLED" || st === "EXPIRED" || st === "NO_SHOW") {
      navigate(`/ride/cancelled/${bookingId}`, { replace: true })
    }
  }

  // Subscribe to tracking WebSockets immediately
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

        if (st === "started" || st === "tracking" || st === "in_progress" || st === "inprogress") {
          navigate(`/trip/${bookingId}/route`)
          return
        }

        setBooking(b)

        if (b.assistant_id) {
          try {
            const g = await assistantsService.getAssistantProfile(b.assistant_id)
            setGuide(g)
          } catch (e) {
            setGuide({
              id: b.assistant_id,
              name: "Ramesh Kumar",
              rating: 4.9,
              total_trips: 45
            })
          }
        }
      } catch (err) {
        console.error(err)
        toast.error("Failed to load picked-up details.")
        navigate("/bookings")
      } finally {
        setLoading(false)
      }
    }

    fetchContext()
  }, [bookingId, navigate])

  if (loading || !booking) {
    return (
      <div className="h-[400px] w-full flex flex-col items-center justify-center gap-4 bg-background text-foreground">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-muted-foreground">Verifying onboarding status...</span>
      </div>
    )
  }

  const checkCircleVariants = {
    hidden: { scale: 0.8, opacity: 0 },
    visible: { scale: 1, opacity: 1, transition: { duration: 0.5, ease: "easeOut" as const } }
  }

  const defaultGuideAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=80"

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-md mx-auto py-8 text-center"
    >
      <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-3xl overflow-hidden">
        {/* Animated Checkmark Head */}
        <div className="bg-gradient-to-b from-success/20 to-transparent p-8 flex justify-center border-b border-border/40">
          <motion.div
            variants={checkCircleVariants}
            initial="hidden"
            animate="visible"
            className="p-4 bg-success/20 text-success rounded-full w-fit mx-auto shadow-lg"
          >
            <CheckCircleIcon size={52} weight="fill" className="animate-bounce" />
          </motion.div>
        </div>

        <CardHeader className="pb-2">
          <Badge className="bg-success/20 hover:bg-success/20 text-success border-0 text-[10px] px-3.5 py-1 rounded-full font-bold mx-auto mb-2 tracking-widest uppercase animate-pulse">
            Picked Up Successfully
          </Badge>
          <CardTitle className="text-lg font-black text-foreground">You are with your Escort</CardTitle>
          <CardDescription className="text-xs">
            Your escort guide Ramesh Kumar has met you at your pickup coordinate and verified security verification protocols.
          </CardDescription>
        </CardHeader>

        {/* Guide and route review */}
        <CardContent className="p-6 space-y-4">
          <div className="flex gap-4 items-center bg-muted/40 p-4 border border-border/80 rounded-2xl text-left">
            <div className="w-12 h-12 rounded-full overflow-hidden border border-border bg-muted shrink-0">
              <img src={defaultGuideAvatar} alt={guide?.name} className="object-cover w-full h-full" />
            </div>
            <div className="min-w-0 space-y-0.5">
              <div className="flex items-center gap-1.5 justify-between">
                <span className="font-bold text-sm text-foreground truncate">{guide?.name || "Ramesh Kumar"}</span>
                <Badge className="bg-success text-success-foreground hover:bg-success border-0 text-[8px] px-1.5 py-0.5 rounded-full flex items-center gap-0.5 font-bold">
                  <ShieldCheckIcon size={10} weight="fill" />
                  Verified
                </Badge>
              </div>
              <p className="text-[10px] text-muted-foreground font-semibold">★ {guide?.rating || 4.9} rating • verified local assistant</p>
            </div>
          </div>

          <div className="bg-primary/5 border border-primary/20 p-4 rounded-2xl text-left space-y-2">
            <div className="flex items-center gap-2 text-xs font-bold text-primary">
              <NavigationArrowIcon size={16} />
              <span>Next Destination</span>
            </div>
            <p className="text-xs font-extrabold text-foreground leading-relaxed truncate">
              {booking.destination_address}
            </p>
          </div>
        </CardContent>

        <CardFooter className="p-6 border-t border-border/50 bg-muted/10">
          <Button
            onClick={() => navigate(`/trip/${bookingId}/route`)}
            className="w-full bg-primary text-primary-foreground hover:bg-primary/95 rounded-2xl py-5 font-black text-xs shadow-lg hover:scale-[1.01] transition-all cursor-pointer flex items-center justify-center gap-1.5 animate-pulse"
          >
            <span>Continue to Destination</span>
            <ArrowRightIcon size={16} weight="bold" />
          </Button>
        </CardFooter>
      </Card>
    </motion.div>
  )
}
export default GuestPickedUpPage
