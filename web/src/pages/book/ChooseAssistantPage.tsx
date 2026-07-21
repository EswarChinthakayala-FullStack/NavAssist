import React, { useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useBookingDraftStore } from "@/store/booking-draft.store"
import { useNearbyAssistants, useActiveBooking } from "@/hooks/useBookingQueries"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { UserCheckIcon, StarIcon, MapPinIcon, ArrowLeftIcon, ArrowRightIcon } from "@phosphor-icons/react"
import { toast } from "sonner"

export function ChooseAssistantPage() {
  const navigate = useNavigate()
  const { pickup, destination, selectedAssistant, setSelectedAssistant } = useBookingDraftStore()

  const { data: activeBooking } = useActiveBooking()

  useEffect(() => {
    if (!pickup || !destination) {
      toast.error("Please complete the path routing step first.")
      navigate("/book/pickup")
      return
    }

    if (activeBooking) {
      toast.error("You already have an active booking. Complete or cancel it before booking a new guide.")
      navigate("/bookings")
    }
  }, [pickup, destination, activeBooking, navigate])

  const { data: assistants = [], isLoading: loading } = useNearbyAssistants(pickup?.lat, pickup?.lng, 15)

  const handleSelect = (ast: any) => {
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

  const handleContinue = () => {
    if (!selectedAssistant) {
      toast.error("Please pick a guide assistant to accompany you.")
      return
    }
    navigate("/book/confirm")
  }

  return (
    <div className="w-full flex flex-col gap-6">
      <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-2xl p-4 flex flex-col justify-between">
        <CardHeader>
          <CardTitle className="text-xl font-bold flex items-center gap-2">
            <UserCheckIcon size={24} className="text-primary" />
            Select Your Assistant
          </CardTitle>
          <CardDescription>
            Choose from online verified assistants within your pickup radius. Guides on active rides are excluded.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4 pt-2 flex-1">
          {loading ? (
            <div className="flex flex-col items-center justify-center py-10 gap-3">
              <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin" />
              <span className="text-xs text-muted-foreground font-semibold uppercase tracking-wider">Locating nearby guides...</span>
            </div>
          ) : assistants.length === 0 ? (
            <div className="text-center py-10 border border-dashed border-border rounded-2xl space-y-2 bg-muted/10">
              <div className="text-sm font-extrabold text-foreground">No online guides found nearby</div>
              <p className="text-xs text-muted-foreground max-w-sm mx-auto px-4">
                There are currently no unassigned guides active near your pickup coordinate. Please try again shortly.
              </p>
            </div>
          ) : (
            <div className="flex flex-col gap-3.5">
              {assistants.map((ast: any) => {
                const isSelected = selectedAssistant?.id === ast.id
                return (
                  <button
                    key={ast.id}
                    onClick={() => handleSelect(ast)}
                    className={`w-full text-left p-4 border rounded-2xl flex items-center justify-between transition-all duration-300 cursor-pointer ${
                      isSelected
                        ? "border-primary bg-primary/5 ring-2 ring-primary/20 scale-101"
                        : "border-border hover:border-muted-foreground bg-card"
                    }`}
                  >
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 bg-accent text-accent-foreground rounded-full flex items-center justify-center font-black text-sm uppercase tracking-wide">
                        {ast.name ? ast.name.split(" ").map((p: string) => p[0]).join("") : "AS"}
                      </div>
                      
                      <div className="space-y-1">
                        <div className="font-extrabold text-xs text-foreground flex items-center gap-2">
                          <span>{ast.name}</span>
                          <span className="bg-success/15 border border-success/30 text-success text-[8px] font-black uppercase tracking-wider px-1.5 py-0.5 rounded-full select-none">
                            Verified
                          </span>
                        </div>
                        
                        <div className="flex items-center gap-3 text-[10px] text-muted-foreground font-semibold">
                          <span className="flex items-center gap-1">
                            <StarIcon size={12} weight="fill" className="text-amber-500" />
                            {ast.rating > 0 ? ast.rating.toFixed(1) : "New"}
                          </span>
                          <span>•</span>
                          <span>{ast.tripsCount > 0 ? `${ast.tripsCount} trips` : "No trips yet"}</span>
                        </div>
                      </div>
                    </div>

                    <div className="text-right space-y-1">
                      <div className="text-[10px] text-muted-foreground font-semibold flex items-center gap-1 justify-end">
                        <MapPinIcon size={12} />
                        {ast.distance_km} km away
                      </div>
                      {isSelected && (
                        <div className="text-[9px] text-primary font-black uppercase tracking-wider">
                          Selected helper
                        </div>
                      )}
                    </div>
                  </button>
                )
              })}
            </div>
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
            onClick={handleContinue}
            disabled={!selectedAssistant}
            className="flex-2 rounded-xl py-5 font-bold bg-primary text-primary-foreground text-xs shadow-md flex items-center justify-center gap-2 hover:scale-102 transition-all cursor-pointer"
          >
            <span>Confirm Booking</span>
            <ArrowRightIcon size={14} weight="bold" />
          </Button>
        </div>
      </Card>
    </div>
  )
}
export default ChooseAssistantPage
