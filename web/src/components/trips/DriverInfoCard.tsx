import React, { useState } from "react"
import { Card, CardContent } from "@/components/ui/card"
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { StarIcon, PhoneIcon, ChatTeardropTextIcon, ShieldCheckIcon } from "@phosphor-icons/react"
import { toast } from "sonner"
import { BookingChatPanel } from "./BookingChatPanel"
import { getFullImageUrl } from "@/components/shared/ImageThumbnail"

interface DriverInfoCardProps {
  name: string
  phone?: string
  rating?: number
  totalTrips?: number
  avatarUrl?: string
  vehicleName?: string
  vehicleNumber?: string
  bookingId?: number
  currentUserId?: number
  status?: string
}

export function DriverInfoCard({
  name,
  phone,
  rating = 4.9,
  totalTrips = 12,
  avatarUrl,
  vehicleName = "NavAssist Verified Escort Guide",
  vehicleNumber = "IND-DEL-2026",
  bookingId,
  currentUserId,
  status,
}: DriverInfoCardProps) {
  const [chatOpen, setChatOpen] = useState(false)
  const initials = name
    ? name
        .split(" ")
        .map((p) => p[0])
        .join("")
    : "AG"

  const handleCall = () => {
    if (phone) {
      window.location.href = `tel:${phone}`
    } else {
      toast.info("Calling assistant guide...")
    }
  }

  const normalizedStatus = (status || "").toUpperCase()
  const isActive = [
    "ACCEPTED",
    "ASSIGNED",
    "ASSISTANT_ENROUTE",
    "ARRIVED_PICKUP",
    "STARTED",
    "IN_PROGRESS",
    "GUEST_PICKED_UP"
  ].includes(normalizedStatus)

  const handleMessage = () => {
    if (!isActive) {
      toast.error("Secure chat is only available for active bookings.")
      return
    }
    setChatOpen(true)
  }

  return (
    <>
      <Card className="rounded-2xl border border-border/80 bg-card p-4 space-y-4 shadow-sm">
        <div className="flex items-center justify-between">
          <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest block">
            Assigned Escort Assistant
          </span>
          <Badge className="bg-emerald-500/15 text-emerald-500 border-0 text-[10px] px-2 py-0.5 rounded-full font-bold flex items-center gap-1">
            <ShieldCheckIcon size={12} weight="fill" />
            Verified
          </Badge>
        </div>

        <div className="flex items-center gap-4">
          <Avatar className="w-14 h-14 border border-border shadow-sm">
            <AvatarImage src={avatarUrl ? getFullImageUrl(avatarUrl) : undefined} alt={name} className="object-cover" />
            <AvatarFallback className="font-extrabold text-sm">{initials}</AvatarFallback>
          </Avatar>

          <div className="flex-1 min-w-0">
            <h4 className="font-extrabold text-sm text-foreground truncate">{name}</h4>
            <p className="text-[11px] text-muted-foreground font-semibold truncate mt-0.5">{vehicleName}</p>

            <div className="flex items-center gap-3 mt-1.5 text-xs">
              <span className="flex items-center gap-1 font-bold text-amber-500">
                <StarIcon size={14} weight="fill" className="text-amber-500" />
                {rating && Number(rating) > 0 ? Number(rating).toFixed(1) : "New"}
              </span>
              <span className="text-muted-foreground">•</span>
              <span className="text-muted-foreground font-semibold">
                {totalTrips && Number(totalTrips) > 0 ? `${totalTrips} Completed ${Number(totalTrips) === 1 ? 'Trip' : 'Trips'}` : "No trips yet"}
              </span>
            </div>
          </div>
        </div>

        {/* Action buttons (Call / Message) */}
        <div className="grid grid-cols-2 gap-3 pt-1">
          <Button
            onClick={handleCall}
            className="bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl py-4 flex items-center justify-center gap-2 cursor-pointer shadow-sm"
          >
            <PhoneIcon size={16} weight="bold" />
            <span>Call Guide</span>
          </Button>

          <Button
            variant="outline"
            onClick={handleMessage}
            disabled={!isActive}
            className="font-bold text-xs rounded-xl py-4 flex items-center justify-center gap-2 cursor-pointer border-border/80 hover:bg-muted disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <ChatTeardropTextIcon size={16} weight="bold" className="text-primary" />
            <span>Send Message</span>
          </Button>
        </div>
      </Card>

      {bookingId && currentUserId && (
        <BookingChatPanel
          open={chatOpen}
          onOpenChange={setChatOpen}
          bookingId={bookingId}
          currentUserId={currentUserId}
        />
      )}
    </>
  )
}
export default DriverInfoCard
