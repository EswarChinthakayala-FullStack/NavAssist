import React, { useState, useEffect } from "react"
import { motion, AnimatePresence } from "framer-motion"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { RatingStars } from "@/components/forms/RatingStars"
import { ratingsService } from "@/services/ratings.service"
import { StarIcon, PaperPlaneRightIcon, CheckCircleIcon } from "@phosphor-icons/react"
import { toast } from "sonner"

interface RatingCardProps {
  bookingId: number
  guideName: string
}

export function RatingCard({ bookingId, guideName }: RatingCardProps) {
  const [hasRated, setHasRated] = useState(false)
  const [checkingRating, setCheckingRating] = useState(true)
  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState("")
  const [submitting, setSubmitting] = useState(false)

  // Verify on mount if booking is already rated
  useEffect(() => {
    let isMounted = true
    const checkRated = async () => {
      try {
        const res = await ratingsService.getBookingRating(bookingId)
        if (isMounted) {
          if (res?.has_rated || res?.rating) {
            setHasRated(true)
          }
        }
      } catch (err) {
        // Ignored if 404/not rated yet
      } finally {
        if (isMounted) setCheckingRating(false)
      }
    }
    checkRated()
    return () => {
      isMounted = false
    }
  }, [bookingId])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      await ratingsService.submitRating(bookingId, rating, comment)
      toast.success("Thank you for your rating and feedback!")
      setHasRated(true)
    } catch (err) {
      console.error(err)
      toast.error("Failed to submit rating. Please try again.")
    } finally {
      setSubmitting(false)
    }
  }

  if (checkingRating) {
    return (
      <div className="bg-card border border-border rounded-2xl h-36 flex items-center justify-center">
        <div className="w-5 h-5 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  return (
    <AnimatePresence mode="wait">
      {!hasRated ? (
        <motion.div
          key="rating-form"
          initial={{ opacity: 0, y: 15 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.95 }}
          transition={{ duration: 0.5 }}
          className="w-full"
        >
          <Card className="border border-border bg-card shadow-xl backdrop-blur-md rounded-2xl overflow-hidden">
            <CardHeader className="text-center pb-4 border-b border-border p-5">
              <CardTitle className="text-base font-black text-card-foreground flex items-center justify-center gap-1.5">
                <StarIcon size={18} className="text-primary fill-primary" />
                Rate Your Assistant
              </CardTitle>
              <CardDescription className="text-xs text-muted-foreground mt-1">
                How was your journey experience with {guideName}?
              </CardDescription>
            </CardHeader>
            <CardContent className="p-5 space-y-4">
              <form onSubmit={handleSubmit} className="space-y-4">
                {/* Rating selection */}
                <div className="flex flex-col items-center gap-2 py-2">
                  <RatingStars rating={rating} onChange={setRating} size={30} className="mx-auto" />
                  <span className="text-[10px] font-black text-primary uppercase tracking-wider">
                    {rating === 5 ? "Excellent Support" : rating === 4 ? "Very Good Guide" : rating === 3 ? "Good Journey" : rating === 2 ? "Needs Improvement" : "Poor Experience"}
                  </span>
                </div>

                {/* Comment Box */}
                <div className="space-y-1 text-left">
                  <label className="text-[9px] font-bold text-muted-foreground uppercase tracking-widest pl-1">
                    Feedback Details (Optional)
                  </label>
                  <Textarea
                    placeholder="Share specific compliments or transition support details..."
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                    className="bg-muted/30 border-border focus-visible:ring-primary/50 text-xs rounded-xl min-h-16 text-card-foreground placeholder:text-muted-foreground/50"
                  />
                </div>

                <Button
                  type="submit"
                  disabled={submitting}
                  className="w-full bg-primary text-primary-foreground font-extrabold text-xs py-5 rounded-xl flex items-center justify-center gap-1.5 shadow-md cursor-pointer transition-all active:scale-98"
                >
                  <PaperPlaneRightIcon size={14} weight="bold" />
                  <span>{submitting ? "Submitting..." : "Submit Review"}</span>
                </Button>
              </form>
            </CardContent>
          </Card>
        </motion.div>
      ) : (
        <motion.div
          key="rating-success"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ type: "spring", stiffness: 200, damping: 20 }}
          className="w-full"
        >
          <Card className="border border-border bg-card p-6 flex flex-col items-center justify-center gap-2 text-center rounded-2xl">
            <CheckCircleIcon size={32} weight="fill" className="text-emerald-500" />
            <p className="text-xs font-black text-card-foreground">Journey Review Submitted</p>
            <p className="text-[10px] text-muted-foreground font-semibold">Thank you for rating your escort assistant guide.</p>
          </Card>
        </motion.div>
      )}
    </AnimatePresence>
  )
}

export default RatingCard
