import React, { useState } from "react"
import { trackingService } from "@/services/tracking.service"
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  SheetTrigger
} from "@/components/ui/sheet"
import { Button } from "@/components/ui/button"
import { ShareNetworkIcon, CopyIcon, CheckIcon } from "@phosphor-icons/react"
import { toast } from "sonner"

// Custom hook to handle share link generation as requested
function useGenerateShareLink() {
  const [loading, setLoading] = useState(false)

  const generateLink = async (bookingId: number) => {
    setLoading(true)
    try {
      const res = await trackingService.generateShareLink(bookingId)
      return `${window.location.origin}/track/${res.token || res.share_token}`
    } finally {
      setLoading(false)
    }
  }

  return { generateLink, loading }
}

interface ShareTripSheetProps {
  bookingId: number
  trigger?: React.ReactNode
}

export function ShareTripSheet({ bookingId, trigger }: ShareTripSheetProps) {
  const { generateLink, loading } = useGenerateShareLink()
  const [copied, setCopied] = useState(false)
  const [shareUrl, setShareUrl] = useState("")

  const handleShare = async () => {
    try {
      const url = await generateLink(bookingId)
      setShareUrl(url)

      // Try using the native Web Share API first
      if (navigator.share) {
        try {
          await navigator.share({
            title: "Track my NavAssist Escort Journey",
            text: "I am sharing my live NavAssist journey. Track my GPS coordinates in real-time here:",
            url: url
          })
          toast.success("Journey shared successfully!")
          return
        } catch (shareErr) {
          // User cancelled or share failed, fallback to copy/sheet display
          console.log("Web Share API rejected or failed, falling back", shareErr)
        }
      }

      // Fallback copy to clipboard
      await navigator.clipboard.writeText(url)
      setCopied(true)
      toast.success("Secure sharing link copied to clipboard!")
      setTimeout(() => setCopied(false), 2000)

    } catch (err) {
      console.error(err)
      toast.error("Failed to generate secure tracking link.")
    }
  }

  return (
    <Sheet>
      <SheetTrigger>
        {trigger || (
          <Button variant="outline" className="rounded-xl font-semibold text-xs gap-1.5 flex items-center justify-center cursor-pointer">
            <ShareNetworkIcon size={16} />
            <span>Share Trip</span>
          </Button>
        )}
      </SheetTrigger>
      <SheetContent side="bottom" className="rounded-t-3xl border-t border-border/80 bg-card p-6 max-w-lg mx-auto">
        <SheetHeader className="text-left space-y-2">
          <SheetTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
            <ShareNetworkIcon size={18} className="text-primary" />
            Share Trip Coordinates
          </SheetTitle>
          <SheetDescription className="text-xs text-muted-foreground leading-relaxed">
            Generate and copy a secure link to let family or friends monitor your live travel progress in real-time.
          </SheetDescription>
        </SheetHeader>

        <div className="py-6 space-y-4">
          <Button
            onClick={handleShare}
            disabled={loading}
            className="w-full bg-primary text-primary-foreground hover:bg-primary/95 rounded-2xl py-6 font-extrabold text-xs shadow-md flex items-center justify-center gap-2 cursor-pointer"
          >
            <ShareNetworkIcon size={18} weight="bold" />
            <span>{loading ? "Generating Track Link..." : "Share Trip with Family"}</span>
          </Button>

          {shareUrl && (
            <div className="flex items-center gap-2 p-3 bg-muted/40 border border-border/80 rounded-2xl">
              <span className="flex-1 text-[10px] font-mono text-muted-foreground truncate select-all">{shareUrl}</span>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => {
                  navigator.clipboard.writeText(shareUrl)
                  setCopied(true)
                  toast.success("Link copied!")
                  setTimeout(() => setCopied(false), 2000)
                }}
                className="shrink-0 rounded-xl"
              >
                {copied ? <CheckIcon size={16} className="text-success" /> : <CopyIcon size={16} />}
              </Button>
            </div>
          )}
        </div>
      </SheetContent>
    </Sheet>
  )
}
export default ShareTripSheet
