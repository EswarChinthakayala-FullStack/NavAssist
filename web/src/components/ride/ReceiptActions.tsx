import React, { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import {
  DownloadSimpleIcon,
  ClockAfternoonIcon,
  ShareNetworkIcon,
  HeadsetIcon
} from "@phosphor-icons/react"
import { bookingsService } from "@/services/bookings.service"
import { toast } from "sonner"
import { motion } from "framer-motion"

interface ReceiptActionsProps {
  bookingId: number
}

export function ReceiptActions({ bookingId }: ReceiptActionsProps) {
  const navigate = useNavigate()
  const [downloading, setDownloading] = useState(false)

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

  const handleShare = () => {
    const shareUrl = `${window.location.origin}/share/BK-${bookingId}`
    navigator.clipboard.writeText(shareUrl)
    toast.success("Shareable journey receipt link copied to clipboard!")
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6, ease: "easeOut", delay: 0.45 }}
      className="grid grid-cols-2 gap-2.5 w-full"
    >
      <Button
        variant="outline"
        onClick={handleDownloadInvoice}
        disabled={downloading}
        className="bg-card border-border hover:bg-muted text-card-foreground text-[10px] font-bold py-3 rounded-xl flex items-center justify-center gap-1.5 cursor-pointer h-11"
      >
        <DownloadSimpleIcon size={14} className="text-primary" />
        <span>{downloading ? "Loading..." : "Invoice"}</span>
      </Button>

      <Button
        variant="outline"
        onClick={handleShare}
        className="bg-card border-border hover:bg-muted text-card-foreground text-[10px] font-bold py-3 rounded-xl flex items-center justify-center gap-1.5 cursor-pointer h-11"
      >
        <ShareNetworkIcon size={14} className="text-primary" />
        <span>Share Ride</span>
      </Button>

      <Button
        variant="outline"
        onClick={() => navigate("/trips")}
        className="bg-card border-border hover:bg-muted text-card-foreground text-[10px] font-bold py-3 rounded-xl flex items-center justify-center gap-1.5 cursor-pointer h-11"
      >
        <ClockAfternoonIcon size={14} className="text-primary" />
        <span>Ride History</span>
      </Button>

      <Button
        variant="outline"
        onClick={() => navigate("/support")}
        className="bg-card border-border hover:bg-muted text-card-foreground text-[10px] font-bold py-3 rounded-xl flex items-center justify-center gap-1.5 cursor-pointer h-11"
      >
        <HeadsetIcon size={14} className="text-primary" />
        <span>Support</span>
      </Button>
    </motion.div>
  )
}

export default ReceiptActions
