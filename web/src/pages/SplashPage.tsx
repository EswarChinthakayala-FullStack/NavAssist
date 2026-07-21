import React, { useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useAuth } from "@/hooks/useAuth"

export function SplashPage() {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()

  useEffect(() => {
    // Session check and skippable onboarding logic
    const timer = setTimeout(() => {
      if (isAuthenticated) {
        navigate("/home", { replace: true })
      } else {
        const onboardingSkipped = localStorage.getItem("onboarding_skipped")
        if (onboardingSkipped === "true") {
          navigate("/login", { replace: true })
        } else {
          navigate("/welcome", { replace: true })
        }
      }
    }, 1200)

    return () => clearTimeout(timer)
  }, [isAuthenticated, navigate])

  return (
    <div className="h-screen w-screen bg-background text-foreground flex flex-col items-center justify-center gap-6 relative overflow-hidden">
      {/* Dot matrix background pattern */}
      <div className="absolute inset-0 opacity-[0.05] bg-[radial-gradient(circle_at_center,var(--foreground)_10%,transparent_50%)] bg-[size:24px_24px] pointer-events-none" />
      
      {/* Decorative radial gradient glow */}
      <div className="absolute inset-0 opacity-20 bg-[radial-gradient(ellipse_at_top_right,hsl(var(--primary)/0.25),transparent_60%)] pointer-events-none" />

      <div className="flex flex-col items-center gap-4 relative z-10 animate-pulse">
        <div className="w-16 h-16 rounded-3xl bg-primary flex items-center justify-center shadow-glow-primary">
          {/* Simple vector icon logo */}
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" className="w-8 h-8 text-white">
            <path d="M12 2L2 22H22L12 2Z" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </div>
        <h2 className="text-xl font-black tracking-widest text-foreground uppercase mt-2">NavAssist</h2>
        <span className="text-[10px] font-black uppercase tracking-wider text-muted-foreground">Safe Passage Secured</span>
      </div>
    </div>
  )
}
export default SplashPage
