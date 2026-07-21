import React, { useState, useEffect } from "react"
import { Map, MapMarker, MarkerContent, MapControls } from "@/components/ui/map"
import { api } from "@/services/api"
import { toast } from "sonner"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { MapPinIcon, MagnifyingGlassIcon } from "@phosphor-icons/react"
import { cn } from "@/lib/utils"

interface MapPickerProps {
  label: string
  initialLat?: number
  initialLon?: number
  onLocationSelect: (location: { address: string; lat: number; lon: number }) => void
}

interface AutocompleteItem {
  description: string
  place_id: string
}

export function MapPicker({ label, initialLat = 28.6139, initialLon = 77.2090, onLocationSelect }: MapPickerProps) {
  const [coords, setCoords] = useState({ lat: initialLat, lon: initialLon })
  const [address, setAddress] = useState("")
  const [searchQuery, setSearchQuery] = useState("")
  const [suggestions, setSuggestions] = useState<AutocompleteItem[]>([])
  const [loading, setLoading] = useState(false)
  const [viewport, setViewport] = useState({
    center: [initialLon, initialLat] as [number, number],
    zoom: 14,
  })

  // Synchronize with parent on initial load or coordinate changes
  const reverseGeocode = async (latitude: number, longitude: number, updateInput = true) => {
    try {
      const res = await api.get(`/locations/reverse-geocode?latitude=${latitude}&longitude=${longitude}`)
      const addr = res.data.formatted_address
      setAddress(addr)
      if (updateInput) {
        setSearchQuery(addr)
      }
      onLocationSelect({ address: addr, lat: latitude, lon: longitude })
    } catch (err) {
      toast.error("Error retrieving address for coordinate details.")
    }
  }

  useEffect(() => {
    reverseGeocode(coords.lat, coords.lon)
  }, [])

  // Autocomplete debouncing
  useEffect(() => {
    if (searchQuery.length < 3 || searchQuery === address) {
      setSuggestions([])
      return
    }
    const timer = setTimeout(async () => {
      try {
        const res = await api.get(`/locations/autocomplete?q=${encodeURIComponent(searchQuery)}`)
        setSuggestions(res.data)
      } catch (err) {}
    }, 450)
    return () => clearTimeout(timer)
  }, [searchQuery, address])

  const handleDragEnd = async (lngLat: { lng: number; lat: number }) => {
    setCoords({ lat: lngLat.lat, lon: lngLat.lng })
    setViewport(prev => ({
      ...prev,
      center: [lngLat.lng, lngLat.lat] as [number, number]
    }))
    await reverseGeocode(lngLat.lat, lngLat.lng, true)
  }

  const handleSelectSuggestion = async (item: AutocompleteItem) => {
    setSearchQuery(item.description)
    setSuggestions([])
    setLoading(true)
    try {
      const res = await api.get(`/locations/geocode?address=${encodeURIComponent(item.description)}`)
      const lat = res.data.latitude
      const lon = res.data.longitude
      setCoords({ lat, lon })
      setAddress(item.description)
      setViewport({
        center: [lon, lat],
        zoom: 14.5
      })
      onLocationSelect({ address: item.description, lat, lon })
    } catch (err) {
      toast.error("Geocoding failed for selection.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex flex-col gap-3.5 w-full relative">
      <div className="relative overflow-visible">
        <Label className="text-xs font-bold text-muted-foreground uppercase tracking-wider">{label}</Label>
        <div className="relative mt-1.5 z-20">
          <Input
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search address or drag map pin..."
            className="pr-10"
          />
          <div className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground">
            <MagnifyingGlassIcon size={16} />
          </div>
        </div>

        {/* Suggestions overlay */}
        {suggestions.length > 0 && (
          <div className="absolute z-30 left-0 right-0 mt-1 bg-popover text-popover-foreground border rounded-xl shadow-lg max-h-48 overflow-y-auto">
            {suggestions.map((item) => (
              <button
                key={item.place_id}
                type="button"
                onClick={() => handleSelectSuggestion(item)}
                className="w-full text-left px-4 py-2.5 text-xs hover:bg-muted font-medium transition-colors border-b last:border-0"
              >
                {item.description}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Map viewport container */}
      <div className="w-full h-64 rounded-xl overflow-hidden border border-border shadow-inner relative z-10">
        <Map
          viewport={viewport}
          onViewportChange={(v) => setViewport({ center: v.center, zoom: v.zoom })}
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
              <div className="flex flex-col items-center -translate-y-1/2 animate-bounce">
                <div className="bg-primary text-primary-foreground p-1.5 rounded-full shadow-lg border-2 border-background scale-110">
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
export default MapPicker
