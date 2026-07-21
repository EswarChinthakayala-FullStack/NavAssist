import React, { useState, useEffect } from "react"
import { Map, MapMarker, MarkerContent, MapRoute, MapControls } from "@/components/ui/map"
import { pickup as defaultPickup, dropoff as defaultDropoff, mapView, progressFraction, buildRouteUrl, routeStyle } from "@/app/delivery/data"
import { MapPinIcon, NavigationArrowIcon, UserIcon } from "@phosphor-icons/react"
import { toast } from "sonner"

interface LiveMapProps {
  pickupLat?: number
  pickupLon?: number
  destinationLat?: number
  destinationLon?: number
  assistantLat?: number
  assistantLon?: number
  status?: string
}

export function LiveMap({
  pickupLat,
  pickupLon,
  destinationLat,
  destinationLon,
  assistantLat,
  assistantLon,
  status = "STARTED"
}: LiveMapProps) {
  const [routeCoords, setRouteCoords] = useState<[number, number][]>([])
  const [progressCoords, setProgressCoords] = useState<[number, number][]>([])
  const [remainingCoords, setRemainingCoords] = useState<[number, number][]>([])
  const [loading, setLoading] = useState(true)

  // Use props coordinates if available, otherwise fallback to delivery data
  const pickupPoint = pickupLat && pickupLon ? { lat: pickupLat, lng: pickupLon } : defaultPickup
  const dropoffPoint = destinationLat && destinationLon ? { lat: destinationLat, lng: destinationLon } : defaultDropoff
  
  // Courier position defaults to assistant coords or mid-point animation
  const [courierPos, setCourierPos] = useState<[number, number]>([pickupPoint.lng, pickupPoint.lat])

  const fetchRoute = async () => {
    try {
      const url = buildRouteUrl(pickupPoint, dropoffPoint)
      const res = await fetch(url)
      const data = await res.json()
      
      const coords = data.routes?.[0]?.geometry?.coordinates
      if (coords && coords.length > 0) {
        setRouteCoords(coords)
        
        let courierIdx = Math.floor(coords.length * progressFraction)
        
        // If real assistant coordinates are passed, resolve to closest index on route
        if (assistantLat && assistantLon) {
          setCourierPos([assistantLon, assistantLat])
          
          // Find closest point in route index to assistant
          let minDistance = Infinity
          let closestIdx = 0
          for (let i = 0; i < coords.length; i++) {
            const [lng, lat] = coords[i]
            const d = Math.pow(lng - assistantLon, 2) + Math.pow(lat - assistantLat, 2)
            if (d < minDistance) {
              minDistance = d
              closestIdx = i
            }
          }
          courierIdx = closestIdx
        } else {
          setCourierPos(coords[courierIdx] || coords[0])
        }

        // Split coordinates list
        setProgressCoords(coords.slice(0, courierIdx + 1))
        setRemainingCoords(coords.slice(courierIdx))
      } else {
        // Fallback straight line coordinates if OSRM endpoint fails
        const mockCoords: [number, number][] = [
          [pickupPoint.lng, pickupPoint.lat],
          [(pickupPoint.lng + dropoffPoint.lng) / 2, (pickupPoint.lat + dropoffPoint.lat) / 2],
          [dropoffPoint.lng, dropoffPoint.lat]
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
  }, [pickupLat, pickupLon, destinationLat, destinationLon, assistantLat, assistantLon])

  return (
    <div className="w-full h-full relative overflow-hidden rounded-2xl border border-border shadow-md">
      {loading && (
        <div className="absolute inset-0 bg-muted/20 flex flex-col items-center justify-center gap-2 backdrop-blur-xs z-20">
          <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
          <span className="text-xs text-muted-foreground font-semibold">Connecting map telemetry...</span>
        </div>
      )}

      <Map
        viewport={{
          center: [pickupPoint.lng, pickupPoint.lat],
          zoom: 13.5
        }}
        className="w-full h-full"
      >
        <MapControls position="top-right" />

        {/* Start pickup point marker */}
        <MapMarker latitude={pickupPoint.lat} longitude={pickupPoint.lng}>
          <MarkerContent>
            <div className="flex flex-col items-center -translate-y-1/2">
              <div className="bg-success text-success-foreground p-1.5 rounded-full shadow-lg border-2 border-background">
                <MapPinIcon size={16} weight="fill" />
              </div>
            </div>
          </MarkerContent>
        </MapMarker>

        {/* End destination point marker */}
        <MapMarker latitude={dropoffPoint.lat} longitude={dropoffPoint.lng}>
          <MarkerContent>
            <div className="flex flex-col items-center -translate-y-1/2">
              <div className="bg-destructive text-destructive-foreground p-1.5 rounded-full shadow-lg border-2 border-background">
                <MapPinIcon size={16} weight="fill" />
              </div>
            </div>
          </MarkerContent>
        </MapMarker>

        {/* Courier/Assistant moving marker */}
        {status !== "PENDING" && (
          <MapMarker latitude={courierPos[1]} longitude={courierPos[0]}>
            <MarkerContent>
              <div className="flex flex-col items-center -translate-y-1/2 z-30 animate-pulse">
                <div className="bg-primary text-primary-foreground p-2 rounded-full shadow-xl border-2 border-background">
                  <NavigationArrowIcon size={18} weight="fill" className="rotate-45" />
                </div>
              </div>
            </MarkerContent>
          </MapMarker>
        )}

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
  )
}
export default LiveMap
