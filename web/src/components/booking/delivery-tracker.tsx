import React, { useState, useEffect } from "react"
import { Map, MapMarker, MarkerContent, MapRoute, MapControls } from "@/components/ui/map"
import { pickup, dropoff, mapView, progressFraction, buildRouteUrl, routeStyle } from "@/app/delivery/data"
import { MapPinIcon, NavigationArrowIcon, InfoIcon } from "@phosphor-icons/react"
import { toast } from "sonner"

export function DeliveryTracker() {
  const [routeCoords, setRouteCoords] = useState<[number, number][]>([])
  const [progressCoords, setProgressCoords] = useState<[number, number][]>([])
  const [remainingCoords, setRemainingCoords] = useState<[number, number][]>([])
  const [courierPos, setCourierPos] = useState<[number, number]>([pickup.lng, pickup.lat])
  const [loading, setLoading] = useState(true)

  const fetchRoute = async () => {
    try {
      const url = buildRouteUrl(pickup, dropoff)
      const res = await fetch(url)
      const data = await res.json()
      
      const coords = data.routes?.[0]?.geometry?.coordinates
      if (coords && coords.length > 0) {
        setRouteCoords(coords)
        
        // Calculate split point based on progressFraction
        const courierIdx = Math.floor(coords.length * progressFraction)
        const currentPos = coords[courierIdx] || coords[0]
        setCourierPos(currentPos)

        // Split coordinates list
        const prog = coords.slice(0, courierIdx + 1)
        const rem = coords.slice(courierIdx)
        setProgressCoords(prog)
        setRemainingCoords(rem)
      } else {
        // Fallback straight line coordinates if OSRM endpoint fails
        const mockCoords: [number, number][] = [
          [pickup.lng, pickup.lat],
          [(pickup.lng + dropoff.lng) / 2, (pickup.lat + dropoff.lat) / 2],
          [dropoff.lng, dropoff.lat]
        ]
        setRouteCoords(mockCoords)
        const courierIdx = Math.floor(mockCoords.length * progressFraction)
        setCourierPos(mockCoords[courierIdx])
        setProgressCoords(mockCoords.slice(0, courierIdx + 1))
        setRemainingCoords(mockCoords.slice(courierIdx))
      }
    } catch (err) {
      toast.error("Error drawing live telemetry route tracking path.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchRoute()
  }, [])

  return (
    <div className="w-full flex flex-col gap-3">
      {/* Map visualization block */}
      <div className="w-full h-80 rounded-2xl overflow-hidden border border-border shadow-md relative z-10">
        {loading ? (
          <div className="absolute inset-0 bg-muted/20 flex flex-col items-center justify-center gap-2 backdrop-blur-xs">
            <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
            <span className="text-xs text-muted-foreground font-semibold">Connecting telemetry stream...</span>
          </div>
        ) : null}

        <Map
          viewport={{
            center: mapView.center,
            zoom: mapView.zoom
          }}
          className="w-full h-full"
        >
          <MapControls position="top-right" />

          {/* Start pickup point marker */}
          <MapMarker latitude={pickup.lat} longitude={pickup.lng}>
            <MarkerContent>
              <div className="flex flex-col items-center -translate-y-1/2">
                <div className="bg-success text-white p-1 rounded-full shadow-lg border-2 border-white scale-90">
                  <MapPinIcon size={14} weight="fill" />
                </div>
              </div>
            </MarkerContent>
          </MapMarker>

          {/* End dropoff point marker */}
          <MapMarker latitude={dropoff.lat} longitude={dropoff.lng}>
            <MarkerContent>
              <div className="flex flex-col items-center -translate-y-1/2">
                <div className="bg-destructive text-white p-1 rounded-full shadow-lg border-2 border-white scale-90">
                  <MapPinIcon size={14} weight="fill" />
                </div>
              </div>
            </MarkerContent>
          </MapMarker>

          {/* Courier/Assistant moving marker */}
          <MapMarker latitude={courierPos[1]} longitude={courierPos[0]}>
            <MarkerContent>
              <div className="flex flex-col items-center -translate-y-1/2 z-30">
                <div className="bg-primary text-white p-2 rounded-full shadow-xl border-2 border-white animate-pulse">
                  <NavigationArrowIcon size={16} weight="fill" className="rotate-45" />
                </div>
              </div>
            </MarkerContent>
          </MapMarker>

          {/* Progress covered path (Blue) */}
          {progressCoords.length > 0 && (
            <MapRoute
              id="progress-route"
              coordinates={progressCoords}
              color={routeStyle.progress.color}
              width={routeStyle.progress.width}
              opacity={routeStyle.progress.opacity}
            />
          )}

          {/* Remaining en-route path (Gray) */}
          {remainingCoords.length > 0 && (
            <MapRoute
              id="remaining-route"
              coordinates={remainingCoords}
              color={routeStyle.remaining.color.light}
              width={routeStyle.remaining.width}
              opacity={routeStyle.remaining.opacity}
              dashArray={[2, 2]}
            />
          )}
        </Map>
      </div>

      <div className="p-3 bg-muted/40 border border-border/80 rounded-xl text-[10px] text-muted-foreground flex gap-2 items-start leading-relaxed font-semibold">
        <InfoIcon size={14} className="flex-shrink-0 text-primary mt-0.5" />
        <span>This live routing utilizes public OSRM tile rendering coordinates. Current en-route progress stream resolves at {(progressFraction * 100).toFixed(0)}%.</span>
      </div>
    </div>
  )
}
export default DeliveryTracker
