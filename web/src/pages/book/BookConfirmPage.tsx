import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useBookingDraftStore } from "@/store/booking-draft.store"
import { pricingService } from "@/services/pricing.service"
import { bookingsService } from "@/services/bookings.service"
import { api } from "@/services/api"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { FareBreakdownCard } from "@/components/booking/FareBreakdownCard"
import { CouponInput } from "@/components/booking/CouponInput"
import { MapPinIcon, UserCheckIcon, ClockIcon, ArrowLeftIcon, CheckIcon } from "@phosphor-icons/react"
import { toast } from "sonner"

interface FareBreakdown {
  base_fare: number
  distance_charges: number
  duration_charges: number
  surge_amount?: number
  discount_amount?: number
  surge_multiplier?: number
  total_fare: number
}

export function BookConfirmPage() {
  const navigate = useNavigate()
  const { pickup, destination, schedule, selectedAssistant, resetDraft } = useBookingDraftStore()

  const [transitInfo, setTransitInfo] = useState<{
    transitType: string
    transitNumber?: string
    transitSpot?: string
    transitNotes?: string
  } | null>(null)

  useEffect(() => {
    const cached = sessionStorage.getItem("booking_transit_details")
    if (cached) {
      try {
        setTransitInfo(JSON.parse(cached))
      } catch (err) {
        console.error("Failed to parse cached transit details:", err)
      }
    }
  }, [])

  useEffect(() => {
    if (!pickup || !destination || !selectedAssistant) {
      toast.error("Please complete all previous steps first.")
      navigate("/book/pickup")
    }
  }, [pickup, destination, selectedAssistant, navigate])

  const [breakdown, setBreakdown] = useState<FareBreakdown | null>(null)
  const [loading, setLoading] = useState(true)
  const [bookingLoading, setBookingLoading] = useState(false)
  const [appliedCouponCode, setAppliedCouponCode] = useState<string | null>(null)

  // Fetch initial pricing estimate on mount
  useEffect(() => {
    if (!pickup || !destination) return
    const fetchEstimate = async () => {
      setLoading(true)
      try {
        const res = await pricingService.estimateFare(
          pickup.lat,
          pickup.lng,
          destination.lat,
          destination.lng
        )
        setBreakdown({
          base_fare: Math.round(Number(res.base_fare ?? 50.0)),
          distance_charges: Math.round(Number(res.distance_charges ?? 0.0)),
          duration_charges: Math.round(Number(res.duration_charges ?? 0.0)),
          surge_amount: Math.round(Number(res.surge_amount ?? 0.0)),
          surge_multiplier: Number(res.surge_multiplier ?? 1.0),
          total_fare: Math.round(Number(res.total_fare ?? res.estimated_fare ?? 50.0)),
        })
      } catch (err) {
        console.error("Failed to fetch estimate:", err)
        // Fallback mock estimate for offline / local testing
        setBreakdown({
          base_fare: 50.0,
          distance_charges: 120.0,
          duration_charges: 45.0,
          surge_amount: 0.0,
          surge_multiplier: 1.0,
          total_fare: 215.0,
        })
      } finally {
        setLoading(false)
      }
    }
    fetchEstimate()
  }, [pickup, destination])

  const handleCouponApplied = (coupon: { code: string; discount: number; finalAmount: number }) => {
    setAppliedCouponCode(coupon.code)
    if (breakdown) {
      setBreakdown((prev) => prev ? {
        ...prev,
        discount_amount: coupon.discount,
        total_fare: coupon.finalAmount,
      } : null)
    }
  }

  const handleCouponRemoved = async () => {
    setAppliedCouponCode(null)
    if (!pickup || !destination) return
    setLoading(true)
    try {
      const res = await pricingService.estimateFare(
        pickup.lat,
        pickup.lng,
        destination.lat,
        destination.lng
      )
      setBreakdown({
        base_fare: Math.round(res.base_fare || 50.0),
        distance_charges: Math.round(res.distance_charges || 0.0),
        duration_charges: Math.round(res.duration_charges || 0.0),
        surge_amount: Math.round(res.surge_amount || 0.0),
        surge_multiplier: res.surge_multiplier || 1.0,
        total_fare: Math.round(res.total_fare || res.estimated_fare || 50.0),
      })
    } catch (err) {
      console.error("Failed to restore estimate:", err)
    } finally {
      setLoading(false)
    }
  }

  const handleBookNow = async () => {
    if (!pickup || !destination || !selectedAssistant || !breakdown) return
    setBookingLoading(true)
    try {
      let finalPickupAddress = pickup.name
      if (transitInfo) {
        const parts = []
        if (transitInfo.transitNumber) {
          const prefix = transitInfo.transitType === "flight" ? "✈️ Flight" :
                         transitInfo.transitType === "train" ? "🚄 Train" :
                         transitInfo.transitType === "bus" ? "🚌 Bus" : "📍 Spot"
          parts.push(`${prefix} ${transitInfo.transitNumber}`)
        }
        if (transitInfo.transitSpot) {
          parts.push(`Terminal/Platform: ${transitInfo.transitSpot}`)
        }
        if (transitInfo.transitNotes) {
          parts.push(`Notes: ${transitInfo.transitNotes}`)
        }
        if (parts.length > 0) {
          finalPickupAddress = `${pickup.name} (${parts.join(", ")})`
        }
      }

      // 1. Create booking request on backend passing pre-selected assistant
      const bookingRes = await bookingsService.createBooking({
        pickup_latitude: pickup.lat,
        pickup_longitude: pickup.lng,
        pickup_address: finalPickupAddress,
        destination_latitude: destination.lat,
        destination_longitude: destination.lng,
        destination_address: destination.name,
        assistant_id: selectedAssistant.id,
      })

      // 2. If a coupon code is verified, apply it to the booking
      if (appliedCouponCode) {
        try {
          await api.post("/coupons/apply", {
            booking_id: bookingRes.id,
            code: appliedCouponCode,
          })
        } catch (couponErr) {
          console.error("Failed to apply coupon to booking:", couponErr)
        }
      }

      toast.success("Assistance booking confirmed successfully!")
      
      // Save ID so bookings view can poll it
      sessionStorage.setItem("active_booking_id", bookingRes.id.toString())
      
      // Clear draft details from store
      resetDraft()
      
      // Redirect to bookings list details page
      navigate("/bookings")
    } catch (err) {
      console.error("Booking creation failed:", err)
    } finally {
      setBookingLoading(false)
    }
  }

  if (!pickup || !destination || !selectedAssistant) return null

  return (
    <div className="grid gap-6 md:grid-cols-12 w-full">
      {/* Left panel: Journey Summary */}
      <div className="md:col-span-6 flex flex-col gap-4">
        <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-2xl flex-1 flex flex-col justify-between">
          <CardHeader>
            <CardTitle className="text-xl font-bold flex items-center gap-2">
              <CheckIcon size={24} className="text-success" />
              Review & Confirm
            </CardTitle>
            <CardDescription>
              Double check your escort details before confirming the assistant assignment.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-5 pt-2 flex-1">
            {/* Pickup details */}
            <div className="flex gap-3">
              <div className="mt-0.5 p-2 bg-success/10 text-success rounded-xl h-fit">
                <MapPinIcon size={16} weight="fill" />
              </div>
              <div className="space-y-1">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-wider">Pickup Gate / Spot</div>
                <p className="text-xs font-bold leading-normal text-foreground">{pickup.name}</p>
              </div>
            </div>

            {/* Destination details */}
            <div className="flex gap-3">
              <div className="mt-0.5 p-2 bg-destructive/10 text-destructive rounded-xl h-fit">
                <MapPinIcon size={16} weight="fill" />
              </div>
              <div className="space-y-1">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-wider">Dropoff Lodging / Station</div>
                <p className="text-xs font-bold leading-normal text-foreground">{destination.name}</p>
              </div>
            </div>

            {/* Assistant details */}
            <div className="flex gap-3">
              <div className="mt-0.5 p-2 bg-primary/10 text-primary rounded-xl h-fit">
                <UserCheckIcon size={16} weight="fill" />
              </div>
              <div className="space-y-1">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-wider">Selected Guide</div>
                <p className="text-xs font-bold leading-normal text-foreground">{selectedAssistant.name}</p>
                <div className="text-[10px] text-muted-foreground font-semibold">Rating: ★ {selectedAssistant.rating} • {selectedAssistant.total_trips} trips</div>
              </div>
            </div>

            {/* Schedule details */}
            <div className="flex gap-3">
              <div className="mt-0.5 p-2 bg-accent text-accent-foreground rounded-xl h-fit">
                <ClockIcon size={16} weight="fill" />
              </div>
              <div className="space-y-1">
                <div className="text-[10px] font-black text-muted-foreground uppercase tracking-wider">Arrival Time</div>
                <p className="text-xs font-bold leading-normal text-foreground">
                  {schedule ? new Date(schedule).toLocaleString("en-US", {
                    dateStyle: "medium",
                    timeStyle: "short",
                  }) : "Now (Immediate dispatch)"}
                </p>
                {transitInfo && (
                  <div className="text-[10px] text-muted-foreground mt-2 border-t pt-2 space-y-1">
                    <span className="font-extrabold uppercase text-[8px] block text-primary mt-1">Transit schedule details</span>
                    <div>Type: <span className="font-bold text-foreground capitalize">{transitInfo.transitType}</span></div>
                    {transitInfo.transitNumber && <div>Transit Code: <span className="font-bold text-foreground">{transitInfo.transitNumber}</span></div>}
                    {transitInfo.transitSpot && <div>Gate / Platform: <span className="font-bold text-foreground">{transitInfo.transitSpot}</span></div>}
                    {transitInfo.transitNotes && <div className="italic text-[9px]">"{transitInfo.transitNotes}"</div>}
                  </div>
                )}
              </div>
            </div>
          </CardContent>
          <div className="p-6 border-t border-border">
            <Button
              variant="outline"
              onClick={() => navigate("/book/assistant")}
              className="w-full rounded-xl py-5 font-bold text-xs flex items-center justify-center gap-2 hover:bg-accent cursor-pointer"
            >
              <ArrowLeftIcon size={14} weight="bold" />
              <span>Change Assistant</span>
            </Button>
          </div>
        </Card>
      </div>

      {/* Right panel: Pricing, Coupons & Booking confirmation */}
      <div className="md:col-span-6 flex flex-col gap-4">
        {loading || !breakdown ? (
          <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-2xl h-full flex items-center justify-center p-12">
            <div className="flex flex-col items-center gap-3">
              <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin" />
              <span className="text-xs text-muted-foreground font-semibold uppercase tracking-wider">Computing rates...</span>
            </div>
          </Card>
        ) : (
          <div className="flex flex-col gap-4">
            <FareBreakdownCard breakdown={breakdown} />
            
            <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-2xl p-6">
              <CouponInput
                bookingAmount={breakdown.base_fare + breakdown.distance_charges + breakdown.duration_charges + (breakdown.surge_amount || 0)}
                onCouponApplied={handleCouponApplied}
                onCouponRemoved={handleCouponRemoved}
              />
            </Card>

            <Button
              onClick={handleBookNow}
              disabled={bookingLoading}
              className="w-full rounded-2xl py-6 bg-primary text-primary-foreground font-extrabold text-xs shadow-xl flex items-center justify-center gap-2 hover:scale-102 transition-all cursor-pointer mt-2"
            >
              {bookingLoading ? (
                <>
                  <div className="w-4 h-4 border-2 border-primary-foreground border-t-transparent rounded-full animate-spin" />
                  <span>Creating request...</span>
                </>
              ) : (
                <>
                  <CheckIcon size={16} weight="bold" />
                  <span>Confirm & Book Guide</span>
                </>
              )}
            </Button>
          </div>
        )}
      </div>
    </div>
  )
}
export default BookConfirmPage
