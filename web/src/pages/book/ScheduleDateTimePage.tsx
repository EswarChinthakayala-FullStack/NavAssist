import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useBookingDraftStore } from "@/store/booking-draft.store"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import {
  ClockIcon,
  CalendarIcon,
  ArrowLeftIcon,
  ArrowRightIcon,
  TrainIcon,
  AirplaneIcon,
  MapPinIcon,
  InfoIcon,
  LightningIcon,
  CaretDownIcon,
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion, AnimatePresence } from "framer-motion"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from "@/components/ui/select"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Calendar } from "@/components/ui/calendar"

export function ScheduleDateTimePage() {
  const navigate = useNavigate()
  const { pickup, destination, schedule, setSchedule } = useBookingDraftStore()

  useEffect(() => {
    if (!pickup || !destination) {
      toast.error("Please define pickup and destination points first.")
      navigate("/book/pickup")
    }
  }, [pickup, destination, navigate])

  const [activeTab, setActiveTab] = useState<"now" | "later">(schedule ? "later" : "now")

  // Shadcn Calendar date state & time state
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(() => {
    if (schedule) {
      const d = new Date(schedule)
      if (!isNaN(d.getTime())) return d
    }
    const now = new Date()
    now.setHours(now.getHours() + 1)
    return now
  })

  const [selectedTime, setSelectedTime] = useState<string>(() => {
    if (schedule) {
      const d = new Date(schedule)
      if (!isNaN(d.getTime())) {
        const hh = String(d.getHours()).padStart(2, "0")
        const mm = String(d.getMinutes()).padStart(2, "0")
        return `${hh}:${mm}`
      }
    }
    const now = new Date()
    now.setHours(now.getHours() + 1)
    const hh = String(now.getHours()).padStart(2, "0")
    const mm = String(now.getMinutes()).padStart(2, "0")
    return `${hh}:${mm}`
  })

  // Transit schedule details
  const [transitType, setTransitType] = useState<"flight" | "train" | "bus" | "custom">("custom")
  const [transitNumber, setTransitNumber] = useState("")
  const [transitSpot, setTransitSpot] = useState("")
  const [transitNotes, setTransitNotes] = useState("")

  // Load existing details if back navigated
  useEffect(() => {
    const cached = sessionStorage.getItem("booking_transit_details")
    if (cached) {
      try {
        const parsed = JSON.parse(cached)
        setTransitType(parsed.transitType || "custom")
        setTransitNumber(parsed.transitNumber || "")
        setTransitSpot(parsed.transitSpot || "")
        setTransitNotes(parsed.transitNotes || "")
      } catch (err) {
        console.error("Failed to parse cached transit details:", err)
      }
    }
  }, [])

  const getCombinedISO = () => {
    if (!selectedDate) return null
    const year = selectedDate.getFullYear()
    const month = String(selectedDate.getMonth() + 1).padStart(2, "0")
    const day = String(selectedDate.getDate()).padStart(2, "0")
    const [hh, mm] = (selectedTime || "12:00").split(":")
    return `${year}-${month}-${day}T${hh || "12"}:${mm || "00"}`
  }

  const handleContinue = () => {
    if (activeTab === "now") {
      setSchedule(null) // immediate
      sessionStorage.removeItem("booking_transit_details")
    } else {
      const isoStr = getCombinedISO()
      if (!isoStr) {
        toast.error("Please pick a valid date and time for your scheduled assistance.")
        return
      }
      if (new Date(isoStr) <= new Date()) {
        toast.error("Please select a date and time in the future.")
        return
      }
      setSchedule(isoStr)

      sessionStorage.setItem("booking_transit_details", JSON.stringify({
        transitType,
        transitNumber,
        transitSpot,
        transitNotes
      }))
    }
    navigate("/book/assistants")
  }

  return (
    <motion.div
      initial={{ x: 30, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      exit={{ x: -30, opacity: 0 }}
      transition={{ duration: 0.25 }}
      className="w-full max-w-5xl mx-auto"
    >
      <Card className="border border-border/80 shadow-xl backdrop-blur-xl rounded-3xl overflow-hidden bg-card/95 text-left">
        {/* Header */}
        <CardHeader className="p-5 sm:p-6 bg-gradient-to-b from-muted/30 to-transparent border-b border-border/40">
          <CardTitle className="text-lg sm:text-xl font-black flex items-center gap-2.5 text-foreground">
            <div className="p-2 rounded-xl bg-primary/10 text-primary shrink-0">
              <ClockIcon size={22} weight="bold" />
            </div>
            <span>Set Time & Schedule</span>
          </CardTitle>
          <CardDescription className="text-xs text-muted-foreground mt-1">
            Choose whether to start your trip immediately or book an assistant for a future scheduled arrival.
          </CardDescription>
        </CardHeader>

        <CardContent className="p-5 sm:p-6 space-y-6">
          {/* Tabs Pill Selector */}
          <div className="grid grid-cols-2 bg-muted/40 p-1.5 rounded-2xl border border-border/60 max-w-md">
            <button
              type="button"
              onClick={() => setActiveTab("now")}
              className={`py-2.5 sm:py-3 rounded-xl font-extrabold text-xs transition-all duration-300 flex items-center justify-center gap-2 cursor-pointer ${
                activeTab === "now"
                  ? "bg-card text-foreground shadow-md ring-1 ring-border"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              <LightningIcon size={16} weight={activeTab === "now" ? "fill" : "bold"} className={activeTab === "now" ? "text-primary" : ""} />
              <span>Now (Immediate)</span>
            </button>

            <button
              type="button"
              onClick={() => setActiveTab("later")}
              className={`py-2.5 sm:py-3 rounded-xl font-extrabold text-xs transition-all duration-300 flex items-center justify-center gap-2 cursor-pointer ${
                activeTab === "later"
                  ? "bg-card text-foreground shadow-md ring-1 ring-border"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              <CalendarIcon size={16} weight={activeTab === "later" ? "fill" : "bold"} className={activeTab === "later" ? "text-primary" : ""} />
              <span>Schedule Later</span>
            </button>
          </div>

          {/* Tab Content Animated */}
          <AnimatePresence mode="wait">
            {activeTab === "later" ? (
              <motion.div
                key="later-tab"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.2 }}
                className="space-y-4 pt-1"
              >
                {/* Row 1: Date & Time + Transit Category */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-5">
                  {/* Shadcn UI Calendar Date & Time Picker */}
                  <div className="space-y-1.5">
                    <Label
                      htmlFor="schedule-date"
                      className="text-[11px] font-extrabold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5"
                    >
                      <CalendarIcon size={14} className="text-primary shrink-0" />
                      <span>Select Date and Time</span>
                    </Label>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                      <Popover>
                        <PopoverTrigger
                          type="button"
                          className="w-full h-12 rounded-xl border border-border/80 bg-background/50 dark:bg-card/60 px-3.5 text-xs font-semibold shadow-xs hover:border-primary/50 focus:ring-1 focus:ring-primary flex items-center justify-between cursor-pointer transition-all"
                        >
                          <span className="flex items-center gap-2 truncate">
                            <CalendarIcon size={16} className="text-primary shrink-0" />
                            <span>
                              {selectedDate
                                ? selectedDate.toLocaleDateString("en-GB", {
                                    day: "2-digit",
                                    month: "short",
                                    year: "numeric",
                                  })
                                : "Pick a date"}
                            </span>
                          </span>
                          <CaretDownIcon size={14} className="text-muted-foreground shrink-0" />
                        </PopoverTrigger>
                        <PopoverContent align="start" className="w-auto p-0 z-50 bg-popover border border-border/80 shadow-2xl rounded-2xl">
                          <Calendar
                            mode="single"
                            selected={selectedDate}
                            onSelect={(d) => d && setSelectedDate(d)}
                            disabled={(date) => date < new Date(new Date().setHours(0, 0, 0, 0))}
                            className="rounded-2xl border-0 p-3"
                          />
                        </PopoverContent>
                      </Popover>

                      <div className="relative">
                        <Input
                          type="time"
                          value={selectedTime}
                          onChange={(e) => setSelectedTime(e.target.value)}
                          className="w-full h-12 rounded-xl border border-border/80 bg-background/50 dark:bg-card/60 px-3.5 text-xs font-semibold shadow-xs focus-visible:ring-1 focus-visible:ring-primary transition-all cursor-pointer"
                        />
                      </div>
                    </div>
                  </div>

                  {/* Transit Category Dropdown */}
                  <div className="space-y-1.5">
                    <Label
                      htmlFor="transit-type"
                      className="text-[11px] font-extrabold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5"
                    >
                      <TrainIcon size={14} className="text-primary shrink-0" />
                      <span>Transit Category</span>
                    </Label>
                    <Select value={transitType} onValueChange={(val) => setTransitType((val || "custom") as any)}>
                      <SelectTrigger
                        id="transit-type"
                        className="w-full h-12 rounded-xl border border-border/80 bg-background/50 dark:bg-card/60 px-3.5 text-xs font-semibold shadow-xs focus:ring-1 focus:ring-primary flex items-center justify-between cursor-pointer"
                      >
                        <SelectValue placeholder="Select Category" />
                      </SelectTrigger>
                      <SelectContent className="bg-popover border border-border/80 rounded-xl shadow-xl z-50">
                        <SelectItem value="custom">📍 Custom / Local Meeting</SelectItem>
                        <SelectItem value="flight">✈️ Flight Arrival</SelectItem>
                        <SelectItem value="train">🚄 Train Arrival</SelectItem>
                        <SelectItem value="bus">🚌 Bus / Coach Arrival</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                {/* Row 2: Flight/Train Code + Terminal/Gate */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-5">
                  <div className="space-y-1.5">
                    <Label
                      htmlFor="transit-number"
                      className="text-[11px] font-extrabold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5"
                    >
                      <AirplaneIcon size={14} className="text-primary shrink-0" />
                      <span>Flight / Train Code (Optional)</span>
                    </Label>
                    <Input
                      id="transit-number"
                      value={transitNumber}
                      onChange={(e) => setTransitNumber(e.target.value)}
                      placeholder="e.g. AI-102 or 12626 Express"
                      className="w-full h-12 rounded-xl border border-border/80 bg-background/50 dark:bg-card/60 px-3.5 text-xs font-semibold shadow-xs focus-visible:ring-1 focus-visible:ring-primary transition-all"
                    />
                  </div>

                  <div className="space-y-1.5">
                    <Label
                      htmlFor="transit-spot"
                      className="text-[11px] font-extrabold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5"
                    >
                      <MapPinIcon size={14} className="text-primary shrink-0" />
                      <span>Arrival Terminal / Platform / Gate (Optional)</span>
                    </Label>
                    <Input
                      id="transit-spot"
                      value={transitSpot}
                      onChange={(e) => setTransitSpot(e.target.value)}
                      placeholder="e.g. Terminal 3 Gate 4 or Platform 8"
                      className="w-full h-12 rounded-xl border border-border/80 bg-background/50 dark:bg-card/60 px-3.5 text-xs font-semibold shadow-xs focus-visible:ring-1 focus-visible:ring-primary transition-all"
                    />
                  </div>
                </div>

                {/* Row 3: Special Instructions */}
                <div className="space-y-1.5">
                  <Label
                    htmlFor="transit-notes"
                    className="text-[11px] font-extrabold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5"
                  >
                    <InfoIcon size={14} className="text-primary shrink-0" />
                    <span>Special Instructions / Meeting Notes for Guide (Optional)</span>
                  </Label>
                  <Input
                    id="transit-notes"
                    value={transitNotes}
                    onChange={(e) => setTransitNotes(e.target.value)}
                    placeholder="e.g. 'Wait near exit gate 3B.'"
                    className="w-full h-12 rounded-xl border border-border/80 bg-background/50 dark:bg-card/60 px-3.5 text-xs font-semibold shadow-xs focus-visible:ring-1 focus-visible:ring-primary transition-all"
                  />
                </div>

                <p className="text-[11px] text-muted-foreground/80 leading-normal pt-1">
                  Providing arrival details lets guides monitor delayed arrivals automatically. We will notify you when your helper sets off.
                </p>
              </motion.div>
            ) : (
              <motion.div
                key="now-tab"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.2 }}
                className="p-5 border border-dashed border-border/80 rounded-2xl bg-muted/10 space-y-2 select-none text-left"
              >
                <h4 className="font-extrabold text-xs uppercase tracking-wider text-primary flex items-center gap-2">
                  <LightningIcon size={16} weight="fill" className="text-primary" />
                  Immediate Dispatch Active
                </h4>
                <p className="text-xs text-muted-foreground leading-relaxed">
                  Choosing immediate dispatch will match available verified escort guides checked-in near your pickup coordinate. Once assigned, your escort assistant will begin traveling to meet you immediately.
                </p>
              </motion.div>
            )}
          </AnimatePresence>
        </CardContent>

        {/* Footer Actions */}
        <div className="p-5 sm:p-6 border-t border-border/60 bg-muted/10 flex flex-col sm:flex-row gap-3">
          <Button
            type="button"
            variant="outline"
            onClick={() => navigate("/book/destination")}
            className="w-full sm:w-1/3 h-12 rounded-xl font-bold text-xs flex items-center justify-center gap-2 hover:bg-accent cursor-pointer border-border/80 shrink-0"
          >
            <ArrowLeftIcon size={16} weight="bold" />
            <span>Back</span>
          </Button>

          <Button
            type="button"
            onClick={handleContinue}
            className="w-full sm:w-2/3 h-12 rounded-xl font-black bg-primary text-primary-foreground text-xs shadow-lg hover:scale-[1.01] transition-all cursor-pointer flex items-center justify-center gap-2"
          >
            <span>Choose Assistant</span>
            <ArrowRightIcon size={16} weight="bold" />
          </Button>
        </div>
      </Card>
    </motion.div>
  )
}
export default ScheduleDateTimePage
