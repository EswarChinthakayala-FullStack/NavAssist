import React from "react"
import { Badge } from "@/components/ui/badge"
import { ClockIcon, MapPinIcon } from "@phosphor-icons/react"
import { cn } from "@/lib/utils"

interface EtaBadgeProps {
  etaMins: number
  distanceKm: number
  className?: string
}

export function EtaBadge({ etaMins, distanceKm, className }: EtaBadgeProps) {
  return (
    <div className={cn("flex gap-2 items-center select-none", className)}>
      <Badge className="bg-primary hover:bg-primary/95 text-white border-0 px-3 py-1.5 rounded-full text-xs font-black shadow-glow-primary flex items-center gap-1.5 animate-pulse">
        <ClockIcon size={14} weight="fill" />
        ETA: {etaMins} mins
      </Badge>
      <Badge variant="outline" className="border-border text-muted-foreground bg-card/50 px-2.5 py-1.5 rounded-full text-xs font-extrabold flex items-center gap-1.5">
        <MapPinIcon size={14} className="text-muted-foreground" />
        {distanceKm.toFixed(1)} km away
      </Badge>
    </div>
  )
}
export default EtaBadge
