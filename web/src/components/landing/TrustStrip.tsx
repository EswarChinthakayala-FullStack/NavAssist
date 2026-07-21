import React, { useEffect, useState, useRef } from "react"
import { motion, useInView } from "framer-motion"
import { Card, CardContent } from "@/components/ui/card"
import { fadeInUp, staggerContainer } from "@/lib/motion-variants"

interface StatItemProps {
  number: string
  label: string
  suffix?: string
}

function StatItem({ number, label, suffix = "" }: StatItemProps) {
  const ref = useRef(null)
  const isInView = useInView(ref, { once: true, margin: "-100px" })
  const [count, setCount] = useState(0)

  const numericValue = parseInt(number.replace(/\D/g, ""), 10)

  useEffect(() => {
    if (!isInView) return

    let start = 0
    const duration = 1200
    const increment = numericValue / (duration / 16)
    
    const timer = setInterval(() => {
      start += increment
      if (start >= numericValue) {
        setCount(numericValue)
        clearInterval(timer)
      } else {
        setCount(Math.floor(start))
      }
    }, 16)

    return () => clearInterval(timer)
  }, [isInView, numericValue])

  // Formatting helper
  const displayCount = () => {
    if (number.includes("★")) return "4.8★"
    if (number.includes("<")) return "<4"
    return count.toLocaleString()
  }

  return (
    <Card ref={ref} className="border-0 bg-transparent shadow-none">
      <CardContent className="p-4 text-center">
        <motion.div 
          className="text-3xl sm:text-4xl font-extrabold text-primary tracking-tight"
          variants={fadeInUp}
        >
          {displayCount()}
          {suffix}
        </motion.div>
        <motion.p 
          className="text-xs sm:text-sm text-muted-foreground font-semibold mt-1"
          variants={fadeInUp}
        >
          {label}
        </motion.p>
      </CardContent>
    </Card>
  )
}

export function TrustStrip() {
  const stripRef = useRef(null)
  const isStripInView = useInView(stripRef, { once: true, margin: "-100px" })

  const stats = [
    { number: "40", label: "Stations & airports covered", suffix: "+" },
    { number: "12000", label: "Trips completed safely", suffix: "+" },
    { number: "4.8★", label: "Average assistant rating" },
    { number: "<4", label: "Average assignment time", suffix: " min" }
  ]

  return (
    <div ref={stripRef} className="w-full bg-muted/40 border-y py-8">
      <div className="max-w-7xl mx-auto px-6">
        <motion.div 
          className="grid grid-cols-2 lg:grid-cols-4 gap-6"
          variants={staggerContainer}
          initial="hidden"
          animate={isStripInView ? "visible" : "hidden"}
        >
          {stats.map((stat, i) => (
            <StatItem 
              key={i} 
              number={stat.number} 
              label={stat.label} 
              suffix={stat.suffix} 
            />
          ))}
        </motion.div>
      </div>
    </div>
  )
}
