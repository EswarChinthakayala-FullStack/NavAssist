import React, { useEffect, useState } from "react"
import { Map, MapMarker, MarkerContent, MapRoute, MapControls } from "@/components/ui/map"

interface TripRouteMapProps {
  pickupLat: number
  pickupLng: number
  pickupAddress?: string
  destLat: number
  destLng: number
  destAddress?: string
  className?: string
}

export function TripRouteMap({
  pickupLat,
  pickupLng,
  destLat,
  destLng,
  className = "h-56",
}: TripRouteMapProps) {
  const [routeCoords, setRouteCoords] = useState<[number, number][]>([
    [pickupLng, pickupLat],
    [destLng, destLat],
  ])
  const [loadingRoute, setLoadingRoute] = useState(false)

  // Fetch actual driving road geometry from OSRM
  useEffect(() => {
    if (!pickupLat || !pickupLng || !destLat || !destLng) return

    let isMounted = true
    const fetchOSRMRoute = async () => {
      setLoadingRoute(true)
      try {
        const url = `https://router.project-osrm.org/route/v1/driving/${pickupLng},${pickupLat};${destLng},${destLat}?overview=full&geometries=geojson`
        const res = await fetch(url)
        const data = await res.json()

        if (isMounted && data.routes && data.routes[0]?.geometry?.coordinates) {
          setRouteCoords(data.routes[0].geometry.coordinates)
        }
      } catch (err) {
        console.warn("Failed to fetch OSRM road route, falling back to straight line:", err)
      } finally {
        if (isMounted) setLoadingRoute(false)
      }
    }

    fetchOSRMRoute()
    return () => {
      isMounted = false
    }
  }, [pickupLat, pickupLng, destLat, destLng])

  // Center calculation
  const midLat = (pickupLat + destLat) / 2
  const midLng = (pickupLng + destLng) / 2

  // Dynamic zoom calculation based on coordinate bounding box
  const calculateZoom = () => {
    const latDiff = Math.abs(pickupLat - destLat)
    const lonDiff = Math.abs(pickupLng - destLng)
    const maxDiff = Math.max(latDiff, lonDiff)

    if (maxDiff > 0.5) return 9.5
    if (maxDiff > 0.2) return 10.5
    if (maxDiff > 0.1) return 11.5
    if (maxDiff > 0.05) return 12.5
    if (maxDiff > 0.02) return 13.5
    return 14
  }

  return (
    <div className={`w-full rounded-2xl overflow-hidden border border-border/80 shadow-md relative bg-muted/20 ${className}`}>
      <Map
        viewport={{
          center: [midLng, midLat],
          zoom: calculateZoom(),
        }}
        className="w-full h-full"
      >
        <MapControls />

        {/* Pickup Marker A */}
        <MapMarker latitude={pickupLat} longitude={pickupLng}>
          <MarkerContent>
            <div className="flex flex-col items-center -translate-y-1/2 group">
              <div className="bg-primary text-primary-foreground w-8 h-8 rounded-full flex items-center justify-center shadow-xl border-2 border-background font-black text-xs group-hover:scale-110 transition-transform">
                A
              </div>
              <div className="w-2 h-2 bg-primary rounded-full mt-0.5 animate-ping" />
            </div>
          </MarkerContent>
        </MapMarker>

        {/* Destination Marker B */}
        <MapMarker latitude={destLat} longitude={destLng}>
          <MarkerContent>
            <div className="flex flex-col items-center -translate-y-1/2 group">
              <div className="bg-destructive text-destructive-foreground w-8 h-8 rounded-full flex items-center justify-center shadow-xl border-2 border-background font-black text-xs group-hover:scale-110 transition-transform">
                B
              </div>
              <div className="w-2 h-2 bg-destructive rounded-full mt-0.5" />
            </div>
          </MarkerContent>
        </MapMarker>

        {/* Actual Road Route Line */}
        <MapRoute
          coordinates={routeCoords}
          color="#3b82f6"
          width={5}
        />
      </Map>

      {/* Loading overlay badge */}
      {loadingRoute && (
        <div className="absolute top-3 left-3 bg-background/80 backdrop-blur-md text-[10px] font-bold px-2.5 py-1 rounded-full border border-border shadow-sm flex items-center gap-1.5 z-20">
          <div className="w-2 h-2 rounded-full bg-primary animate-spin" />
          <span>Plotting Road Route...</span>
        </div>
      )}
    </div>
  )
}
export default TripRouteMap
