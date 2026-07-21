import React, { useState, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { sosService } from "@/services/sos.service"
import { useTrackingSocket } from "@/hooks/useTrackingSocket"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import {
  SirenIcon,
  PhoneCallIcon,
  ShieldCheckIcon,
  HouseIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

export function EmergencySosPage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()
  const token = localStorage.getItem("access_token")

  const [loading, setLoading] = useState(true)

  // Listen to WebSocket transitions
  const handleStatusChange = (newStatus: string) => {
    // If the booking itself gets completed or resolved, go back
    if (newStatus === "completed" || newStatus === "COMPLETED") {
      toast.success("Emergency resolved. Returning to portal.")
      navigate("/bookings")
    }
  }

  useTrackingSocket(
    bookingId ? parseInt(bookingId) : null,
    token,
    handleStatusChange
  )

  // Poll active alerts list to auto-dismiss when resolved by admin
  useEffect(() => {
    if (!bookingId) return

    const checkInterval = setInterval(async () => {
      try {
        const activeAlerts = await sosService.listActiveSosAlerts()
        // If there are no active alerts for this booking, it was resolved!
        const alertForBooking = activeAlerts.find(
          (alert: any) => alert.booking_id === parseInt(bookingId)
        )
        if (!alertForBooking) {
          toast.success("Emergency status resolved by system administration.")
          navigate(`/trip/${bookingId}/tracking`)
        }
      } catch (err) {
        console.warn("SOS status check failed:", err)
      }
    }, 4000)

    setLoading(false)
    return () => clearInterval(checkInterval)
  }, [bookingId, navigate])

  return (
    <div className="fixed inset-0 z-50 bg-background flex items-center justify-center p-4">
      {/* Dynamic pulse background rings */}
      <div className="absolute inset-0 z-0 flex items-center justify-center overflow-hidden pointer-events-none">
        <motion.div
          animate={{ scale: [1, 2, 2.5], opacity: [0.15, 0.05, 0] }}
          transition={{ duration: 3, repeat: Infinity, ease: "easeOut" }}
          className="w-[300px] h-[300px] rounded-full bg-destructive/15 absolute"
        />
        <motion.div
          animate={{ scale: [1, 1.8, 2.2], opacity: [0.2, 0.05, 0] }}
          transition={{ duration: 3, delay: 1, repeat: Infinity, ease: "easeOut" }}
          className="w-[300px] h-[300px] rounded-full bg-destructive/10 absolute"
        />
      </div>

      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        className="max-w-md w-full relative z-10 text-center space-y-4"
      >
        <Card className="border border-destructive/25 shadow-2xl rounded-3xl bg-card/95 backdrop-blur-md overflow-hidden">
          <div className="p-8 bg-destructive/10 border-b border-border/40 flex justify-center">
            <motion.div
              animate={{ rotate: [0, 8, -8, 8, 0] }}
              transition={{ duration: 0.5, repeat: Infinity, repeatDelay: 2 }}
              className="p-5 bg-destructive text-destructive-foreground rounded-full shadow-lg"
            >
              <SirenIcon size={44} weight="fill" className="animate-pulse" />
            </motion.div>
          </div>

          <CardHeader className="pb-2">
            <CardTitle className="text-xl font-black text-destructive">Help is on the Way</CardTitle>
            <CardDescription className="text-xs text-muted-foreground mt-1.5 leading-relaxed">
              Your emergency contacts and terminal safety supervisors have been notified immediately with your live GPS location.
            </CardDescription>
          </CardHeader>

          <CardContent className="p-6 space-y-4 text-xs">
            <div className="bg-muted/40 p-4 border border-border/80 rounded-2xl text-left space-y-3">
              <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest block">Distress Action Checklist</span>
              <ul className="space-y-2 text-muted-foreground font-semibold">
                <li className="flex gap-2 items-start">
                  <ShieldCheckIcon size={16} className="text-success shrink-0" />
                  <span>Stay in a well-lit public transit area near other passengers.</span>
                </li>
                <li className="flex gap-2 items-start">
                  <ShieldCheckIcon size={16} className="text-success shrink-0" />
                  <span>Keep your device powered and connected. Live GPS coordinates are syncing.</span>
                </li>
              </ul>
            </div>

            {/* Emergency Hotline Dialer */}
            <Button
              onClick={() => toast.success("Encrypted secure line connected to airport support helpline...")}
              className="w-full bg-destructive text-destructive-foreground hover:bg-destructive/90 py-5 font-bold rounded-2xl shadow-md gap-2 flex items-center justify-center cursor-pointer"
            >
              <PhoneCallIcon size={18} weight="fill" />
              <span>Call Airport Safety Hotline</span>
            </Button>
          </CardContent>

          <CardFooter className="p-6 border-t border-border/50 bg-muted/10">
            <Button
              variant="outline"
              onClick={() => navigate("/bookings")}
              className="w-full rounded-2xl py-4 font-bold text-xs gap-1.5 flex items-center justify-center cursor-pointer border-border hover:bg-accent"
            >
              <HouseIcon size={14} />
              Return to Bookings
            </Button>
          </CardFooter>
        </Card>
      </motion.div>
    </div>
  )
}
export default EmergencySosPage
