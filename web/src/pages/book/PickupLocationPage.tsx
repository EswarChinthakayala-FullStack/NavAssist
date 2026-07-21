import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useBookingDraftStore } from "@/store/booking-draft.store"
import { locationsService } from "@/services/locations.service"
import { useGeolocation } from "@/hooks/useGeolocation"
import { Map, MapMarker, MarkerContent, MapControls } from "@/components/ui/map"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { MapPinIcon, MagnifyingGlassIcon, ArrowRightIcon, CompassIcon, TrainIcon, AirplaneIcon, BusIcon } from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

interface AutocompleteSuggestion {
  description: string
  place_id: string
  latitude: number
  longitude: number
}

interface ServicePoint {
  id: number
  name: string
  type: "railway_station" | "airport" | "bus_stand" | "general"
  city: string
  state: string
  latitude: number
  longitude: number
  code?: string
}

export function PickupLocationPage() {
  const navigate = useNavigate()
  const { pickup, setPickup } = useBookingDraftStore()

  const { latitude: geoLat, longitude: geoLng, getPosition } = useGeolocation()
  const [isLocating, setIsLocating] = useState(false)

  const [searchText, setSearchText] = useState(pickup?.name || "")
  const [suggestions, setSuggestions] = useState<AutocompleteSuggestion[]>([])
  const [loading, setLoading] = useState(false)

  // Service Points states
  const [servicePoints, setServicePoints] = useState<ServicePoint[]>([])
  const [activeCategory, setActiveCategory] = useState<string | null>(null)

  // Default coordinate (e.g. Mumbai center or current pickup coordinate if available)
  const [coords, setCoords] = useState({
    lat: pickup?.lat || 19.0760,
    lon: pickup?.lng || 72.8777,
  })

  // Set default view area
  const [viewport, setViewport] = useState({
    center: [coords.lon, coords.lat] as [number, number],
    zoom: 14,
  })

  // Fetch service points on mount
  useEffect(() => {
    const fetchServicePoints = async () => {
      try {
        const points = await locationsService.getServicePoints()
        setServicePoints(points || [])
      } catch (err) {
        console.error("Failed to load service points:", err)
      }
    }
    fetchServicePoints()
  }, [])

  // Fetch suggestions when typing
  useEffect(() => {
    if (searchText.trim().length < 2) {
      setSuggestions([])
      return
    }

    if (pickup && searchText === pickup.name) return

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
    setViewport({ center: [suggestion.longitude, suggestion.latitude], zoom: 15 })
    setPickup({
      name: suggestion.description,
      lat: suggestion.latitude,
      lng: suggestion.longitude,
    })
    setSearchText(suggestion.description)
    setSuggestions([])
    setActiveCategory(null)
  }

  const handleSelectServicePoint = (point: ServicePoint) => {
    const desc = `${point.name}, ${point.city}, ${point.state}`
    setCoords({ lat: point.latitude, lon: point.longitude })
    setViewport({ center: [point.longitude, point.latitude], zoom: 15 })
    setPickup({
      name: desc,
      lat: point.latitude,
      lng: point.longitude,
    })
    setSearchText(desc)
    setActiveCategory(null)
  }

  const handleUseCurrentLocation = () => {
    setIsLocating(true)
    getPosition()
  }

  // Handle GPS location resolution
  useEffect(() => {
    if (geoLat && geoLng && isLocating) {
      const resolveAddress = async () => {
        try {
          const res = await locationsService.reverseGeocode(geoLat, geoLng)
          const address = res.formatted_address || `${geoLat.toFixed(5)}, ${geoLng.toFixed(5)}`
          
          setCoords({ lat: geoLat, lon: geoLng })
          setViewport({ center: [geoLng, geoLat], zoom: 15 })
          setPickup({ name: address, lat: geoLat, lng: geoLng })
          setSearchText(address)
          toast.success("Current location detected!")
        } catch (err) {
          console.error("Reverse geocoding failed:", err)
          toast.error("Failed to resolve current location details.")
        } finally {
          setIsLocating(false)
        }
      }
      resolveAddress()
    }
  }, [geoLat, geoLng, isLocating])

  const handleDragEnd = async (event: any) => {
    const { lat, lng } = event.target.getLngLat()
    setCoords({ lat, lon: lng })
    try {
      const res = await locationsService.reverseGeocode(lat, lng)
      const address = res.formatted_address || `${lat.toFixed(5)}, ${lng.toFixed(5)}`
      setPickup({ name: address, lat, lng })
      setSearchText(address)
    } catch (err) {
      console.error("Reverse geocoding failed:", err)
    }
  }

  const handleContinue = () => {
    if (!pickup) {
      toast.error("Please select a pickup landmark location to proceed.")
      return
    }
    navigate("/book/destination")
  }

  // Filter service points by selected category
  const filteredServicePoints = servicePoints.filter(p => p.type === activeCategory)

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
              <MapPinIcon size={24} className="text-primary" />
              Set Pickup Location
            </CardTitle>
            <CardDescription>
              Specify where your assistant will meet you (airport terminals, railway gates, etc.).
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4 flex-1 overflow-visible">
            {/* Quick-Select Badges */}
            <div className="space-y-2 select-none">
              <Label className="font-bold text-xs text-muted-foreground uppercase tracking-wider">Quick Select Transit Hubs</Label>
              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={() => {
                    setActiveCategory(activeCategory === "railway_station" ? null : "railway_station")
                    setSearchText("")
                    setSuggestions([])
                  }}
                  className={`px-3 py-1.5 rounded-full border text-[10px] font-bold flex items-center gap-1 transition-all duration-350 cursor-pointer ${
                    activeCategory === "railway_station"
                      ? "bg-primary border-primary text-primary-foreground shadow-glow-primary scale-102"
                      : "bg-card border-border hover:bg-accent text-muted-foreground hover:text-foreground"
                  }`}
                >
                  <TrainIcon size={12} weight="bold" />
                  Railway Stations
                </button>

                <button
                  type="button"
                  onClick={() => {
                    setActiveCategory(activeCategory === "airport" ? null : "airport")
                    setSearchText("")
                    setSuggestions([])
                  }}
                  className={`px-3 py-1.5 rounded-full border text-[10px] font-bold flex items-center gap-1 transition-all duration-350 cursor-pointer ${
                    activeCategory === "airport"
                      ? "bg-primary border-primary text-primary-foreground shadow-glow-primary scale-102"
                      : "bg-card border-border hover:bg-accent text-muted-foreground hover:text-foreground"
                  }`}
                >
                  <AirplaneIcon size={12} weight="bold" />
                  Airports
                </button>

                <button
                  type="button"
                  onClick={() => {
                    setActiveCategory(activeCategory === "bus_stand" ? null : "bus_stand")
                    setSearchText("")
                    setSuggestions([])
                  }}
                  className={`px-3 py-1.5 rounded-full border text-[10px] font-bold flex items-center gap-1 transition-all duration-350 cursor-pointer ${
                    activeCategory === "bus_stand"
                      ? "bg-primary border-primary text-primary-foreground shadow-glow-primary scale-102"
                      : "bg-card border-border hover:bg-accent text-muted-foreground hover:text-foreground"
                  }`}
                >
                  <BusIcon size={12} weight="bold" />
                  Bus Stands
                </button>
              </div>
            </div>

            <div className="relative mt-2">
              <Label htmlFor="pickup-search" className="font-bold text-xs text-muted-foreground uppercase tracking-wider">
                Search Custom Landmark
              </Label>
              <div className="relative mt-2">
                <MagnifyingGlassIcon className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted-foreground" size={16} />
                <Input
                  id="pickup-search"
                  value={searchText}
                  onChange={(e) => {
                    setSearchText(e.target.value)
                    setActiveCategory(null) // clear active badge filter
                  }}
                  placeholder="e.g. New Delhi Railway Station Gate 2"
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
              {suggestions.length > 0 && !activeCategory && (
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

            {/* Service Points Results list */}
            {activeCategory && (
              <div className="border border-border rounded-xl p-2 bg-muted/20 space-y-1 max-h-56 overflow-y-auto mt-2">
                <div className="text-[9px] font-black text-muted-foreground uppercase tracking-widest px-2 py-1 select-none">
                  Available Hubs
                </div>
                {filteredServicePoints.length === 0 ? (
                  <div className="text-center text-xs text-muted-foreground py-4 font-semibold">
                    No matching service hubs found.
                  </div>
                ) : (
                  filteredServicePoints.map((point) => (
                    <button
                      key={point.id}
                      onClick={() => handleSelectServicePoint(point)}
                      className="w-full text-left px-3 py-2 rounded-lg hover:bg-accent/85 transition-all text-xs font-semibold flex items-center gap-2 group cursor-pointer"
                    >
                      <MapPinIcon size={14} className="text-primary/70 group-hover:text-primary transition-colors shrink-0" />
                      <span className="truncate text-foreground">{point.name} ({point.code})</span>
                    </button>
                  ))
                )}
              </div>
            )}

            {/* GPS Locator Button */}
            <Button
              type="button"
              variant="outline"
              disabled={isLocating}
              onClick={handleUseCurrentLocation}
              className="w-full rounded-xl py-5 h-10 font-bold text-xs flex items-center justify-center gap-2 hover:bg-accent border-dashed border-primary/40 text-primary cursor-pointer mt-1"
            >
              {isLocating ? (
                <>
                  <div className="w-3.5 h-3.5 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                  <span>Locating GPS...</span>
                </>
              ) : (
                <>
                  <CompassIcon size={16} weight="bold" />
                  <span>Use Current Location</span>
                </>
              )}
            </Button>

            {pickup && (
              <div className="bg-muted/30 border border-border rounded-xl p-4 space-y-2 mt-4 select-none">
                <div className="text-[10px] font-bold text-primary uppercase tracking-wider">Selected Pickup Point</div>
                <div className="text-xs font-bold leading-relaxed">{pickup.name}</div>
                <div className="text-[10px] text-muted-foreground font-semibold">
                  Coordinates: {pickup.lat.toFixed(5)}, {pickup.lng.toFixed(5)}
                </div>
              </div>
            )}
          </CardContent>
          <div className="p-6 border-t border-border">
            <Button
              onClick={handleContinue}
              disabled={!pickup}
              className="w-full rounded-xl py-5 font-bold bg-primary text-primary-foreground text-xs shadow-md flex items-center justify-center gap-2 hover:scale-102 transition-all cursor-pointer"
            >
              <span>Continue to Destination</span>
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
          <MapMarker
            latitude={coords.lat}
            longitude={coords.lon}
            draggable
            onDragEnd={handleDragEnd}
          >
            <MarkerContent>
              <div className="flex flex-col items-center -translate-y-1/2">
                <div className="bg-primary text-primary-foreground p-2 rounded-full shadow-lg border-2 border-background animate-bounce">
                  <MapPinIcon size={18} weight="fill" />
                </div>
                <div className="w-1.5 h-1.5 bg-primary rounded-full mt-0.5 border border-background" />
              </div>
            </MarkerContent>
          </MapMarker>
        </Map>
      </div>
    </motion.div>
  )
}
export default PickupLocationPage
