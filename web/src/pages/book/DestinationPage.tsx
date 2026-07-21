import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useBookingDraftStore } from "@/store/booking-draft.store"
import { locationsService } from "@/services/locations.service"
import { Map, MapMarker, MarkerContent, MapControls, MapRoute } from "@/components/ui/map"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { MapPinIcon, MagnifyingGlassIcon, ArrowRightIcon, ArrowLeftIcon } from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

interface AutocompleteSuggestion {
  description: string
  place_id: string
  latitude: number
  longitude: number
}

export function DestinationPage() {
  const navigate = useNavigate()
  const { pickup, destination, setDestination } = useBookingDraftStore()

  // Redirect back if pickup is missing
  useEffect(() => {
    if (!pickup) {
      toast.error("Please set a pickup location first.")
      navigate("/book/pickup")
    }
  }, [pickup, navigate])

  const [searchText, setSearchText] = useState(destination?.name || "")
  const [suggestions, setSuggestions] = useState<AutocompleteSuggestion[]>([])
  const [loading, setLoading] = useState(false)
  const [routeCoords, setRouteCoords] = useState<[number, number][]>([])

  useEffect(() => {
    if (!pickup || !destination) {
      setRouteCoords([])
      return
    }
    setRouteCoords([[pickup.lng, pickup.lat], [destination.lng, destination.lat]])

    const fetchOSRMRoute = async () => {
      try {
        const url = `https://router.project-osrm.org/route/v1/driving/${pickup.lng},${pickup.lat};${destination.lng},${destination.lat}?overview=full&geometries=geojson`
        const response = await fetch(url)
        if (response.ok) {
          const data = await response.json()
          const coords = data.routes?.[0]?.geometry?.coordinates
          if (coords && coords.length > 0) {
            setRouteCoords(coords)
          }
        }
      } catch (err) {
        console.error("OSRM routing failed:", err)
      }
    }
    fetchOSRMRoute()
  }, [pickup, destination])

  // Default coordinate center (falls back to pickup location)
  const [coords, setCoords] = useState<{ lat: number; lon: number } | null>(
    destination
      ? { lat: destination.lat, lon: destination.lng }
      : pickup
      ? { lat: pickup.lat, lon: pickup.lng }
      : null
  )

  // Set default view area centered on pickup
  const [viewport, setViewport] = useState({
    center: pickup ? [pickup.lng, pickup.lat] as [number, number] : [72.8777, 19.0760] as [number, number],
    zoom: 13,
  })

  // Fetch suggestions when typing
  useEffect(() => {
    if (searchText.trim().length < 2) {
      setSuggestions([])
      return
    }

    if (destination && searchText === destination.name) return

    const timer = setTimeout(async () => {
      setLoading(true)
      try {
        const res = await locationsService.autocomplete(searchText)
        setSuggestions(res || [])
      } catch (err) {
        console.error("Autocomplete failed:", err)
      } finally {
        setLoading(false)
      }
    }, 300)

    return () => clearTimeout(timer)
  }, [searchText])

  const handleSelectSuggestion = (suggestion: AutocompleteSuggestion) => {
    setCoords({ lat: suggestion.latitude, lon: suggestion.longitude })
    setViewport({ center: [suggestion.longitude, suggestion.latitude], zoom: 14 })
    setDestination({
      name: suggestion.description,
      lat: suggestion.latitude,
      lng: suggestion.longitude,
    })
    setSearchText(suggestion.description)
    setSuggestions([])
  }

  const handleDragEnd = async (event: any) => {
    const { lat, lng } = event.target.getLngLat()
    setCoords({ lat, lon: lng })
    try {
      const res = await locationsService.reverseGeocode(lat, lng)
      const address = res.formatted_address || `${lat.toFixed(5)}, ${lng.toFixed(5)}`
      setDestination({ name: address, lat, lng })
      setSearchText(address)
    } catch (err) {
      console.error("Reverse geocoding failed:", err)
    }
  }

  const handleContinue = () => {
    if (!destination) {
      toast.error("Please select a destination lodging or station to proceed.")
      return
    }
    navigate("/book/schedule")
  }

  if (!pickup) return null

  return (
    <motion.div
      initial={{ x: 50, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      exit={{ x: -50, opacity: 0 }}
      transition={{ duration: 0.3 }}
      className="grid gap-6 md:grid-cols-12 w-full animate-fade-in"
    >
      {/* Left panel: Form input and details */}
      <div className="md:col-span-5 flex flex-col gap-4">
        <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-2xl h-full flex flex-col justify-between">
          <CardHeader>
            <CardTitle className="text-xl font-bold flex items-center gap-2">
              <MapPinIcon size={24} className="text-destructive" />
              Where are you headed?
            </CardTitle>
            <CardDescription>
              Select your dropoff destination (hotel check-in lobby, transit gate, office building, etc.).
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4 flex-1 overflow-visible">
            <div className="relative mt-2">
              <Label htmlFor="destination-search" className="font-bold text-xs text-muted-foreground uppercase tracking-wider">
                Search Destination Place
              </Label>
              <div className="relative mt-2">
                <MagnifyingGlassIcon className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted-foreground" size={16} />
                <Input
                  id="destination-search"
                  value={searchText}
                  onChange={(e) => setSearchText(e.target.value)}
                  placeholder="e.g. Taj Mahal Palace Hotel Lobby"
                  className="pl-10 pr-10 py-5 rounded-xl border-border focus-visible:ring-primary font-semibold text-xs"
                />
                {searchText && (
                  <button
                    onClick={() => {
                      setSearchText("")
                      setSuggestions([])
                    }}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-xs font-semibold text-muted-foreground hover:text-foreground cursor-pointer"
                  >
                    Clear
                  </button>
                )}
              </div>

              {/* Autocomplete suggestions */}
              {suggestions.length > 0 && (
                <div className="absolute top-[calc(100%+4px)] left-0 right-0 z-50 bg-card border border-border rounded-xl shadow-xl max-h-60 overflow-y-auto p-1.5 flex flex-col gap-1">
                  {suggestions.map((item) => (
                    <button
                      key={item.place_id}
                      onClick={() => handleSelectSuggestion(item)}
                      className="w-full text-left px-3 py-2.5 rounded-lg hover:bg-accent/85 transition-all text-xs font-semibold flex items-center gap-2 group cursor-pointer"
                    >
                      <MapPinIcon size={14} className="text-muted-foreground group-hover:text-primary transition-colors shrink-0" />
                      <span className="truncate text-foreground">{item.description}</span>
                    </button>
                  ))}
                </div>
              )}
            </div>

            {pickup && (
              <div className="bg-muted/10 border border-border/60 rounded-xl p-3 space-y-1 select-none">
                <div className="text-[9px] font-bold text-muted-foreground uppercase tracking-wider">Pickup Spot (Locked)</div>
                <div className="text-xs font-semibold leading-normal text-muted-foreground truncate">{pickup.name}</div>
              </div>
            )}

            {destination && (
              <div className="bg-muted/30 border border-border rounded-xl p-4 space-y-2 select-none">
                <div className="text-[10px] font-bold text-destructive uppercase tracking-wider">Selected Destination</div>
                <div className="text-xs font-bold leading-relaxed">{destination.name}</div>
                <div className="text-[10px] text-muted-foreground font-semibold">
                  Coordinates: {destination.lat.toFixed(5)}, {destination.lng.toFixed(5)}
                </div>
              </div>
            )}
          </CardContent>
          <div className="p-6 border-t border-border flex gap-3">
            <Button
              variant="outline"
              onClick={() => navigate("/book/pickup")}
              className="flex-1 rounded-xl py-5 font-bold text-xs flex items-center justify-center gap-2 hover:bg-accent cursor-pointer"
            >
              <ArrowLeftIcon size={14} weight="bold" />
              <span>Back</span>
            </Button>
            <Button
              onClick={handleContinue}
              disabled={!destination}
              className="flex-2 rounded-xl py-5 font-bold bg-primary text-primary-foreground text-xs shadow-md flex items-center justify-center gap-2 hover:scale-102 transition-all cursor-pointer"
            >
              <span>Continue to Schedule</span>
              <ArrowRightIcon size={14} weight="bold" />
            </Button>
          </div>
        </Card>
      </div>

      {/* Right panel: Interactive map */}
      <div className="md:col-span-7 h-[500px] md:h-auto min-h-[450px] relative rounded-2xl overflow-hidden border border-border/80 shadow-md">
        <Map
          viewport={viewport}
          onViewportChange={(next) => setViewport(next)}
          className="w-full h-full"
        >
          <MapControls position="top-right" />
          
          {/* Static Pickup Pin */}
          <MapMarker latitude={pickup.lat} longitude={pickup.lng}>
            <MarkerContent>
              <div className="flex flex-col items-center -translate-y-1/2">
                <div className="bg-primary text-primary-foreground p-2 rounded-full shadow-lg border-2 border-background">
                  <MapPinIcon size={18} weight="fill" />
                </div>
                <div className="w-1.5 h-1.5 bg-primary rounded-full mt-0.5 border border-background" />
              </div>
            </MarkerContent>
          </MapMarker>

          {/* Draggable Destination Pin */}
          {coords && (
            <MapMarker
              latitude={coords.lat}
              longitude={coords.lon}
              draggable
              onDragEnd={handleDragEnd}
            >
              <MarkerContent>
                <div className="flex flex-col items-center -translate-y-1/2">
                  <div className="bg-destructive text-destructive-foreground p-2 rounded-full shadow-lg border-2 border-background animate-bounce">
                    <MapPinIcon size={18} weight="fill" />
                  </div>
                  <div className="w-1.5 h-1.5 bg-destructive rounded-full mt-0.5 border border-background" />
                </div>
              </MarkerContent>
            </MapMarker>
          )}

          {/* Route Preview Line linking Pickup and Destination */}
          {coords && routeCoords.length > 0 && (
            <MapRoute
              coordinates={routeCoords}
              color="#4285F4"
              width={4}
            />
          )}
        </Map>
      </div>
    </motion.div>
  )
}
export default DestinationPage
