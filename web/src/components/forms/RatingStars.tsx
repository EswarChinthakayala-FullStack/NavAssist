import React, { useState } from "react"
import { StarIcon } from "@phosphor-icons/react"
import { cn } from "@/lib/utils"

interface RatingStarsProps {
  rating: number
  onChange?: (rating: number) => void
  readOnly?: boolean
  size?: number
  className?: string
}

export function RatingStars({
  rating,
  onChange,
  readOnly = false,
  size = 28,
  className
}: RatingStarsProps) {
  const [hoverRating, setHoverRating] = useState<number | null>(null)

  const activeRating = hoverRating !== null ? hoverRating : rating

  const handleSelectStar = (e: React.MouseEvent<HTMLButtonElement>, star: number) => {
    e.preventDefault()
    e.stopPropagation()
    if (!readOnly && onChange) {
      onChange(star)
    }
  }

  return (
    <div className={cn("flex gap-2 items-center select-none", className)}>
      {[1, 2, 3, 4, 5].map((star) => {
        const filled = star <= activeRating

        return (
          <button
            key={star}
            type="button"
            disabled={readOnly}
            onClick={(e) => handleSelectStar(e, star)}
            onMouseEnter={() => !readOnly && setHoverRating(star)}
            onMouseLeave={() => !readOnly && setHoverRating(null)}
            className={cn(
              "focus:outline-none transition-all duration-150 p-1.5 rounded-xl flex items-center justify-center",
              readOnly
                ? "pointer-events-none cursor-default"
                : "cursor-pointer hover:scale-115 active:scale-90"
            )}
            title={`Rate ${star} star${star > 1 ? "s" : ""}`}
          >
            <StarIcon
              size={size}
              weight={filled ? "fill" : "regular"}
              className={cn(
                "transition-colors duration-150 pointer-events-none",
                filled
                  ? "text-amber-400 drop-shadow-[0_0_8px_rgba(251,191,36,0.5)]"
                  : "text-muted-foreground/30 hover:text-muted-foreground/70"
              )}
            />
          </button>
        )
      })}
    </div>
  )
}

export default RatingStars
