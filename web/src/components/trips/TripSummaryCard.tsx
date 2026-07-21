import React from "react"
import { Card, CardContent, CardHeader } from "@/components/ui/card"
import { CompassIcon, MapPinIcon, ClockIcon, PathIcon } from "@phosphor-icons/react"

interface TripSummaryCardProps {
  pickupAddress: string
  destinationAddress: string
  distanceKm?: number
  durationMin?: number
  serviceType?: string
}

export function TripSummaryCard({
  pickupAddress,
  destinationAddress,
  distanceKm,
  durationMin,
  serviceType = "Personal Travel Assistance",
}: TripSummaryCardProps) {
  return (
    <Card className="rounded-2xl border border-border/80 bg-card overflow-hidden shadow-sm">
      <CardHeader className="bg-muted/15 p-3.5 border-b border-border/40 flex flex-row items-center justify-between">
        <h4 className="text-xs font-bold uppercase tracking-widest text-primary flex items-center gap-1.5">
          <CompassIcon size={16} />
          Route & Journey Summary
        </h4>
        <span className="text-[10px] font-semibold text-muted-foreground uppercase bg-muted/40 px-2 py-0.5 rounded-full border border-border/50">
          {serviceType}
        </span>
      </CardHeader>

      <CardContent className="p-4 space-y-4 text-xs">
        {/* Origin */}
        <div className="flex items-start gap-3">
          <div className="w-6 h-6 rounded-full bg-primary/10 text-primary flex items-center justify-center font-black text-xs shrink-0 mt-0.5">
            A
          </div>
          <div className="space-y-0.5 flex-1 min-w-0">
            <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider block">Pickup Point</span>
            <p className="font-bold text-foreground leading-snug break-words">{pickupAddress}</p>
          </div>
        </div>

        {/* Destination */}
        <div className="flex items-start gap-3">
          <div className="w-6 h-6 rounded-full bg-destructive/10 text-destructive flex items-center justify-center font-black text-xs shrink-0 mt-0.5">
            B
          </div>
          <div className="space-y-0.5 flex-1 min-w-0">
            <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider block">Destination Point</span>
            <p className="font-bold text-foreground leading-snug break-words">{destinationAddress}</p>
          </div>
        </div>

        {/* Trip Metrics Pill Row */}
        {(distanceKm != null || durationMin != null) && (
          <div className="pt-2 border-t border-border/40 grid grid-cols-2 gap-3">
            <div className="flex items-center gap-2 p-2.5 rounded-xl bg-muted/20 border border-border/50">
              <PathIcon size={16} className="text-primary shrink-0" />
              <div>
                <span className="text-[9px] font-bold uppercase tracking-wider text-muted-foreground block">Distance</span>
                <span className="font-extrabold text-foreground">{distanceKm != null ? `${distanceKm.toFixed(1)} km` : "N/A"}</span>
              </div>
            </div>

            <div className="flex items-center gap-2 p-2.5 rounded-xl bg-muted/20 border border-border/50">
              <ClockIcon size={16} className="text-primary shrink-0" />
              <div>
                <span className="text-[9px] font-bold uppercase tracking-wider text-muted-foreground block">Est. Time</span>
                <span className="font-extrabold text-foreground">{durationMin != null ? `${durationMin} mins` : "N/A"}</span>
              </div>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
export default TripSummaryCard
