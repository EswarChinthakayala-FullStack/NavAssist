import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useAuth } from "@/store/auth-context"
import { api } from "@/services/api"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { toast } from "sonner"
import {
  MapPinIcon,
  ShieldCheckIcon,
  SirenIcon,
  MapTrifoldIcon,
  ClockIcon,
  CheckCircleIcon,
  MagnifyingGlassIcon,
  CompassIcon,
  NavigationArrowIcon
} from "@phosphor-icons/react"

interface AutocompleteItem {
  description: string
  place_id: string
}

export function DashboardPage() {
  const navigate = useNavigate()
  const { user, userFullName } = useAuth()

  // Booking Form Coordinates States
  const [pickupText, setPickupText] = useState("")
  const [destText, setDestText] = useState("")
  
  const [pickupCoords, setPickupCoords] = useState<{ lat: number; lon: number } | null>(null)
  const [destCoords, setDestCoords] = useState<{ lat: number; lon: number } | null>(null)

  const [pickupSuggestions, setPickupSuggestions] = useState<AutocompleteItem[]>([])
  const [destSuggestions, setDestSuggestions] = useState<AutocompleteItem[]>([])

  const [estimation, setEstimation] = useState<{
    distance_km: number
    duration_minutes: number
    estimated_fare: number
    pickup_address: string
    destination_address: string
  } | null>(null)

  const [loading, setLoading] = useState(false)
  const [bookingLoading, setBookingLoading] = useState(false)

  // Stats from backend
  const [contactsCount, setContactsCount] = useState(0)
  const [tripsCount, setTripsCount] = useState(0)
  const [kycBadgeStatus, setKycBadgeStatus] = useState("Unverified")

  // Assistant Location Telemetry State
  const [locLoading, setLocLoading] = useState(false)
  const [currentCoords, setCurrentCoords] = useState<{ lat: number; lng: number } | null>(null)
  const [manualLandmark, setManualLandmark] = useState("")

  const handleUseCurrentLocation = () => {
    if (!navigator.geolocation) {
      toast.error("Browser geolocation is not supported by your browser.")
      return
    }
    setLocLoading(true)
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const { latitude, longitude } = pos.coords
        try {
          await api.patch("/assistants/me/location", { latitude, longitude })
          setCurrentCoords({ lat: latitude, lng: longitude })
          toast.success(`Live GPS location updated: ${latitude.toFixed(4)}°, ${longitude.toFixed(4)}°`)
        } catch (err) {
          toast.error("Failed to sync location to backend server.")
        } finally {
          setLocLoading(false)
        }
      },
      (err) => {
        toast.error(`Geolocation error: ${err.message}`)
        setLocLoading(false)
      },
      { enableHighAccuracy: true, timeout: 10000 }
    )
  }

  const handleManualLocationSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!manualLandmark.trim()) {
      toast.error("Please enter a valid landmark or city address.")
      return
    }
    setLocLoading(true)
    try {
      const gRes = await api.get(`/locations/geocode?address=${encodeURIComponent(manualLandmark)}`)
      const lat = gRes.data.latitude
      const lng = gRes.data.longitude
      await api.patch("/assistants/me/location", { latitude: lat, longitude: lng })
      setCurrentCoords({ lat, lng })
      toast.success(`Duty location updated to ${gRes.data.address_name || manualLandmark}!`)
      setManualLandmark("")
    } catch (err) {
      toast.error("Failed to geocode or update landmark location.")
    } finally {
      setLocLoading(false)
    }
  }

  useEffect(() => {
    // Fetch user stats
    const fetchStats = async () => {
      try {
        const contactsRes = await api.get("/users/me/emergency-contacts")
        setContactsCount(contactsRes.data.length)

        const bookingsRes = await api.get("/bookings/")
        const completed = bookingsRes.data.filter((b: any) => b.status === "COMPLETED" || b.status === "completed")
        setTripsCount(completed.length)

        if (user?.role === "assistant") {
          const kycRes = await api.get("/kyc/status")
          const rawStatus = (kycRes.data.verification_status || kycRes.data.status || "DRAFT").toUpperCase()
          setKycBadgeStatus(rawStatus === "NOT_SUBMITTED" ? "DRAFT" : rawStatus)
          if (kycRes.data.current_latitude && kycRes.data.current_longitude) {
            setCurrentCoords({ lat: kycRes.data.current_latitude, lng: kycRes.data.current_longitude })
          }
        } else {
          setKycBadgeStatus(user ? "Aadhaar Verified" : "Active Profile")
        }
      } catch (err) {
        // Fallback silently on stats error
      }
    }
    if (user) {
      fetchStats()
    }
  }, [user])

  // Autocomplete debounced calls
  useEffect(() => {
    if (pickupText.length < 3 || pickupCoords) {
      setPickupSuggestions([])
      return
    }
    const timer = setTimeout(async () => {
      try {
        const res = await api.get(`/locations/autocomplete?q=${encodeURIComponent(pickupText)}`)
        setPickupSuggestions(res.data)
      } catch (err) {}
    }, 400)
    return () => clearTimeout(timer)
  }, [pickupText, pickupCoords])

  useEffect(() => {
    if (destText.length < 3 || destCoords) {
      setDestSuggestions([])
      return
    }
    const timer = setTimeout(async () => {
      try {
        const res = await api.get(`/locations/autocomplete?q=${encodeURIComponent(destText)}`)
        setDestSuggestions(res.data)
      } catch (err) {}
    }, 400)
    return () => clearTimeout(timer)
  }, [destText, destCoords])

  const selectPickup = async (item: AutocompleteItem) => {
    setPickupText(item.description)
    setPickupSuggestions([])
    try {
      const res = await api.get(`/locations/geocode?address=${encodeURIComponent(item.description)}`)
      setPickupCoords({ lat: res.data.latitude, lon: res.data.longitude })
    } catch (err) {
      toast.error("Failed to geocode pickup landmark coordinates.")
    }
  }

  const selectDest = async (item: AutocompleteItem) => {
    setDestText(item.description)
    setDestSuggestions([])
    try {
      const res = await api.get(`/locations/geocode?address=${encodeURIComponent(item.description)}`)
      setDestCoords({ lat: res.data.latitude, lon: res.data.longitude })
    } catch (err) {
      toast.error("Failed to geocode destination coordinates.")
    }
  }

  const handleEstimate = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!pickupText || !destText) {
      toast.error("Please fill in both coordinates landmark fields.")
      return
    }

    setLoading(true)
    try {
      let pLat = pickupCoords?.lat
      let pLon = pickupCoords?.lon
      let dLat = destCoords?.lat
      let dLon = destCoords?.lon

      // Geocode on-the-fly if not already geocoded from autocomplete click
      if (!pLat || !pLon) {
        const pRes = await api.get(`/locations/geocode?address=${encodeURIComponent(pickupText)}`)
        pLat = pRes.data.latitude
        pLon = pRes.data.longitude
      }

      if (!dLat || !dLon) {
        const dRes = await api.get(`/locations/geocode?address=${encodeURIComponent(destText)}`)
        dLat = dRes.data.latitude
        dLon = dRes.data.longitude
      }

      // Determine service type (Airports usually trigger special base fare)
      const isAirport = pickupText.toLowerCase().includes("airport") || destText.toLowerCase().includes("airport")
      const servicePointType = isAirport ? "airport" : "general"

      const res = await api.post("/pricing/estimate", {
        pickup_latitude: pLat,
        pickup_longitude: pLon,
        destination_latitude: dLat,
        destination_longitude: dLon,
        service_point_type: servicePointType
      })

      setEstimation(res.data)
      setPickupCoords({ lat: pLat!, lon: pLon! })
      setDestCoords({ lat: dLat!, lon: dLon! })
    } catch (err) {
      // Axios interceptor will show the error toast automatically
    } finally {
      setLoading(false)
    }
  }

  const handleBook = async () => {
    if (!pickupCoords || !destCoords || !estimation) {
      toast.error("Please query a route fare calculation estimate first.")
      return
    }

    setBookingLoading(true)
    try {
      const res = await api.post("/bookings/", {
        pickup_latitude: pickupCoords.lat,
        pickup_longitude: pickupCoords.lon,
        pickup_address: estimation.pickup_address,
        destination_latitude: destCoords.lat,
        destination_longitude: destCoords.lon,
        destination_address: estimation.destination_address
      })

      toast.success("Booking request registered! Searching for helper...")
      // Save ID so bookings view can poll it
      sessionStorage.setItem("active_booking_id", res.data.id.toString())
      navigate("/bookings")
    } catch (err) {
      // Handled globally
    } finally {
      setBookingLoading(false)
    }
  }

  return (
    <div className="grid gap-6 md:grid-cols-12 max-w-6xl mx-auto py-2">
      {/* Welcome Card & Stats Grid */}
      <div className="md:col-span-8 grid gap-6">
        <Card className="bg-gradient-to-r from-primary/10 via-primary/5 to-transparent border-primary/20 shadow-md relative overflow-hidden rounded-2xl">
          <div className="absolute right-0 bottom-0 top-0 opacity-5 flex items-center justify-center p-6 pointer-events-none">
            <MapPinIcon size={140} weight="fill" className="text-primary" />
          </div>
          <CardHeader className="p-8">
            <div className="flex items-center gap-2">
              <Badge className="bg-success text-success-foreground border-0 hover:bg-success font-semibold px-2.5 py-0.5 rounded-full text-xs">
                {kycBadgeStatus === "APPROVED" || kycBadgeStatus === "Aadhaar Verified" ? "Aadhaar Verified" : kycBadgeStatus}
              </Badge>
              <Badge variant="outline" className="font-semibold text-xs rounded-full uppercase tracking-wider">
                {user?.role} Profile
              </Badge>
            </div>
            <CardTitle className="text-3xl font-extrabold mt-4 text-foreground tracking-tight">
              Welcome back, {userFullName || "Traveler"}!
            </CardTitle>
            <CardDescription className="text-base text-muted-foreground mt-2 max-w-xl leading-relaxed">
              Ready to explore? Request a certified assistant to pick you up directly from airport gates, railway platforms or stations and navigate safely to your lodging.
            </CardDescription>
          </CardHeader>
        </Card>

        {/* Dynamic Metric Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
          <Card className="hover:shadow-lg hover:-translate-y-0.5 transition-all duration-350 cursor-pointer" onClick={() => navigate("/kyc")}>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Identity</span>
                <div className="p-2.5 rounded-xl bg-success/10 text-success">
                  <ShieldCheckIcon size={20} weight="fill" />
                </div>
              </div>
              <h2 className="text-2xl font-black mt-4 text-foreground">
                {kycBadgeStatus === "APPROVED" || kycBadgeStatus === "Aadhaar Verified" ? "Verified" : "Verification Status"}
              </h2>
              <p className="text-xs text-muted-foreground mt-1.5">Click to check KYC credentials</p>
            </CardContent>
          </Card>

          <Card className="hover:shadow-lg hover:-translate-y-0.5 transition-all duration-350 cursor-pointer" onClick={() => navigate("/safety")}>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Safety status</span>
                <div className="p-2.5 rounded-xl bg-destructive/10 text-destructive">
                  <SirenIcon size={20} weight="fill" />
                </div>
              </div>
              <h2 className="text-2xl font-black mt-4 text-foreground">
                {contactsCount} Contacts
              </h2>
              <p className="text-xs text-muted-foreground mt-1.5">Registered safety guardians</p>
            </CardContent>
          </Card>

          <Card className="hover:shadow-lg hover:-translate-y-0.5 transition-all duration-350 cursor-pointer" onClick={() => navigate("/bookings")}>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Total trips</span>
                <div className="p-2.5 rounded-xl bg-primary/10 text-primary">
                  <MapTrifoldIcon size={20} weight="fill" />
                </div>
              </div>
              <h2 className="text-2xl font-black mt-4 text-foreground">
                {tripsCount} Completed
              </h2>
              <p className="text-xs text-muted-foreground mt-1.5">Safe guidance logs verified</p>
            </CardContent>
          </Card>
        </div>

        {/* Assistant Live Duty Location Card */}
        {user?.role === "assistant" && (
          <Card className="shadow-md border border-primary/30 bg-card rounded-2xl overflow-hidden text-left">
            <CardHeader className="p-6">
              <div className="flex items-center justify-between">
                <Badge className="bg-primary/20 text-primary border-0 font-bold px-3 py-1 rounded-full text-[10px] uppercase tracking-wider">
                  Assistant Duty Telemetry
                </Badge>
                {currentCoords && (
                  <span className="text-xs font-mono text-success font-bold flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-success animate-ping inline-block" />
                    GPS: {currentCoords.lat.toFixed(4)}°, {currentCoords.lng.toFixed(4)}°
                  </span>
                )}
              </div>
              <CardTitle className="text-xl font-extrabold mt-3 flex items-center gap-2">
                <CompassIcon size={24} className="text-primary" />
                Update Live Duty Location
              </CardTitle>
              <CardDescription className="text-xs text-muted-foreground mt-1">
                Keep your real-time GPS location updated so nearby travelers can discover and dispatch bookings to you.
              </CardDescription>
            </CardHeader>
            <CardContent className="px-6 pb-6 pt-0 space-y-4">
              <div className="flex flex-col sm:flex-row gap-3">
                <Button
                  onClick={handleUseCurrentLocation}
                  disabled={locLoading}
                  className="h-11 px-5 rounded-xl font-bold text-xs bg-primary text-primary-foreground hover:bg-primary/90 flex items-center justify-center gap-2 shadow-sm shrink-0 cursor-pointer"
                >
                  {locLoading ? (
                    <div className="w-4 h-4 border-2 border-primary-foreground border-t-transparent rounded-full animate-spin" />
                  ) : (
                    <NavigationArrowIcon size={18} weight="fill" />
                  )}
                  Use Current Location
                </Button>

                <form onSubmit={handleManualLocationSubmit} className="flex-1 flex gap-2">
                  <Input
                    value={manualLandmark}
                    onChange={(e) => setManualLandmark(e.target.value)}
                    placeholder="Or enter landmark (e.g. Ongole Railway Station)"
                    className="rounded-xl h-11 text-xs font-medium"
                  />
                  <Button
                    type="submit"
                    disabled={locLoading}
                    variant="outline"
                    className="h-11 px-4 rounded-xl font-bold text-xs shrink-0 cursor-pointer"
                  >
                    Update
                  </Button>
                </form>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Free OSRM Routing Tool Card */}
        <Card className="shadow-md border border-border/80 rounded-2xl overflow-visible">
          <CardHeader>
            <CardTitle className="text-xl font-bold flex items-center gap-2">
              <MapPinIcon size={24} className="text-primary" />
              Real-Time Route & Fare Estimator
            </CardTitle>
            <CardDescription>
              Retrieve spatial details and estimated marketplace fares powered by OSRM routing telemetry.
            </CardDescription>
          </CardHeader>
          <CardContent className="overflow-visible">
            <form onSubmit={handleEstimate} className="grid gap-4 sm:grid-cols-2 relative">
              {/* Pickup Address Input */}
              <div className="relative">
                <Label htmlFor="pickup">Pickup Landmark (e.g. Airport T3)</Label>
                <div className="relative mt-1.5">
                  <Input
                    id="pickup"
                    value={pickupText}
                    onChange={(e) => {
                      setPickupText(e.target.value)
                      setPickupCoords(null)
                    }}
                    placeholder="Enter railway/airport point"
                    required
                    className="pr-10"
                  />
                  <div className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                    <MagnifyingGlassIcon size={16} />
                  </div>
                </div>
                {/* Suggestions List */}
                {pickupSuggestions.length > 0 && (
                  <div className="absolute z-50 left-0 right-0 mt-1 bg-popover text-popover-foreground border rounded-xl shadow-lg max-h-48 overflow-y-auto">
                    {pickupSuggestions.map((item) => (
                      <button
                        key={item.place_id}
                        type="button"
                        onClick={() => selectPickup(item)}
                        className="w-full text-left px-4 py-2.5 text-xs hover:bg-muted font-medium transition-colors border-b last:border-0"
                      >
                        {item.description}
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {/* Destination Address Input */}
              <div className="relative">
                <Label htmlFor="destination">Destination address</Label>
                <div className="relative mt-1.5">
                  <Input
                    id="destination"
                    value={destText}
                    onChange={(e) => {
                      setDestText(e.target.value)
                      setDestCoords(null)
                    }}
                    placeholder="Enter hotel/home address"
                    required
                    className="pr-10"
                  />
                  <div className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                    <MagnifyingGlassIcon size={16} />
                  </div>
                </div>
                {/* Suggestions List */}
                {destSuggestions.length > 0 && (
                  <div className="absolute z-50 left-0 right-0 mt-1 bg-popover text-popover-foreground border rounded-xl shadow-lg max-h-48 overflow-y-auto">
                    {destSuggestions.map((item) => (
                      <button
                        key={item.place_id}
                        type="button"
                        onClick={() => selectDest(item)}
                        className="w-full text-left px-4 py-2.5 text-xs hover:bg-muted font-medium transition-colors border-b last:border-0"
                      >
                        {item.description}
                      </button>
                    ))}
                  </div>
                )}
              </div>

              <div className="sm:col-span-2">
                <Button type="submit" className="w-full bg-primary hover:bg-primary/95 text-primary-foreground font-bold py-5 rounded-xl shadow-sm mt-2" disabled={loading}>
                  {loading ? "Estimating Route telemetry..." : "Get Route & Price Details"}
                </Button>
              </div>
            </form>
          </CardContent>
          
          {/* Form Result / Estimation Card */}
          {estimation && (
            <CardFooter className="flex flex-col items-stretch border-t p-6 bg-muted/20 rounded-b-2xl">
              <div className="grid grid-cols-3 gap-4 text-center">
                <div>
                  <span className="text-[10px] text-muted-foreground font-bold uppercase tracking-wider">Distance</span>
                  <p className="text-base font-extrabold text-foreground mt-1">{estimation.distance_km} km</p>
                </div>
                <div>
                  <span className="text-[10px] text-muted-foreground font-bold uppercase tracking-wider">Est. Duration</span>
                  <p className="text-base font-extrabold text-foreground mt-1">{estimation.duration_minutes} mins</p>
                </div>
                <div>
                  <span className="text-[10px] text-muted-foreground font-bold uppercase tracking-wider">Suggested Fare</span>
                  <p className="text-base font-extrabold text-primary mt-1">₹{Number(estimation.estimated_fare).toFixed(2)}</p>
                </div>
              </div>
              
              {user?.role === "guest" ? (
                <Button onClick={handleBook} disabled={bookingLoading} className="w-full mt-6 bg-success text-success-foreground hover:bg-success/90 font-bold py-5 rounded-xl shadow-md">
                  {bookingLoading ? "Requesting dispatch..." : "Confirm Booking & Dispatch Assistant"}
                </Button>
              ) : (
                <div className="p-3 bg-accent/15 border border-accent rounded-xl text-center text-xs font-semibold text-accent-foreground mt-4">
                  Fare estimations are visible to assistants. Only guests can create booking requests.
                </div>
              )}
            </CardFooter>
          )}
        </Card>
      </div>

      {/* Safety SOS Quick Action Panel */}
      <div className="md:col-span-4 grid gap-6 h-fit">
        <Card className="border-destructive/30 bg-destructive/5 overflow-hidden rounded-2xl shadow-sm">
          <CardHeader className="text-center pb-2">
            <div className="w-12 h-12 bg-destructive/10 text-destructive rounded-full flex items-center justify-center mx-auto mb-2 animate-pulse">
              <SirenIcon size={28} weight="fill" />
            </div>
            <CardTitle className="text-lg font-bold text-destructive">Emergency SOS Console</CardTitle>
            <CardDescription className="text-xs">
              Immediate alert triggers to registered safety contacts.
            </CardDescription>
          </CardHeader>
          <CardContent className="pt-2 text-center">
            <p className="text-xs text-muted-foreground mb-4 leading-relaxed">
              Pressing the trigger sends your live GPS coordinates, status telemetry, and location sharing links to your emergency contacts.
            </p>
            <Button
              onClick={() => navigate("/safety")}
              className="w-full bg-destructive text-destructive-foreground hover:bg-destructive/90 py-5 text-xs font-extrabold uppercase tracking-wider shadow-md rounded-xl"
            >
              Access SOS Control
            </Button>
          </CardContent>
        </Card>

        {/* Quick Tips */}
        <Card className="rounded-2xl shadow-sm">
          <CardHeader>
            <CardTitle className="text-xs font-bold uppercase tracking-widest text-muted-foreground">Safety Guideline</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-4">
            <div className="flex gap-3 text-xs leading-relaxed text-muted-foreground">
              <div className="p-1 h-fit rounded-full bg-primary/10 text-primary">
                <CheckCircleIcon size={16} />
              </div>
              <p>Always verify the assistant badge ID matches the credential shown in your active booking view before departure.</p>
            </div>
            <div className="flex gap-3 text-xs leading-relaxed text-muted-foreground">
              <div className="p-1 h-fit rounded-full bg-primary/10 text-primary">
                <CheckCircleIcon size={16} />
              </div>
              <p>Share your active tracking link with family. They can follow you without installing the application.</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
export default DashboardPage
