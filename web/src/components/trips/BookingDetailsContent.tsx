import React, { useEffect, useState } from "react"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Skeleton } from "@/components/ui/skeleton"
import { Empty, EmptyTitle, EmptyDescription } from "@/components/ui/empty"
import { Button } from "@/components/ui/button"
import { BookingStatusBadge } from "./BookingStatusBadge"
import { TripRouteMap } from "./TripRouteMap"
import { TripSummaryCard } from "./TripSummaryCard"
import { DriverInfoCard } from "./DriverInfoCard"
import { RideTimeline } from "./RideTimeline"
import { BookingActions } from "./BookingActions"
import { FareBreakdownCard } from "@/components/booking/FareBreakdownCard"
import { RideOtpCard } from "@/components/booking/RideOtpCard"
import { OtpVerificationCard } from "@/components/booking/OtpVerificationCard"
import { useAuth } from "@/store/auth-context"
import { XIcon, MapPinIcon } from "@phosphor-icons/react"
import { assistantsService } from "@/services/assistants.service"

export interface StatusHistoryItem {
  status: string
  changed_at: string
  changed_by?: number
}

export interface BookingDetailsData {
  id: number
  guest_id: number
  assistant_id?: number | null
  status: string
  pickup_latitude: number
  pickup_longitude: number
  pickup_address: string
  destination_latitude: number
  destination_longitude: number
  destination_address: string
  fare_amount?: number | string
  final_fare?: number | string
  estimated_fare?: number | string
  otp_start?: string
  created_at: string
  scheduled_time?: string
  coupon_code?: string
  status_history?: StatusHistoryItem[]
}

interface BookingDetailsContentProps {
  booking: BookingDetailsData | null
  loading?: boolean
  onClose?: () => void
}

export function BookingDetailsContent({ booking, loading, onClose }: BookingDetailsContentProps) {
  const { user } = useAuth()
  const [guide, setGuide] = useState<any>(null)
  const [loadingGuide, setLoadingGuide] = useState(false)

  useEffect(() => {
    if (!booking?.assistant_id) {
      setGuide(null)
      return
    }

    const fetchGuide = async () => {
      setLoadingGuide(true)
      try {
        const p = await assistantsService.getAssistantProfile(booking.assistant_id!)
        setGuide(p)
      } catch (err) {
        console.error("Failed to load guide details for sheet", err)
      } finally {
        setLoadingGuide(false)
      }
    }

    fetchGuide()
  }, [booking?.assistant_id])

  if (loading) {
    return (
      <div className="flex flex-col h-full bg-background text-foreground">
        <div className="p-4 border-b border-border space-y-2">
          <Skeleton className="h-6 w-1/3 rounded-lg" />
          <Skeleton className="h-4 w-1/2 rounded-lg" />
        </div>
        <div className="p-4 space-y-4 flex-1">
          <Skeleton className="h-48 w-full rounded-2xl" />
          <Skeleton className="h-32 w-full rounded-2xl" />
          <Skeleton className="h-28 w-full rounded-2xl" />
        </div>
      </div>
    )
  }

  if (!booking) {
    return (
      <div className="flex flex-col items-center justify-center h-full p-6 text-center">
        <Empty>
          <EmptyTitle>No Booking Details Found</EmptyTitle>
          <EmptyDescription>Select a booking record from your trip history or active tracking list.</EmptyDescription>
        </Empty>
        {onClose && (
          <Button onClick={onClose} variant="outline" className="mt-4 rounded-xl font-bold text-xs">
            Close Panel
          </Button>
        )}
      </div>
    )
  }

  const createdDate = new Date(booking.created_at).toLocaleDateString([], {
    month: "short",
    day: "numeric",
    year: "numeric",
  })

  const hasCoords =
    booking.pickup_latitude &&
    booking.pickup_longitude &&
    booking.destination_latitude &&
    booking.destination_longitude

  const totalFare = Number(booking.fare_amount || booking.final_fare || booking.estimated_fare || 0)

  const statusUpper = (booking.status || "").toUpperCase()
  const isPreStart = ["PENDING", "SEARCHING", "ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE", "ARRIVED_PICKUP"].includes(statusUpper)
  const isRoleAssistant = (user?.role || "").toLowerCase() === "assistant"
  const isRoleGuest = !isRoleAssistant

  return (
    <div className="flex flex-col h-full max-h-screen bg-background text-foreground relative overflow-hidden">
      {/* Fixed Header */}
      <div className="shrink-0 p-4 sm:p-5 border-b border-border/80 bg-background/95 backdrop-blur-md flex items-center justify-between gap-4 z-30">
        <div className="space-y-1 text-left min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <span className="text-sm font-black tracking-tight text-foreground font-mono">
              #BK-{booking.id}
            </span>
            <BookingStatusBadge status={booking.status} />
          </div>
          <p className="text-[10px] text-muted-foreground font-bold">Booked on {createdDate}</p>
        </div>

        {onClose && (
          <Button
            variant="ghost"
            size="icon"
            onClick={onClose}
            className="rounded-full shrink-0 hover:bg-muted cursor-pointer"
          >
            <XIcon size={18} />
            <span className="sr-only">Close Panel</span>
          </Button>
        )}
      </div>

      {/* Main Scrollable Content Area */}
      <div className="flex-1 min-h-0 overflow-y-auto p-4 sm:p-5 space-y-5 text-left pb-8">
        {/* Map Section */}
        {hasCoords && (
          <TripRouteMap
            pickupLat={booking.pickup_latitude}
            pickupLng={booking.pickup_longitude}
            pickupAddress={booking.pickup_address}
            destLat={booking.destination_latitude}
            destLng={booking.destination_longitude}
            destAddress={booking.destination_address}
            className="h-56 sm:h-64 md:h-72 shadow-md"
          />
        )}

        {/* Trip Summary */}
        <TripSummaryCard
          pickupAddress={booking.pickup_address}
          destinationAddress={booking.destination_address}
        />

        {/* Driver / Assistant Information */}
        {booking.assistant_id && (
          loadingGuide ? (
            <Skeleton className="h-28 w-full rounded-2xl" />
          ) : (
            <DriverInfoCard
              name={guide?.name || guide?.user?.name || `Guide #${booking.assistant_id}`}
              phone={guide?.phone || guide?.user?.phone}
              rating={
                guide?.avg_rating != null
                  ? Number(guide.avg_rating)
                  : guide?.rating != null
                  ? Number(guide.rating)
                  : guide?.trust_score != null
                  ? Number(guide.trust_score)
                  : 4.9
              }
              totalTrips={
                guide?.total_trips != null
                  ? Number(guide.total_trips)
                  : guide?.completed_trips != null
                  ? Number(guide.completed_trips)
                  : 12
              }
              avatarUrl={guide?.profile_photo_url || guide?.avatar_url}
              bookingId={booking.id}
              currentUserId={user?.id}
              status={booking.status}
            />
          )
        )}

        {/* Passenger View: Ride Start OTP Card */}
        {isRoleGuest && isPreStart && (
          <RideOtpCard otp={booking.otp_start || "123456"} />
        )}

        {/* Assistant View: OTP Verification Card */}
        {isRoleAssistant && isPreStart && (
          <OtpVerificationCard bookingId={booking.id} onSuccess={onClose} />
        )}

        {/* Fare Breakdown */}
        <FareBreakdownCard
          breakdown={{
            base_fare: 50.00,
            distance_fare: Math.round(totalFare * 0.55),
            time_fare: Math.round(totalFare * 0.25),
            booking_fee: 15.00,
            taxes: Math.round(totalFare * 0.05),
            total_fare: totalFare,
          }}
        />

        {/* Ride Timeline */}
        <RideTimeline
          status={booking.status}
          createdAt={booking.created_at}
          scheduledAt={booking.scheduled_time}
          statusHistory={booking.status_history}
        />
      </div>

      {/* Fixed Action Footer */}
      <div className="shrink-0 border-t border-border/80 bg-card/95 backdrop-blur-md z-30">
        <BookingActions bookingId={booking.id} status={booking.status} onClose={onClose} />
      </div>
    </div>
  )
}
export default BookingDetailsContent
