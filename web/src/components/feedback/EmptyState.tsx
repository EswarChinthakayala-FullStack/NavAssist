import React from "react"
import { ArchiveIcon } from "@phosphor-icons/react"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"

interface EmptyStateProps {
  title: string
  description: string
  icon?: React.ReactNode
  actionLabel?: string
  onAction?: () => void
  className?: string
  wide?: boolean
}

export function EmptyState({
  title,
  description,
  icon = <ArchiveIcon size={40} weight="light" className="text-muted-foreground" />,
  actionLabel,
  onAction,
  className,
  wide = false
}: EmptyStateProps) {
  return (
    <div className={cn(
      "flex flex-col items-center justify-center text-center p-8 rounded-2xl border border-dashed border-border bg-card/30 backdrop-blur-xs select-none",
      wide ? "w-full py-12" : "max-w-sm mx-auto",
      className
    )}>
      <div className="w-16 h-16 rounded-full bg-muted flex items-center justify-center mb-4 text-muted-foreground">
        {icon}
      </div>
      <h4 className={cn("font-extrabold text-foreground tracking-tight", wide ? "text-base" : "text-sm")}>{title}</h4>
      <p className={cn("text-muted-foreground mt-2 leading-relaxed mx-auto", wide ? "max-w-md text-xs" : "max-w-[280px] text-[11px]")}>{description}</p>
      
      {actionLabel && onAction && (
        <Button
          onClick={onAction}
          className="mt-5 h-11 text-xs font-black shadow-lg bg-primary text-primary-foreground hover:bg-primary/90 rounded-xl px-5 py-4 cursor-pointer hover:scale-102 active:scale-98 transition-all"
        >
          {actionLabel}
        </Button>
      )}
    </div>
  )
}
export default EmptyState
