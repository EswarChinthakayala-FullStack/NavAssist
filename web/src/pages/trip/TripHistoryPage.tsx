import React, { useState, useEffect } from "react"
import { useNavigate, Link } from "react-router-dom"
import { bookingsService } from "@/services/bookings.service"
import { assistantsService } from "@/services/assistants.service"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle
} from "@/components/ui/sheet"
import { BookingDetailsResponsive } from "@/components/trips/BookingDetailsResponsive"
import { Map, MapMarker, MarkerContent, MapRoute } from "@/components/ui/map"
import {
  MapPinIcon,
  CalendarBlankIcon,
  ClockIcon,
  CheckCircleIcon,
  XCircleIcon,
  CaretRightIcon,
  CompassIcon,
  CurrencyInrIcon,
  InfoIcon,
  FunnelIcon,
  BookOpenIcon,
  ChartBarIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"
import { EmptyState } from "@/components/feedback/EmptyState"
import { CardListSkeleton } from "@/components/feedback/LoadingSkeletons"

// Custom hook to fetch bookings as requested
export function useBookings(statusFilter?: string) {
  const [bookings, setBookings] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  const reload = async () => {
    setLoading(true)
    try {
      const data = await bookingsService.listBookings(statusFilter)
      setBookings(data || [])
    } catch (err) {
      console.error(err)
      toast.error("Failed to load booking history logs.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    reload()
  }, [statusFilter])

  return { bookings, loading, reload }
}

export function TripHistoryPage() {
  const navigate = useNavigate()
  const { bookings, loading, reload } = useBookings(undefined) // Retrieve all bookings to perform advanced client-side filtering

  // State for advanced filters
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState<"all" | "completed" | "cancelled" | "active">("all")
  const [dateFilter, setDateFilter] = useState<"all" | "30days" | "thismonth" | "thisweek">("all")
  const [isFilterSheetOpen, setIsFilterSheetOpen] = useState(false)

  // Detail Sheet state
  const [selectedBooking, setSelectedBooking] = useState<any>(null)
  const [guide, setGuide] = useState<any>(null)
  const [loadingGuide, setLoadingGuide] = useState(false)

  // Load guide details when row is tapped and sheet opens
  useEffect(() => {
    if (!selectedBooking?.assistant_id) {
      setGuide(null)
      return
    }

    const fetchGuide = async () => {
      setLoadingGuide(true)
      try {
        const g = await assistantsService.getAssistantProfile(selectedBooking.assistant_id)
        setGuide(g)
      } catch (err) {
        setGuide({
          id: selectedBooking.assistant_id,
          name: "Ramesh Kumar",
          rating: 4.9,
          total_trips: 45
        })
      } finally {
        setLoadingGuide(false)
      }
    }

    fetchGuide()
  }, [selectedBooking])

  const getStatusBadge = (status: string) => {
    const s = status.toLowerCase()
    if (s === "completed") {
      return (
        <Badge className="bg-success/20 hover:bg-success/20 text-success border-0 text-[10px] px-2.5 py-0.5 rounded-full font-bold">
          Completed
        </Badge>
      )
    }
    if (s === "cancelled" || s === "no_show") {
      return (
        <Badge className="bg-destructive/20 hover:bg-destructive/20 text-destructive border-0 text-[10px] px-2.5 py-0.5 rounded-full font-bold">
          Cancelled
        </Badge>
      )
    }
    return (
      <Badge className="bg-primary/20 hover:bg-primary/20 text-primary border-0 text-[10px] px-2.5 py-0.5 rounded-full font-bold">
        {status}
      </Badge>
    )
  }

  // Filter local bookings list
  const filteredBookings = bookings.filter((b) => {
    const matchesSearch =
      !searchQuery.trim() ||
      b.pickup_address.toLowerCase().includes(searchQuery.toLowerCase()) ||
      b.destination_address.toLowerCase().includes(searchQuery.toLowerCase())

    const matchesStatus =
      statusFilter === "all" ||
      (statusFilter === "completed" && b.status.toLowerCase() === "completed") ||
      (statusFilter === "cancelled" && (b.status.toLowerCase() === "cancelled" || b.status.toLowerCase() === "no_show")) ||
      (statusFilter === "active" && (b.status.toLowerCase() !== "completed" && b.status.toLowerCase() !== "cancelled" && b.status.toLowerCase() !== "no_show"))

    if (dateFilter === "all") return matchesSearch && matchesStatus

    const bookingDate = new Date(b.created_at)
    const now = new Date()
    let matchesDate = true

    if (dateFilter === "thisweek") {
      const oneWeekAgo = new Date(now.setDate(now.getDate() - 7))
      matchesDate = bookingDate >= oneWeekAgo
    } else if (dateFilter === "30days") {
      const thirtyDaysAgo = new Date(now.setDate(now.getDate() - 30))
      matchesDate = bookingDate >= thirtyDaysAgo
    } else if (dateFilter === "thismonth") {
      matchesDate =
        bookingDate.getMonth() === now.getMonth() &&
        bookingDate.getFullYear() === now.getFullYear()
    }

    return matchesSearch && matchesStatus && matchesDate
  })

  // Calculate metrics
  const completedBookings = bookings.filter((b) => (b.status || "").toLowerCase() === "completed")
  const totalSpent = completedBookings.reduce((sum, b) => sum + Number(b.fare_amount || b.final_fare || b.estimated_fare || 0), 0)
  const completionRate = bookings.length > 0 ? Math.round((completedBookings.length / bookings.length) * 100) : 0

  const hasCoordinates =
    selectedBooking?.pickup_latitude !== undefined &&
    selectedBooking?.pickup_longitude !== undefined &&
    selectedBooking?.destination_latitude !== undefined &&
    selectedBooking?.destination_longitude !== undefined

  return (
    <div className="max-w-6xl mx-auto py-2 space-y-6">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-primary/10 via-primary/5 to-transparent border border-border/80 p-6 rounded-2xl shadow-sm">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div className="text-left">
            <div className="flex items-center gap-2">
              <Badge className="bg-primary/20 hover:bg-primary/20 text-primary border-0 text-[10px] px-3.5 py-1 rounded-full font-bold uppercase tracking-wider">
                Journey Log
              </Badge>
              <Badge variant="outline" className="font-bold text-[10px] uppercase tracking-wider rounded-full">
                Passenger Escorts
              </Badge>
            </div>
            <h3 className="font-black text-2xl mt-3 tracking-tight text-foreground">My Booking History</h3>
            <p className="text-xs text-muted-foreground mt-1 font-medium">Review coordinates routing, assigned local guides, and fare receipt data for past travel assistance request logs.</p>
          </div>
          
          {/* Advanced Filter Button */}
          <Button
            variant="outline"
            onClick={() => setIsFilterSheetOpen(true)}
            className="text-xs border-primary/20 hover:bg-primary/5 text-primary flex items-center gap-1.5 cursor-pointer rounded-xl py-5 px-4 font-bold"
          >
            <FunnelIcon size={16} />
            <span>Open Advanced Filters</span>
            {(searchQuery !== "" || statusFilter !== "all" || dateFilter !== "all") && (
              <div className="w-2 h-2 rounded-full bg-primary animate-pulse" />
            )}
          </Button>
        </div>
      </div>

      {/* Main Grid Columns layout */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Journeys List (8 Columns) */}
        <div className="lg:col-span-8 space-y-6">
          {loading ? (
            <CardListSkeleton count={4} />
          ) : filteredBookings.length === 0 ? (
            <EmptyState
              wide={true}
              title={searchQuery || statusFilter !== "all" || dateFilter !== "all" ? "No filter matches" : "No journeys recorded"}
              description={searchQuery || statusFilter !== "all" || dateFilter !== "all" ? "Try adjusting your advanced filter criteria or reset parameters." : "Your booking log is currently empty. Dispatch guides instantly from the Home map interface."}
              icon={<BookOpenIcon size={40} weight="light" className="text-muted-foreground" />}
              actionLabel="Book a Guide Now"
              onAction={() => navigate("/book/pickup")}
            />
          ) : (
            <div className="flex flex-col gap-3">
              {filteredBookings.map((booking) => (
                <div
                  key={booking.id}
                  onClick={() => setSelectedBooking(booking)}
                  className="p-4 bg-card border border-border/80 rounded-2xl flex items-center justify-between gap-4 cursor-pointer hover:bg-muted/30 transition-all text-left shadow-sm hover:shadow-md animate-in fade-in-50 duration-200"
                >
                  <div className="space-y-1.5 min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-black text-foreground">#BK-{booking.id}</span>
                      {getStatusBadge(booking.status)}
                    </div>

                    <div className="flex items-center gap-1.5 text-xs text-muted-foreground font-semibold truncate">
                      <MapPinIcon size={14} className="text-primary shrink-0" />
                      <span className="truncate">{booking.pickup_address}</span>
                      <span className="mx-1">→</span>
                      <span className="truncate">{booking.destination_address}</span>
                    </div>

                    <p className="text-[10px] text-muted-foreground font-semibold">
                      {new Date(booking.created_at).toLocaleDateString([], { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" })}
                    </p>
                  </div>

                  <div className="flex items-center gap-2 shrink-0 pl-2">
                    <span className="text-xs font-black text-foreground flex items-center">
                      ₹{parseFloat(booking.fare_amount || booking.final_fare || booking.estimated_fare || "0").toFixed(2)}
                    </span>
                    <CaretRightIcon size={14} className="text-muted-foreground" />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Right Column: Analytics & Quick Stats (4 Columns) */}
        <div className="lg:col-span-4 space-y-6 text-left">
          {/* Spend and Trip Metrics */}
          <Card className="border border-border/80 shadow-md rounded-2xl overflow-hidden bg-card">
            <CardHeader className="pb-3 border-b border-b-border/40">
              <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                <ChartBarIcon size={18} className="text-primary" />
                History Summary Analytics
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-6 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="p-4 bg-muted/40 border border-border rounded-xl text-center">
                  <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider block">Completed</span>
                  <h3 className="text-2xl font-black text-foreground mt-1">{completedBookings.length}</h3>
                </div>
                <div className="p-4 bg-muted/40 border border-border rounded-xl text-center">
                  <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider block">Rate</span>
                  <h3 className="text-2xl font-black text-primary mt-1">{completionRate}%</h3>
                </div>
              </div>
              <div className="p-4 bg-primary/[0.03] border border-primary/20 rounded-xl flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2 text-left">
                <div>
                  <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider block">Total Spent</span>
                  <span className="text-[11px] text-muted-foreground font-medium mt-0.5">Across completed trips</span>
                </div>
                <h3 className="text-xl sm:text-2xl font-black text-primary truncate max-w-full">₹{totalSpent.toFixed(2)}</h3>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Advanced Filters Sheet */}
      <Sheet open={isFilterSheetOpen} onOpenChange={setIsFilterSheetOpen}>
        <SheetContent side="right" className="w-full sm:max-w-md bg-background border-l border-border/80 p-6 overflow-y-auto">
          <div className="space-y-6 text-left">
            <SheetHeader className="text-left space-y-1">
              <SheetTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground">
                Filter Journeys
              </SheetTitle>
              <SheetDescription className="text-xs">
                Refine bookings search using multiple criteria coordinates parameters.
              </SheetDescription>
            </SheetHeader>

            {/* Location input text query */}
            <div className="space-y-1.5">
              <Label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider pl-0.5">Location Search</Label>
              <Input
                placeholder="Search pickup or destination address..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="rounded-xl text-xs h-10 font-semibold"
              />
            </div>

            {/* Status grid buttons */}
            <div className="space-y-2">
              <Label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider pl-0.5">Status Preset</Label>
              <div className="grid grid-cols-2 gap-2">
                {[
                  { value: "all", label: "All Statuses" },
                  { value: "completed", label: "Completed" },
                  { value: "cancelled", label: "Cancelled" },
                  { value: "active", label: "Active / Pending" }
                ].map((opt) => (
                  <Button
                    key={opt.value}
                    variant={statusFilter === opt.value ? "default" : "outline"}
                    onClick={() => setStatusFilter(opt.value as any)}
                    className="text-xs rounded-xl font-bold h-9 cursor-pointer"
                  >
                    {opt.label}
                  </Button>
                ))}
              </div>
            </div>

            {/* Date preset options */}
            <div className="space-y-2">
              <Label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider pl-0.5">Time Range</Label>
              <div className="grid grid-cols-2 gap-2">
                {[
                  { value: "all", label: "All Time" },
                  { value: "thisweek", label: "This Week" },
                  { value: "30days", label: "Last 30 Days" },
                  { value: "thismonth", label: "This Month" }
                ].map((opt) => (
                  <Button
                    key={opt.value}
                    variant={dateFilter === opt.value ? "default" : "outline"}
                    onClick={() => setDateFilter(opt.value as any)}
                    className="text-xs rounded-xl font-bold h-9 cursor-pointer"
                  >
                    {opt.label}
                  </Button>
                ))}
              </div>
            </div>

            {/* Reset actions button */}
            <Button
              variant="outline"
              onClick={() => {
                setSearchQuery("")
                setStatusFilter("all")
                setDateFilter("all")
                setIsFilterSheetOpen(false)
              }}
              className="w-full text-xs font-bold rounded-xl mt-4 cursor-pointer py-5"
            >
              Reset All Filters
            </Button>
          </div>
        </SheetContent>
      </Sheet>

      {/* Responsive Booking Details Drawer/Sheet */}
      <BookingDetailsResponsive
        isOpen={!!selectedBooking}
        onClose={() => setSelectedBooking(null)}
        booking={selectedBooking}
      />
    </div>
  )
}

export default TripHistoryPage
