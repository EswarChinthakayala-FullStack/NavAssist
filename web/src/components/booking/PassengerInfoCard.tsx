import React from "react"
import { Card } from "@/components/ui/card"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { PhoneIcon, ChatTeardropTextIcon, UserIcon, MapPinIcon } from "@phosphor-icons/react"
import { toast } from "sonner"

interface PassengerInfoCardProps {
  name: string
  phone?: string
  pickupAddress: string
  destinationAddress: string
  distanceKm?: number
  className?: string
}

export function PassengerInfoCard({
  name,
  phone = "+91 98765 00000",
  pickupAddress,
  destinationAddress,
  distanceKm,
  className,
}: PassengerInfoCardProps) {
  const initials = name
    ? name
        .split(" ")
        .map((p) => p[0])
        .join("")
    : "PA"

  const handleCall = () => {
    if (phone) window.location.href = `tel:${phone}`
    else toast.info("Calling passenger...")
  }

  const handleMessage = () => {
    toast.info(`Opening secure chat with passenger ${name}...`)
  }

  return (
    <Card className={`rounded-2xl border border-border/80 bg-card p-4 space-y-4 shadow-sm text-left ${className}`}>
      <div className="flex items-center justify-between">
        <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest block">
          Passenger Request Details
        </span>
        <Badge variant="outline" className="text-[10px] font-bold px-2 py-0.5 rounded-full uppercase">
          Passenger
        </Badge>
      </div>

      <div className="flex items-center gap-4">
        <Avatar className="w-12 h-12 border border-border bg-muted">
          <AvatarFallback className="font-extrabold text-sm text-foreground">{initials}</AvatarFallback>
        </Avatar>

        <div className="flex-1 min-w-0">
          <h4 className="font-extrabold text-sm text-foreground truncate">{name}</h4>
          <p className="text-[11px] text-muted-foreground font-semibold truncate mt-0.5">
            {distanceKm != null ? `${distanceKm.toFixed(1)} km journey` : "Assistance Request"}
          </p>
        </div>
      </div>

      {/* Locations */}
      <div className="space-y-2.5 text-xs bg-muted/20 p-3 rounded-xl border border-border/40">
        <div className="flex items-start gap-2.5">
          <div className="w-5 h-5 rounded-full bg-primary/10 text-primary flex items-center justify-center font-black text-[10px] shrink-0 mt-0.5">
            A
          </div>
          <div className="min-w-0 flex-1">
            <span className="text-[9px] font-bold text-muted-foreground uppercase block">Pickup Location</span>
            <p className="font-bold text-foreground truncate">{pickupAddress}</p>
          </div>
        </div>

        <div className="flex items-start gap-2.5">
          <div className="w-5 h-5 rounded-full bg-destructive/10 text-destructive flex items-center justify-center font-black text-[10px] shrink-0 mt-0.5">
            B
          </div>
          <div className="min-w-0 flex-1">
            <span className="text-[9px] font-bold text-muted-foreground uppercase block">Destination Location</span>
            <p className="font-bold text-foreground truncate">{destinationAddress}</p>
          </div>
        </div>
      </div>

      {/* Actions */}
      <div className="grid grid-cols-2 gap-3 pt-1">
        <Button
          onClick={handleCall}
          className="bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl py-4 flex items-center justify-center gap-2 cursor-pointer shadow-sm"
        >
          <PhoneIcon size={16} weight="bold" />
          <span>Call Passenger</span>
        </Button>

        <Button
          variant="outline"
          onClick={handleMessage}
          className="font-bold text-xs rounded-xl py-4 flex items-center justify-center gap-2 cursor-pointer border-border/80 hover:bg-muted"
        >
          <ChatTeardropTextIcon size={16} weight="bold" className="text-primary" />
          <span>Message</span>
        </Button>
      </div>
    </Card>
  )
}
export default PassengerInfoCard
