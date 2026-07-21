import React, { useEffect, useState } from "react"
import { useParams } from "react-router-dom"
import { api } from "@/services/api"
import { LiveMap } from "@/components/tracking/LiveMap"
import { ProgressTimeline } from "@/components/tracking/ProgressTimeline"
import { EtaBadge } from "@/components/tracking/EtaBadge"
import { ShieldCheckIcon, SirenIcon, WarningOctagonIcon } from "@phosphor-icons/react"
import { Button } from "@/components/ui/button"

interface PublicTrackingData {
  booking_id: number
  status: "PENDING" | "ACCEPTED" | "STARTED" | "COMPLETED" | "CANCELLED"
  latitude: number
  longitude: number
  source: string
}

export function PublicTrackingPage() {
  const { token } = useParams<{ token: string }>()
  const [data, setData] = useState<PublicTrackingData | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const fetchPublicTelemetry = async () => {
    try {
      const res = await api.get(`/share/public/${token}`)
      setData(res.data)
      setError(null)
    } catch (err: any) {
      const msg = err.response?.data?.detail || "This live tracking link is expired, disabled, or invalid."
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchPublicTelemetry()
    // Poll updates every 10 seconds for real-time tracking feel
    const interval = setInterval(fetchPublicTelemetry, 10000)
    return () => clearInterval(interval)
  }, [token])

  if (loading) {
    return (
      <div className="min-h-screen w-screen bg-background flex flex-col items-center justify-center gap-4">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-muted-foreground">Connecting Public Live Stream...</span>
      </div>
    )
  }

  if (error || !data) {
    return (
      <div className="min-h-screen w-screen bg-background relative overflow-hidden flex items-center justify-center p-6">
        {/* Dot pattern background */}
        <div className="absolute inset-0 opacity-[0.06] bg-[radial-gradient(circle_at_center,var(--foreground)_10%,transparent_50%)] bg-[size:24px_24px] pointer-events-none" />
        
        <div className="max-w-md w-full bg-card border border-border rounded-2xl p-8 shadow-2xl relative z-10 text-center flex flex-col gap-6 items-center">
          <div className="w-16 h-16 bg-destructive/10 text-destructive rounded-full flex items-center justify-center">
            <WarningOctagonIcon size={32} weight="fill" />
          </div>
          <div>
            <h3 className="text-lg font-black tracking-tight text-foreground uppercase">Tracking Link Unavailable</h3>
            <p className="text-xs text-muted-foreground mt-2 leading-relaxed">
              {error || "The active trip sharing session has expired, been revoked, or completed."}
            </p>
          </div>
          <Button onClick={() => window.close()} className="w-full py-5 rounded-xl font-bold bg-primary text-white">
            Close Window
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen w-screen bg-background text-foreground flex flex-col relative overflow-hidden">
      {/* Dot matrix background pattern */}
      <div className="absolute inset-0 opacity-[0.05] bg-[radial-gradient(circle_at_center,var(--foreground)_10%,transparent_50%)] bg-[size:24px_24px] pointer-events-none" />

      {/* Header bar */}
      <header className="h-16 shrink-0 border-b bg-card/40 backdrop-blur-md flex items-center justify-between px-6 relative z-10">
        <div className="flex items-center gap-2">
          <div className="p-2 bg-success/15 text-success rounded-lg">
            <ShieldCheckIcon size={20} weight="fill" />
          </div>
          <div>
            <span className="text-xs font-black uppercase tracking-widest text-success">NavAssist Live</span>
            <h4 className="text-xs text-muted-foreground leading-tight -mt-0.5">Secure Share Link Active</h4>
          </div>
        </div>
        
        <div className="flex items-center gap-2">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-success opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-success"></span>
          </span>
          <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Live Position Broadcast</span>
        </div>
      </header>

      {/* Main layout split view */}
      <div className="flex-1 flex flex-col lg:flex-row overflow-hidden relative z-10">
        {/* Left: Map telemetry */}
        <div className="flex-1 relative h-[50vh] lg:h-auto border-b lg:border-b-0 lg:border-r">
          <LiveMap
            pickupLat={data.latitude}
            pickupLon={data.longitude}
            assistantLat={data.latitude}
            assistantLon={data.longitude}
            status={data.status}
          />
        </div>

        {/* Right: Info side panel */}
        <div className="w-full lg:w-96 shrink-0 bg-card/60 backdrop-blur-md p-6 flex flex-col gap-6 overflow-y-auto">
          <div>
            <h3 className="font-extrabold text-base leading-tight">Escort Status</h3>
            <p className="text-[10px] text-muted-foreground mt-0.5">Tracking updates for Booking ID: #{data.booking_id}</p>
          </div>

          <EtaBadge etaMins={12} distanceKm={1.8} className="mt-1" />

          <hr className="border-border" />

          <div>
            <h4 className="text-xs font-black uppercase tracking-wider text-muted-foreground mb-4">Journey Progress</h4>
            <ProgressTimeline currentStatus={data.status} />
          </div>

          <hr className="border-border" />

          {/* Safety alert message */}
          <div className="p-4 bg-muted/40 border border-border rounded-xl flex gap-3 items-start">
            <SirenIcon size={18} weight="fill" className="text-destructive shrink-0 mt-0.5" />
            <div>
              <h5 className="text-[10px] font-black uppercase tracking-wider text-destructive">Emergency Ready</h5>
              <p className="text-[9px] text-muted-foreground mt-1 leading-relaxed">
                If the traveler triggers their local SOS device, NavAssist security response will instantly mobilize.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
export default PublicTrackingPage
