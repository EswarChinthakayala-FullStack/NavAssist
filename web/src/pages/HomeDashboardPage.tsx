import React, { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { useGeolocation } from "@/hooks/useGeolocation"
import { usersService } from "@/services/users.service"
import { locationsService } from "@/services/locations.service"
import { assistantsService } from "@/services/assistants.service"
import { bookingsService } from "@/services/bookings.service"
import { Map, MapMarker, MarkerContent, MapControls, useMap } from "@/components/ui/map"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { RotateCcw, Mountain } from "lucide-react"
import { 
  MapPinIcon, 
  MagnifyingGlassIcon, 
  HouseIcon, 
  BriefcaseIcon, 
  StarIcon,
  PlusIcon,
  NavigationArrowIcon,
  HeadsetIcon
} from "@phosphor-icons/react"
import { useBookingDraftStore } from "@/store/booking-draft.store"

interface SavedLocation {
  id: number
  label: string
  custom_label: string | null
  address: string
  latitude: number
  longitude: number
}

interface AutocompleteSuggestion {
  description: string
  place_id: string
  latitude: number
  longitude: number
}

function MapController() {
  const { map, isLoaded } = useMap()
  const [pitch, setPitch] = useState(0)
  const [bearing, setBearing] = useState(0)

  useEffect(() => {
    if (!map || !isLoaded) return

    const handleMove = () => {
      setPitch(Math.round(map.getPitch()))
      setBearing(Math.round(map.getBearing()))
    }

    map.on("move", handleMove)
    return () => {
      map.off("move", handleMove)
    }
  }, [map, isLoaded])

  const handle3DView = () => {
    map?.easeTo({
      pitch: 60,
      bearing: -20,
      duration: 1000,
    })
  }

  const handleReset = () => {
    map?.easeTo({
      pitch: 0,
      bearing: 0,
      duration: 1000,
    })
  }

  if (!isLoaded) return null

  return (
    <div className="absolute bottom-6 left-6 z-10 flex flex-col gap-2 pointer-events-auto">
      <div className="flex gap-2">
        <Button size="sm" variant="secondary" onClick={handle3DView} className="h-8 font-bold text-[10px] rounded-lg shadow-md border border-border">
          <Mountain className="mr-1 size-3.5" />
          3D View
        </Button>
        <Button size="sm" variant="secondary" onClick={handleReset} className="h-8 font-bold text-[10px] rounded-lg shadow-md border border-border">
          <RotateCcw className="mr-1 size-3.5" />
          Reset
        </Button>
      </div>
      <div className="bg-card/95 border border-border rounded-xl px-3 py-1.5 font-mono text-[9px] font-bold shadow-md backdrop-blur-md text-muted-foreground w-fit">
        <div>Pitch: {pitch}° | Bearing: {bearing}°</div>
      </div>
    </div>
  )
}

export function HomeDashboardPage() {
  const navigate = useNavigate()
  const { latitude, longitude, getPosition } = useGeolocation()
  const bookingDraft = useBookingDraftStore()

  const [savedLocations, setSavedLocations] = useState<SavedLocation[]>([])
  const [searchQuery, setSearchQuery] = useState("")
  const [suggestions, setSuggestions] = useState<AutocompleteSuggestion[]>([])
  const [loadingSuggestions, setLoadingSuggestions] = useState(false)
  const [activeBooking, setActiveBooking] = useState<any>(null)
  
  // Set local state-driven interactive viewport coordinates (default Mumbai)
  const [viewport, setViewport] = useState({
    center: [72.8777, 19.0760] as [number, number],
    zoom: 14,
    bearing: 0,
    pitch: 0
  })

  // Sync GPS updates to map center instantly
  useEffect(() => {
    if (latitude && longitude) {
      setViewport((prev) => ({
        ...prev,
        center: [longitude, latitude]
      }))
    }
  }, [latitude, longitude])

  // Fetch active booking & saved locations on mount
  useEffect(() => {
    getPosition()
    const checkActive = async () => {
      try {
        const res = await bookingsService.getActiveBooking()
        if (res && res.id) {
          setActiveBooking(res)
        }
      } catch (err) {
        setActiveBooking(null)
      }
    }
    checkActive()

    const loadSaved = async () => {
      try {
        const res = await usersService.getSavedLocations()
        setSavedLocations(res)
      } catch (err) {
        setSavedLocations([])
      }
    }
    loadSaved()
  }, [])

  // Fetch suggestions when typing (debounced 300ms)
  useEffect(() => {
    if (searchQuery.trim().length < 2) {
      setSuggestions([])
      return
    }

    const timer = setTimeout(async () => {
      setLoadingSuggestions(true)
      try {
        const res = await locationsService.autocomplete(searchQuery)
        setSuggestions(res)
      } catch (err) {
        setSuggestions([])
      } finally {
        setLoadingSuggestions(false)
      }
    }, 300)

    return () => clearTimeout(timer)
  }, [searchQuery])

  const handleSelectLocation = (place: { address: string; latitude: number; longitude: number }) => {
    bookingDraft.setDestination({
      name: place.address,
      lat: place.latitude,
      lng: place.longitude,
    })

    if (latitude && longitude) {
      bookingDraft.setPickup({
        name: "Current Location",
        lat: latitude,
        lng: longitude,
      })
    }

    navigate("/book/pickup")
  }

  const markerLat = latitude || 19.0760
  const markerLng = longitude || 72.8777

  const [nearbyAssistants, setNearbyAssistants] = useState<any[]>([])

  useEffect(() => {
    const fetchNearby = async () => {
      try {
        const res = await assistantsService.getNearbyAssistants(markerLat, markerLng, 10)
        setNearbyAssistants(res)
      } catch (err) {
        setNearbyAssistants([])
      }
    }
    fetchNearby()
  }, [markerLat, markerLng])

  return (
    <div className="w-full h-[calc(100vh-100px)] relative overflow-hidden rounded-2xl border border-border shadow-2xl bg-muted/10">
      {/* Background Interactive Map */}
      <Map
        viewport={viewport}
        onViewportChange={(next) => setViewport(next)}
        className="w-full h-full"
      >
        <MapControls position="top-right" />
        <MapController />

        <MapMarker latitude={markerLat} longitude={markerLng}>
          <MarkerContent>
            <div className="flex flex-col items-center -translate-y-1/2">
              <div className="bg-primary text-primary-foreground p-2 rounded-full shadow-2xl border-2 border-background animate-pulse">
                <NavigationArrowIcon size={18} weight="fill" className="rotate-45" />
              </div>
            </div>
          </MarkerContent>
        </MapMarker>

        {/* Nearby Active Assistants Map Markers */}
        {nearbyAssistants.map((ast) => (
          <MapMarker 
            key={ast.assistant_id} 
            latitude={ast.latitude} 
            longitude={ast.longitude}
          >
            <MarkerContent>
              <div className="flex flex-col items-center -translate-y-1/2 select-none group pointer-events-auto">
                <div className="bg-purple-600 dark:bg-purple-700 text-white p-2 rounded-full shadow-2xl border-2 border-background hover:scale-110 transition-all cursor-pointer">
                  <HeadsetIcon size={16} weight="fill" />
                </div>
                <span className="bg-card border border-border text-[9px] font-extrabold px-1.5 py-0.5 rounded-full shadow-md mt-1 text-foreground whitespace-nowrap opacity-90 group-hover:opacity-100 transition-opacity capitalize">
                  Guide #{ast.assistant_id}
                </span>
              </div>
            </MarkerContent>
          </MapMarker>
        ))}
      </Map>

      {/* Active Ride Banner floating alert */}
      {activeBooking && (
        <div className="absolute top-4 left-4 right-4 z-20 max-w-md mx-auto pointer-events-auto">
          <div className="bg-card/95 border border-emerald-500/30 text-card-foreground p-3.5 px-4 rounded-2xl shadow-2xl flex items-center justify-between gap-3 backdrop-blur-xl">
            <div className="flex items-center gap-3 min-w-0">
              <div className="p-2.5 bg-emerald-500/15 text-emerald-500 rounded-xl shrink-0 animate-pulse">
                <NavigationArrowIcon size={20} weight="fill" />
              </div>
              <div className="text-left min-w-0">
                <p className="text-xs font-black uppercase tracking-wider text-foreground truncate">Active Ride #{activeBooking.id}</p>
                <p className="text-[10px] text-muted-foreground font-semibold capitalize truncate">{activeBooking.status?.replace(/_/g, " ")}</p>
              </div>
            </div>
            <Button
              onClick={() => {
                const enrouteStatuses = ["PENDING", "ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE", "ARRIVED_PICKUP", "EN_ROUTE"]
                const st = (activeBooking.status || "").toUpperCase()
                if (enrouteStatuses.includes(st)) {
                  navigate(`/trip/${activeBooking.id}/enroute`)
                } else {
                  navigate(`/trip/${activeBooking.id}/tracking`)
                }
              }}
              className="bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold text-xs py-2 px-3.5 rounded-xl shadow-md cursor-pointer shrink-0 border border-emerald-500/30 transition-transform active:scale-95"
            >
              Track Live Ride
            </Button>
          </div>
        </div>
      )}

      {/* Floating Card containing search input & saved location chips */}
      <div className={`absolute left-4 right-4 z-10 flex flex-col gap-3 max-w-md mx-auto pointer-events-auto ${activeBooking ? "top-24" : "top-4"}`}>
        {/* Floating Command Search Bar */}
        <div className="relative">
          <div className="bg-card/95 border border-border rounded-2xl p-3.5 shadow-xl flex items-center gap-3 backdrop-blur-md">
            <MagnifyingGlassIcon size={20} className="text-muted-foreground shrink-0" />
            <Input
              type="text"
              placeholder="Where to? (e.g. Apollo Hospital, Station)"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="border-0 focus-visible:ring-0 focus-visible:ring-offset-0 bg-transparent text-sm font-semibold h-9 p-0 placeholder:text-muted-foreground/70"
            />
          </div>

          {/* Autocomplete Dropdown List */}
          {suggestions.length > 0 && (
            <div className="absolute top-full left-0 right-0 mt-2 bg-card border border-border rounded-2xl shadow-2xl max-h-60 overflow-y-auto z-20 p-2 space-y-1 backdrop-blur-md">
              {suggestions.map((item) => (
                <div
                  key={item.place_id}
                  onClick={() => handleSelectLocation({
                    address: item.description,
                    latitude: item.latitude,
                    longitude: item.longitude,
                  })}
                  className="p-2.5 rounded-xl hover:bg-muted flex items-center gap-3 cursor-pointer text-left transition-colors"
                >
                  <MapPinIcon size={18} className="text-primary shrink-0" />
                  <span className="text-xs font-semibold text-foreground line-clamp-1">
                    {item.description}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Saved Locations Shortcut Chips */}
        <div className="flex gap-2 overflow-x-auto pb-1 no-scrollbar">
          {savedLocations.map((loc) => {
            const isHome = loc.label.toLowerCase() === "home"
            const isWork = loc.label.toLowerCase() === "work"
            return (
              <button
                key={loc.id}
                onClick={() => handleSelectLocation(loc)}
                className="bg-card/95 border border-border hover:border-primary/50 text-foreground px-3.5 py-2 rounded-xl text-xs font-bold shadow-lg backdrop-blur-md flex items-center gap-2 shrink-0 transition-all hover:scale-105 cursor-pointer"
              >
                {isHome && <HouseIcon size={16} className="text-primary" />}
                {isWork && <BriefcaseIcon size={16} className="text-primary" />}
                {!isHome && !isWork && <StarIcon size={16} className="text-amber-500" />}
                <span>{loc.custom_label || loc.label}</span>
              </button>
            )
          })}

          <button
            onClick={() => navigate("/book/destination")}
            className="bg-primary/10 border border-primary/30 text-primary hover:bg-primary/20 px-3.5 py-2 rounded-xl text-xs font-extrabold shadow-lg backdrop-blur-md flex items-center gap-1.5 shrink-0 transition-all cursor-pointer"
          >
            <PlusIcon size={14} weight="bold" />
            <span>More Places</span>
          </button>
        </div>
      </div>
    </div>
  )
}
export default HomeDashboardPage
