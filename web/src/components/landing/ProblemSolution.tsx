import React, { useRef } from "react"
import { motion, useInView } from "framer-motion"
import { fadeInUp } from "@/lib/motion-variants"

export function ProblemSolution() {
  const containerRef = useRef(null)
  const isInView = useInView(containerRef, { once: true, margin: "-100px" })

  return (
    <section 
      ref={containerRef}
      className="py-20 bg-background flex items-center justify-center border-b"
    >
      <motion.div 
        className="max-w-3xl mx-auto px-6 text-center flex flex-col gap-6"
        initial="hidden"
        animate={isInView ? "visible" : "hidden"}
        variants={fadeInUp}
      >
        <span className="text-xs font-bold tracking-widest text-primary uppercase">
          Why NavAssist
        </span>
        <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-foreground leading-tight">
          Arriving somewhere new shouldn't feel stressful.
        </h2>
        <div className="text-base sm:text-lg text-muted-foreground leading-relaxed flex flex-col gap-4">
          <p>
            Unfamiliar stations, language barriers, heavy luggage, and unreliable local directions — for elderly travelers, solo women, first-time visitors, and international guests, the last mile of any trip is often the hardest and most anxious part.
          </p>
          <p>
            NavAssist puts a verified, trained local assistant on the ground before you even arrive, ensuring someone is waiting right at your platform or gate to guide you safely to your target destination.
          </p>
        </div>
      </motion.div>
    </section>
  )
}
