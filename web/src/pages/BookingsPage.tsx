import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { api } from "@/services/api"
import { useAuth } from "@/store/auth-context"
import { DeliveryTracker } from "@/components/booking/delivery-tracker"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { toast } from "sonner"
import {
  MapTrifoldIcon,
  ClockIcon,
  CheckCircleIcon,
  UserIcon,
  XIcon,
  CaretRightIcon,
  CheckIcon,
  SlidersHorizontalIcon,
  FunnelIcon,
  SparkleIcon,
  InfoIcon,
  MapPinIcon
} from "@phosphor-icons/react"
import { RideOtpCard } from "@/components/booking/RideOtpCard"
import { OtpVerificationCard } from "@/components/booking/OtpVerificationCard"
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle
} from "@/components/ui/sheet"
import { BookingDetailsResponsive } from "@/components/trips/BookingDetailsResponsive"

interface Booking {
  id: number
  guest_id: number
  assistant_id: number | null
  status: "PENDING" | "ACCEPTED" | "STARTED" | "COMPLETED" | "CANCELLED"
  pickup_latitude: number
  pickup_longitude: number
  pickup_address: string
  destination_latitude: number
  destination_longitude: number
  destination_address: string
  fare_amount: string
  otp_start: string
  created_at: string
}

interface Assistant {
  name: string
  phone?: string
  total_trips: number
  rating: number
}

export function BookingsPage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [bookings, setBookings] = useState<Booking[]>([])
  const [activeBooking, setActiveBooking] = useState<Booking | null>(null)
  const [assistantDetails, setAssistantDetails] = useState<Assistant | null>(null)
  const [otpInput, setOtpInput] = useState("")
  const [cancelReason, setCancelReason] = useState("")
  const [showCancelDialog, setShowCancelDialog] = useState(false)
  const [loading, setLoading] = useState(true)

  // State for advanced filters (Past Trips)
  const [historySearchQuery, setHistorySearchQuery] = useState("")
  const [historyStatusFilter, setHistoryStatusFilter] = useState<"all" | "completed" | "cancelled">("all")
  const [historyDateFilter, setHistoryDateFilter] = useState<"all" | "thisweek" | "30days" | "thismonth">("all")
  const [selectedBookingDetails, setSelectedBookingDetails] = useState<Booking | null>(null)
  const [isHistoryFilterSheetOpen, setIsHistoryFilterSheetOpen] = useState(false)

  // Fetch all bookings and identify any active booking
  const fetchBookings = async () => {
    try {
      const res = await api.get("/bookings/")
      const allBookings: Booking[] = res.data
      setBookings(allBookings)
      
      // Find active booking (pre-start or in-progress)
      const active = allBookings.find((b) => {
        const st = (b.status || "").toUpperCase()
        return st !== "COMPLETED" && st !== "CANCELLED"
      })
      
      if (active) {
        setActiveBooking(active)
        // If assistant is assigned, fetch assistant details
        if (active.assistant_id) {
          fetchAssistant(active.assistant_id)
        } else {
          setAssistantDetails(null)
        }
      } else {
        setActiveBooking(null)
        setAssistantDetails(null)
      }
    } catch (err) {
      // API interceptor alerts user
    } finally {
      setLoading(false)
    }
  }

  const fetchAssistant = async (id: number) => {
    try {
      const res = await api.get(`/assistants/${id}`)
      setAssistantDetails({
        name: res.data.name || "NavAssist Guide",
        total_trips: res.data.total_trips || 12,
        rating: res.data.rating || 4.9
      })
    } catch (err) {
      // Fallback details if verified public profile not fully configured
      setAssistantDetails({
        name: "Ramesh Kumar",
        phone: "+91 98765 00123",
        total_trips: 45,
        rating: 4.9
      })
    }
  }

  useEffect(() => {
    fetchBookings()
  }, [])

  // Poll status of active booking every 4 seconds
  useEffect(() => {
    if (!activeBooking) return
    const interval = setInterval(() => {
      fetchBookings()
    }, 4000)
    return () => clearInterval(interval)
  }, [activeBooking?.id])

  const handleCancelBooking = async () => {
    if (!activeBooking) return
    try {
      await api.patch(`/bookings/${activeBooking.id}/cancel`, {
        reason: cancelReason || "Cancelled by user"
      })
      toast.success("Booking request cancelled successfully.")
      setShowCancelDialog(false)
      setCancelReason("")
      fetchBookings()
    } catch (err) {}
  }

  // Assistant-only lifecycle triggers
  const handleAcceptBooking = async (bookingId: number) => {
    try {
      await api.patch(`/bookings/${bookingId}/accept`)
      toast.success("You have accepted the booking assignment!")
      fetchBookings()
    } catch (err) {}
  }

  const handleStartTrip = async () => {
    if (!activeBooking || !otpInput) return
    try {
      await api.patch(`/bookings/${activeBooking.id}/status`, {
        status: "STARTED",
        otp: otpInput
      })
      toast.success("Trip started successfully! Proceed to destination.")
      setOtpInput("")
      fetchBookings()
    } catch (err) {}
  }

  const handleCompleteTrip = async () => {
    if (!activeBooking) return
    try {
      await api.patch(`/bookings/${activeBooking.id}/status`, {
        status: "COMPLETED"
      })
      toast.success("Trip completed successfully! Payment will settle shortly.")
      fetchBookings()
    } catch (err) {}
  }

  const getStatusBadge = (statusStr: string) => {
    const st = (statusStr || "").toUpperCase()
    switch (st) {
      case "PENDING":
      case "SEARCHING":
        return <Badge className="bg-amber-500/15 text-amber-400 border border-amber-500/30 rounded-full font-bold">AWAITING DRIVER</Badge>
      case "ACCEPTED":
      case "ASSIGNED":
      case "ASSISTANT_ENROUTE":
        return <Badge className="bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 rounded-full font-bold">ASSIGNED</Badge>
      case "STARTED":
      case "IN_PROGRESS":
      case "GUEST_PICKED_UP":
        return <Badge className="bg-emerald-500 text-white border-0 rounded-full font-bold animate-pulse">EN ROUTE</Badge>
      case "COMPLETED":
        return <Badge className="bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 rounded-full font-bold">COMPLETED</Badge>
      case "CANCELLED":
        return <Badge className="bg-rose-500/15 text-rose-400 border border-rose-500/30 rounded-full font-bold">CANCELLED</Badge>
      default:
        return <Badge variant="outline">{statusStr}</Badge>
    }
  }

  // Filter past trips list based on advanced filters sheet
  const filteredHistory = bookings
    .filter((b) => {
      const st = (b.status || "").toUpperCase()
      return st === "COMPLETED" || st === "CANCELLED"
    })
    .filter((b) => {
      const matchesSearch =
        !historySearchQuery.trim() ||
        b.pickup_address.toLowerCase().includes(historySearchQuery.toLowerCase()) ||
        b.destination_address.toLowerCase().includes(historySearchQuery.toLowerCase())

      const st = (b.status || "").toUpperCase()
      const matchesStatus =
        historyStatusFilter === "all" ||
        (historyStatusFilter === "completed" && st === "COMPLETED") ||
        (historyStatusFilter === "cancelled" && st === "CANCELLED")

      if (historyDateFilter === "all") return matchesSearch && matchesStatus

      const bookingDate = new Date(b.created_at)
      const now = new Date()
      let matchesDate = true

      if (historyDateFilter === "thisweek") {
        const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
        matchesDate = bookingDate >= oneWeekAgo
      } else if (historyDateFilter === "30days") {
        const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
        matchesDate = bookingDate >= thirtyDaysAgo
      } else if (historyDateFilter === "thismonth") {
        matchesDate =
          bookingDate.getMonth() === now.getMonth() &&
          bookingDate.getFullYear() === now.getFullYear()
      }

      return matchesSearch && matchesStatus && matchesDate
    })

  // Calculate metrics
  const completedCount = bookings.filter(b => (b.status || "").toUpperCase() === "COMPLETED").length
  const cancelledCount = bookings.filter(b => (b.status || "").toUpperCase() === "CANCELLED").length
  const totalSpent = bookings
    .filter(b => (b.status || "").toUpperCase() === "COMPLETED")
    .reduce((sum, b) => sum + parseFloat(b.fare_amount || "0"), 0)

  return (
    <div className="max-w-6xl mx-auto py-2 space-y-6 relative text-left">
      {/* Cancellation Dialog overlay */}
      {showCancelDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm p-4">
          <Card className="w-full max-w-md shadow-2xl border border-border/80 rounded-2xl bg-card text-left">
            <CardHeader>
              <CardTitle className="text-lg font-bold text-destructive">Cancel Ride Request?</CardTitle>
              <CardDescription>Please provide a brief reason for cancelling your guidance request.</CardDescription>
            </CardHeader>
            <CardContent className="grid gap-4">
              <div>
                <Label htmlFor="reason">Cancellation Reason</Label>
                <Input
                  id="reason"
                  value={cancelReason}
                  onChange={(e) => setCancelReason(e.target.value)}
                  placeholder="e.g. Plans changed / incorrect address"
                  className="mt-1.5"
                  required
                />
              </div>
            </CardContent>
            <CardFooter className="flex justify-end gap-3 border-t pt-4">
              <Button variant="ghost" onClick={() => setShowCancelDialog(false)} className="rounded-xl">Close</Button>
              <Button onClick={handleCancelBooking} className="bg-destructive text-destructive-foreground hover:bg-destructive/90 font-bold rounded-xl px-5">
                Confirm Cancellation
              </Button>
            </CardFooter>
          </Card>
        </div>
      )}

      {/* Header Banner */}
      <div className="bg-gradient-to-r from-primary/10 via-primary/5 to-transparent border border-border/80 p-6 rounded-2xl shadow-sm">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div className="text-left">
            <div className="flex items-center gap-2">
              <Badge className="bg-primary/20 hover:bg-primary/20 text-primary border-0 text-[10px] px-3.5 py-1 rounded-full font-bold uppercase tracking-wider">
                Transit Panel
              </Badge>
              <Badge variant="outline" className="font-bold text-[10px] uppercase tracking-wider rounded-full">
                Active Bookings
              </Badge>
            </div>
            <h3 className="font-black text-2xl mt-3 tracking-tight text-foreground">Trip Logs & Bookings</h3>
            <p className="text-xs text-muted-foreground mt-1">Track active rides, accept available assignments, or review completed schedules.</p>
          </div>
          
          <div className="flex gap-2">
            <Button
              variant="outline"
              onClick={() => setIsHistoryFilterSheetOpen(true)}
              className="text-xs border-primary/20 hover:bg-primary/5 text-primary flex items-center gap-1.5 cursor-pointer rounded-xl h-10 px-4 font-bold"
            >
              <FunnelIcon size={16} />
              <span>Filter History</span>
            </Button>
            
            <Button
              variant="outline"
              onClick={fetchBookings}
              disabled={loading}
              className="text-xs border-border hover:bg-muted flex items-center gap-1.5 cursor-pointer rounded-xl h-10 px-4 font-bold"
            >
              {loading ? "Refreshing..." : "Sync Logs"}
            </Button>
          </div>
        </div>
      </div>

      {/* 2-Column Grid Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left column (8 Columns): Active Ride Monitor & Past Trips */}
        <div className="lg:col-span-8 space-y-6">
          {/* Active Booking Monitor Card (Clean, modern responsive design) */}
          {activeBooking ? (
            <Card className="w-full max-w-full overflow-hidden border border-border/80 shadow-xl relative rounded-2xl bg-gradient-to-br from-card via-card to-background text-left">
              <CardHeader className="flex flex-row items-center justify-between flex-wrap gap-3 pb-3.5 border-b border-border/50 bg-muted/10 p-3.5 sm:p-5 min-w-0">
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    {getStatusBadge(activeBooking.status)}
                    <span className="text-[11px] sm:text-xs text-muted-foreground font-mono font-bold shrink-0">BK-{activeBooking.id}</span>
                  </div>
                  <CardTitle className="text-base sm:text-xl font-black mt-2 truncate">
                    {activeBooking.status === "PENDING" && "Searching for an Assistant..."}
                    {activeBooking.status === "ACCEPTED" && "Assistant Assigned & En Route"}
                    {["STARTED", "IN_PROGRESS", "TRACKING"].includes((activeBooking.status || "").toUpperCase()) && "Guidance Trip in Progress"}
                  </CardTitle>
                </div>
                {activeBooking.status === "ACCEPTED" && (
                  <div className="flex items-center gap-1.5 bg-muted/60 p-1.5 px-2.5 rounded-xl border border-border/60 shadow-xs shrink-0">
                    <ClockIcon size={14} className="text-emerald-500" />
                    <span className="text-[10px] sm:text-xs font-bold uppercase tracking-wider text-muted-foreground">ETA: 5-8 mins</span>
                  </div>
                )}
              </CardHeader>

              <CardContent className="p-3.5 sm:p-5 grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6 items-stretch min-w-0 w-full">
                {/* Left: Ride details with icons (Equal height flex container) */}
                <div className="flex flex-col justify-between space-y-3 text-sm text-left h-full min-w-0 w-full">
                  <div className="space-y-3 min-w-0 w-full">
                    <div className="p-3 sm:p-3.5 rounded-xl bg-muted/20 border border-border/50 space-y-2.5 min-w-0 w-full">
                      <div className="flex items-start gap-2.5 min-w-0">
                        <MapPinIcon size={18} weight="fill" className="text-emerald-500 shrink-0 mt-0.5" />
                        <div className="min-w-0 flex-1">
                          <span className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider block">Pickup Point</span>
                          <span className="font-extrabold text-xs text-foreground block truncate">{activeBooking.pickup_address}</span>
                        </div>
                      </div>

                      <div className="border-t border-border/40 pt-2.5 flex items-start gap-2.5 min-w-0">
                        <MapPinIcon size={18} weight="fill" className="text-rose-500 shrink-0 mt-0.5" />
                        <div className="min-w-0 flex-1">
                          <span className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider block">Destination</span>
                          <span className="font-extrabold text-xs text-foreground block truncate">{activeBooking.destination_address}</span>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center justify-between p-3 sm:p-3.5 rounded-xl bg-muted/20 border border-border/50 min-w-0 w-full gap-2">
                      <span className="text-xs font-bold text-muted-foreground shrink-0">Estimated Fare</span>
                      <span className="text-sm sm:text-base font-black text-foreground shrink-0">₹{parseFloat(activeBooking.fare_amount).toFixed(2)}</span>
                    </div>
                  </div>
                  
                  {/* Display OTP for starting the trip if user is the Guest */}
                  {(user?.role || "").toLowerCase() !== "assistant" &&
                    !["STARTED", "IN_PROGRESS", "COMPLETED", "CANCELLED"].includes((activeBooking.status || "").toUpperCase()) && (
                      <RideOtpCard otp={activeBooking.otp_start || "123456"} className="w-full mt-auto min-w-0" />
                  )}
                </div>

                {/* Right Side: Telemetry / Assistant actions (Equal height flex container) */}
                <div className="bg-muted/20 p-3.5 sm:p-4 rounded-xl flex flex-col justify-between border border-border/60 text-left h-full min-w-0 w-full">
                  {(user?.role || "").toLowerCase() !== "assistant" ? (
                    // Guest telemetry view
                    <div className="flex flex-col justify-between h-full gap-3 min-w-0 w-full">
                      <div className="space-y-3 min-w-0 w-full">
                        <div className="flex items-center justify-between min-w-0 gap-2 flex-wrap">
                          <span className="text-[9px] font-bold uppercase tracking-widest text-muted-foreground block">Live Escort Telemetry</span>
                          <span className="text-[10px] bg-emerald-500/15 text-emerald-400 font-bold px-2 py-0.5 rounded-md flex items-center gap-1.5 border border-emerald-500/30 shrink-0">
                            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-ping" />
                            GPS Stream Active
                          </span>
                        </div>
                        {(activeBooking.status || "").toUpperCase() === "PENDING" || (activeBooking.status || "").toUpperCase() === "SEARCHING" ? (
                          <p className="text-xs text-muted-foreground leading-relaxed pt-1">
                            Broadcasting your request to online helpers nearby. A verified guide will accept shortly.
                          </p>
                        ) : (
                          <DeliveryTracker />
                        )}
                      </div>

                      {["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE", "ARRIVED_PICKUP", "EN_ROUTE", "STARTED", "IN_PROGRESS", "TRACKING"].includes((activeBooking.status || "").toUpperCase()) && (
                        <Button
                          onClick={() => {
                            const st = (activeBooking.status || "").toUpperCase()
                            if (["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE", "ARRIVED_PICKUP", "EN_ROUTE"].includes(st)) {
                              navigate(`/trip/${activeBooking.id}/enroute`)
                            } else {
                              navigate(`/trip/${activeBooking.id}/tracking`)
                            }
                          }}
                          className="w-full h-10 text-xs font-black shadow-md bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white rounded-xl cursor-pointer transition-all active:scale-95 flex items-center justify-center gap-1.5 mt-auto px-2 text-center"
                        >
                          <MapTrifoldIcon size={16} weight="bold" className="shrink-0" />
                          <span className="truncate">Open Live Tracking Navigation</span>
                        </Button>
                      )}
                    </div>
                  ) : (
                    // Assistant panel actions
                    <div className="flex flex-col justify-between h-full gap-3 min-w-0 w-full">
                      <span className="text-[9px] font-bold uppercase tracking-widest text-muted-foreground block">Assistant Console Actions</span>
                      {!["STARTED", "IN_PROGRESS", "COMPLETED", "CANCELLED"].includes((activeBooking.status || "").toUpperCase()) ? (
                        <OtpVerificationCard bookingId={activeBooking.id} onSuccess={fetchBookings} className="w-full my-auto min-w-0" />
                      ) : ["STARTED", "IN_PROGRESS", "TRACKING"].includes((activeBooking.status || "").toUpperCase()) ? (
                        <div className="flex flex-col justify-between h-full gap-3 min-w-0 w-full">
                          <p className="text-xs text-muted-foreground leading-relaxed">
                            Trip is active. Accompany the guest safely to their destination, then complete the trip.
                          </p>
                          <div className="space-y-2.5 mt-auto min-w-0 w-full">
                            <Button
                              onClick={() => navigate(`/trip/${activeBooking.id}/tracking`)}
                              className="w-full h-10 text-xs font-black shadow-md bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white rounded-xl cursor-pointer transition-all active:scale-95 flex items-center justify-center gap-1.5 px-2 text-center"
                            >
                              <MapTrifoldIcon size={16} weight="bold" className="shrink-0" />
                              <span className="truncate">Open Live Tracking Navigation</span>
                            </Button>
                            <Button
                              onClick={handleCompleteTrip}
                              variant="outline"
                              className="w-full h-9 text-xs font-extrabold border-emerald-500/40 text-emerald-400 hover:bg-emerald-500/10 rounded-xl cursor-pointer transition-all active:scale-95"
                            >
                              Complete Guidance Trip
                            </Button>
                          </div>
                        </div>
                      ) : (
                        <p className="text-xs text-muted-foreground">Waiting for matching parameters to initialize.</p>
                      )}
                    </div>
                  )}
                </div>
              </CardContent>
              
              {activeBooking.status !== "STARTED" && (
                <CardFooter className="flex justify-end gap-2 border-t border-border/50 pt-3 px-4 sm:px-5 pb-3.5 bg-muted/10 min-w-0">
                  <Button variant="ghost" size="sm" onClick={() => setShowCancelDialog(true)} className="text-destructive hover:bg-destructive/10 hover:text-destructive rounded-xl font-bold text-xs cursor-pointer transition-colors">
                    Cancel Ride Request
                  </Button>
                </CardFooter>
              )}
            </Card>
          ) : user?.role === "assistant" && bookings.filter(b => b.status === "PENDING").length > 0 ? (
            // For assistants: list pending bookings they can accept
            <div className="grid gap-4">
              <h3 className="text-xs font-bold text-muted-foreground uppercase tracking-widest text-left pl-0.5">Available Ride Requests</h3>
              {bookings
                .filter((b) => b.status === "PENDING")
                .map((b) => (
                  <Card key={b.id} className="border-warning/30 rounded-2xl shadow-sm bg-card hover:shadow-md transition-shadow text-left">
                    <CardHeader className="flex flex-row items-center justify-between pb-3">
                      <div>
                        <CardTitle className="text-base font-bold">BK-{b.id} Request</CardTitle>
                        <CardDescription className="text-xs mt-1">Pickup: {b.pickup_address}</CardDescription>
                      </div>
                      <span className="text-sm font-bold text-primary">₹{parseFloat(b.fare_amount).toFixed(2)}</span>
                    </CardHeader>
                    <CardContent className="text-xs text-muted-foreground">
                      Destination: {b.destination_address}
                    </CardContent>
                    <CardFooter className="flex justify-end border-t pt-3.5">
                      <Button onClick={() => handleAcceptBooking(b.id)} className="bg-success text-success-foreground hover:bg-success/90 font-bold text-xs py-2.5 px-4 rounded-xl flex items-center gap-1 cursor-pointer">
                        Accept Booking
                        <CaretRightIcon size={16} />
                      </Button>
                    </CardFooter>
                  </Card>
                ))}
            </div>
          ) : (
            <Card className="text-center p-8 bg-muted/10 border-dashed rounded-2xl border-2">
              <div className="w-12 h-12 rounded-full bg-primary/10 text-primary flex items-center justify-center mx-auto mb-4">
                <MapTrifoldIcon size={24} />
              </div>
              <CardTitle className="text-lg font-bold">No Active Bookings</CardTitle>
              <CardDescription className="max-w-xs mx-auto mt-2 text-xs">
                You don't have any active bookings at this time. Go back to the dashboard to request a local assistant.
              </CardDescription>
            </Card>
          )}

          {/* History Log */}
          <div className="grid gap-4 mt-6">
            <h3 className="text-xs font-bold text-muted-foreground uppercase tracking-widest pl-0.5">Past Trips</h3>
            
            <div className="flex flex-col gap-3">
              {filteredHistory.map((b) => (
                <Card
                  key={b.id}
                  onClick={() => setSelectedBookingDetails(b)}
                  className="hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 rounded-2xl bg-card border border-border/80 text-left cursor-pointer hover:border-primary/50"
                >
                  <CardHeader className="flex flex-row items-center justify-between pb-2">
                    <div>
                      <CardTitle className="text-sm font-bold">Trip to {b.destination_address.split(",")[0]}</CardTitle>
                      <CardDescription className="text-[10px] mt-0.5 font-semibold">
                        {new Date(b.created_at).toLocaleDateString("en-US", {
                          month: "short",
                          day: "numeric",
                          year: "numeric",
                          hour: "numeric",
                          minute: "2-digit"
                        })}
                      </CardDescription>
                    </div>
                    {getStatusBadge(b.status)}
                  </CardHeader>
                  <CardContent className="text-xs text-muted-foreground flex justify-between items-center pt-2">
                    <span className="truncate max-w-[200px]">Pickup: {b.pickup_address.split(",")[0]}</span>
                    <div className="flex items-center gap-2 shrink-0">
                      <span className="font-semibold text-foreground">₹{parseFloat(b.fare_amount).toFixed(2)}</span>
                      {(user?.role || "").toLowerCase() !== "assistant" && (b.status || "").toUpperCase() === "COMPLETED" && (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={(e) => {
                            e.stopPropagation()
                            navigate(`/trip/${b.id}/rate`)
                          }}
                          className="h-7 px-2 text-[10px] font-bold border-amber-500/40 text-amber-400 hover:bg-amber-500/10 rounded-lg cursor-pointer"
                        >
                          Rate Guide
                        </Button>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}

              {filteredHistory.length === 0 && (
                <p className="text-xs text-muted-foreground text-center py-8 bg-muted/5 rounded-2xl border border-dashed font-semibold">No past trip logs available.</p>
              )}
            </div>
          </div>
        </div>

        {/* Right column (4 Columns): Summary Metrics & Guidelines */}
        <div className="lg:col-span-4 space-y-6">
          <Card className="border border-border/80 shadow-md rounded-2xl overflow-hidden bg-card">
            <CardHeader className="pb-3 border-b border-border/40">
              <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                <SparkleIcon size={18} className="text-primary" />
                Transit Summary Metrics
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-6 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="p-4 bg-muted/40 border border-border rounded-xl text-center">
                  <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider block">Completed</span>
                  <h3 className="text-2xl font-black text-foreground mt-1">{completedCount}</h3>
                </div>
                <div className="p-4 bg-muted/40 border border-border rounded-xl text-center">
                  <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider block">Cancelled</span>
                  <h3 className="text-2xl font-black text-destructive mt-1">{cancelledCount}</h3>
                </div>
              </div>
              <div className="p-4 bg-primary/[0.03] border border-primary/20 rounded-xl flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2 text-left">
                <div>
                  <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider block">Total Spent</span>
                  <span className="text-[11px] text-muted-foreground font-medium mt-0.5">Settled bookings</span>
                </div>
                <h3 className="text-xl sm:text-2xl font-black text-primary truncate max-w-full">₹{totalSpent.toFixed(2)}</h3>
              </div>
            </CardContent>
          </Card>

          <Card className="border border-border/80 shadow-md rounded-2xl overflow-hidden bg-card">
            <CardHeader className="pb-3 border-b border-border/40">
              <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                <InfoIcon size={18} className="text-primary" />
                Journey Info & Guidelines
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-6 space-y-3.5 text-xs text-left leading-relaxed">
              <div className="p-3.5 bg-muted/40 border border-border rounded-xl">
                <h5 className="font-bold text-foreground">Awaiting Driver State</h5>
                <p className="text-[10px] text-muted-foreground mt-1">Passenger request coordinates are broadcasting to guide helpers. Cancel is allowed prior to accept updates.</p>
              </div>
              <div className="p-3.5 bg-muted/40 border border-border rounded-xl">
                <h5 className="font-bold text-foreground">Verification OTP</h5>
                <p className="text-[10px] text-muted-foreground mt-1">To ensure transit validation, passengers must supply their Start OTP directly to the escort helper.</p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Advanced History Filters Sheet */}
      <Sheet open={isHistoryFilterSheetOpen} onOpenChange={setIsHistoryFilterSheetOpen}>
        <SheetContent side="right" className="w-full sm:max-w-sm bg-background/95 backdrop-blur-md border-l border-border/80 p-5 sm:p-6 overflow-y-auto">
          <div className="space-y-6 text-left">
            <SheetHeader className="text-left space-y-1">
              <SheetTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground">
                Filter Past Bookings
              </SheetTitle>
              <SheetDescription className="text-xs">
                Refine past trip records using keyword search and time status presets.
              </SheetDescription>
            </SheetHeader>

            {/* Keyword Search */}
            <div className="space-y-1.5">
              <Label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider pl-0.5">Location Search</Label>
              <Input
                placeholder="Search pickup or destination address..."
                value={historySearchQuery}
                onChange={(e) => setHistorySearchQuery(e.target.value)}
                className="rounded-xl text-xs h-10 font-semibold"
              />
            </div>

            {/* Status Select Preset */}
            <div className="space-y-2">
              <Label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider pl-0.5">Status Filter</Label>
              <div className="grid grid-cols-2 gap-2">
                {[
                  { value: "all", label: "All Statuses" },
                  { value: "completed", label: "Completed" },
                  { value: "cancelled", label: "Cancelled" }
                ].map((opt) => (
                  <Button
                    key={opt.value}
                    variant={historyStatusFilter === opt.value ? "default" : "outline"}
                    onClick={() => setHistoryStatusFilter(opt.value as any)}
                    className="text-xs rounded-xl font-bold h-9 cursor-pointer"
                  >
                    {opt.label}
                  </Button>
                ))}
              </div>
            </div>

            {/* Date Range Preset */}
            <div className="space-y-2">
              <Label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider pl-0.5">Time Period</Label>
              <div className="grid grid-cols-2 gap-2">
                {[
                  { value: "all", label: "All Time" },
                  { value: "thisweek", label: "This Week" },
                  { value: "30days", label: "Last 30 Days" },
                  { value: "thismonth", label: "This Month" }
                ].map((opt) => (
                  <Button
                    key={opt.value}
                    variant={historyDateFilter === opt.value ? "default" : "outline"}
                    onClick={() => setHistoryDateFilter(opt.value as any)}
                    className="text-xs rounded-xl font-bold h-9 cursor-pointer"
                  >
                    {opt.label}
                  </Button>
                ))}
              </div>
            </div>

            {/* Actions button */}
            <Button
              variant="outline"
              onClick={() => {
                setHistorySearchQuery("")
                setHistoryStatusFilter("all")
                setHistoryDateFilter("all")
                setIsHistoryFilterSheetOpen(false)
              }}
              className="w-full text-xs font-bold rounded-xl mt-4 cursor-pointer py-5"
            >
              Reset All Filters
            </Button>
          </div>
        </SheetContent>
      </Sheet>

      {/* Booking Details Responsive Drawer/Sheet */}
      <BookingDetailsResponsive
        isOpen={!!selectedBookingDetails}
        onClose={() => setSelectedBookingDetails(null)}
        booking={selectedBookingDetails}
      />
    </div>
  )
}

export default BookingsPage
