import React from "react"
import { useNavigate } from "react-router-dom"
import { useBookingDraftStore } from "@/store/booking-draft.store"
import { toast } from "sonner"
import { cn } from "@/lib/utils"
import { CheckIcon } from "@phosphor-icons/react"

interface Step {
  title: string
  description: string
  path: string
}

interface BookingStepperProps {
  currentStep: number
  steps: Step[]
}

export function BookingStepper({ currentStep, steps }: BookingStepperProps) {
  const navigate = useNavigate()
  const { pickup, destination, schedule } = useBookingDraftStore()

  const handleStepClick = (idx: number) => {
    // Guards to prevent jumping ahead without necessary inputs filled out
    if (idx === 0) {
      navigate("/book/pickup")
    } else if (idx === 1) {
      if (!pickup) {
        toast.warning("Specify your pickup location first to access the destination step.")
        return
      }
      navigate("/book/destination")
    } else if (idx === 2) {
      if (!pickup) {
        toast.warning("Specify your pickup location first.")
        return
      }
      if (!destination) {
        toast.warning("Set your dropoff destination first to access the scheduling step.")
        return
      }
      navigate("/book/schedule")
    } else if (idx === 3) {
      if (!pickup) {
        toast.warning("Specify your pickup location first.")
        return
      }
      if (!destination) {
        toast.warning("Set your destination first.")
        return
      }
      if (!schedule) {
        toast.warning("Select your schedule date & time first to see available assistants.")
        return
      }
      navigate("/book/assistants")
    } else if (idx === 4) {
      if (!pickup) {
        toast.warning("Specify your pickup location first.")
        return
      }
      if (!destination) {
        toast.warning("Set your destination first.")
        return
      }
      if (!schedule) {
        toast.warning("Select your schedule date & time first.")
        return
      }
      navigate("/book/confirm")
    }
  }

  // Determine if a step is unlocked/clickable (past, current, or immediately next when current requirements met)
  const isStepUnlocked = (idx: number): boolean => {
    if (idx === 0) return true
    if (idx === 1) return !!pickup
    if (idx === 2) return !!pickup && !!destination
    if (idx === 3) return !!pickup && !!destination && !!schedule
    if (idx === 4) return !!pickup && !!destination && !!schedule
    return false
  }

  return (
    <div className="w-full py-4 px-2 select-none">
      <div className="flex items-center justify-between w-full">
        {steps.map((step, idx) => {
          const isCompleted = currentStep > idx
          const isActive = currentStep === idx
          const isUpcoming = currentStep < idx
          const unlocked = isStepUnlocked(idx)

          return (
            <React.Fragment key={idx}>
              {/* Step indicator wrapper with group hover capabilities */}
              <div
                onClick={() => handleStepClick(idx)}
                className={cn(
                  "flex flex-col items-center flex-1 relative group transition-all duration-200",
                  unlocked ? "cursor-pointer" : "cursor-pointer opacity-70 hover:opacity-100"
                )}
              >
                {/* Visual circle bubble indicator */}
                <div
                  className={cn(
                    "w-9 h-9 rounded-full flex items-center justify-center border-2 transition-all duration-300 font-bold text-sm shadow-sm group-hover:scale-105",
                    isCompleted && "bg-success border-success text-success-foreground group-hover:bg-success/90 group-hover:border-success/90",
                    isActive && "bg-primary border-primary text-primary-foreground shadow-glow-primary scale-105",
                    isUpcoming && "bg-card border-border text-muted-foreground group-hover:border-primary/50"
                  )}
                >
                  {isCompleted ? (
                    <CheckIcon size={16} weight="bold" />
                  ) : (
                    <span>{idx + 1}</span>
                  )}
                </div>

                <span
                  className={cn(
                    "text-xs font-bold mt-2.5 transition-all text-center group-hover:text-primary",
                    isActive ? "text-primary" : isCompleted ? "text-foreground" : "text-muted-foreground"
                  )}
                >
                  {step.title}
                </span>

                <span className="text-[10px] text-muted-foreground text-center hidden md:block mt-0.5 group-hover:text-muted-foreground/80">
                  {step.description}
                </span>
              </div>

              {/* Line connector link linking visual steps */}
              {idx < steps.length - 1 && (
                <div className="flex-1 h-0.5 bg-border relative -translate-y-4 max-w-[120px] mx-2">
                  <div
                    className="absolute inset-0 bg-primary transition-all duration-500"
                    style={{ width: isCompleted ? "100%" : "0%" }}
                  />
                </div>
              )}
            </React.Fragment>
          )
        })}
      </div>
    </div>
  )
}
export default BookingStepper
