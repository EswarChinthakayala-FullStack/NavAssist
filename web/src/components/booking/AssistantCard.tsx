import React from "react"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { StarIcon, ShieldCheckIcon, MapPinIcon } from "@phosphor-icons/react"
import { cn } from "@/lib/utils"
import { motion } from "framer-motion"
import { ImageThumbnail } from "@/components/shared/ImageThumbnail"

export interface Assistant {
  id: number
  name: string
  rating: number
  tripsCount: number
  distance_km: number
  eta_mins: number
  avatar_url?: string
}

interface AssistantCardProps {
  assistant: Assistant
  selected: boolean
  onClick: () => void
  onViewProfile?: () => void
}

export function AssistantCard({ assistant, selected, onClick, onViewProfile }: AssistantCardProps) {
  const avatar = assistant.avatar_url || "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=80"

  return (
    <Card
      onClick={onClick}
      className={cn(
        "cursor-pointer transition-all duration-300 rounded-xl overflow-hidden border shadow-md",
        selected
          ? "border-primary bg-primary/5 ring-2 ring-primary/20 shadow-lg scale-[1.02]"
          : "border-border bg-card hover:border-primary/40 hover:shadow-md hover:-translate-y-0.5"
      )}
    >
      <CardContent className="p-4 flex gap-4 items-center">
        <motion.div
          layoutId={`avatar-${assistant.id}`}
          className="w-14 h-14 rounded-full overflow-hidden border border-border bg-muted flex-shrink-0"
        >
          <ImageThumbnail
            url={avatar}
            alt={assistant.name}
            aspectRatio="circle"
            metadata={{
              title: `${assistant.name} - Profile`,
              documentType: "Guide Avatar",
              uploadedBy: "Assistant",
              resolution: "800 x 800",
              fileSize: "142 KB"
            }}
          />
        </motion.div>

        {/* Details column */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-1.5 justify-between">
            <h4 className="font-extrabold text-sm text-foreground truncate">{assistant.name}</h4>
            <div className="flex items-center gap-2">
              <Badge className="bg-success text-success-foreground hover:bg-success border-0 text-[10px] px-2 py-0.5 rounded-full flex items-center gap-0.5 font-bold">
                <ShieldCheckIcon size={12} weight="fill" />
                Verified
              </Badge>
              {onViewProfile && (
                <span
                  onClick={(e) => {
                    e.stopPropagation()
                    onViewProfile()
                  }}
                  className="text-[10px] font-bold text-primary hover:underline cursor-pointer bg-transparent border-0 p-0"
                >
                  View Profile
                </span>
              )}
            </div>
          </div>
          
          <div className="flex items-center gap-4 mt-2 text-xs text-muted-foreground">
            <span className="flex items-center gap-0.5 font-bold text-warning">
              <StarIcon size={14} weight="fill" />
              {assistant.rating > 0 ? assistant.rating.toFixed(1) : "New"}
            </span>
            <span className="font-semibold">{assistant.tripsCount > 0 ? `${assistant.tripsCount} Completed Trips` : "No trips yet"}</span>
          </div>

          <div className="flex items-center gap-3.5 mt-2.5 text-[10px] text-muted-foreground font-bold uppercase tracking-wider">
            <span className="flex items-center gap-1">
              <MapPinIcon size={12} className="text-primary" />
              {assistant.distance_km.toFixed(1)} km away
            </span>
            <span>•</span>
            <span className="text-primary font-extrabold">ETA: {assistant.eta_mins} mins</span>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
export default AssistantCard
