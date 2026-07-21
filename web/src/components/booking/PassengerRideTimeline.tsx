import React from "react"
import { Card, CardContent, CardHeader } from "@/components/ui/card"
import {
  ClockIcon,
  CheckCircleIcon,
  UserCheckIcon,
  NavigationArrowIcon,
  FlagIcon,
  PathIcon,
} from "@phosphor-icons/react"

interface Step {
  label: string
  desc: string
  completed: boolean
  current: boolean
  icon: React.ReactNode
}

interface PassengerRideTimelineProps {
  status: string
  scheduledAt?: string
  className?: string
}

export function PassengerRideTimeline({ status, scheduledAt, className }: PassengerRideTimelineProps) {
  const norm = (status || "PENDING").toUpperCase()

  const steps: Step[] = [
    {
      label: "Request Sent",
      desc: "Broadcasting to nearby guides",
      completed: true,
      current: norm === "PENDING" || norm === "SEARCHING",
      icon: <ClockIcon size={14} weight="bold" />,
    },
    {
      label: "Guide Assigned",
      desc: "Escort guide confirmed assignment",
      completed: ["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE", "ARRIVED_PICKUP", "STARTED", "IN_PROGRESS", "COMPLETED"].includes(norm),
      current: ["ACCEPTED", "ASSIGNED", "ASSISTANT_ENROUTE"].includes(norm),
      icon: <UserCheckIcon size={14} weight="bold" />,
    },
    {
      label: "Guide Arrived",
      desc: "Guide waiting at pickup location",
      completed: ["ARRIVED_PICKUP", "STARTED", "IN_PROGRESS", "COMPLETED"].includes(norm),
      current: norm === "ARRIVED_PICKUP",
      icon: <FlagIcon size={14} weight="bold" />,
    },
    {
      label: "Trip Started",
      desc: "OTP verified & journey active",
      completed: ["STARTED", "IN_PROGRESS", "COMPLETED"].includes(norm),
      current: norm === "STARTED" || norm === "IN_PROGRESS" || norm === "GUEST_PICKED_UP",
      icon: <NavigationArrowIcon size={14} weight="bold" />,
    },
    {
      label: "Destination Reached",
      desc: "Safely escorted to destination",
      completed: norm === "COMPLETED",
      current: false,
      icon: <CheckCircleIcon size={14} weight="bold" />,
    },
  ]

  return (
    <Card className={`rounded-2xl border border-border/80 bg-card overflow-hidden shadow-sm ${className}`}>
      <CardHeader className="bg-muted/15 p-3.5 border-b border-border/40">
        <h4 className="text-xs font-bold uppercase tracking-widest text-primary flex items-center gap-1.5">
          <PathIcon size={16} />
          Passenger Journey Status
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

                <div className="flex-1">
                  <div className="flex justify-between items-center">
                    <span
                      className={`font-bold text-xs ${
                        step.completed || step.current ? "text-foreground" : "text-muted-foreground"
                      }`}
                    >
                      {step.label}
                    </span>
                    {step.current && (
                      <span className="text-[10px] text-primary font-extrabold uppercase tracking-wider">
                        Active
                      </span>
                    )}
                  </div>
                  <p className="text-[10px] text-muted-foreground mt-0.5">{step.desc}</p>
                </div>
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}
export default PassengerRideTimeline
