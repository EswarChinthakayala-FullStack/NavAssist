import React from "react"
import { WarningOctagonIcon } from "@phosphor-icons/react"
import { Button } from "@/components/ui/button"

interface ImageErrorFallbackProps {
  onRetry?: () => void
}

export function ImageErrorFallback({ onRetry }: ImageErrorFallbackProps) {
  return (
    <div className="w-full h-full min-h-[120px] flex flex-col items-center justify-center border border-dashed border-zinc-800 bg-zinc-950 p-4 rounded-lg text-center">
      <WarningOctagonIcon className="w-8 h-8 text-zinc-600 mb-2" />
      <span className="text-[11px] font-bold text-zinc-400 block mb-2">Image unavailable</span>
      {onRetry && (
        <Button
          size="sm"
          variant="outline"
          onClick={(e) => {
            e.stopPropagation()
            onRetry()
          }}
          className="text-[10px] h-7 px-2.5 rounded-md border-zinc-800 hover:bg-zinc-900"
        >
          Retry
        </Button>
      )}
    </div>
  )
}
export default ImageErrorFallback
