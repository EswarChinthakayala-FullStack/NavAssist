import React, { useState, useEffect } from "react"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { KeyIcon, CopyIcon, CheckIcon, EyeIcon, EyeSlashIcon, ClockIcon } from "@phosphor-icons/react"
import { toast } from "sonner"

interface RideOtpCardProps {
  otp: string
  expirySeconds?: number
  className?: string
}

export function RideOtpCard({ otp, expirySeconds = 300, className }: RideOtpCardProps) {
  const [copied, setCopied] = useState(false)
  const [showOtp, setShowOtp] = useState(true)
  const [timeLeft, setTimeLeft] = useState(expirySeconds)

  useEffect(() => {
    if (timeLeft <= 0) return
    const timer = setInterval(() => {
      setTimeLeft((prev) => (prev > 0 ? prev - 1 : 0))
    }, 1000)
    return () => clearInterval(timer)
  }, [timeLeft])

  const formatTime = (secs: number) => {
    const m = Math.floor(secs / 60)
    const s = secs % 60
    return `${m}:${s < 10 ? "0" : ""}${s}`
  }

  const handleCopy = () => {
    if (!otp) return
    navigator.clipboard.writeText(otp)
    setCopied(true)
    toast.success("OTP copied to clipboard!")
    setTimeout(() => setCopied(false), 2000)
  }

  if (!otp) return null

  return (
    <div className={`w-full rounded-xl border border-amber-500/30 bg-gradient-to-r from-amber-500/15 via-card to-background p-2 px-3 shadow-md flex items-center justify-between gap-2 text-left relative overflow-hidden backdrop-blur-md ${className}`}>
      {/* Left: Key icon & OTP Digits */}
      <div className="flex items-center gap-2 min-w-0">
        <div className="p-1.5 rounded-lg bg-amber-500/20 text-amber-400 border border-amber-500/30 shrink-0">
          <KeyIcon size={14} weight="fill" />
        </div>
        <div className="flex flex-col min-w-0">
          <span className="text-[9px] uppercase font-extrabold tracking-wider text-muted-foreground block truncate">Start OTP</span>
          <span className="text-sm sm:text-base font-mono font-black tracking-widest text-amber-400 block truncate">
            {showOtp ? otp : "••••••"}
          </span>
        </div>
      </div>

      {/* Right: Timer & Action Icons */}
      <div className="flex items-center gap-1 shrink-0">
        <Badge className="bg-amber-500/15 text-amber-400 border border-amber-500/30 rounded-md text-[9px] font-bold px-1.5 py-0.5 flex items-center gap-1">
          <ClockIcon size={10} weight="bold" />
          <span>{timeLeft > 0 ? formatTime(timeLeft) : "Exp"}</span>
        </Badge>

        <Button
          variant="ghost"
          size="icon"
          onClick={() => setShowOtp(!showOtp)}
          className="h-7 w-7 rounded-md text-muted-foreground hover:text-foreground hover:bg-amber-500/10 cursor-pointer"
          title={showOtp ? "Hide OTP" : "Show OTP"}
        >
          {showOtp ? <EyeSlashIcon size={13} /> : <EyeIcon size={13} />}
        </Button>

        <Button
          variant="secondary"
          size="icon"
          onClick={handleCopy}
          className="h-7 w-7 rounded-md border border-amber-500/40 bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 cursor-pointer shadow-xs active:scale-95 transition-transform"
          title="Copy OTP"
        >
          {copied ? <CheckIcon size={13} weight="bold" /> : <CopyIcon size={13} weight="bold" />}
        </Button>
      </div>
    </div>
  )
}
export default RideOtpCard
