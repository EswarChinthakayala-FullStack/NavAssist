import React, { useState, useEffect } from "react"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import { RatingStars } from "@/components/forms/RatingStars"
import { ShieldCheckIcon, PaperPlaneRightIcon } from "@phosphor-icons/react"
import { toast } from "sonner"
import { ratingsService } from "@/services/ratings.service"
import { bookingsService } from "@/services/bookings.service"
import { assistantsService } from "@/services/assistants.service"
import { getFullImageUrl } from "@/components/shared/ImageThumbnail"

interface RateAssistantDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  bookingId: number
  guideName?: string
  guideAvatar?: string
  totalTrips?: number
  onSuccess?: () => void
}

export function RateAssistantDialog({
  open,
  onOpenChange,
  bookingId,
  guideName,
  guideAvatar,
  totalTrips,
  onSuccess,
}: RateAssistantDialogProps) {
  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState("")
  const [submitting, setSubmitting] = useState(false)
  const [fetching, setFetching] = useState(false)

  const [guideInfo, setGuideInfo] = useState<{
    name: string
    avatar?: string
    totalTrips: number
    rating: number
  }>({
    name: guideName || "Escort Guide",
    avatar: guideAvatar,
    totalTrips: totalTrips ?? 0,
    rating: 5.0,
  })

  useEffect(() => {
    if (!open || !bookingId) return

    let isMounted = true
    const fetchBookingAndGuide = async () => {
      setFetching(true)
      try {
        const booking = await bookingsService.getBooking(bookingId)
        const assistantId = booking?.assistant_id

        let name = guideName || booking?.assistant?.full_name || booking?.assistant?.name
        let avatar = guideAvatar || booking?.assistant?.profile_photo_url
        let trips = totalTrips ?? booking?.assistant?.total_trips ?? 0
        let guideRating = booking?.assistant?.rating ?? 5.0

        if (assistantId) {
          try {
            const profile = await assistantsService.getAssistantProfile(assistantId)
            if (profile) {
              name = profile.name || profile.full_name || profile.user?.full_name || name
              avatar = profile.profile_photo_url || profile.avatar_url || avatar
              trips = profile.total_trips ?? profile.completed_trips ?? trips
              guideRating = profile.rating ?? guideRating
            }
          } catch (profileErr) {
            console.error("Failed to fetch guide profile:", profileErr)
          }
        }

        if (isMounted) {
          setGuideInfo({
            name: name || "Escort Guide",
            avatar,
            totalTrips: trips,
            rating: guideRating,
          })
        }
      } catch (err) {
        console.error("Failed to load booking details for rating dialog:", err)
      } finally {
        if (isMounted) setFetching(false)
      }
    }

    fetchBookingAndGuide()
    return () => {
      isMounted = false
    }
  }, [open, bookingId, guideName, guideAvatar, totalTrips])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!bookingId) return

    setSubmitting(true)
    try {
      await ratingsService.submitRating(bookingId, rating, comment)
      toast.success("Thank you for your rating and feedback!")
      onOpenChange(false)
      if (onSuccess) onSuccess()
    } catch (err: any) {
      console.error("Failed to submit rating:", err)
      const detail = err.response?.data?.detail || "Failed to submit rating. Please try again."
      toast.error(detail)
    } finally {
      setSubmitting(false)
    }
  }

  const defaultAvatar =
    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=120"

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent showCloseButton={true} className="max-w-md rounded-3xl p-0 overflow-hidden border border-border/80 shadow-2xl">
        <form onSubmit={handleSubmit}>
          {/* Header Banner */}
          <div className="bg-gradient-to-b from-primary/15 via-primary/5 to-transparent p-6 text-center space-y-2 border-b border-border/40">
            <Badge className="bg-primary/20 hover:bg-primary/20 text-primary border-0 text-[10px] px-3.5 py-1 rounded-full font-extrabold mx-auto tracking-widest uppercase">
              Journey Completed
            </Badge>
            <DialogTitle className="text-lg font-black text-foreground">
              Rate Escort Assistant
            </DialogTitle>
            <DialogDescription className="text-xs text-muted-foreground">
              How was your escort guide experience for Booking #{bookingId}?
            </DialogDescription>
          </div>

          <div className="p-6 space-y-5 text-left">
            {/* Guide Info Card */}
            <div className="flex gap-3.5 items-center bg-muted/40 p-3.5 border border-border/80 rounded-2xl">
              <div className="w-11 h-11 rounded-full overflow-hidden border border-border/80 bg-muted shrink-0 shadow-xs relative">
                {fetching ? (
                  <div className="w-full h-full bg-muted animate-pulse flex items-center justify-center text-[10px]">
                    ...
                  </div>
                ) : (
                  <img
                    src={getFullImageUrl(guideInfo.avatar || defaultAvatar)}
                    alt={guideInfo.name}
                    className="object-cover w-full h-full"
                  />
                )}
              </div>
              <div className="min-w-0 flex-1 space-y-0.5">
                <div className="flex items-center justify-between gap-1">
                  <span className="font-extrabold text-xs text-foreground truncate">
                    {fetching ? "Loading assistant..." : guideInfo.name}
                  </span>
                  <Badge className="bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30 text-[9px] px-1.5 py-0.2 rounded-full font-bold shrink-0 flex items-center gap-0.5">
                    <ShieldCheckIcon size={10} weight="fill" />
                    Verified
                  </Badge>
                </div>
                <p className="text-[10px] text-muted-foreground font-semibold">
                  Verified Escort Guide • {guideInfo.totalTrips > 0 ? `${guideInfo.totalTrips} completed trips` : `★ ${guideInfo.rating.toFixed(1)} rating`}
                </p>
              </div>
            </div>

            {/* Interactive Stars Selector */}
            <div className="flex flex-col items-center gap-2 py-3 border-y border-border/40 text-center">
              <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest block">
                Tap stars to rate
              </span>
              <RatingStars rating={rating} onChange={setRating} size={34} className="mx-auto" />
              <span className="text-xs font-black text-primary mt-1">
                {rating === 5
                  ? "⭐ Excellent journey!"
                  : rating === 4
                  ? "👍 Very good guide"
                  : rating === 3
                  ? "👌 Good support"
                  : rating === 2
                  ? "⚠️ Needs improvement"
                  : "👎 Poor experience"}
              </span>
            </div>

            {/* Review Comment Box */}
            <div className="space-y-1.5">
              <label className="text-[10px] font-extrabold text-muted-foreground uppercase tracking-widest pl-0.5">
                Review Comments (Optional)
              </label>
              <Textarea
                placeholder="Share feedback or compliments about your guide's service..."
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                rows={3}
                className="rounded-2xl border border-border resize-none p-3 focus:ring-1 focus:ring-primary text-xs"
              />
            </div>
          </div>

          <DialogFooter className="p-4 sm:p-5 border-t border-border/50 bg-muted/10">
            <Button
              type="submit"
              disabled={submitting || fetching}
              className="w-full bg-primary text-primary-foreground hover:bg-primary/95 rounded-2xl py-5 font-black text-xs shadow-lg hover:scale-[1.01] transition-all cursor-pointer flex items-center justify-center gap-2"
            >
              <span>{submitting ? "Submitting..." : "Submit Rating & Review"}</span>
              <PaperPlaneRightIcon size={16} weight="fill" />
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
export default RateAssistantDialog
