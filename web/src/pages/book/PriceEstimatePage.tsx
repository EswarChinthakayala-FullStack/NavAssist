import React, { useState, useEffect } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import { useBookingDraftStore } from "@/store/booking-draft.store"
import { pricingService } from "@/services/pricing.service"
import { bookingsService } from "@/services/bookings.service"
import { FareBreakdownCard, formatCurrency } from "@/components/booking/FareBreakdownCard"
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { ReceiptIcon, TagIcon, CreditCardIcon } from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

export function PriceEstimatePage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { pickup, destination, coupon } = useBookingDraftStore()

  const [bookingId, setBookingId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [estimate, setEstimate] = useState<any>(null)

  useEffect(() => {
    // 1. Resolve booking ID from navigation state or session storage
    const stateId = location.state?.bookingId
    const sessionStr = sessionStorage.getItem("active_booking_id")
    const resolvedId = stateId || (sessionStr ? parseInt(sessionStr) : null)

    if (resolvedId) {
      setBookingId(resolvedId)
    }

    // 2. Fetch price estimate breakdown from backend
    const loadPriceEstimate = async () => {
      setLoading(true)
      try {
        let lat1 = 0, lon1 = 0, lat2 = 0, lon2 = 0
        let activeCouponCode = coupon?.code || undefined

        if (resolvedId) {
          try {
            const booking = await bookingsService.getBooking(resolvedId)
            lat1 = booking.pickup_latitude || pickup?.lat || 15.7352
            lon1 = booking.pickup_longitude || pickup?.lng || 79.8801
            lat2 = booking.destination_latitude || destination?.lat || 15.7694
            lon2 = booking.destination_longitude || destination?.lng || 79.6776
            if (booking.coupon_code) {
              activeCouponCode = booking.coupon_code
            }
          } catch {
            if (pickup && destination) {
              lat1 = pickup.lat
              lon1 = pickup.lng
              lat2 = destination.lat
              lon2 = destination.lng
            }
          }
        } else if (pickup && destination) {
          lat1 = pickup.lat
          lon1 = pickup.lng
          lat2 = destination.lat
          lon2 = destination.lng
        }

        if (!lat1 || !lon1) {
          lat1 = 15.7352
          lon1 = 79.8801
          lat2 = 15.7694
          lon2 = 79.6776
        }

        // Backend authoritative pricing estimate
        const res = await pricingService.estimateFare(lat1, lon1, lat2, lon2, activeCouponCode)
        setEstimate(res)
      } catch (err: any) {
        console.error("Failed to compile price estimate:", err)
        toast.error("Failed to calculate fare estimate from backend")
      } finally {
        setLoading(false)
      }
    }

    loadPriceEstimate()
  }, [pickup, destination, coupon, location.state, navigate])

  const handleProceed = () => {
    navigate("/book/payment-method", { state: { bookingId } })
  }

  if (loading || !estimate) {
    return (
      <div className="h-[380px] w-full flex flex-col items-center justify-center gap-4 bg-background text-foreground">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-muted-foreground">Retrieving backend pricing breakdown...</span>
      </div>
    )
  }

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.98 }}
      animate={{ opacity: 1, scale: 1 }}
      className="max-w-xl mx-auto py-4 space-y-4"
    >
      <FareBreakdownCard breakdown={estimate} />

      <Card className="rounded-2xl border border-border shadow-md bg-card overflow-hidden">
        <CardContent className="p-4">
          {estimate.discount_amount && Number(estimate.discount_amount) > 0 ? (
            <div className="flex justify-between items-center text-emerald-500 font-bold bg-emerald-500/10 p-3 rounded-xl border border-emerald-500/20 text-xs">
              <div className="flex items-center gap-2">
                <TagIcon size={18} />
                <span>Coupon Applied ({coupon?.code})</span>
              </div>
              <span>-{formatCurrency(estimate.discount_amount)}</span>
            </div>
          ) : (
            <button
              onClick={() => navigate("/book/offers", { state: { bookingId, totalFare: estimate.total_fare } })}
              className="w-full py-3 border border-dashed border-border hover:border-primary/50 bg-muted/20 hover:bg-primary/5 rounded-xl text-xs font-bold text-primary flex items-center justify-center gap-2 transition-all cursor-pointer"
            >
              <TagIcon size={16} />
              <span>Apply Promo Coupon Code?</span>
            </button>
          )}
        </CardContent>

        <CardFooter className="flex flex-col p-6 bg-muted/20 border-t border-border/50 gap-4">
          <div className="flex justify-between items-center w-full">
            <div>
              <span className="text-xs text-muted-foreground font-bold uppercase tracking-wider">Total Payable Fare</span>
              <p className="text-[10px] text-muted-foreground">(Inclusive of platform charges & taxes)</p>
            </div>
            <div className="text-2xl font-black text-primary">
              {formatCurrency(estimate.total_fare ?? estimate.estimated_fare)}
            </div>
          </div>

          <div className="flex w-full gap-3">
            <Button
              variant="outline"
              onClick={() => navigate("/book/summary")}
              className="flex-1 rounded-xl py-5 font-bold text-xs cursor-pointer"
            >
              Review Summary
            </Button>
            <Button
              onClick={handleProceed}
              className="flex-1 bg-primary text-primary-foreground hover:bg-primary/95 rounded-xl py-5 font-extrabold text-xs shadow-md flex items-center justify-center gap-1.5 hover:scale-[1.02] transition-all cursor-pointer"
            >
              <span>Proceed to Pay</span>
              <CreditCardIcon size={16} weight="bold" />
            </Button>
          </div>
        </CardFooter>
      </Card>
    </motion.div>
  )
}
export default PriceEstimatePage
