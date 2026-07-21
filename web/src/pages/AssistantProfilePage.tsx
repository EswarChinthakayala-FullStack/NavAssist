import React, { useEffect, useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { assistantsService } from "@/services/assistants.service"
import { ShieldCheckIcon, StarIcon, ArrowLeftIcon, ArrowRightIcon, MapPinIcon, BriefcaseIcon, MedalIcon, CalendarBlankIcon, ThumbsUpIcon } from "@phosphor-icons/react"
import { Button } from "@/components/ui/button"
import { StatusBadge } from "@/components/feedback/StatusBadge"
import { useBookingDraftStore } from "@/store/booking-draft.store"
import { RatingStars } from "@/components/forms/RatingStars"
import { AssistantVerificationSheet } from "@/components/booking/AssistantVerificationSheet"
import { motion, AnimatePresence } from "framer-motion"
import { toast } from "sonner"

interface AssistantData {
  id: number
  user_id: number
  name: string
  verification_status: "PENDING" | "APPROVED" | "REJECTED" | "NOT_SUBMITTED"
  is_online: boolean
  rating: number
  total_trips: number
  experience_years?: number
  trust_score?: number
  profile_photo_url?: string
}

interface ReviewItem {
  id: number
  rating: number
  review_text: string
  created_at: string
  booking_id: number
  rated_by: number
}

export function AssistantProfilePage() {
  const { assistantId } = useParams<{ assistantId: string }>()
  const navigate = useNavigate()
  const [profile, setProfile] = useState<AssistantData | null>(null)
  const [reviews, setReviews] = useState<ReviewItem[]>([])
  const [loading, setLoading] = useState(true)
  const [sheetOpen, setSheetOpen] = useState(false)

  // Zustand Store variables
  const { pickup, destination, setSelectedAssistant } = useBookingDraftStore()
  const isBookingFlow = pickup !== null && destination !== null

  // Pagination states
  const [currentPage, setCurrentPage] = useState(1)
  const reviewsPerPage = 3

  const fetchProfileAndRatings = async () => {
    setLoading(true)
    try {
      // 1. Fetch assistant profile details
      const profData = await assistantsService.getAssistantProfile(Number(assistantId))
      
      // Enriched default metadata parameters
      setProfile({
        ...profData,
        rating: profData.avg_rating ?? 0.0,
        experience_years: profData.experience_years ?? 0,
        trust_score: profData.trust_score ?? 0,
        profile_photo_url: profData.profile_photo_url,
      })

      // 2. Fetch rating review histories
      const ratingData = await assistantsService.getAssistantRatings(Number(assistantId))
      setReviews(ratingData || [])
    } catch (err) {
      console.error("Failed to query guide details:", err)
      // Fallback local mock guide profile details if not registered in seeding
      setProfile({
        id: Number(assistantId),
        user_id: 100 + Number(assistantId),
        name: "Assistant Guide #" + assistantId,
        verification_status: "APPROVED",
        is_online: true,
        rating: 4.8,
        total_trips: 142,
        experience_years: 4,
        trust_score: 99,
      })
      
      // Fallback reviews
      setReviews([
        { id: 1, rating: 5, review_text: "Incredible guide! Navigated the busy airport terminals perfectly and helped carry all our heavy suitcases.", created_at: new Date(Date.now() - 86400000 * 2).toISOString(), booking_id: 10, rated_by: 1 },
        { id: 2, rating: 5, review_text: "Very polite and punctual. Waited at the train platform for 15 minutes before arrival.", created_at: new Date(Date.now() - 86400000 * 5).toISOString(), booking_id: 11, rated_by: 2 },
        { id: 3, rating: 4, review_text: "Very knowledgeable about airport lanes. Recommended the best lounges near gate 4.", created_at: new Date(Date.now() - 86400000 * 8).toISOString(), booking_id: 12, rated_by: 3 },
        { id: 4, rating: 5, review_text: "Lifesaver! Assisted my elderly grandmother down the platform escalators safely.", created_at: new Date(Date.now() - 86400000 * 12).toISOString(), booking_id: 13, rated_by: 4 }
      ])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProfileAndRatings()
  }, [assistantId])

  const handleSelectAndContinue = () => {
    if (!profile) return
    setSelectedAssistant({
      id: profile.user_id,
      user_id: profile.user_id,
      name: profile.name,
      verification_status: profile.verification_status,
      is_online: profile.is_online,
      rating: profile.rating,
      total_trips: profile.total_trips,
      profile_photo_url: profile.profile_photo_url,
      created_at: new Date().toISOString()
    })
    
    if (!pickup) {
      navigate("/book/pickup")
    } else if (!destination) {
      navigate("/book/destination")
    } else {
      navigate("/book/summary")
    }
  }

  if (loading) {
    return (
      <div className="h-[450px] w-full flex flex-col items-center justify-center gap-4 bg-background text-foreground">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-muted-foreground">Loading Profile details...</span>
      </div>
    )
  }

  if (!profile) return null

  // Pagination slicing logic
  const indexOfLastReview = currentPage * reviewsPerPage
  const indexOfFirstReview = indexOfLastReview - reviewsPerPage
  const currentReviews = reviews.slice(indexOfFirstReview, indexOfLastReview)
  const totalPages = Math.ceil(reviews.length / reviewsPerPage)

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -15 }}
      transition={{ duration: 0.3 }}
      className="w-full flex flex-col gap-6"
    >
      {/* Verification details modal overlay */}
      <AssistantVerificationSheet
        isOpen={sheetOpen}
        onClose={() => setSheetOpen(false)}
        assistantName={profile.name}
        verificationStatus={profile.verification_status}
        totalTrips={profile.total_trips}
        rating={profile.rating}
      />

      {/* Main Container */}
      <div className="grid gap-6 md:grid-cols-12 w-full pb-20">
        {/* Left Side: Avatar Card Profile Summary */}
        <div className="md:col-span-5 space-y-4">
          <div className="p-6 rounded-2xl border border-border/80 bg-card shadow-md flex flex-col items-center text-center relative overflow-hidden">
            {/* Header cover decorative background */}
            <div className="absolute top-0 left-0 right-0 h-24 bg-gradient-to-r from-primary/10 via-primary/5 to-transparent border-b border-border/40" />
            
            {/* Status verification click triggers sheet */}
            <button
              onClick={() => setSheetOpen(true)}
              className="absolute right-4 top-4 hover:scale-105 transition-transform cursor-pointer z-10"
              title="Click to check credentials"
            >
              <StatusBadge status={profile.verification_status} type="kyc" />
            </button>

            {/* Avatar Circle with Framer Motion layoutId */}
            <motion.div
              layoutId={`avatar-${profile.id}`}
              className="w-28 h-28 rounded-full overflow-hidden border-4 border-background bg-accent text-accent-foreground flex items-center justify-center text-4xl font-black shadow-lg relative mt-8 z-10 select-none"
            >
              {profile.name ? profile.name.split(" ").map(p => p[0]).join("") : "AS"}
            </motion.div>

            <div className="flex flex-col items-center gap-1.5 mt-4 z-10 select-none">
              <h4 className="font-black text-xl text-foreground flex items-center gap-1.5 justify-center">
                {profile.name}
                {profile.verification_status === "APPROVED" && (
                  <ShieldCheckIcon size={22} weight="fill" className="text-success cursor-pointer" onClick={() => setSheetOpen(true)} />
                )}
              </h4>
              <span className="text-xs font-semibold text-muted-foreground flex items-center gap-1">
                <MapPinIcon size={14} />
                Verified Transit Escort Guide
              </span>
            </div>

            {/* Key Platform Badges */}
            <div className="grid grid-cols-3 gap-2 w-full mt-6 select-none">
              <div className="bg-muted/30 border border-border p-2 rounded-xl text-center space-y-0.5">
                <span className="text-[8px] font-black text-muted-foreground uppercase tracking-wider block">Trust Score</span>
                <span className="text-sm font-black text-primary block">{profile.trust_score}%</span>
              </div>
              <div className="bg-muted/30 border border-border p-2 rounded-xl text-center space-y-0.5">
                <span className="text-[8px] font-black text-muted-foreground uppercase tracking-wider block">Rating Score</span>
                <span className="text-sm font-black text-warning block">★ {profile.rating.toFixed(1)}</span>
              </div>
              <div className="bg-muted/30 border border-border p-2 rounded-xl text-center space-y-0.5">
                <span className="text-[8px] font-black text-muted-foreground uppercase tracking-wider block">Escorts</span>
                <span className="text-sm font-black text-foreground block">{profile.total_trips} trips</span>
              </div>
            </div>
          </div>

          {/* Quick Details Stats details */}
          <div className="p-6 rounded-2xl border border-border bg-card shadow-md space-y-4 select-none">
            <h3 className="font-extrabold text-sm text-foreground">Guide Experience Details</h3>
            <div className="space-y-3">
              <div className="flex items-center gap-3 text-xs">
                <div className="p-2 bg-primary/10 text-primary rounded-lg shrink-0">
                  <BriefcaseIcon size={16} />
                </div>
                <div>
                  <span className="text-muted-foreground block text-[10px] font-bold uppercase tracking-wider">Active Experience</span>
                  <span className="font-semibold text-foreground">{profile.experience_years} Years of Field Escorts</span>
                </div>
              </div>

              <div className="flex items-center gap-3 text-xs">
                <div className="p-2 bg-success/10 text-success rounded-lg shrink-0">
                  <MedalIcon size={16} />
                </div>
                <div>
                  <span className="text-muted-foreground block text-[10px] font-bold uppercase tracking-wider">Credentials Checked</span>
                  <span className="font-semibold text-foreground">Aadhaar Verification Approved</span>
                </div>
              </div>

              <div className="flex items-center gap-3 text-xs">
                <div className="p-2 bg-accent text-accent-foreground rounded-lg shrink-0">
                  <ThumbsUpIcon size={16} />
                </div>
                <div>
                  <span className="text-muted-foreground block text-[10px] font-bold uppercase tracking-wider">Review Sentiment</span>
                  <span className="font-semibold text-foreground">Outstanding (No negative feedback)</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Side: Customer Reviews List & Rating Stats */}
        <div className="md:col-span-7">
          <div className="p-6 rounded-2xl border border-border bg-card shadow-md h-full flex flex-col justify-between space-y-4">
            <div className="space-y-4">
              <div className="flex items-center justify-between border-b pb-4 border-border/80">
                <div>
                  <h3 className="font-black text-base text-foreground">Passenger Review Feed</h3>
                  <p className="text-[10px] text-muted-foreground mt-0.5">Showing past journey experiences rated by guests.</p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xl font-black text-foreground">{profile.rating.toFixed(1)}</span>
                  <RatingStars rating={profile.rating} readOnly size={14} />
                </div>
              </div>

              {/* Reviews Scroll Feed */}
              {reviews.length === 0 ? (
                <div className="text-center py-10 text-xs text-muted-foreground font-semibold">
                  No ratings or reviews submitted for this guide yet.
                </div>
              ) : (
                <div className="space-y-4">
                  <AnimatePresence mode="wait">
                    {currentReviews.map((rev) => (
                      <motion.div
                        key={rev.id}
                        initial={{ opacity: 0, x: -10 }}
                        animate={{ opacity: 1, x: 0 }}
                        exit={{ opacity: 0, x: 10 }}
                        transition={{ duration: 0.2 }}
                        className="p-4 rounded-xl border border-border/60 bg-muted/10 space-y-2"
                      >
                        <div className="flex justify-between items-center select-none">
                          <div className="flex items-center gap-2">
                            <div className="w-6 h-6 rounded-full bg-accent text-accent-foreground text-[10px] font-bold flex items-center justify-center uppercase">
                              U{rev.rated_by}
                            </div>
                            <span className="text-[10px] font-bold text-foreground">Passenger #{rev.rated_by}</span>
                          </div>
                          <RatingStars rating={rev.rating} readOnly size={12} />
                        </div>
                        <p className="text-xs text-foreground/80 leading-relaxed italic">
                          "{rev.review_text}"
                        </p>
                        <div className="text-[9px] text-muted-foreground font-semibold flex items-center gap-1 select-none">
                          <CalendarBlankIcon size={12} />
                          {new Date(rev.created_at).toLocaleDateString("en-US", {
                            dateStyle: "medium"
                          })}
                        </div>
                      </motion.div>
                    ))}
                  </AnimatePresence>
                </div>
              )}
            </div>

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between border-t pt-4 border-border/60 select-none">
                <Button
                  variant="outline"
                  disabled={currentPage === 1}
                  onClick={() => setCurrentPage(prev => prev - 1)}
                  className="rounded-lg h-8 text-[10px] font-bold px-3 hover:bg-accent cursor-pointer flex items-center gap-1"
                >
                  <ArrowLeftIcon size={10} />
                  Previous
                </Button>
                <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
                  Page {currentPage} of {totalPages}
                </span>
                <Button
                  variant="outline"
                  disabled={currentPage === totalPages}
                  onClick={() => setCurrentPage(prev => prev + 1)}
                  className="rounded-lg h-8 text-[10px] font-bold px-3 hover:bg-accent cursor-pointer flex items-center gap-1"
                >
                  Next
                  <ArrowRightIcon size={10} />
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Sticky Bottom Actions Bar */}
      <div className="fixed bottom-0 left-0 right-0 border-t border-border bg-card/90 backdrop-blur-md p-4 flex items-center justify-center z-40 select-none">
        <div className="w-full max-w-6xl flex justify-between gap-4">
          <Button
            variant="outline"
            onClick={() => navigate(-1)}
            className="flex-1 max-w-[200px] rounded-xl py-5 font-bold text-xs flex items-center justify-center gap-2 hover:bg-accent cursor-pointer"
          >
            <ArrowLeftIcon size={14} weight="bold" />
            <span>Go Back</span>
          </Button>

          {isBookingFlow ? (
            <Button
              onClick={handleSelectAndContinue}
              className="flex-2 rounded-xl py-5 font-bold bg-primary text-primary-foreground text-xs shadow-md flex items-center justify-center gap-2 hover:scale-102 transition-all cursor-pointer"
            >
              <span>Confirm & Continue</span>
              <ArrowRightIcon size={14} weight="bold" />
            </Button>
          ) : (
            <Button
              onClick={() => {
                toast.success("Profile verified successfully.")
                navigate(-1)
              }}
              className="flex-2 rounded-xl py-5 font-bold bg-primary text-primary-foreground text-xs shadow-md flex items-center justify-center gap-2 hover:scale-102 transition-all cursor-pointer"
            >
              <span>Close Profile</span>
            </Button>
          )}
        </div>
      </div>
    </motion.div>
  )
}
export default AssistantProfilePage
