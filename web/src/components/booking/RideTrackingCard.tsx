import React from "react"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { BroadcastIcon, MapPinIcon, ClockIcon, PathIcon } from "@phosphor-icons/react"

interface RideTrackingCardProps {
  status: string
  distanceKm?: number
  durationMin?: number
  className?: string
}

export function RideTrackingCard({
  status,
  distanceKm,
  durationMin,
  className,
}: RideTrackingCardProps) {
  const isLive = ["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE", "ARRIVED_PICKUP", "STARTED", "IN_PROGRESS"].includes(status.toUpperCase())

  return (
    <Card className={`rounded-2xl border border-border/80 bg-card p-4 space-y-3 shadow-sm text-left ${className}`}>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Badge className="bg-emerald-500/15 text-emerald-500 border border-emerald-500/30 rounded-full font-bold text-[10px] px-2.5 py-0.5 flex items-center gap-1.5">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-ping" />
            <span>{isLive ? "Live GPS Sync" : "Journey Details"}</span>
          </Badge>
        </div>

        <Badge variant="outline" className="font-bold text-[10px] uppercase rounded-full">
          {status}
        </Badge>
      </div>

      <div className="grid grid-cols-2 gap-3 pt-1">
        <div className="p-3 bg-muted/20 border border-border/40 rounded-xl text-center">
          <span className="text-[9px] font-bold text-muted-foreground uppercase tracking-wider block">Est. Distance</span>
          <span className="text-sm font-black text-foreground mt-0.5 block">
            {distanceKm != null ? `${distanceKm.toFixed(1)} km` : "N/A"}
          </span>
        </div>

        <div className="p-3 bg-muted/20 border border-border/40 rounded-xl text-center">
          <span className="text-[9px] font-bold text-muted-foreground uppercase tracking-wider block">Est. Duration</span>
          <span className="text-sm font-black text-foreground mt-0.5 block">
            {durationMin != null ? `${durationMin} mins` : "N/A"}
          </span>
        </div>
      </div>
    </Card>
  )
}
export default RideTrackingCard
