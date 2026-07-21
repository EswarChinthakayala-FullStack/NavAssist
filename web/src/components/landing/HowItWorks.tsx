import React, { useRef } from "react"
import { motion, useInView } from "framer-motion"
import { Card, CardContent } from "@/components/ui/card"
import { MapPinIcon, UserCheckIcon, CompassIcon, CheckCircleIcon } from "@phosphor-icons/react"
import { fadeInUp, staggerContainer } from "@/lib/motion-variants"

export function HowItWorks() {
  const containerRef = useRef(null)
  const isInView = useInView(containerRef, { once: true, margin: "-100px" })

  const steps = [
    {
      number: "1",
      icon: <MapPinIcon size={28} className="text-primary" weight="bold" />,
      title: "Tell us where",
      description: "Enter your pickup point — station, airport or bus stand — and where you're headed."
    },
    {
      number: "2",
      icon: <UserCheckIcon size={28} className="text-primary" weight="bold" />,
      title: "Choose helper",
      description: "Browse verified assistants nearby, see their rating and trust score, and pick one."
    },
    {
      number: "3",
      icon: <CompassIcon size={28} className="text-primary" weight="bold" />,
      title: "Track them live",
      description: "Watch your assistant arrive in real time, right up to the moment they meet you."
    },
    {
      number: "4",
      icon: <CheckCircleIcon size={28} className="text-primary" weight="bold" />,
      title: "Arrive safely",
      description: "They guide you to your destination — pay securely in the app, then rate your trip."
    }
  ]

  return (
    <section 
      ref={containerRef}
      id="how-it-works"
      className="py-20 bg-muted/20 border-b relative"
    >
      <div className="max-w-7xl mx-auto px-6">
        {/* Header */}
        <div className="text-center flex flex-col gap-3 mb-16">
          <span className="text-xs font-bold tracking-widest text-primary uppercase">
            Simple Process
          </span>
          <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-foreground">
            Book an assistant in four simple steps
          </h2>
        </div>

        {/* Steps Grid */}
        <div className="relative">
          {/* Desktop Connecting Line */}
          <div className="hidden lg:block absolute top-[52px] left-[12%] right-[12%] h-[2px] bg-border z-0">
            <motion.div 
              className="h-full bg-primary"
              initial={{ width: 0 }}
              animate={isInView ? { width: "100%" } : { width: 0 }}
              transition={{ duration: 1.5, ease: "easeInOut" }}
            />
          </div>

          <motion.div 
            className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 relative z-10"
            variants={staggerContainer}
            initial="hidden"
            animate={isInView ? "visible" : "hidden"}
          >
            {steps.map((step, i) => (
              <motion.div key={i} variants={fadeInUp} className="relative pt-6">
                {/* Number Badge */}
                <div className="absolute top-0 left-4 w-10 h-10 rounded-full bg-primary text-primary-foreground font-extrabold flex items-center justify-center shadow-md">
                  {step.number}
                </div>
                
                {/* Step Card */}
                <Card className="hover:shadow-md transition-shadow h-full pt-8">
                  <CardContent className="flex flex-col items-start gap-4">
                    <div className="p-3 rounded-2xl bg-primary/10 text-primary">
                      {step.icon}
                    </div>
                    <h3 className="text-lg font-bold text-foreground">{step.title}</h3>
                    <p className="text-sm text-muted-foreground leading-relaxed">{step.description}</p>
                  </CardContent>
                </Card>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </div>
    </section>
  )
}
