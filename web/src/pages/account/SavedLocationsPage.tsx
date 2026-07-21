import React, { useState } from "react"
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { usersService } from "@/services/users.service"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"
import { Map, MapMarker, MarkerContent, MapControls } from "@/components/ui/map"
import {
  MapPinIcon,
  HouseLineIcon,
  BriefcaseIcon,
  HeartIcon,
  BookmarkIcon,
  PlusIcon,
  TrashIcon,
  PencilSimpleIcon,
  CompassIcon,
  InfoIcon,
  NavigationArrowIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from "@/components/ui/select"

export function SavedLocationsPage() {
  const queryClient = useQueryClient()

  // Fetch saved locations
  const { data: locations = [], isLoading } = useQuery({
    queryKey: ["saved-locations"],
    queryFn: () => usersService.getSavedLocations()
  })

  // State controls
  const [open, setOpen] = useState(false)
  const [editingLoc, setEditingLoc] = useState<any>(null)
  
  // Form fields
  const [label, setLabel] = useState("home")
  const [address, setAddress] = useState("")
  const [latitude, setLatitude] = useState(28.6139)
  const [longitude, setLongitude] = useState(77.2090)

  // Controlled map viewport
  const [viewport, setViewport] = useState({
    center: [77.2090, 28.6139] as [number, number],
    zoom: 14,
  })

  const [detecting, setDetecting] = useState(false)

  const handleDetectLocation = () => {
    if (!navigator.geolocation) {
      toast.error("Geolocation is not supported by your browser.")
      return
    }

    setDetecting(true)
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const lat = position.coords.latitude
        const lng = position.coords.longitude
        setLatitude(lat)
        setLongitude(lng)
        setViewport({
          center: [lng, lat],
          zoom: 14
        })
        
        toast.success("Location auto-detected successfully!")

        try {
          const response = await fetch(
            `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`
          )
          const data = await response.json()
          if (data && data.display_name) {
            setAddress(data.display_name)
          }
        } catch (e) {
          console.warn("Failed to reverse geocode address:", e)
        } finally {
          setDetecting(false)
        }
      },
      (err) => {
        console.error("Geolocation error:", err)
        toast.error("Failed to detect location. Please check browser permissions.")
        setDetecting(false)
      },
      { enableHighAccuracy: true, timeout: 8000 }
    )
  }

  // TanStack Query Optimistic mutations
  const addMutation = useMutation({
    mutationFn: (newLoc: any) => usersService.addSavedLocation(newLoc),
    onMutate: async (newLoc) => {
      await queryClient.cancelQueries({ queryKey: ["saved-locations"] })
      const previous = queryClient.getQueryData(["saved-locations"])
      queryClient.setQueryData(["saved-locations"], (old: any) => [
        ...(old || []),
        { id: Math.random(), ...newLoc }
      ])
      return { previous }
    },
    onError: (err, newLoc, context) => {
      queryClient.setQueryData(["saved-locations"], context?.previous)
      toast.error("Failed to add location.")
    },
    onSuccess: () => {
      toast.success("Location saved successfully!")
      setOpen(false)
      resetForm()
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["saved-locations"] })
    }
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: any }) =>
      usersService.updateSavedLocation(id, payload),
    onMutate: async ({ id, payload }) => {
      await queryClient.cancelQueries({ queryKey: ["saved-locations"] })
      const previous = queryClient.getQueryData(["saved-locations"])
      queryClient.setQueryData(["saved-locations"], (old: any) =>
        (old || []).map((loc: any) => (loc.id === id ? { ...loc, ...payload } : loc))
      )
      return { previous }
    },
    onError: (err, variables, context) => {
      queryClient.setQueryData(["saved-locations"], context?.previous)
      toast.error("Failed to update location.")
    },
    onSuccess: () => {
      toast.success("Location updated successfully!")
      setOpen(false)
      resetForm()
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["saved-locations"] })
    }
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => usersService.deleteSavedLocation(id),
    onMutate: async (id) => {
      await queryClient.cancelQueries({ queryKey: ["saved-locations"] })
      const previous = queryClient.getQueryData(["saved-locations"])
      queryClient.setQueryData(["saved-locations"], (old: any) =>
        (old || []).filter((loc: any) => loc.id !== id)
      )
      return { previous }
    },
    onError: (err, id, context) => {
      queryClient.setQueryData(["saved-locations"], context?.previous)
      toast.error("Failed to remove location.")
    },
    onSuccess: () => {
      toast.success("Location deleted successfully.")
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["saved-locations"] })
    }
  })

  const resetForm = () => {
    setLabel("home")
    setAddress("")
    setLatitude(28.6139)
    setLongitude(77.2090)
    setViewport({
      center: [77.2090, 28.6139] as [number, number],
      zoom: 14,
    })
    setEditingLoc(null)
  }

  const handleOpenAdd = () => {
    resetForm()
    setOpen(true)
  }

  const handleOpenEdit = (loc: any) => {
    setEditingLoc(loc)
    setLabel(loc.label)
    setAddress(loc.address)
    setLatitude(loc.latitude)
    setLongitude(loc.longitude)
    setViewport({
      center: [loc.longitude, loc.latitude] as [number, number],
      zoom: 14,
    })
    setOpen(true)
  }

  const handleDelete = (id: number) => {
    deleteMutation.mutate(id)
  }

  const handleSave = () => {
    if (!address.trim()) {
      toast.error("Please insert a location address.")
      return
    }

    const payload = {
      label,
      address,
      latitude,
      longitude
    }

    if (editingLoc) {
      updateMutation.mutate({ id: editingLoc.id, payload })
    } else {
      addMutation.mutate(payload)
    }
  }

  const getLabelIcon = (locLabel: string) => {
    const l = locLabel.toLowerCase()
    if (l === "home") return <HouseLineIcon size={18} weight="fill" className="text-primary" />
    if (l === "office" || l === "work") return <BriefcaseIcon size={18} weight="fill" className="text-primary" />
    if (l === "favorite" || l === "favourite") return <HeartIcon size={18} weight="fill" className="text-destructive" />
    return <BookmarkIcon size={18} weight="fill" className="text-success" />
  }

  const grouped = locations.reduce((acc: any, loc: any) => {
    const rawKey = loc.label || "other"
    const capitalizedKey = rawKey.charAt(0).toUpperCase() + rawKey.slice(1).toLowerCase()
    if (!acc[capitalizedKey]) acc[capitalizedKey] = []
    acc[capitalizedKey].push(loc)
    return acc
  }, {})

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-6xl mx-auto py-2 space-y-6 text-left"
    >
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-primary/10 via-primary/5 to-transparent border border-border/80 p-6 rounded-2xl shadow-sm">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <div className="flex items-center gap-2">
              <Badge className="bg-primary/20 hover:bg-primary/20 text-primary border-0 text-[10px] px-3.5 py-1 rounded-full font-bold uppercase tracking-wider">
                Bookmarks
              </Badge>
              <Badge variant="outline" className="font-bold text-[10px] uppercase tracking-wider rounded-full">
                Saved Locations
              </Badge>
            </div>
            <h3 className="font-black text-2xl mt-3 tracking-tight text-foreground">My Saved Locations</h3>
            <p className="text-xs text-muted-foreground mt-1">Bookmark frequent addresses such as home, office, and transit points for instantaneous ride bookings.</p>
          </div>
          <Button
            onClick={handleOpenAdd}
            className="bg-primary text-primary-foreground hover:bg-primary/95 rounded-xl py-4.5 px-6 font-extrabold text-xs shadow-md shrink-0 flex items-center gap-1.5 cursor-pointer hover:scale-[1.01] transition-transform"
          >
            <PlusIcon size={16} weight="bold" />
            Add Saved Address
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Side: Address Book Category Cards (8 columns) */}
        <div className="lg:col-span-8 space-y-6">
          {isLoading ? (
            <div className="h-[250px] w-full flex flex-col items-center justify-center gap-2 bg-card border rounded-2xl">
              <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin" />
              <span className="text-[10px] uppercase font-bold tracking-widest text-muted-foreground">Loading Address Book...</span>
            </div>
          ) : locations.length === 0 ? (
            <Card className="border border-border/80 p-12 text-center rounded-2xl bg-card shadow-sm">
              <CompassIcon size={44} className="text-muted-foreground/50 mx-auto mb-3" />
              <h4 className="text-sm font-bold text-foreground">No saved addresses found</h4>
              <p className="text-xs text-muted-foreground mt-1.5">You haven't bookmarked any locations yet. Add frequent locations to start.</p>
            </Card>
          ) : (
            <div className="space-y-6">
              {Object.keys(grouped).map((groupKey) => (
                <div key={groupKey} className="space-y-3 text-left">
                  <h4 className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground pl-1">
                    {groupKey}
                  </h4>
                  
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {grouped[groupKey].map((loc: any) => (
                      <Card key={loc.id} className="border border-border/80 bg-card rounded-2xl overflow-hidden shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow">
                        <CardHeader className="pb-2 flex flex-row items-start justify-between gap-2">
                          <div className="flex items-center gap-2">
                            <div className="p-2.5 bg-muted rounded-xl">
                              {getLabelIcon(loc.label)}
                            </div>
                            <span className="text-xs font-bold text-foreground capitalize">
                              {loc.custom_label || loc.label}
                            </span>
                          </div>
                          <div className="flex gap-1 shrink-0">
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => handleOpenEdit(loc)}
                              className="w-8 h-8 rounded-full text-muted-foreground hover:text-primary hover:bg-muted"
                            >
                              <PencilSimpleIcon size={14} />
                            </Button>
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => handleDelete(loc.id)}
                              className="w-8 h-8 rounded-full text-muted-foreground hover:text-destructive hover:bg-destructive/10"
                            >
                              <TrashIcon size={14} />
                            </Button>
                          </div>
                        </CardHeader>
                        <CardContent className="pb-4">
                          <p className="text-[11px] text-muted-foreground leading-relaxed">
                            {loc.address}
                          </p>
                        </CardContent>
                        <CardFooter className="pt-2 border-t border-border/40 bg-muted/10 flex justify-between items-center px-4 py-2.5">
                          <span className="text-[9px] font-mono text-muted-foreground">
                            {loc.latitude.toFixed(4)}, {loc.longitude.toFixed(4)}
                          </span>
                        </CardFooter>
                      </Card>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Right Side: Map picker informational guide (4 columns) */}
        <div className="lg:col-span-4 space-y-4">
          <Card className="border border-border/85 bg-card rounded-2xl shadow-sm overflow-hidden text-left">
            <CardHeader className="pb-3 border-b border-border/40">
              <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                <InfoIcon size={18} className="text-primary" />
                Quick Guides
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-6 space-y-3.5 text-xs text-muted-foreground leading-relaxed">
              <p>
                Saving frequently visited locations like your <strong>Home</strong> or <strong>Office</strong> allows you to book trips in just one tap.
              </p>
              <Separator className="border-border/40" />
              <div className="flex items-start gap-2">
                <NavigationArrowIcon size={16} className="text-primary shrink-0 mt-0.5" />
                <p>
                  Bookmarked locations will automatically appear as rapid shortcuts in your main map booking screen.
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Add / Edit Location Dialog with Map Picker */}
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="max-w-md rounded-2xl border bg-card p-6 text-left">
          <DialogHeader className="space-y-1.5">
            <DialogTitle className="text-base font-bold text-foreground">
              {editingLoc ? "Update Saved Address" : "Save New Address"}
            </DialogTitle>
            <DialogDescription className="text-xs">
              Drag coordinates marker on the live map and set location label description.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 my-2">
            {/* Label picker */}
            <div className="space-y-1">
              <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider pl-0.5">Label Type</label>
              <Select value={label} onValueChange={(val) => setLabel(val || "home")}>
                <SelectTrigger className="w-full h-10 rounded-xl border border-border px-3 text-xs font-bold bg-card cursor-pointer justify-between">
                  <SelectValue placeholder="Label Type" />
                </SelectTrigger>
                <SelectContent className="bg-popover border border-border rounded-xl">
                  <SelectItem value="home">Home</SelectItem>
                  <SelectItem value="office">Office</SelectItem>
                  <SelectItem value="favorite">Favorite</SelectItem>
                  <SelectItem value="other">Other</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Address Name */}
            <div className="space-y-1">
              <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider pl-0.5">Descriptive Address</label>
              <Input
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                placeholder="e.g. 12, Terminal-1 Metro Stn, New Delhi"
                required
                className="rounded-xl border border-border text-xs"
              />
            </div>

            {/* Interactive Map Coordinates Picker */}
            <div className="space-y-1.5">
              <div className="flex justify-between items-center pr-0.5">
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider pl-0.5">Coordinates Pin (Drag to select)</label>
                <button
                  type="button"
                  onClick={handleDetectLocation}
                  disabled={detecting}
                  className="text-[10px] font-bold text-primary flex items-center gap-1 hover:underline cursor-pointer disabled:opacity-50"
                >
                  <CompassIcon size={12} weight="fill" className={detecting ? "animate-spin" : ""} />
                  <span>{detecting ? "Detecting..." : "Auto Detect"}</span>
                </button>
              </div>
              <div className="h-44 rounded-2xl overflow-hidden border border-border relative">
                <Map
                  viewport={viewport}
                  onViewportChange={(v) => {
                    setViewport(v)
                    setLongitude(v.center[0])
                    setLatitude(v.center[1])
                  }}
                >
                  <MapMarker
                    longitude={longitude}
                    latitude={latitude}
                    draggable={true}
                    onDragEnd={(lngLat) => {
                      setLongitude(lngLat.lng)
                      setLatitude(lngLat.lat)
                    }}
                  >
                    <MarkerContent>
                      <div className="p-1.5 bg-primary text-primary-foreground rounded-full shadow-lg border border-background">
                        <MapPinIcon size={16} weight="fill" />
                      </div>
                    </MarkerContent>
                  </MapMarker>
                  <MapControls />
                </Map>
              </div>
              <span className="text-[9px] text-muted-foreground font-mono pl-1">
                Lat: {latitude.toFixed(6)} • Lng: {longitude.toFixed(6)}
              </span>
            </div>
          </div>

          <DialogFooter className="flex gap-2.5 mt-2">
            <Button
              variant="outline"
              onClick={() => setOpen(false)}
              className="rounded-xl py-3 text-xs font-bold"
            >
              Cancel
            </Button>
            <Button
              onClick={handleSave}
              className="bg-primary text-primary-foreground hover:bg-primary/95 rounded-xl py-3 px-5 text-xs font-extrabold shadow-md cursor-pointer"
            >
              Save Address
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </motion.div>
  )
}
export default SavedLocationsPage
