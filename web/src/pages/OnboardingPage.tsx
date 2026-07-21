import React, { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { CompassIcon, ShieldCheckIcon, StarIcon } from "@phosphor-icons/react"

interface Stage {
  title: string
  subtitle: string
  icon: React.ReactNode
  description: string
}

export function OnboardingPage() {
  const navigate = useNavigate()
  const [currentStage, setCurrentStage] = useState(0)

  const stages: Stage[] = [
    {
      title: "Welcome to NavAssist",
      subtitle: "Secured Transit Navigation",
      icon: <CompassIcon size={38} className="text-primary-foreground" />,
      description: "Book a verified local guide to receive you at railway platforms, airports, or bus stands, ensuring safe passage to your local lodgings."
    },
    {
      title: "Vetted & Verified Assistants",
      subtitle: "Peace of Mind Guaranteed",
      icon: <ShieldCheckIcon size={38} className="text-success-foreground" />,
      description: "Every guide undergoes Aadhaar identity checks and police clearance checks before accepting trips on our secure network."
    },
    {
      title: "Safe Real-Time Sharing",
      subtitle: "Emergency Integration Ready",
      icon: <StarIcon size={38} className="text-amber-400" />,
      description: "Share live position tracking links with trusted contacts and use the one-tap emergency SOS broadcast whenever needed."
    }
  ]

  const handleNext = () => {
    if (currentStage < stages.length - 1) {
      setCurrentStage(currentStage + 1)
    } else {
      handleSkip()
    }
  }

  const handleSkip = () => {
    localStorage.setItem("onboarding_skipped", "true")
    navigate("/login", { replace: true })
  }

  const stage = stages[currentStage]

  return (
    <div className="h-screen w-screen bg-background text-foreground flex flex-col items-center justify-center p-6 relative overflow-hidden select-none">
      {/* Dot matrix background pattern */}
      <div className="absolute inset-0 opacity-[0.05] bg-[radial-gradient(circle_at_center,var(--foreground)_10%,transparent_50%)] bg-[size:24px_24px] pointer-events-none" />
      
      {/* Radial glow */}
      <div className="absolute inset-0 opacity-20 bg-[radial-gradient(ellipse_at_top_right,hsl(var(--primary)/0.25),transparent_60%)] pointer-events-none" />

      <div className="max-w-sm w-full bg-card border border-border rounded-3xl p-8 shadow-2xl relative z-10 flex flex-col gap-6 items-center text-center animate-in fade-in zoom-in-95 duration-200">
        {/* Top Indicators */}
        <div className="flex gap-2 mb-2">
          {stages.map((_, idx) => (
            <div
              key={idx}
              className={`w-8 h-1.5 rounded-full transition-all duration-300 ${
                idx === currentStage ? "bg-primary w-12" : "bg-border"
              }`}
            />
          ))}
        </div>

        {/* Stage Icon */}
        <div className="w-16 h-16 bg-primary/10 rounded-2xl flex items-center justify-center text-primary mt-2">
          {stage.icon}
        </div>

        {/* Copy */}
        <div className="flex flex-col gap-2">
          <span className="text-[10px] font-black uppercase tracking-widest text-primary">
            {stage.subtitle}
          </span>
          <h3 className="text-xl font-black tracking-tight text-foreground">
            {stage.title}
          </h3>
          <p className="text-xs text-muted-foreground leading-relaxed mt-2">
            {stage.description}
          </p>
        </div>

        {/* Controls */}
        <div className="flex flex-col gap-3 w-full mt-4">
          <Button
            onClick={handleNext}
            className="w-full py-5 rounded-xl font-extrabold bg-primary text-white text-xs shadow-sm hover:scale-102 transition-all"
          >
            {currentStage === stages.length - 1 ? "Get Started" : "Continue"}
          </Button>

          {currentStage < stages.length - 1 && (
            <Button
              variant="ghost"
              onClick={handleSkip}
              className="w-full text-xs text-muted-foreground hover:bg-transparent"
            >
              Skip Onboarding
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}
export default OnboardingPage
