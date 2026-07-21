import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useAuth } from "@/store/auth-context"
import { authService } from "@/services/auth.service"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { OtpInput } from "@/components/forms/OtpInput"
import { CompassIcon, TimerIcon, ShieldWarningIcon, EnvelopeIcon, PhoneIcon, SignOutIcon } from "@phosphor-icons/react"
import { motion } from "framer-motion"

export function OtpVerificationPage() {
  const navigate = useNavigate()
  const { user, fetchProfile, logout } = useAuth()
  const [code, setCode] = useState("")
  const [loading, setLoading] = useState(false)
  const [timer, setTimer] = useState(30)
  const [shake, setShake] = useState(false)

  const [debugPhoneOtp, setDebugPhoneOtp] = useState("")
  const [debugEmailCode, setDebugEmailCode] = useState("")

  useEffect(() => {
    const debugOtp = sessionStorage.getItem("debug_otp")
    const debugEmail = sessionStorage.getItem("debug_email_code")
    if (debugOtp) setDebugPhoneOtp(debugOtp)
    if (debugEmail) setDebugEmailCode(debugEmail)
    if (debugOtp || debugEmail) {
      const parts = []
      if (debugOtp) parts.push(`Phone Code: ${debugOtp}`)
      if (debugEmail) parts.push(`Email Code: ${debugEmail}`)
      toast.info(`[DEBUG] Verification codes: ${parts.join(" | ")}`, { duration: 15000 })
      sessionStorage.removeItem("debug_otp")
      sessionStorage.removeItem("debug_email_code")
    }
  }, [])

  // Redirect to login if user session is not active
  useEffect(() => {
    if (!user) {
      // If there's a stored access token, it might be loading.
      // Otherwise, kick back to login.
      const token = localStorage.getItem("access_token")
      if (!token) {
        navigate("/login", { replace: true })
      }
      return
    }

    if (user.is_phone_verified && user.is_email_verified) {
      toast.success("Account verified successfully!")
      navigate("/location-permission", { replace: true })
    }
  }, [user, navigate])

  useEffect(() => {
    const interval = setInterval(() => {
      setTimer((prev) => (prev > 0 ? prev - 1 : 0))
    }, 1000)
    return () => clearInterval(interval)
  }, [user])

  const handleVerify = async (otpValue: string) => {
    if (!user) return
    setLoading(true)
    try {
      if (!user.is_phone_verified) {
        // Verify Phone
        await authService.verifyPhone(user.phone, otpValue)
        toast.success("Phone number verified successfully!")
        setCode("")
        setTimer(30)
        await fetchProfile()
      } else if (!user.is_email_verified && user.email) {
        // Verify Email
        await authService.verifyEmail(user.email, otpValue)
        toast.success("Email address verified successfully!")
        setCode("")
        setTimer(30)
        await fetchProfile()
      }
    } catch (err: any) {
      setShake(true)
      setTimeout(() => setShake(false), 500)
      const errorMsg = err?.response?.data?.detail || "Invalid verification code entered."
      toast.error(errorMsg)
    } finally {
      setLoading(false)
    }
  }

  const handleResend = async () => {
    if (timer > 0 || !user) return
    setLoading(true)
    try {
      if (!user.is_phone_verified) {
        const res = await authService.sendOtp(user.phone)
        const debugOtp = res?.debug_otp
        if (debugOtp) {
          setDebugPhoneOtp(debugOtp)
          toast.info(`[DEV] Your phone OTP: ${debugOtp}`, { duration: 15000 })
        }
        toast.success("Phone verification OTP sent successfully.")
      } else if (!user.is_email_verified && user.email) {
        const res = await authService.resendEmailOtp(user.email)
        const debugCode = res?.debug_code
        if (debugCode) {
          setDebugEmailCode(debugCode)
          toast.info(`[DEV] Your email code: ${debugCode}`, { duration: 15000 })
        }
        toast.success("Email verification code sent successfully.")
      }
      setTimer(30)
    } catch (err) {
      toast.error("Failed to resend code. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  if (!user) {
    return (
      <div className="h-screen w-screen flex flex-col items-center justify-center bg-background text-foreground gap-4">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-semibold tracking-wider text-muted-foreground">Checking verification status...</span>
      </div>
    )
  }

  const stepTarget = !user.is_phone_verified ? user.phone : user.email

  return (
    <div className="h-screen w-screen bg-background text-foreground flex items-center justify-center p-6 relative overflow-hidden select-none">
      {/* Dot matrix background pattern */}
      <div className="absolute inset-0 opacity-[0.05] bg-[radial-gradient(circle_at_center,var(--foreground)_10%,transparent_50%)] bg-[size:24px_24px] pointer-events-none" />

      {/* Radial glow */}
      <div className="absolute inset-0 opacity-20 bg-[radial-gradient(ellipse_at_top_right,hsl(var(--primary)/0.25),transparent_60%)] pointer-events-none" />

      <motion.div
        animate={shake ? { x: [-10, 10, -10, 10, 0] } : {}}
        transition={{ duration: 0.4 }}
        className="max-w-sm w-full bg-card border border-border rounded-3xl p-8 shadow-2xl relative z-10 flex flex-col gap-6 items-center text-center animate-in fade-in zoom-in-95 duration-200"
      >
        <div className="w-12 h-12 bg-primary/10 rounded-2xl flex items-center justify-center text-primary mb-2">
          {!user.is_phone_verified ? (
            <PhoneIcon size={28} weight="fill" />
          ) : (
            <EnvelopeIcon size={28} weight="fill" />
          )}
        </div>

        <div className="flex flex-col gap-2">
          <h3 className="text-xl font-black tracking-tight">
            {!user.is_phone_verified ? "Verify Phone Number" : "Verify Email Address"}
          </h3>
          <p className="text-xs text-muted-foreground leading-relaxed">
            Enter the 6-digit verification code sent to <br />
            <span className="font-extrabold text-foreground">{stepTarget}</span>.
          </p>
        </div>

        {/* DEV: Show debug verification code inline */}
        {((!user.is_phone_verified && debugPhoneOtp) || (user.is_phone_verified && !user.is_email_verified && debugEmailCode)) && (
          <div className="w-full bg-amber-500/10 border border-amber-500/30 rounded-xl px-4 py-2.5 text-center">
            <p className="text-[10px] uppercase font-bold tracking-widest text-amber-500 mb-1">Dev Mode — Your Code</p>
            <p className="text-2xl font-black tracking-[0.3em] text-amber-400 font-mono">
              {!user.is_phone_verified ? debugPhoneOtp : debugEmailCode}
            </p>
          </div>
        )}

        {/* 6-digit OtpInput */}
        <div className="w-full flex justify-center py-2">
          <OtpInput
            value={code}
            onChange={(val) => {
              setCode(val)
              if (val.length === 6) {
                handleVerify(val)
              }
            }}
            length={6}
            disabled={loading}
          />
        </div>

        {/* Resend and Countdown Timer */}
        <div className="flex items-center gap-1.5 mt-2">
          <TimerIcon size={14} className="text-muted-foreground" />
          {timer > 0 ? (
            <span className="text-xs text-muted-foreground font-semibold">
              Resend code in {timer}s
            </span>
          ) : (
            <button
              onClick={handleResend}
              disabled={loading}
              className="text-xs font-extrabold text-primary hover:underline cursor-pointer"
            >
              Resend Code
            </button>
          )}
        </div>

        <div className="w-full border-t border-border mt-4 pt-4 flex justify-center">
          <Button
            variant="ghost"
            size="sm"
            onClick={logout}
            className="text-xs text-muted-foreground hover:text-destructive flex items-center gap-2 hover:bg-transparent"
          >
            <SignOutIcon size={14} />
            Log Out & Register Again
          </Button>
        </div>
      </motion.div>
    </div>
  )
}
export default OtpVerificationPage
