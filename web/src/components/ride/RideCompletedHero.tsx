import React from "react"
import { motion } from "framer-motion"

export function RideCompletedHero() {
  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6, ease: "easeOut", delay: 0.2 }}
      className="space-y-2.5 text-center mt-3"
    >
      <h1 className="text-3xl font-black tracking-tight text-foreground sm:text-4xl">
        Ride Completed
      </h1>
      <p className="text-sm font-semibold text-muted-foreground max-w-sm mx-auto leading-relaxed">
        Thank you for riding with <span className="text-primary font-extrabold">NavAssist</span>. We hope you had a safe and comfortable journey.
      </p>
    </motion.div>
  )
}

export default RideCompletedHero
