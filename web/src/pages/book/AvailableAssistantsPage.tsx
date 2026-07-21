import React, { useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useBookingDraftStore } from "@/store/booking-draft.store"
import { useNearbyAssistants, useActiveBooking } from "@/hooks/useBookingQueries"
import { AssistantCard } from "@/components/booking/AssistantCard"
import type { Assistant } from "@/components/booking/AssistantCard"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { UserCheckIcon, ArrowLeftIcon, ArrowRightIcon, UsersIcon } from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

const containerVariants = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: {
      staggerChildren: 0.08,
    },
  },
}

const itemVariants = {
  hidden: { y: 15, opacity: 0 },
  show: { y: 0, opacity: 1, transition: { type: "spring" as const, stiffness: 100 } },
}

export function AvailableAssistantsPage() {
  const navigate = useNavigate()
  const { pickup, destination, selectedAssistant, setSelectedAssistant } = useBookingDraftStore()

  const { data: activeBooking } = useActiveBooking()

  useEffect(() => {
    if (!pickup || !destination) {
      toast.error("Please define pickup and destination routing coordinates first.")
      navigate("/book/pickup")
      return
    }

    if (activeBooking) {
      toast.error("You already have an active booking. Complete or cancel it before booking a new guide.")
      navigate("/bookings")
    }
  }, [pickup, destination, activeBooking, navigate])

  const { data: assistants = [], isLoading: loading } = useNearbyAssistants(pickup?.lat, pickup?.lng, 15)

  const handleSelectAssistant = (ast: Assistant) => {
    setSelectedAssistant({
      id: ast.id,
      user_id: ast.id,
      name: ast.name,
      verification_status: "APPROVED",
      is_online: true,
      rating: ast.rating,
      total_trips: ast.tripsCount,
      profile_photo_url: ast.avatar_url,
      created_at: new Date().toISOString(),
    })
  }

  const handleViewProfile = (ast: Assistant) => {
    navigate(`/assistant/${ast.id}`)
  }

  const LoadingSkeletons = () => (
    <div className="space-y-3.5">
      {[1, 2, 3].map((n) => (
        <div key={n} className="w-full p-4 border border-border bg-card/60 rounded-xl flex items-center gap-4 animate-pulse">
          <div className="w-14 h-14 bg-muted rounded-full" />
          <div className="flex-1 space-y-2">
            <div className="flex items-center justify-between">
              <div className="h-4 bg-muted rounded w-1/3" />
              <div className="h-4 bg-muted rounded w-1/6" />
            </div>
            <div className="h-3 bg-muted rounded w-1/2" />
            <div className="h-3 bg-muted rounded w-1/4" />
          </div>
        </div>
      ))}
    </div>
  )

  const EmptyState = () => (
    <div className="text-center py-12 border border-dashed border-border rounded-2xl bg-muted/10 space-y-3">
      <div className="p-3 bg-muted/40 rounded-full w-fit mx-auto text-muted-foreground">
        <UsersIcon size={28} />
      </div>
      <div className="text-sm font-extrabold text-foreground">No available guides nearby</div>
      <p className="text-xs text-muted-foreground max-w-sm mx-auto px-4">
        We couldn't locate any available, unassigned guides within a 15km pickup radius. Guides currently on active rides are excluded.
      </p>
    </div>
  )

  return (
    <motion.div
      initial={{ x: 50, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      exit={{ x: -50, opacity: 0 }}
      transition={{ duration: 0.3 }}
      className="w-full animate-fade-in"
    >
      <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-2xl p-4 flex flex-col justify-between">
        <CardHeader>
          <CardTitle className="text-xl font-bold flex items-center gap-2">
            <UserCheckIcon size={24} className="text-primary" />
            Choose Your Escort Guide
          </CardTitle>
          <CardDescription>
            Verified, unassigned guides currently online within your pickup radius. Guides on active rides are excluded.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4 pt-2 flex-1">
          {loading ? (
            <LoadingSkeletons />
          ) : assistants.length === 0 ? (
            <EmptyState />
          ) : (
            <motion.div
              variants={containerVariants}
              initial="hidden"
              animate="show"
              className="flex flex-col gap-3.5"
            >
              {assistants.map((ast: Assistant) => {
                const isSelected = selectedAssistant?.id === ast.id
                return (
                  <motion.div key={ast.id} variants={itemVariants}>
                    <AssistantCard
                      assistant={ast}
                      selected={isSelected}
                      onClick={() => handleSelectAssistant(ast)}
                      onViewProfile={() => handleViewProfile(ast)}
                    />
                  </motion.div>
                )
              })}
            </motion.div>
          )}
        </CardContent>
        <div className="p-6 border-t border-border flex gap-3 mt-4">
          <Button
            variant="outline"
            onClick={() => navigate("/book/schedule")}
            className="flex-1 rounded-xl py-5 font-bold text-xs flex items-center justify-center gap-2 hover:bg-accent cursor-pointer"
          >
            <ArrowLeftIcon size={14} weight="bold" />
            <span>Back</span>
          </Button>
          <Button
            onClick={() => navigate("/book/summary")}
            disabled={!selectedAssistant}
            className="flex-2 rounded-xl py-5 font-bold bg-primary text-primary-foreground text-xs shadow-md flex items-center justify-center gap-2 hover:scale-102 transition-all cursor-pointer"
          >
            <span>Proceed to Confirmation</span>
            <ArrowRightIcon size={14} weight="bold" />
          </Button>
        </div>
      </Card>
    </motion.div>
  )
}
export default AvailableAssistantsPage
