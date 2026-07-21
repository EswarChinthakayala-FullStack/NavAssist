import React, { useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useGeolocation } from "@/hooks/useGeolocation"
import { MapPinIcon, NavigationArrowIcon } from "@phosphor-icons/react"
import { Button } from "@/components/ui/button"

export function LocationPermissionPage() {
  const navigate = useNavigate()
  const { latitude, longitude, permissionState, getPosition, error } = useGeolocation()

  useEffect(() => {
    // If permission is already granted, auto redirect to home
    if (permissionState === "granted" && latitude && longitude) {
      navigate("/home", { replace: true })
    }
  }, [permissionState, latitude, longitude, navigate])

  const handleGrant = () => {
    getPosition()
    // Small delay to allow location callback, then go home
    setTimeout(() => {
      navigate("/home", { replace: true })
    }, 1500)
  }

  return (
    <div className="min-h-[calc(100vh-10rem)] w-full flex flex-col items-center justify-center p-6 relative overflow-hidden">
      {/* Dot matrix background pattern */}
      <div className="absolute inset-0 opacity-[0.05] bg-[radial-gradient(circle_at_center,var(--foreground)_10%,transparent_50%)] bg-[size:24px_24px] pointer-events-none" />
      
      {/* Radial glow */}
      <div className="absolute inset-0 opacity-20 bg-[radial-gradient(ellipse_at_top_right,hsl(var(--primary)/0.25),transparent_60%)] pointer-events-none" />

      <div className="max-w-sm w-full bg-card border border-border rounded-3xl p-8 shadow-2xl relative z-10 text-center flex flex-col gap-6 items-center animate-in fade-in zoom-in-95 duration-200">
        <div className="w-16 h-16 bg-primary/10 rounded-2xl flex items-center justify-center text-primary animate-bounce">
          <MapPinIcon size={38} weight="fill" />
        </div>

        <div className="flex flex-col gap-2">
          <span className="text-[10px] font-black uppercase tracking-widest text-primary">
            Location Settings Required
          </span>
          <h3 className="text-xl font-black tracking-tight text-foreground">
            Allow Location Access
          </h3>
          <p className="text-xs text-muted-foreground leading-relaxed mt-2">
            NavAssist requires active GPS coordinates to matching you with nearby assistants and rendering live OSRM routes.
          </p>
        </div>

        {error && (
          <span className="text-[10px] text-destructive bg-destructive/5 border border-destructive/10 px-3 py-1.5 rounded-lg w-full font-bold leading-normal">
            GPS Error: {error}
          </span>
        )}

        {permissionState === "denied" && (
          <div className="p-4 bg-amber-500/10 border border-amber-500/20 text-amber-500 text-[10px] font-bold rounded-xl text-left flex flex-col gap-2 w-full">
            <span>Location permission denied. Manual address searches will be used instead of auto-matching.</span>
            <button
              onClick={() => navigate("/home", { replace: true })}
              className="text-[9px] font-black uppercase tracking-wider text-primary hover:underline self-start mt-1 cursor-pointer"
            >
              Continue anyway &rarr;
            </button>
          </div>
        )}

        <div className="flex flex-col gap-3 w-full mt-2">
          {permissionState !== "denied" && (
            <Button
              onClick={handleGrant}
              className="w-full py-5 rounded-xl font-extrabold text-xs shadow-sm hover:scale-102 transition-all flex items-center justify-center gap-1.5"
            >
              <NavigationArrowIcon size={14} weight="fill" />
              Enable Geolocation GPS
            </Button>
          )}

          <Button
            variant="ghost"
            onClick={() => navigate("/home", { replace: true })}
            className="w-full text-xs text-muted-foreground hover:bg-transparent"
          >
            Skip for now
          </Button>
        </div>
      </div>
    </div>
  )
}
export default LocationPermissionPage
