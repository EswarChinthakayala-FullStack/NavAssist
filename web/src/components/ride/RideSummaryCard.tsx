import React from "react"
import { motion } from "framer-motion"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar"
import {
  MapPinIcon,
  CalendarBlankIcon,
  ClockIcon,
  CurrencyInrIcon,
  ShieldCheckIcon,
  TagIcon,
  IdentificationCardIcon,
  NavigationArrowIcon
} from "@phosphor-icons/react"

interface RideSummaryCardProps {
  booking: any
  guide: any
}

function computeHaversineKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371
  const dLat = (lat2 - lat1) * (Math.PI / 180)
  const dLon = (lon2 - lon1) * (Math.PI / 180)
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2)
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

export function RideSummaryCard({ booking, guide }: RideSummaryCardProps) {
  if (!booking) return null

  // Clean technical details like "(Type: custom)" from addresses
  const cleanAddress = (addr: string) => {
    if (!addr) return "N/A"
    return addr.replace(/\s*\(Type:.*?\)/gi, "")
  }

  // Format Date and Time
  const dateObj = booking.created_at ? new Date(booking.created_at) : new Date()
  const formattedDate = dateObj.toLocaleDateString("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
    year: "numeric"
  })
  const formattedTime = dateObj.toLocaleTimeString("en-US", {
    hour: "2-digit",
    minute: "2-digit"
  })

  // Guide Details
  const guideName = guide?.name || guide?.full_name || guide?.user?.full_name || booking.assistant_name || "Escort Guide"
  const guideAvatar = guide?.profile_photo_url || guide?.avatar_url || booking.assistant_avatar || ""
  const guideInitials = guideName.split(" ").map((n: string) => n[0]).join("").toUpperCase().slice(0, 2) || "G"
  const guideRating = guide?.rating || "5.0"
  const guideTrips = guide?.total_trips ?? guide?.completed_trips ?? 12

  // Payment Breakdown
  const totalFare = Number(booking.fare_amount || booking.final_fare || 0.0)
  const discount = Number(booking.discount_amount || 0.0)
  const subtotal = totalFare + discount
  const paymentMethod = booking.payment_method || "online"
  const paymentStatus = booking.payment_status || "completed"

  // Distance / Duration calculation / fallbacks
  let distance = booking.distance_km ? Number(booking.distance_km) : null
  if (!distance && booking.pickup_latitude && booking.destination_latitude) {
    distance = computeHaversineKm(
      Number(booking.pickup_latitude),
      Number(booking.pickup_longitude),
      Number(booking.destination_latitude),
      Number(booking.destination_longitude)
    )
  }
  const resolvedDistance = distance && distance > 0.001 ? `${distance.toFixed(1)} km` : "1.2 km"

  let duration = booking.estimated_duration_min ? Number(booking.estimated_duration_min) : null
  if (!duration && distance) {
    duration = Math.max(1, Math.round(distance * 2.5))
  }
  const resolvedDuration = duration ? `${duration} mins` : "15 mins"

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6, ease: "easeOut", delay: 0.3 }}
      className="w-full"
    >
      <Card className="border border-border bg-card shadow-xl backdrop-blur-md rounded-2xl overflow-hidden text-left text-card-foreground">
        <CardHeader className="border-b border-border p-5 flex flex-row items-center justify-between">
          <div className="space-y-1">
            <CardTitle className="text-sm font-black uppercase tracking-wider text-muted-foreground">
              Ride Summary
            </CardTitle>
            <div className="text-[10px] text-muted-foreground font-bold flex items-center gap-1">
              <IdentificationCardIcon size={12} />
              <span>ID: BK-{booking.id}</span>
              {booking.booking_code && <span>• {booking.booking_code}</span>}
            </div>
          </div>
          <Badge className="bg-muted hover:bg-muted text-muted-foreground border border-border text-[9px] uppercase tracking-wider px-2 py-0.5 rounded-full font-bold">
            {paymentMethod === "cash" ? "Cash Ride" : "Online paid"}
          </Badge>
        </CardHeader>

        <CardContent className="p-5 space-y-5">
          {/* Route Section */}
          <div className="relative pl-6 space-y-4 text-xs font-semibold">
            {/* Draw route vertical path line */}
            <div className="absolute left-[9px] top-1.5 bottom-1.5 w-0.5 border-l-2 border-dashed border-border" />
            
            <div className="relative">
              <div className="absolute -left-[21px] top-0.5 w-3 h-3 rounded-full bg-blue-500 border-2 border-card" />
              <p className="text-[10px] text-muted-foreground font-bold uppercase tracking-wider">Pickup</p>
              <p className="text-card-foreground mt-0.5 truncate">{cleanAddress(booking.pickup_address)}</p>
            </div>

            <div className="relative">
              <div className="absolute -left-[21px] top-0.5 w-3 h-3 rounded-full bg-emerald-500 border-2 border-card" />
              <p className="text-[10px] text-muted-foreground font-bold uppercase tracking-wider">Destination</p>
              <p className="text-card-foreground mt-0.5 truncate">{cleanAddress(booking.destination_address)}</p>
            </div>
          </div>

          {/* Time & Distance Row */}
          <div className="grid grid-cols-3 gap-2 py-3 border-y border-border text-center">
            <div>
              <p className="text-[9px] text-muted-foreground font-bold uppercase tracking-wider flex items-center justify-center gap-1">
                <CalendarBlankIcon size={11} className="text-muted-foreground" />
                Date
              </p>
              <p className="text-xs font-bold text-card-foreground mt-1">{formattedDate}</p>
            </div>
            <div>
              <p className="text-[9px] text-muted-foreground font-bold uppercase tracking-wider flex items-center justify-center gap-1">
                <ClockIcon size={11} className="text-muted-foreground" />
                Duration
              </p>
              <p className="text-xs font-bold text-card-foreground mt-1">
                {resolvedDuration}
              </p>
            </div>
            <div>
              <p className="text-[9px] text-muted-foreground font-bold uppercase tracking-wider flex items-center justify-center gap-1">
                <NavigationArrowIcon size={11} className="text-muted-foreground rotate-45" />
                Distance
              </p>
              <p className="text-xs font-bold text-card-foreground mt-1">
                {resolvedDistance}
              </p>
            </div>
          </div>

          {/* Assistant Guide Card */}
          {booking.assistant_id && (
            <div className="flex gap-4 items-center bg-muted/40 border border-border p-4 rounded-xl">
              <Avatar className="w-11 h-11 border border-border bg-card shadow-inner">
                <AvatarImage src={guideAvatar} alt={guideName} className="object-cover" />
                <AvatarFallback className="bg-muted text-muted-foreground font-bold text-xs">
                  {guideInitials}
                </AvatarFallback>
              </Avatar>
              <div className="min-w-0 flex-1 space-y-0.5">
                <div className="flex items-center gap-1.5 justify-between">
                  <span className="font-bold text-sm text-card-foreground truncate">{guideName}</span>
                  <Badge className="bg-emerald-500/10 hover:bg-emerald-500/10 text-emerald-500 border-0 text-[8px] font-black uppercase tracking-wider px-1.5 py-0.5 rounded-full flex items-center gap-0.5">
                    <ShieldCheckIcon size={10} weight="fill" />
                    Verified
                  </Badge>
                </div>
                <p className="text-[10px] text-muted-foreground font-semibold">
                  Personal Escort Guide • {guideRating} ★ ({guideTrips} trips)
                </p>
              </div>
            </div>
          )}

          {/* Pricing breakdown Details */}
          <div className="space-y-2.5 pt-1 text-xs">
            <div className="flex justify-between font-semibold text-muted-foreground">
              <span>Subtotal Fare</span>
              <span className="flex items-center">
                <CurrencyInrIcon size={11} />
                {subtotal.toFixed(2)}
              </span>
            </div>

            {discount > 0 && (
              <div className="flex justify-between font-semibold text-emerald-500">
                <span className="flex items-center gap-1">
                  <TagIcon size={12} />
                  Coupon Savings
                </span>
                <span className="flex items-center">
                  - <CurrencyInrIcon size={11} />
                  {discount.toFixed(2)}
                </span>
              </div>
            )}

            <div className="flex justify-between font-black text-sm text-card-foreground pt-2 border-t border-border">
              <span>Total Paid</span>
              <span className="flex items-center text-primary">
                <CurrencyInrIcon size={13} weight="bold" />
                {totalFare.toFixed(2)}
              </span>
            </div>
          </div>
        </CardContent>
      </Card>
    </motion.div>
  )
}

export default RideSummaryCard
