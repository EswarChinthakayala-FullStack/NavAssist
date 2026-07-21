import React, { useState, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { bookingsService } from "@/services/bookings.service"
import { assistantsService } from "@/services/assistants.service"
import { ratingsService } from "@/services/ratings.service"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import { RatingStars } from "@/components/forms/RatingStars"
import { StarIcon, ShieldCheckIcon, PaperPlaneRightIcon } from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

export function RateAssistantPage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()

  const [booking, setBooking] = useState<any>(null)
  const [guide, setGuide] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState("")
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!bookingId) {
      navigate("/bookings")
      return
    }

    const fetchBookingDetails = async () => {
      setLoading(true)
      try {
        const b = await bookingsService.getBooking(parseInt(bookingId))
        setBooking(b)

        if (b.assistant_id) {
          try {
            const g = await assistantsService.getAssistantProfile(b.assistant_id)
            setGuide(g)
          } catch (e) {
            setGuide(b.assistant || {
              id: b.assistant_id,
              name: "Escort Guide",
              rating: 5.0,
              total_trips: 0
            })
          }
        }
      } catch (err) {
        console.error(err)
        toast.error("Failed to load booking details for rating.")
        navigate("/bookings")
      } finally {
        setLoading(false)
      }
    }

    fetchBookingDetails()
  }, [bookingId, navigate])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!bookingId) return

    setSubmitting(true)
    try {
      await ratingsService.submitRating(parseInt(bookingId), rating, comment)
      toast.success("Thank you for your rating and feedback!")
      navigate("/bookings")
    } catch (err) {
      console.error(err)
      toast.error("Failed to submit rating. Please try again.")
    } finally {
      setSubmitting(false)
    }
  }

  if (loading || !booking) {
    return (
      <div className="h-[300px] w-full flex flex-col items-center justify-center gap-4 bg-background text-foreground">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-muted-foreground">Preparing rating view...</span>
      </div>
    )
  }

  const defaultGuideAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=80"
  const guideName = guide?.name || guide?.full_name || guide?.user?.full_name || booking?.assistant?.full_name || "Escort Guide"
  const guideAvatar = guide?.profile_photo_url || guide?.avatar_url || booking?.assistant?.profile_photo_url || defaultGuideAvatar

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-md mx-auto py-8 text-center"
    >
      <form onSubmit={handleSubmit}>
        <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-3xl overflow-hidden text-left">
          {/* Header Banner */}
          <div className="bg-gradient-to-b from-primary/10 to-transparent p-6 text-center space-y-2 border-b border-border/40">
            <Badge className="bg-primary/20 hover:bg-primary/20 text-primary border-0 text-[10px] px-3.5 py-1 rounded-full font-bold mx-auto tracking-widest uppercase">
              Journey Review
            </Badge>
            <CardTitle className="text-lg font-black text-foreground">
              Rate Escort Service
            </CardTitle>
            <CardDescription className="text-xs">
              Let us know how your escort assistant {guideName} assisted you for Booking #{bookingId}.
            </CardDescription>
          </div>

          <CardContent className="p-6 space-y-6">
            {/* Guide Card Section */}
            <div className="flex gap-4 items-center bg-muted/40 p-4 border border-border/80 rounded-2xl text-left">
              <div className="w-12 h-12 rounded-full overflow-hidden border border-border bg-muted shrink-0">
                <img src={guideAvatar} alt={guideName} className="object-cover w-full h-full" />
              </div>
              <div className="min-w-0 space-y-0.5">
                <div className="flex items-center gap-1.5 justify-between">
                  <span className="font-bold text-sm text-foreground truncate">{guideName}</span>
                  <Badge className="bg-success text-success-foreground hover:bg-success border-0 text-[8px] px-1.5 py-0.5 rounded-full flex items-center gap-0.5 font-bold">
                    <ShieldCheckIcon size={10} weight="fill" />
                    Verified
                  </Badge>
                </div>
                <p className="text-[10px] text-muted-foreground font-semibold">
                  Verified Escort Guide • {guide?.total_trips ?? guide?.completed_trips ?? 0} completed trips
                </p>
              </div>
            </div>

            {/* Interactive Stars selector */}
            <div className="flex flex-col items-center gap-2.5 py-4 border-y border-border/40">
              <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest block">Tap to rate</span>
              <RatingStars rating={rating} onChange={setRating} size={36} className="mx-auto" />
              <span className="text-xs font-black text-primary mt-1">
                {rating === 5 ? "Excellent journey!" : rating === 4 ? "Very good escort guide" : rating === 3 ? "Good support" : rating === 2 ? "Needs improvement" : "Poor experience"}
              </span>
            </div>

            {/* Review Comment box */}
            <div className="space-y-2 text-left">
              <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest pl-1">
                Review Comments (Optional)
              </label>
              <Textarea
                placeholder="Share specific compliments or transit details that went well..."
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                rows={4}
                className="rounded-2xl border border-border resize-none p-3.5 focus:ring-1 focus:ring-primary text-xs"
              />
            </div>
          </CardContent>

          <CardFooter className="p-6 border-t border-border/50 bg-muted/10">
            <Button
              type="submit"
              disabled={submitting}
              className="w-full bg-primary text-primary-foreground hover:bg-primary/95 rounded-2xl py-5 font-black text-xs shadow-lg hover:scale-[1.01] transition-all cursor-pointer flex items-center justify-center gap-1.5"
            >
              <span>Submit Rating Details</span>
              <PaperPlaneRightIcon size={16} weight="fill" />
            </Button>
          </CardFooter>
        </Card>
      </form>
    </motion.div>
  )
}
export default RateAssistantPage
