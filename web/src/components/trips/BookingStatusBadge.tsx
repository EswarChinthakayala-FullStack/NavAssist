import React from "react"
import { Badge } from "@/components/ui/badge"
import {
  ClockIcon,
  CheckCircleIcon,
  XCircleIcon,
  NavigationArrowIcon,
  UserCheckIcon,
  FlagIcon,
} from "@phosphor-icons/react"

interface BookingStatusBadgeProps {
  status: string
  className?: string
}

export function BookingStatusBadge({ status, className }: BookingStatusBadgeProps) {
  const normalized = (status || "PENDING").toUpperCase()

  switch (normalized) {
    case "PENDING":
    case "SEARCHING":
      return (
        <Badge className={`bg-amber-500/15 text-amber-500 hover:bg-amber-500/25 border border-amber-500/30 rounded-full font-bold px-2.5 py-1 text-[10px] uppercase tracking-wider flex items-center gap-1.5 ${className}`}>
          <ClockIcon size={13} weight="bold" className="animate-spin" />
          <span>Awaiting Guide</span>
        </Badge>
      )
    case "ACCEPTED":
    case "ASSIGNED":
    case "ASSISTANT_ENROUTE":
      return (
        <Badge className={`bg-blue-500/15 text-blue-500 hover:bg-blue-500/25 border border-blue-500/30 rounded-full font-bold px-2.5 py-1 text-[10px] uppercase tracking-wider flex items-center gap-1.5 ${className}`}>
          <UserCheckIcon size={13} weight="bold" />
          <span>Guide Assigned</span>
        </Badge>
      )
    case "ARRIVED_PICKUP":
      return (
        <Badge className={`bg-purple-500/15 text-purple-500 hover:bg-purple-500/25 border border-purple-500/30 rounded-full font-bold px-2.5 py-1 text-[10px] uppercase tracking-wider flex items-center gap-1.5 ${className}`}>
          <FlagIcon size={13} weight="bold" />
          <span>Guide Arrived</span>
        </Badge>
      )
    case "STARTED":
    case "IN_PROGRESS":
    case "GUEST_PICKED_UP":
      return (
        <Badge className={`bg-emerald-500 text-white border-0 rounded-full font-bold px-2.5 py-1 text-[10px] uppercase tracking-wider flex items-center gap-1.5 animate-pulse ${className}`}>
          <NavigationArrowIcon size={13} weight="bold" />
          <span>En Route</span>
        </Badge>
      )
    case "COMPLETED":
      return (
        <Badge className={`bg-emerald-500/15 text-emerald-500 hover:bg-emerald-500/25 border border-emerald-500/30 rounded-full font-bold px-2.5 py-1 text-[10px] uppercase tracking-wider flex items-center gap-1.5 ${className}`}>
          <CheckCircleIcon size={13} weight="bold" />
          <span>Completed</span>
        </Badge>
      )
    case "CANCELLED":
    case "EXPIRED":
      return (
        <Badge className={`bg-rose-500/15 text-rose-500 hover:bg-rose-500/25 border border-rose-500/30 rounded-full font-bold px-2.5 py-1 text-[10px] uppercase tracking-wider flex items-center gap-1.5 ${className}`}>
          <XCircleIcon size={13} weight="bold" />
          <span>Cancelled</span>
        </Badge>
      )
    default:
      return (
        <Badge variant="outline" className={`rounded-full font-bold text-[10px] uppercase ${className}`}>
          {status}
        </Badge>
      )
  }
}
export default BookingStatusBadge
