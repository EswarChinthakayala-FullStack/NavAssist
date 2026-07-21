import React from "react"
import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"

type StatusType = "booking" | "payment" | "kyc"

interface StatusBadgeProps {
  status: string
  type?: StatusType
  className?: string
}

export function StatusBadge({ status, type = "booking", className }: StatusBadgeProps) {
  const normalizedStatus = status.toUpperCase()

  const getStyleAndLabel = () => {
    switch (type) {
      case "booking":
        switch (normalizedStatus) {
          case "PENDING":
            return {
              bg: "bg-warning/10 text-warning border-warning/20",
              label: "Matching Helper"
            }
          case "ACCEPTED":
            return {
              bg: "bg-primary/10 text-primary border-primary/20",
              label: "Helper Assigned"
            }
          case "STARTED":
            return {
              bg: "bg-success/10 text-success border-success/20 animate-pulse",
              label: "Escort Enroute"
            }
          case "COMPLETED":
            return {
              bg: "bg-success/15 text-success border-success/30",
              label: "Completed Safely"
            }
          case "CANCELLED":
            return {
              bg: "bg-destructive/10 text-destructive border-destructive/20",
              label: "Cancelled"
            }
          default:
            return { bg: "bg-muted text-muted-foreground", label: status }
        }

      case "kyc":
        switch (normalizedStatus) {
          case "PENDING":
          case "PENDING_KYC":
            return {
              bg: "bg-warning/10 text-warning border-warning/20",
              label: "Under Review"
            }
          case "APPROVED":
            return {
              bg: "bg-success/15 text-success border-success/30",
              label: "Verified Guide"
            }
          case "REJECTED":
            return {
              bg: "bg-destructive/10 text-destructive border-destructive/20",
              label: "Rejected"
            }
          default:
            return { bg: "bg-muted text-muted-foreground", label: status }
        }

      case "payment":
        switch (normalizedStatus) {
          case "PENDING":
            return {
              bg: "bg-warning/10 text-warning border-warning/20",
              label: "Payment Pending"
            }
          case "COMPLETED":
          case "PAID":
            return {
              bg: "bg-success/15 text-success border-success/30",
              label: "Paid & Secured"
            }
          case "FAILED":
            return {
              bg: "bg-destructive/10 text-destructive border-destructive/20",
              label: "Payment Failed"
            }
          default:
            return { bg: "bg-muted text-muted-foreground", label: status }
        }

      default:
        return { bg: "bg-muted text-muted-foreground", label: status }
    }
  }

  const { bg, label } = getStyleAndLabel()

  return (
    <Badge 
      variant="outline" 
      className={cn(
        "px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wider", 
        bg, 
        className
      )}
    >
      {label}
    </Badge>
  )
}
export default StatusBadge
