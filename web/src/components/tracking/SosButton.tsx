import React, { useState, useRef, useEffect } from "react"
import { SirenIcon, WarningIcon, XIcon, ShieldIcon } from "@phosphor-icons/react"
import { Button } from "@/components/ui/button"
import { api } from "@/services/api"
import { toast } from "sonner"
import { Dialog } from "@base-ui/react/dialog"

interface SosButtonProps {
  bookingId: number
  latitude?: number
  longitude?: number
}

export function SosButton({ bookingId, latitude = 12.9716, longitude = 77.5946 }: SosButtonProps) {
  const [isOpen, setIsOpen] = useState(false)
  const [isHolding, setIsHolding] = useState(false)
  const [holdProgress, setHoldProgress] = useState(0)
  const [triggering, setTriggering] = useState(false)
  const timerRef = useRef<any>(null)

  const handleHoldStart = () => {
    setIsHolding(true)
    setHoldProgress(0)
  }

  const handleHoldEnd = () => {
    setIsHolding(false)
    setHoldProgress(0)
    if (timerRef.current) {
      clearInterval(timerRef.current)
    }
  }

  useEffect(() => {
    if (isHolding) {
      const interval = 20 // ms
      const step = (interval / 1500) * 100 // 1.5s total hold time
      
      timerRef.current = setInterval(() => {
        setHoldProgress((prev) => {
          if (prev >= 100) {
            clearInterval(timerRef.current!)
            setIsHolding(false)
            triggerSosAlert()
            return 100
          }
          return prev + step
        })
      }, interval)
    } else {
      if (timerRef.current) {
        clearInterval(timerRef.current)
      }
    }

    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current)
      }
    }
  }, [isHolding])

  const triggerSosAlert = async () => {
    setTriggering(true)
    try {
      // Resolve navigator coordinates if possible
      let currentLat = latitude
      let currentLng = longitude
      
      if (navigator.geolocation) {
        await new Promise<void>((resolve) => {
          navigator.geolocation.getCurrentPosition(
            (pos) => {
              currentLat = pos.coords.latitude
              currentLng = pos.coords.longitude
              resolve()
            },
            () => resolve(), // fallback silently
            { timeout: 3000 }
          )
        })
      }

      const res = await api.post("/sos/trigger", {
        booking_id: bookingId,
        latitude: currentLat,
        longitude: currentLng
      })
      
      toast.error("SOS ALERT SENT! Authorities and contacts have been notified.", {
        duration: 10000,
        description: "NavAssist safety response team is calling your phone now."
      })
      setIsOpen(false)
    } catch (err: any) {
      // sonner will automatically toast backend validation messages if configured, 
      // but let's override with emergency feedback
      const msg = err.response?.data?.detail || "Could not trigger emergency SOS broadcast."
      toast.error(msg)
    } finally {
      setTriggering(false)
    }
  }

  return (
    <>
      {/* Floating Panic Pulsing SOS trigger */}
      <button
        onClick={() => setIsOpen(true)}
        className="fixed bottom-24 right-6 z-40 bg-destructive text-destructive-foreground p-4 rounded-full shadow-2xl hover:scale-105 active:scale-95 transition-all flex items-center justify-center border-4 border-background focus:outline-none"
      >
        <span className="absolute -inset-1.5 rounded-full border-2 border-destructive/60 animate-ping pointer-events-none" />
        <span className="absolute -inset-3 rounded-full border border-destructive/20 animate-pulse pointer-events-none" />
        <SirenIcon size={28} weight="fill" className="animate-bounce" />
      </button>

      {/* Confirmation Modal */}
      {isOpen && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-card text-foreground border border-border max-w-sm w-full rounded-2xl p-6 shadow-2xl relative flex flex-col gap-4 animate-in fade-in zoom-in-95 duration-200">
            <button
              onClick={() => setIsOpen(false)}
              className="absolute right-4 top-4 text-muted-foreground hover:text-foreground cursor-pointer"
            >
              <XIcon size={18} />
            </button>

            <div className="flex flex-col items-center text-center gap-3">
              <div className="w-16 h-16 bg-destructive/15 text-destructive rounded-full flex items-center justify-center animate-pulse">
                <ShieldIcon size={32} weight="fill" />
              </div>
              <h3 className="text-lg font-black uppercase tracking-wider text-destructive">Emergency SOS</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                This triggers a broadcast alerting our security center and your emergency contact pins.
              </p>
            </div>

            {/* Press and Hold trigger panel */}
            <div className="flex flex-col gap-3 mt-2">
              <button
                onMouseDown={handleHoldStart}
                onMouseUp={handleHoldEnd}
                onMouseLeave={handleHoldEnd}
                onTouchStart={handleHoldStart}
                onTouchEnd={handleHoldEnd}
                disabled={triggering}
                className="relative overflow-hidden w-full py-5 bg-destructive hover:bg-destructive/95 text-white font-extrabold text-sm rounded-xl shadow-md transition-all active:scale-98 select-none touch-none flex items-center justify-center"
              >
                {/* Hold fill progress indicator bar */}
                <div 
                  className="absolute left-0 top-0 bottom-0 bg-black/20 transition-all duration-75"
                  style={{ width: `${holdProgress}%` }}
                />
                
                <span className="relative z-10">
                  {triggering 
                    ? "TRANSMITTING ALERTS..." 
                    : isHolding 
                      ? "HOLD TO CONFIRM..." 
                      : "PRESS & HOLD FOR 1.5S"}
                </span>
              </button>

              <Button
                variant="ghost"
                onClick={() => setIsOpen(false)}
                className="w-full text-xs text-muted-foreground hover:bg-transparent"
                disabled={triggering}
              >
                Cancel & Dismiss
              </Button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
export default SosButton
