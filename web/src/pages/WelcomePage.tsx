import React from "react"
import { useNavigate } from "react-router-dom"
import { motion } from "framer-motion"
import { Button } from "@/components/ui/button"
import { CompassIcon } from "@phosphor-icons/react"
import { fadeInUp } from "@/lib/motion-variants"

export function WelcomePage() {
  const navigate = useNavigate()

  return (
    <div className="h-screen w-screen bg-secondary text-secondary-foreground flex flex-col items-center justify-between p-8 relative overflow-hidden select-none">
      {/* Dot matrix background pattern */}
      <div className="absolute inset-0 opacity-[0.05] bg-[radial-gradient(circle_at_center,var(--foreground)_10%,transparent_50%)] bg-[size:24px_24px] pointer-events-none" />
      
      {/* Radial glow */}
      <div className="absolute inset-0 opacity-20 bg-[radial-gradient(ellipse_at_top_right,hsl(var(--primary)/0.25),transparent_60%)] pointer-events-none" />

      {/* Top spacer */}
      <div className="h-4" />

      {/* Illustration & Centered content */}
      <motion.div
        className="max-w-sm w-full flex flex-col items-center text-center gap-6 relative z-10"
        initial="hidden"
        animate="visible"
        variants={fadeInUp}
      >
        <div className="w-20 h-20 bg-primary/20 text-white rounded-3xl flex items-center justify-center shadow-glow-primary">
          <CompassIcon size={44} weight="fill" className="text-primary-foreground animate-pulse" />
        </div>

        <div className="flex flex-col gap-3">
          <span className="text-xs font-black uppercase tracking-widest text-primary-foreground/75 shimmer">
            Safe Transit Escorts
          </span>
          <h2 className="text-2xl sm:text-3xl font-black tracking-tight text-white leading-tight">
            Never navigate a new city alone.
          </h2>
          <p className="text-xs sm:text-sm text-secondary-foreground/80 leading-relaxed max-w-xs mt-1">
            Book verified local assistants at platforms, airports, and terminals to guide you safely to your destination.
          </p>
        </div>
      </motion.div>

      {/* Action Button pinned to bottom */}
      <motion.div
        className="w-full max-w-sm relative z-10 pb-6"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3, duration: 0.5 }}
      >
        <Button
          onClick={() => navigate("/intro")}
          className="w-full py-6 rounded-xl font-black bg-primary text-primary-foreground text-sm shadow-lg hover:scale-102 transition-all"
        >
          Get Started
        </Button>
      </motion.div>
    </div>
  )
}
export default WelcomePage
