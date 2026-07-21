import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useBookingDraftStore } from "@/store/booking-draft.store"
import { locationsService } from "@/services/locations.service"
import { Map, MapMarker, MarkerContent, MapControls } from "@/components/ui/map"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { MapPinIcon, MagnifyingGlassIcon, ArrowLeftIcon, ArrowRightIcon } from "@phosphor-icons/react"
import { toast } from "sonner"

interface AutocompleteSuggestion {
  description: string
  place_id: string
  latitude: number
  longitude: number
}

export function BookDestinationPage() {
  const navigate = useNavigate()
  const { pickup, destination, setDestination } = useBookingDraftStore()

  // Guard routing if pickup is missing
  useEffect(() => {
    if (!pickup) {
      toast.error("Please specify your pickup location first.")
      navigate("/book/pickup")
    }
  }, [pickup, navigate])

  const [searchText, setSearchText] = useState(destination?.name || "")
  const [suggestions, setSuggestions] = useState<AutocompleteSuggestion[]>([])
  const [loading, setLoading] = useState(false)

  const [coords, setCoords] = useState({
    lat: destination?.lat || (pickup?.lat ? pickup.lat + 0.005 : 19.0760),
    lon: destination?.lng || (pickup?.lng ? pickup.lng + 0.005 : 72.8777),
  })

  const [viewport, setViewport] = useState({
    center: [coords.lon, coords.lat] as [number, number],
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
      toast.error("Please select a destination landmark location to proceed.")
      return
    }
    navigate("/book/time")
  }

  return (
    <div className="grid gap-6 md:grid-cols-12">
      {/* Left panel: Form input and details */}
      <div className="md:col-span-5 flex flex-col gap-4">
        <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-2xl h-full flex flex-col justify-between">
          <CardHeader>
            <CardTitle className="text-xl font-bold flex items-center gap-2">
              <MapPinIcon size={24} className="text-destructive" />
              Set Destination
            </CardTitle>
            <CardDescription>
              Specify where your assistant should safely deliver or accompany you (hotel, home, transit hub).
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4 flex-1">
            <div className="relative">
              <Label htmlFor="dest-search" className="font-semibold text-xs text-muted-foreground uppercase tracking-wider">
                Search Destination Address
              </Label>
              <div className="relative mt-2">
                <MagnifyingGlassIcon className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted-foreground" size={16} />
                <Input
                  id="dest-search"
                  value={searchText}
                  onChange={(e) => setSearchText(e.target.value)}
                  placeholder="e.g. Taj Mahal Palace Hotel"
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

            {destination && (
              <div className="bg-muted/30 border border-border rounded-xl p-4 space-y-2 mt-4">
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
              <span>Set Schedule</span>
              <ArrowRightIcon size={14} weight="bold" />
            </Button>
          </div>
        </Card>
      </div>

      {/* Right panel: Map showing both pickup and destination */}
      <div className="md:col-span-7 h-[500px] md:h-auto min-h-[450px] relative rounded-2xl overflow-hidden border border-border/80 shadow-md">
        <Map
          viewport={viewport}
          onViewportChange={(next) => setViewport(next)}
          className="w-full h-full"
        >
          <MapControls position="top-right" />
          
          {/* Pickup Marker */}
          {pickup && (
            <MapMarker latitude={pickup.lat} longitude={pickup.lng}>
              <MarkerContent>
                <div className="flex flex-col items-center -translate-y-1/2">
                  <div className="bg-success text-success-foreground p-1.5 rounded-full shadow-lg border-2 border-background">
                    <MapPinIcon size={14} weight="fill" />
                  </div>
                </div>
              </MarkerContent>
            </MapMarker>
          )}

          {/* Destination Marker */}
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
        </Map>
      </div>
    </div>
  )
}
export default BookDestinationPage
