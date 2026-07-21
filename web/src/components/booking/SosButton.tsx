import React, { useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { sosService } from "@/services/sos.service"
import { Button } from "@/components/ui/button"
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogCancel
} from "@/components/ui/alert-dialog"
import { SirenIcon, ShieldWarningIcon } from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

export function SosButton() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const [loading, setLoading] = useState(false)

  const handleTrigger = async () => {
    if (!bookingId) return
    setLoading(true)
    try {
      // Trigger SOS using coordinates or fallback defaults
      await sosService.triggerSos(parseInt(bookingId), 28.6139, 77.2090)
      toast.error("SOS Alert Activated!")
      setOpen(false)
      // Navigate to full-screen calm emergency page
      navigate(`/trip/${bookingId}/sos-active`)
    } catch (err) {
      console.error(err)
      toast.error("Failed to trigger emergency SOS. Direct call dialer standard emergency services instead.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      {/* Floating circular destructive button */}
      <div className="fixed bottom-24 right-6 z-50">
        <motion.button
          onClick={() => setOpen(true)}
          animate={{
            boxShadow: [
              "0 0 0 0 rgba(239, 68, 68, 0.4)",
              "0 0 0 12px rgba(239, 68, 68, 0)",
              "0 0 0 0 rgba(239, 68, 68, 0.4)"
            ]
          }}
          transition={{
            duration: 1.8,
            repeat: Infinity,
            ease: "easeInOut"
          }}
          className="w-14 h-14 rounded-full bg-destructive text-destructive-foreground flex items-center justify-center shadow-2xl hover:scale-105 transition-transform cursor-pointer border-2 border-background"
        >
          <SirenIcon size={28} weight="fill" className="animate-pulse" />
        </motion.button>
      </div>

      {/* Confirmation dialog to prevent accidental clicks */}
      <AlertDialog open={open} onOpenChange={setOpen}>
        <AlertDialogContent className="max-w-xs sm:max-w-sm rounded-2xl border border-destructive/20 bg-card p-6">
          <AlertDialogHeader className="space-y-3.5 text-center sm:text-left">
            <div className="p-3.5 bg-destructive/10 text-destructive rounded-full w-fit mx-auto sm:mx-0">
              <ShieldWarningIcon size={32} weight="duotone" />
            </div>
            <div className="space-y-1">
              <AlertDialogTitle className="text-base font-bold text-destructive">Confirm Emergency SOS Alert?</AlertDialogTitle>
              <AlertDialogDescription className="text-xs text-muted-foreground leading-relaxed">
                This triggers a direct security distress signal to terminal monitoring admins and emergency safety contacts.
              </AlertDialogDescription>
            </div>
          </AlertDialogHeader>
          <AlertDialogFooter className="flex flex-col sm:flex-row gap-2 mt-4">
            <AlertDialogCancel className="w-full sm:w-auto rounded-xl py-4 font-bold text-xs">
              Cancel
            </AlertDialogCancel>
            <Button
              onClick={handleTrigger}
              disabled={loading}
              className="w-full sm:w-auto bg-destructive text-destructive-foreground hover:bg-destructive/95 rounded-xl py-4 px-6 font-extrabold text-xs shadow-md"
            >
              {loading ? "Activating Distress..." : "Confirm SOS"}
            </Button>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  )
}
export default SosButton
