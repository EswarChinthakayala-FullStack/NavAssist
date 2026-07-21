import React from "react"
import { cn } from "@/lib/utils"
import { CheckCircleIcon, ClockIcon, NavigationArrowIcon, SealCheckIcon } from "@phosphor-icons/react"

interface TimelineStep {
  status: string
  label: string
  description: string
  time?: string
}

interface ProgressTimelineProps {
  currentStatus: "PENDING" | "ACCEPTED" | "STARTED" | "COMPLETED" | "CANCELLED"
  updatedAt?: string
}

export function ProgressTimeline({ currentStatus, updatedAt }: ProgressTimelineProps) {
  const steps: TimelineStep[] = [
    {
      status: "PENDING",
      label: "Request Placed",
      description: "Booking registered on the platform, matching helpers nearby."
    },
    {
      status: "ACCEPTED",
      label: "Assistant Assigned",
      description: "A verified local assistant has accepted and is enroute to you."
    },
    {
      status: "STARTED",
      label: "Escort Active",
      description: "OTP check succeeded. Guide is escorting you to lodgings."
    },
    {
      status: "COMPLETED",
      label: "Completed Safely",
      description: "Arrived at destination coordinate point successfully."
    }
  ]

  const getStatusIndex = (status: string) => {
    switch (status) {
      case "PENDING":
        return 0
      case "ACCEPTED":
        return 1
      case "STARTED":
        return 2
      case "COMPLETED":
        return 3
      case "CANCELLED":
        return -1
      default:
        return 0
    }
  }

  const activeIdx = getStatusIndex(currentStatus)

  if (currentStatus === "CANCELLED") {
    return (
      <div className="p-4 bg-destructive/10 border border-destructive/20 rounded-xl text-center">
        <h4 className="text-sm font-extrabold text-destructive uppercase tracking-wider">Trip Cancelled</h4>
        <p className="text-xs text-muted-foreground mt-1">This booking escort request was cancelled by the traveler or guide.</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-5 py-2 pl-2">
      {steps.map((step, idx) => {
        const isCompleted = activeIdx > idx
        const isActive = activeIdx === idx
        const isUpcoming = activeIdx < idx

        let icon = <ClockIcon size={16} weight="bold" />
        if (isCompleted) {
          icon = <CheckCircleIcon size={18} weight="fill" className="text-success-foreground" />
        } else if (isActive) {
          if (step.status === "PENDING") {
            icon = <ClockIcon size={18} className="animate-spin text-primary-foreground" />
          } else if (step.status === "ACCEPTED" || step.status === "STARTED") {
            icon = <NavigationArrowIcon size={18} className="animate-pulse text-primary-foreground" />
          }
        }

        return (
          <div key={idx} className="flex gap-4 items-start relative last:pb-0 pb-2">
            {/* Timeline connector line */}
            {idx < steps.length - 1 && (
              <div 
                className={cn(
                  "absolute left-[13px] top-[26px] bottom-0 w-0.5 -translate-x-1/2 bg-border z-0",
                  isCompleted && "bg-success"
                )}
              />
            )}

            {/* Step Icon Badge */}
            <div
              className={cn(
                "w-7 h-7 rounded-full flex items-center justify-center border-2 z-10 shrink-0 transition-all duration-300",
                isCompleted && "bg-success border-success text-white shadow-sm",
                isActive && "bg-primary border-primary text-white shadow-glow-primary scale-105",
                isUpcoming && "bg-card border-border text-muted-foreground"
              )}
            >
              {icon}
            </div>

            {/* Step Content */}
            <div className="flex-1 min-w-0 -mt-0.5">
              <div className="flex justify-between items-center">
                <span
                  className={cn(
                    "text-xs font-extrabold transition-all",
                    isActive ? "text-primary" : isCompleted ? "text-foreground" : "text-muted-foreground"
                  )}
                >
                  {step.label}
                </span>
                {isActive && updatedAt && (
                  <span className="text-[9px] text-muted-foreground font-bold">
                    {new Date(updatedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                )}
              </div>
              <p
                className={cn(
                  "text-[10px] leading-relaxed mt-1 transition-all",
                  isActive ? "text-foreground/80 font-medium" : "text-muted-foreground"
                )}
              >
                {step.description}
              </p>
            </div>
          </div>
        )
      })}
    </div>
  )
}
export default ProgressTimeline
