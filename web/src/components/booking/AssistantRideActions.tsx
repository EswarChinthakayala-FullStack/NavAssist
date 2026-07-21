import React, { useState } from "react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { FlagIcon, CheckCircleIcon, PhoneIcon, WarningOctagonIcon } from "@phosphor-icons/react"
import { toast } from "sonner"
import { api } from "@/services/api"

interface AssistantRideActionsProps {
  bookingId: number
  status: string
  passengerPhone?: string
  onStatusChange?: () => void
  className?: string
}

export function AssistantRideActions({
  bookingId,
  status,
  passengerPhone,
  onStatusChange,
  className,
}: AssistantRideActionsProps) {
  const [loading, setLoading] = useState(false)
  const norm = (status || "PENDING").toUpperCase()

  const handleArrived = async () => {
    setLoading(true)
    try {
      await api.patch(`/bookings/${bookingId}/status`, {
        status: "ARRIVED_PICKUP",
      })
      toast.success("Marked as Arrived at Pickup Location!")
      if (onStatusChange) onStatusChange()
    } catch (err: any) {
      toast.error(err.response?.data?.detail || "Failed to update status to Arrived.")
    } finally {
      setLoading(false)
    }
  }

  const handleComplete = async () => {
    setLoading(true)
    try {
      await api.patch(`/bookings/${bookingId}/status`, {
        status: "COMPLETED",
      })
      toast.success("Guidance ride completed successfully!")
      if (onStatusChange) onStatusChange()
    } catch (err: any) {
      toast.error(err.response?.data?.detail || "Failed to complete ride.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card className={`rounded-2xl border border-border/80 bg-card p-4 space-y-3 shadow-sm text-left ${className}`}>
      <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest block">
        Assistant Ride Controls
      </span>

      <div className="space-y-2.5">
        {(norm === "ACCEPTED" || norm === "ASSIGNED" || norm === "ASSISTANT_ENROUTE") && (
          <Button
            onClick={handleArrived}
            disabled={loading}
            className="w-full bg-purple-600 hover:bg-purple-700 text-white font-extrabold text-xs py-4 rounded-xl flex items-center justify-center gap-2 shadow-md cursor-pointer"
          >
            <FlagIcon size={18} weight="bold" />
            <span>{loading ? "Updating..." : "I Have Arrived at Pickup"}</span>
          </Button>
        )}

        {(norm === "STARTED" || norm === "IN_PROGRESS" || norm === "GUEST_PICKED_UP") && (
          <Button
            onClick={handleComplete}
            disabled={loading}
            className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold text-xs py-4 rounded-xl flex items-center justify-center gap-2 shadow-md cursor-pointer"
          >
            <CheckCircleIcon size={18} weight="bold" />
            <span>{loading ? "Updating..." : "Complete Guidance Ride"}</span>
          </Button>
        )}
      </div>
    </Card>
  )
}
export default AssistantRideActions
