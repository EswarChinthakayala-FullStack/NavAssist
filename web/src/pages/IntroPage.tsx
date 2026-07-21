import React, { useState } from "react"
import { useNavigate } from "react-router-dom"
import { motion, AnimatePresence } from "framer-motion"
import { Button } from "@/components/ui/button"
import { TrainIcon, AirplaneIcon, BusIcon } from "@phosphor-icons/react"

interface Slide {
  title: string
  subtitle: string
  icon: React.ReactNode
  description: string
}

export function IntroPage() {
  const navigate = useNavigate()
  const [currentSlide, setCurrentSlide] = useState(0)

  const slides: Slide[] = [
    {
      title: "Railway Platform Arrival",
      subtitle: "Station Reception Escort",
      icon: <TrainIcon size={38} className="text-primary-foreground" />,
      description: "Receive guides at platform arrivals to carry luggage and guide you securely past crowded exits."
    },
    {
      title: "Airport Terminal Transfers",
      subtitle: "Seamless Airport Reception",
      icon: <AirplaneIcon size={38} className="text-primary-foreground" />,
      description: "Meet your guide right outside luggage claims to navigate taxi pickups and terminal exits securely."
    },
    {
      title: "Bus Stand Connects",
      subtitle: "Bus Stand Reception",
      icon: <BusIcon size={38} className="text-primary-foreground" />,
      description: "Find verified guides at local bus stands to protect you in unfamiliar surroundings."
    }
  ]

  const handleNext = () => {
    if (currentSlide < slides.length - 1) {
      setCurrentSlide(currentSlide + 1)
    } else {
      handleSkip()
    }
  }

  const handleSkip = () => {
    localStorage.setItem("hasSeenIntro", "true")
    navigate("/features")
  }

  const slide = slides[currentSlide]

  return (
    <div className="h-screen w-screen bg-background text-foreground flex flex-col justify-between p-6 relative overflow-hidden select-none">
      {/* Dot matrix background pattern */}
      <div className="absolute inset-0 opacity-[0.05] bg-[radial-gradient(circle_at_center,var(--foreground)_10%,transparent_50%)] bg-[size:24px_24px] pointer-events-none" />

      {/* Top Bar with Skip link */}
      <div className="flex justify-between items-center relative z-20 h-10">
        <span className="text-[10px] font-black uppercase tracking-widest text-primary">NavAssist Guide</span>
        <button
          onClick={handleSkip}
          className="text-xs font-bold text-muted-foreground hover:text-foreground cursor-pointer"
        >
          Skip
        </button>
      </div>

      {/* Slides Carousel using AnimatePresence */}
      <div className="flex-1 flex items-center justify-center relative my-4">
        <AnimatePresence mode="wait">
          <motion.div
            key={currentSlide}
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -20 }}
            transition={{ duration: 0.3 }}
            className="max-w-sm w-full flex flex-col items-center text-center gap-5 relative z-10"
          >
            <div className="w-16 h-16 bg-primary/10 rounded-2xl flex items-center justify-center text-primary animate-pulse">
              {slide.icon}
            </div>

            <div className="flex flex-col gap-2">
              <span className="text-[10px] font-black uppercase tracking-widest text-primary">
                {slide.subtitle}
              </span>
              <h3 className="text-lg font-black tracking-tight text-foreground leading-tight">
                {slide.title}
              </h3>
              <p className="text-xs text-muted-foreground leading-relaxed mt-2 max-w-[280px]">
                {slide.description}
              </p>
            </div>
          </motion.div>
        </AnimatePresence>
      </div>

      {/* Bottom controls */}
      <div className="w-full max-w-sm mx-auto flex flex-col gap-4 relative z-20 pb-4">
        {/* Dot Indicators */}
        <div className="flex gap-2 justify-center items-center mb-1">
          {slides.map((_, idx) => (
            <button
              key={idx}
              onClick={() => setCurrentSlide(idx)}
              className={`h-1.5 rounded-full transition-all duration-300 ${
                idx === currentSlide ? "bg-primary w-6" : "bg-border w-1.5"
              }`}
            />
          ))}
        </div>

        <Button
          onClick={handleNext}
          className="w-full py-5 rounded-xl font-extrabold bg-primary text-white text-xs shadow-sm hover:scale-102 transition-all"
        >
          {currentSlide === slides.length - 1 ? "Got It, Continue" : "Next Slide"}
        </Button>
      </div>
    </div>
  )
}
export default IntroPage
