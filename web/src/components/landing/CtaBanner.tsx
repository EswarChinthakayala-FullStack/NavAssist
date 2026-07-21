import React, { useRef } from "react"
import { motion, useInView } from "framer-motion"
import { useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { fadeInUp } from "@/lib/motion-variants"

export function CtaBanner() {
  const navigate = useNavigate()
  const containerRef = useRef(null)
  const isInView = useInView(containerRef, { once: true, margin: "-100px" })

  return (
    <section 
      ref={containerRef}
      className="py-16 bg-primary text-primary-foreground border-b text-center relative overflow-hidden"
    >
      {/* Decorative Radial Background Vector */}
      <div className="absolute inset-0 opacity-10 bg-[radial-gradient(circle_at_center,white_10%,transparent_50%)] bg-[size:24px_24px]" />

      <motion.div 
        className="max-w-4xl mx-auto px-6 flex flex-col gap-6 relative z-10 items-center"
        initial="hidden"
        animate={isInView ? "visible" : "hidden"}
        variants={fadeInUp}
      >
        <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight leading-tight">
          Your next arrival doesn't have to be stressful.
        </h2>
        <p className="text-sm sm:text-base text-primary-foreground/80 font-medium">
          Book your first NavAssist assistant in under two minutes.
        </p>
        
        {/* Pulsing CTA button */}
        <motion.div
          animate={{ scale: [1, 1.03, 1] }}
          transition={{ duration: 2.5, repeat: Infinity, ease: "easeInOut" }}
          className="mt-2 w-full sm:w-auto"
        >
          <Button 
            onClick={() => navigate("/dashboard")}
            className="w-full sm:w-auto bg-background text-primary hover:bg-background/95 font-extrabold text-sm sm:text-base px-8 py-6 rounded-xl shadow-lg border-0"
          >
            Get Started — It's Free to Book
          </Button>
        </motion.div>
      </motion.div>
    </section>
  )
}
