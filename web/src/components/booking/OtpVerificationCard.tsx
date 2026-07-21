import React, { useState } from "react"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { ShieldCheckIcon, NavigationArrowIcon, WarningCircleIcon } from "@phosphor-icons/react"
import { toast } from "sonner"
import { api } from "@/services/api"

interface OtpVerificationCardProps {
  bookingId: number
  onSuccess?: () => void
  className?: string
}

export function OtpVerificationCard({ bookingId, onSuccess, className }: OtpVerificationCardProps) {
  const [otpInput, setOtpInput] = useState("")
  const [submitting, setSubmitting] = useState(false)
  const [verified, setVerified] = useState(false)

  const handleVerify = async (e?: React.FormEvent, customOtp?: string) => {
    if (e) e.preventDefault()

    const codeToVerify = (customOtp !== undefined ? customOtp : otpInput).replace(/\D/g, "").slice(0, 6)
    if (codeToVerify.length !== 6) {
      toast.error("Please enter a valid 6-digit OTP code.")
      return
    }

    setSubmitting(true)

    try {
      await api.patch(`/bookings/${bookingId}/status`, {
        status: "in_progress",
        otp: codeToVerify,
      })

      setVerified(true)
      toast.success("OTP verified successfully! Journey in progress.")
      setOtpInput("")
      if (onSuccess) onSuccess()
    } catch (err: any) {
      console.error("OTP verification error:", err)
      const detail = err.response?.data?.detail
      let msg = "Incorrect OTP code. Ask guest for their 6-digit start code."
      if (typeof detail === "string") {
        msg = detail
      } else if (Array.isArray(detail)) {
        msg = detail.map((d: any) => (typeof d === "string" ? d : d.msg || JSON.stringify(d))).join("; ")
      } else if (detail && typeof detail === "object") {
        msg = detail.msg || JSON.stringify(detail)
      }
      toast.error(msg)
    } finally {
      setSubmitting(false)
    }
  }

  const handleChange = (val: string) => {
    const clean = val.replace(/\D/g, "").slice(0, 6)
    setOtpInput(clean)
    if (clean.length === 6 && !submitting) {
      handleVerify(undefined, clean)
    }
  }

  return (
    <div className={`w-full rounded-xl border border-emerald-500/30 bg-gradient-to-r from-emerald-500/15 via-card to-background p-2 px-3 shadow-md flex items-center justify-between gap-2 text-left relative overflow-hidden backdrop-blur-md z-30 pointer-events-auto ${className}`}>
      {/* Left: Shield Icon & Inline Input */}
      <div className="flex items-center gap-2 flex-1 min-w-0 pointer-events-auto">
        <div className="p-1.5 rounded-lg bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 shrink-0">
          <ShieldCheckIcon size={16} weight="fill" />
        </div>
        
        <form onSubmit={handleVerify} className="flex items-center gap-2 flex-1 min-w-0">
          <Input
            type="text"
            inputMode="numeric"
            pattern="[0-9]*"
            autoComplete="one-time-code"
            value={otpInput}
            onChange={(e) => handleChange(e.target.value)}
            placeholder="6-Digit OTP"
            maxLength={6}
            className="h-8 text-xs font-mono font-black tracking-widest bg-background/90 border-emerald-500/40 text-emerald-400 text-center rounded-lg focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/30 min-w-0 flex-1 px-2 pointer-events-auto cursor-text select-text"
            autoFocus
          />

          <Button
            type="submit"
            disabled={submitting || otpInput.length !== 6}
            className="h-8 px-2.5 text-[10px] font-black shadow-xs bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white rounded-lg flex items-center gap-1 cursor-pointer shrink-0 disabled:opacity-50 transition-all active:scale-95"
          >
            <NavigationArrowIcon size={12} weight="bold" />
            <span>{submitting ? "..." : "Verify & Start"}</span>
          </Button>
        </form>
      </div>
    </div>
  )
}
export default OtpVerificationCard
