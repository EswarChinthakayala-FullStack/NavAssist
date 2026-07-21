import React, { useRef } from "react"
import { motion, useInView } from "framer-motion"
import { Badge } from "@/components/ui/badge"
import { MapPinIcon } from "@phosphor-icons/react"
import { staggerContainer } from "@/lib/motion-variants"

export function CoverageSection() {
  const containerRef = useRef(null)
  const isInView = useInView(containerRef, { once: true, margin: "-100px" })

  const cities = [
    "New Delhi", "Mumbai Central", "Bengaluru", "Chennai Central",
    "Kolkata Howrah", "Hyderabad Decan", "Pune Junction", "Ahmedabad",
    "Jaipur", "Lucknow Charbagh", "Guwahati", "Kochi Airport",
    "Goa Dabolim", "Chandigarh", "Patna", "Indore"
  ]

  const badgeVariant = {
    hidden: { opacity: 0, scale: 0.9 },
    visible: { 
      opacity: 1, 
      scale: 1,
      transition: { duration: 0.3 }
    }
  }

  return (
    <section 
      ref={containerRef}
      className="py-20 bg-background border-b"
    >
      <div className="max-w-4xl mx-auto px-6 text-center">
        {/* Header */}
        <div className="flex flex-col gap-3 mb-10">
          <span className="text-xs font-bold tracking-widest text-primary uppercase">
            Active Hubs
          </span>
          <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-foreground">
            Currently live in
          </h2>
        </div>

        {/* City Badges wrap */}
        <motion.div 
          className="flex flex-wrap justify-center gap-3 max-w-2xl mx-auto"
          variants={staggerContainer}
          initial="hidden"
          animate={isInView ? "visible" : "hidden"}
        >
          {cities.map((city, i) => (
            <motion.div key={i} variants={badgeVariant}>
              <Badge className="bg-primary/5 hover:bg-primary/10 border-border text-foreground px-4 py-2 text-xs sm:text-sm font-semibold rounded-full flex items-center gap-1.5">
                <MapPinIcon size={14} className="text-primary" />
                {city}
              </Badge>
            </motion.div>
          ))}
          <motion.div variants={badgeVariant}>
            <Badge variant="outline" className="border-dashed px-4 py-2 text-xs sm:text-sm font-bold rounded-full text-primary border-primary/30">
              + more cities monthly
            </Badge>
          </motion.div>
        </motion.div>
      </div>
    </section>
  )
}
