import React from "react"
import { Card, CardContent, CardHeader } from "@/components/ui/card"
import {
  ClockIcon,
  CheckCircleIcon,
  UserCheckIcon,
  NavigationArrowIcon,
  FlagIcon,
  ListChecksIcon,
} from "@phosphor-icons/react"

export interface StatusHistoryItem {
  status: string
  changed_at: string
  changed_by?: number
}

interface RideStatusTimelineProps {
  status: string
  createdAt?: string
  statusHistory?: StatusHistoryItem[]
  className?: string
}

function formatISTTime(dateStr?: string): string | undefined {
  if (!dateStr) return undefined
  try {
    let normalizedStr = dateStr.trim()
    if (!normalizedStr.endsWith("Z") && !/[+-]\d{2}:?\d{2}$/.test(normalizedStr)) {
      normalizedStr += "Z"
    }
    const d = new Date(normalizedStr)
    if (isNaN(d.getTime())) return undefined

    return d.toLocaleTimeString("en-IN", {
      timeZone: "Asia/Kolkata",
      hour: "numeric",
      minute: "2-digit",
      hour12: true,
    }).toLowerCase()
  } catch {
    return undefined
  }
}

export function RideStatusTimeline({ status, createdAt, statusHistory, className }: RideStatusTimelineProps) {
  const norm = (status || "PENDING").toUpperCase()

  const getStageTime = (targetStatuses: string[], fallback?: string): string | undefined => {
    if (statusHistory && statusHistory.length > 0) {
      const item = statusHistory.find((h) =>
        targetStatuses.includes((h.status || "").toUpperCase())
      )
      if (item?.changed_at) {
        return formatISTTime(item.changed_at)
      }
    }
    return fallback ? formatISTTime(fallback) : undefined
  }

  const steps = [
    {
      label: "Assignment Received",
      timestamp: getStageTime(["PENDING", "SEARCHING"], createdAt),
      completed: true,
      current: norm === "PENDING" || norm === "SEARCHING",
      icon: <ClockIcon size={14} weight="bold" />,
    },
    {
      label: "Accepted & En Route",
      timestamp: ["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE", "ARRIVED_PICKUP", "STARTED", "IN_PROGRESS", "COMPLETED"].includes(norm)
        ? getStageTime(["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE"])
        : undefined,
      completed: ["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE", "ARRIVED_PICKUP", "STARTED", "IN_PROGRESS", "COMPLETED"].includes(norm),
      current: ["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE"].includes(norm),
      icon: <UserCheckIcon size={14} weight="bold" />,
    },
    {
      label: "Arrived at Pickup",
      timestamp: ["ARRIVED_PICKUP", "STARTED", "IN_PROGRESS", "COMPLETED"].includes(norm)
        ? getStageTime(["ARRIVED_PICKUP"])
        : undefined,
      completed: ["ARRIVED_PICKUP", "STARTED", "IN_PROGRESS", "COMPLETED"].includes(norm),
      current: norm === "ARRIVED_PICKUP",
      icon: <FlagIcon size={14} weight="bold" />,
    },
    {
      label: "OTP Verified & Started",
      timestamp: ["STARTED", "IN_PROGRESS", "COMPLETED"].includes(norm)
        ? getStageTime(["STARTED", "IN_PROGRESS", "GUEST_PICKED_UP"])
        : undefined,
      completed: ["STARTED", "IN_PROGRESS", "COMPLETED"].includes(norm),
      current: norm === "STARTED" || norm === "IN_PROGRESS",
      icon: <NavigationArrowIcon size={14} weight="bold" />,
    },
    {
      label: "Ride Completed",
      timestamp: norm === "COMPLETED" ? getStageTime(["COMPLETED"]) : undefined,
      completed: norm === "COMPLETED",
      current: false,
      icon: <CheckCircleIcon size={14} weight="bold" />,
    },
  ]

  return (
    <Card className={`rounded-2xl border border-border/80 bg-card overflow-hidden shadow-sm ${className}`}>
      <CardHeader className="bg-muted/15 p-3.5 border-b border-border/40">
        <h4 className="text-xs font-bold uppercase tracking-widest text-primary flex items-center gap-1.5">
          <ListChecksIcon size={16} />
          Assistant Duty Lifecycle Timeline
        </h4>
      </CardHeader>

      <CardContent className="p-4">
        <div className="space-y-4 relative pl-2">
          {steps.map((step, idx) => {
            const isLast = idx === steps.length - 1

            return (
              <div key={idx} className="flex gap-3.5 relative text-left">
                {!isLast && (
                  <div
                    className={`absolute left-[11px] top-6 w-0.5 bottom-[-16px] transition-colors ${
                      step.completed ? "bg-primary" : "bg-border"
                    }`}
                  />
                )}

                <div
                  className={`w-6 h-6 rounded-full flex items-center justify-center text-xs shrink-0 z-10 transition-all ${
                    step.current
                      ? "bg-primary text-primary-foreground ring-4 ring-primary/20 scale-110"
                      : step.completed
                      ? "bg-primary text-primary-foreground"
                      : "bg-muted text-muted-foreground border border-border"
                  }`}
                >
                  {step.icon}
                </div>

                <div className="flex-1 flex justify-between items-center text-xs">
                  <div>
                    <span
                      className={`font-bold block ${
                        step.completed || step.current ? "text-foreground" : "text-muted-foreground"
                      }`}
                    >
                      {step.label}
                    </span>
                    {step.current && (
                      <span className="text-[10px] text-primary font-extrabold uppercase tracking-wider block mt-0.5">
                        Active Stage
                      </span>
                    )}
                  </div>

                  {step.timestamp && (
                    <span className="text-[10px] font-semibold text-muted-foreground shrink-0">
                      {step.timestamp}
                    </span>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}
export default RideStatusTimeline
