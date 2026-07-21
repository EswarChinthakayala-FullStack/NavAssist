import React from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { InfoIcon, ReceiptIcon } from "lucide-react"

export interface FareBreakdown {
  base_fare: number
  distance_fare?: number
  distance_charges?: number
  time_fare?: number
  duration_charges?: number
  waiting_charges?: number
  booking_fee?: number
  taxes?: number
  surge_amount?: number
  discount_amount?: number
  surge_multiplier?: number
  subtotal?: number
  total_fare?: number
  estimated_fare?: number
}

export function formatCurrency(amount: number | string | undefined | null): string {
  const num = Number(amount || 0)
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(num)
}

interface FareBreakdownCardProps {
  breakdown: FareBreakdown
}

export function FareBreakdownCard({ breakdown }: FareBreakdownCardProps) {
  const baseFare = Number(breakdown.base_fare || 0)
  const distFare = Number(breakdown.distance_fare ?? breakdown.distance_charges ?? 0)
  const timeFare = Number(breakdown.time_fare ?? breakdown.duration_charges ?? 0)
  const waitingCharges = Number(breakdown.waiting_charges || 0)
  const bookingFee = Number(breakdown.booking_fee ?? 15.00)
  const taxes = Number(breakdown.taxes || 0)
  const surgeAmount = Number(breakdown.surge_amount || 0)
  const discountAmount = Number(breakdown.discount_amount || 0)
  const totalFare = Number(breakdown.total_fare ?? breakdown.estimated_fare ?? 0)
  const isSurge = breakdown.surge_multiplier && Number(breakdown.surge_multiplier) > 1.0

  return (
    <Card className="rounded-2xl border border-border shadow-md bg-card overflow-hidden">
      <CardHeader className="pb-3.5 border-b bg-muted/20">
        <CardTitle className="text-sm font-extrabold flex items-center gap-2 text-muted-foreground uppercase tracking-widest">
          <ReceiptIcon size={16} className="text-primary" />
          Checkout Pricing Summary
        </CardTitle>
        <CardDescription className="text-xs">
          Authoritative backend fare breakdown (Uber & Rapido standard).
        </CardDescription>
      </CardHeader>

      <CardContent className="p-5 grid gap-2.5 text-xs">
        {/* Base Fare */}
        <div className="flex justify-between items-center text-muted-foreground">
          <span>Base Escort Fee:</span>
          <span className="font-semibold text-foreground">{formatCurrency(baseFare)}</span>
        </div>

        {/* Distance Fare */}
        <div className="flex justify-between items-center text-muted-foreground">
          <span>Distance Mileage Charge:</span>
          <span className="font-semibold text-foreground">{formatCurrency(distFare)}</span>
        </div>

        {/* Time Fare */}
        <div className="flex justify-between items-center text-muted-foreground">
          <span>Escort Duration Charge:</span>
          <span className="font-semibold text-foreground">{formatCurrency(timeFare)}</span>
        </div>

        {/* Waiting Charges if present */}
        {waitingCharges > 0 && (
          <div className="flex justify-between items-center text-muted-foreground">
            <span>Waiting Charge:</span>
            <span className="font-semibold text-foreground">{formatCurrency(waitingCharges)}</span>
          </div>
        )}

        {/* Booking / Platform Fee */}
        <div className="flex justify-between items-center text-muted-foreground">
          <span>Platform & Safety Fee:</span>
          <span className="font-semibold text-foreground">{formatCurrency(bookingFee)}</span>
        </div>

        {/* Taxes */}
        {taxes > 0 && (
          <div className="flex justify-between items-center text-muted-foreground">
            <span>Government GST Tax (5%):</span>
            <span className="font-semibold text-foreground">{formatCurrency(taxes)}</span>
          </div>
        )}

        {/* Surge Amount */}
        {surgeAmount > 0 && (
          <div className="flex justify-between items-center text-amber-500 font-bold">
            <span>Surge Multiplier ({breakdown.surge_multiplier}x):</span>
            <span>+{formatCurrency(surgeAmount)}</span>
          </div>
        )}

        {/* Discount Amount */}
        {discountAmount > 0 && (
          <div className="flex justify-between items-center text-emerald-500 font-bold">
            <span>Coupon Promo Discount:</span>
            <span>-{formatCurrency(discountAmount)}</span>
          </div>
        )}

        {/* Total Payable */}
        <div className="flex justify-between items-center border-t pt-3 mt-2">
          <span className="text-sm font-black text-foreground">Final Payable Amount:</span>
          <span className="text-lg font-black text-primary">{formatCurrency(totalFare)}</span>
        </div>

        {isSurge && (
          <div className="p-3 bg-amber-500/10 border border-amber-500/30 rounded-xl text-[10px] text-amber-500 font-semibold flex gap-2 items-start mt-2">
            <InfoIcon size={14} className="flex-shrink-0 mt-0.5" />
            <span>High local demand is triggering dynamic peak rates.</span>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
export default FareBreakdownCard
