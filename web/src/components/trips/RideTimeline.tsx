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

interface TimelineStep {
  title: string
  timestamp?: string
  completed: boolean
  current: boolean
  icon: React.ReactNode
}

interface RideTimelineProps {
  status: string
  createdAt?: string
  scheduledAt?: string
  statusHistory?: StatusHistoryItem[]
}

/**
 * Parses UTC or ISO datetime strings and formats strictly into Indian Standard Time (IST, Asia/Kolkata).
 * Ensures that 1:00 PM IST is displayed as "1:00 pm" regardless of browser system timezone.
 */
function formatISTTime(dateStr?: string): string | undefined {
  if (!dateStr) return undefined
  try {
    let normalizedStr = dateStr.trim()
    // If ISO string is naive without Z or timezone offset (+HH:MM), append Z to mark as UTC
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

export function RideTimeline({ status, createdAt, scheduledAt, statusHistory }: RideTimelineProps) {
  const normalized = (status || "PENDING").toUpperCase()

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

  const steps: TimelineStep[] = [
    {
      title: "Booking Requested",
      timestamp: getStageTime(["PENDING", "SEARCHING"], createdAt),
      completed: true,
      current: normalized === "PENDING" || normalized === "SEARCHING",
      icon: <ClockIcon size={14} weight="bold" />,
    },
    {
      title: "Guide Accepted & Assigned",
      timestamp: ["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE", "ARRIVED_PICKUP", "STARTED", "IN_PROGRESS", "COMPLETED"].includes(normalized)
        ? getStageTime(["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE"])
        : undefined,
      completed: ["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE", "ARRIVED_PICKUP", "STARTED", "IN_PROGRESS", "COMPLETED"].includes(normalized),
      current: ["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE"].includes(normalized),
      icon: <UserCheckIcon size={14} weight="bold" />,
    },
    {
      title: "Guide Arrived at Pickup",
      timestamp: ["ARRIVED_PICKUP", "STARTED", "IN_PROGRESS", "COMPLETED"].includes(normalized)
        ? getStageTime(["ARRIVED_PICKUP"])
        : undefined,
      completed: ["ARRIVED_PICKUP", "STARTED", "IN_PROGRESS", "COMPLETED"].includes(normalized),
      current: normalized === "ARRIVED_PICKUP",
      icon: <FlagIcon size={14} weight="bold" />,
    },
    {
      title: "Ride Started",
      timestamp: ["STARTED", "IN_PROGRESS", "COMPLETED"].includes(normalized)
        ? getStageTime(["STARTED", "IN_PROGRESS", "GUEST_PICKED_UP"])
        : undefined,
      completed: ["STARTED", "IN_PROGRESS", "COMPLETED"].includes(normalized),
      current: normalized === "STARTED" || normalized === "IN_PROGRESS" || normalized === "GUEST_PICKED_UP",
      icon: <NavigationArrowIcon size={14} weight="bold" />,
    },
    {
      title: "Ride Completed",
      timestamp: normalized === "COMPLETED" ? getStageTime(["COMPLETED"]) : undefined,
      completed: normalized === "COMPLETED",
      current: false,
      icon: <CheckCircleIcon size={14} weight="bold" />,
    },
  ]

  return (
    <Card className="rounded-2xl border border-border/80 bg-card overflow-hidden shadow-sm">
      <CardHeader className="bg-muted/15 p-3.5 border-b border-border/40">
        <h4 className="text-xs font-bold uppercase tracking-widest text-primary flex items-center gap-1.5">
          <ListChecksIcon size={16} />
          Ride Status Lifecycle Timeline
        </h4>
      </CardHeader>

      <CardContent className="p-4">
        <div className="space-y-4 relative pl-2">
          {steps.map((step, idx) => {
            const isLast = idx === steps.length - 1

            return (
              <div key={idx} className="flex gap-3.5 relative">
                {/* Vertical connecting line */}
                {!isLast && (
                  <div
                    className={`absolute left-[11px] top-6 w-0.5 bottom-[-16px] transition-colors ${
                      step.completed ? "bg-primary" : "bg-border"
                    }`}
                  />
                )}

                {/* Dot Icon */}
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

                {/* Step info */}
                <div className="flex-1 flex justify-between items-center text-xs">
                  <div>
                    <span
                      className={`font-bold block ${
                        step.completed || step.current ? "text-foreground" : "text-muted-foreground"
                      }`}
                    >
                      {step.title}
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
export default RideTimeline
