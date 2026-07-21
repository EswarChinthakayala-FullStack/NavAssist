import React from "react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { NavigationArrowIcon, MapPinIcon, CompassIcon } from "@phosphor-icons/react"

interface RideNavigationCardProps {
  destLat: number
  destLng: number
  destAddress: string
  className?: string
}

export function RideNavigationCard({
  destLat,
  destLng,
  destAddress,
  className,
}: RideNavigationCardProps) {
  const handleOpenMaps = () => {
    const url = `https://www.google.com/maps/dir/?api=1&destination=${destLat},${destLng}`
    window.open(url, "_blank")
  }

  return (
    <Card className={`rounded-2xl border border-primary/30 bg-primary/5 p-4 space-y-3 shadow-sm text-left ${className}`}>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-primary/10 text-primary">
            <CompassIcon size={20} weight="fill" />
          </div>
          <div>
            <h4 className="font-extrabold text-sm text-foreground">Turn-by-Turn Navigation</h4>
            <p className="text-[10px] text-muted-foreground font-semibold">Open external GPS navigation</p>
          </div>
        </div>
      </div>

      <div className="text-xs space-y-1 bg-background/80 p-3 rounded-xl border border-border/50">
        <span className="text-[9px] font-bold text-muted-foreground uppercase block">Navigating To</span>
        <p className="font-bold text-foreground truncate">{destAddress}</p>
      </div>

      <Button
        onClick={handleOpenMaps}
        className="w-full bg-primary text-primary-foreground font-extrabold text-xs py-4 rounded-xl flex items-center justify-center gap-2 shadow-md cursor-pointer hover:scale-[1.01] transition-transform"
      >
        <NavigationArrowIcon size={18} weight="bold" />
        <span>Open Navigation in Maps</span>
      </Button>
    </Card>
  )
}
export default RideNavigationCard
