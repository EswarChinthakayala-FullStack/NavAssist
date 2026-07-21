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
import { MapPinIcon, MagnifyingGlassIcon, ArrowRightIcon, CompassIcon } from "@phosphor-icons/react"
import { toast } from "sonner"

interface AutocompleteSuggestion {
  description: string
  place_id: string
  latitude: number
  longitude: number
}

export function BookPickupPage() {
  const navigate = useNavigate()
  const { pickup, setPickup } = useBookingDraftStore()

  const { latitude: geoLat, longitude: geoLng, getPosition } = useGeolocation()
  const [isLocating, setIsLocating] = useState(false)

  const [searchText, setSearchText] = useState(pickup?.name || "")
  const [suggestions, setSuggestions] = useState<AutocompleteSuggestion[]>([])
  const [loading, setLoading] = useState(false)

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

  // Fetch suggestions when typing
  useEffect(() => {
    if (searchText.trim().length < 2) {
      setSuggestions([])
      return
    }

    // Skip autocomplete if search query is already matching the current selected pickup address
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

  return (
    <div className="grid gap-6 md:grid-cols-12">
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
          <CardContent className="space-y-4 flex-1">
            <div className="relative">
              <Label htmlFor="pickup-search" className="font-semibold text-xs text-muted-foreground uppercase tracking-wider">
                Search Landmark or Station
              </Label>
              <div className="relative mt-2">
                <MagnifyingGlassIcon className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted-foreground" size={16} />
                <Input
                  id="pickup-search"
                  value={searchText}
                  onChange={(e) => setSearchText(e.target.value)}
                  placeholder="e.g. Chhatrapati Shivaji Airport Terminal 2"
                  className="pl-10 pr-10 py-5 rounded-xl border-border focus-visible:ring-primary font-medium text-xs"
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
              <div className="bg-muted/30 border border-border rounded-xl p-4 space-y-2 mt-4">
                <div className="text-[10px] font-bold text-primary uppercase tracking-wider">Selected Location</div>
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
    </div>
  )
}
export default BookPickupPage
