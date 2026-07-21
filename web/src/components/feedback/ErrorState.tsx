import React from "react"
import { WarningCircleIcon, ArrowClockwiseIcon } from "@phosphor-icons/react"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"

interface ErrorStateProps {
  message?: string
  description?: string
  onRetry?: () => void
  className?: string
}

export function ErrorState({
  message = "Failed to load details",
  description = "Something went wrong. Please check your connection and try again.",
  onRetry,
  className
}: ErrorStateProps) {
  return (
    <div className={cn("flex flex-col items-center justify-center text-center p-8 rounded-2xl border border-destructive/15 bg-destructive/5 max-w-sm mx-auto select-none", className)}>
      <div className="w-12 h-12 rounded-full bg-destructive/10 text-destructive flex items-center justify-center mb-4">
        <WarningCircleIcon size={28} weight="fill" />
      </div>
      <h4 className="text-sm font-extrabold text-foreground tracking-tight">{message}</h4>
      <p className="text-xs text-muted-foreground mt-2 leading-relaxed max-w-[280px]">{description}</p>
      
      {onRetry && (
        <Button
          onClick={onRetry}
          variant="outline"
          className="mt-5 py-4 border-border rounded-xl font-bold flex items-center gap-1.5 hover:bg-muted text-xs transition-all"
        >
          <ArrowClockwiseIcon size={14} className="text-muted-foreground" />
          Try Again
        </Button>
      )}
    </div>
  )
}
export default ErrorState
