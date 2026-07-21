import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import {
  DownloadSimpleIcon,
  WarningOctagonIcon,
  HeadsetIcon,
  ArrowClockwiseIcon,
  NavigationArrowIcon,
  StarIcon,
  CheckCircleIcon,
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { bookingsService } from "@/services/bookings.service"
import { ratingsService } from "@/services/ratings.service"
import { RateAssistantDialog } from "@/components/booking/RateAssistantDialog"
import { ReportRideDialog } from "./ReportRideDialog"

interface BookingActionsProps {
  bookingId: number
  status: string
  onClose?: () => void
}

export function BookingActions({ bookingId, status, onClose }: BookingActionsProps) {
  const navigate = useNavigate()
  const [downloading, setDownloading] = useState(false)
  const [showRatingDialog, setShowRatingDialog] = useState(false)
  const [showReportDialog, setShowReportDialog] = useState(false)
  const [hasRated, setHasRated] = useState(false)
  const [userStars, setUserStars] = useState<number | null>(null)

  const normalized = (status || "PENDING").toUpperCase()
  const isActive = ["PENDING", "ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE", "ARRIVED_PICKUP", "STARTED", "IN_PROGRESS"].includes(normalized)

  useEffect(() => {
    if (normalized !== "COMPLETED" || !bookingId) return

    let isMounted = true
    const checkRatingStatus = async () => {
      try {
        const res = await ratingsService.getBookingRating(bookingId)
        if (isMounted && res?.has_rated) {
          setHasRated(true)
          setUserStars(res.rating?.stars || null)
        }
      } catch (err) {
        console.error("Failed to check rating status:", err)
      }
    }

    checkRatingStatus()
    return () => {
      isMounted = false
    }
  }, [bookingId, normalized])

  const handleDownloadInvoice = async () => {
    setDownloading(true)
    try {
      const response = await bookingsService.downloadInvoice(bookingId)
      
      // Determine file name from Content-Disposition header if possible
      const contentDisposition = response.headers?.["content-disposition"]
      let filename = `Invoice_INV-${bookingId}.pdf`
      if (contentDisposition) {
        const match = contentDisposition.match(/filename="(.+?)"/)
        if (match && match[1]) {
          filename = match[1]
        }
      }

      // Convert file blob and trigger browser download
      const blob = new Blob([response.data], { type: "application/pdf" })
      const url = URL.createObjectURL(blob)
      const a = document.createElement("a")
      a.href = url
      a.download = filename
      a.click()
      URL.revokeObjectURL(url)
      
      toast.success("Invoice PDF downloaded successfully!")
    } catch (err: any) {
      console.error("Receipt error during download:", err)
    } finally {
      setDownloading(false)
    }
  }

  const handleReportIssue = () => {
    setShowReportDialog(true)
  }

  const handleContactSupport = () => {
    navigate("/support")
    if (onClose) onClose()
  }

  const handleBookAgain = () => {
    if (onClose) onClose()
    navigate("/book/pickup")
  }

  const handleTrackLive = () => {
    if (onClose) onClose()
    const enrouteStatuses = ["PENDING", "ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE", "ARRIVED_PICKUP", "EN_ROUTE"]
    if (enrouteStatuses.includes(normalized)) {
      navigate(`/trip/${bookingId}/enroute`)
    } else {
      navigate(`/trip/${bookingId}/tracking`)
    }
  }

  return (
    <div className="p-4 space-y-2.5">
      {isActive ? (
        <Button
          onClick={handleTrackLive}
          className="w-full bg-primary text-primary-foreground font-extrabold text-xs py-5 rounded-xl flex items-center justify-center gap-2 shadow-lg hover:scale-[1.01] transition-all cursor-pointer"
        >
          <NavigationArrowIcon size={18} weight="bold" />
          <span>Track Live Journey</span>
        </Button>
      ) : (
        <div className="grid grid-cols-2 gap-2">
          {normalized === "COMPLETED" && (
            hasRated ? (
              <Button
                disabled
                className="w-full bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30 font-extrabold text-xs py-5 rounded-xl flex items-center justify-center gap-1.5 opacity-90 cursor-not-allowed"
              >
                <CheckCircleIcon size={16} weight="fill" />
                <span>Rated {userStars ? `(${userStars} ★)` : ""}</span>
              </Button>
            ) : (
              <Button
                onClick={() => setShowRatingDialog(true)}
                className="w-full bg-amber-500 hover:bg-amber-600 text-white font-extrabold text-xs py-5 rounded-xl flex items-center justify-center gap-1.5 shadow-md hover:scale-[1.01] transition-all cursor-pointer"
              >
                <StarIcon size={16} weight="fill" />
                <span>Rate Assistant</span>
              </Button>
            )
          )}
          <Button
            onClick={handleBookAgain}
            className={`w-full bg-primary text-primary-foreground font-extrabold text-xs py-5 rounded-xl flex items-center justify-center gap-1.5 shadow-md hover:scale-[1.01] transition-all cursor-pointer ${
              normalized !== "COMPLETED" ? "col-span-2" : ""
            }`}
          >
            <ArrowClockwiseIcon size={16} weight="bold" />
            <span>Book Again</span>
          </Button>
        </div>
      )}

      <div className="grid grid-cols-3 gap-2">
        <Button
          variant="outline"
          onClick={handleDownloadInvoice}
          disabled={downloading}
          className="text-[11px] font-bold py-3 rounded-xl flex flex-col items-center gap-1 cursor-pointer border-border/80 hover:bg-muted h-auto"
        >
          <DownloadSimpleIcon size={16} className="text-primary" />
          <span>{downloading ? "Loading..." : "Invoice"}</span>
        </Button>

        <Button
          variant="outline"
          onClick={handleReportIssue}
          className="text-[11px] font-bold py-3 rounded-xl flex flex-col items-center gap-1 cursor-pointer border-border/80 hover:bg-muted h-auto"
        >
          <WarningOctagonIcon size={16} className="text-amber-500" />
          <span>Report</span>
        </Button>

        <Button
          variant="outline"
          onClick={handleContactSupport}
          className="text-[11px] font-bold py-3 rounded-xl flex flex-col items-center gap-1 cursor-pointer border-border/80 hover:bg-muted h-auto"
        >
          <HeadsetIcon size={16} className="text-primary" />
          <span>Support</span>
        </Button>
      </div>

      <RateAssistantDialog
        open={showRatingDialog}
        onOpenChange={setShowRatingDialog}
        bookingId={bookingId}
        onSuccess={() => {
          setHasRated(true)
        }}
      />

      <ReportRideDialog
        open={showReportDialog}
        onOpenChange={setShowReportDialog}
        bookingId={bookingId}
      />
    </div>
  )
}
export default BookingActions
