import React, { useRef } from "react"
import { motion, useInView } from "framer-motion"
import { Card, CardContent } from "@/components/ui/card"
import {
  CompassIcon,
  ShieldCheckIcon,
  CreditCardIcon,
  SirenIcon,
  MapPinIcon,
  CalendarCheckIcon
} from "@phosphor-icons/react"
import { fadeInUp, staggerContainer } from "@/lib/motion-variants"

export function FeaturesGrid() {
  const containerRef = useRef(null)
  const isInView = useInView(containerRef, { once: true, margin: "-100px" })

  const features = [
    {
      icon: <CompassIcon size={28} weight="bold" />,
      title: "Live Location Tracking",
      description: "Follow your assistant's exact position from assignment to arrival."
    },
    {
      icon: <ShieldCheckIcon size={28} weight="bold" />,
      title: "Verified Assistants",
      description: "Every assistant passes Aadhaar verification and a trust-score review before going live."
    },
    {
      icon: <CreditCardIcon size={28} weight="bold" />,
      title: "Secure Payments",
      description: "Pay by UPI, card or wallet — every transaction is encrypted and receipted."
    },
    {
      icon: <SirenIcon size={28} weight="bold" />,
      title: "24/7 Safety & SOS",
      description: "One tap alerts your emergency contacts and our safety team with your live location."
    },
    {
      icon: <MapPinIcon size={28} weight="bold" />,
      title: "Saved Locations",
      description: "Save Home, Office and favorite spots for one-tap rebooking."
    },
    {
      icon: <CalendarCheckIcon size={28} weight="bold" />,
      title: "Flexible Scheduling",
      description: "Book an assistant instantly or schedule one for a later arrival."
    }
  ]

  return (
    <section 
      ref={containerRef}
      className="py-20 bg-background border-b"
    >
      <div className="max-w-7xl mx-auto px-6">
        {/* Header */}
        <div className="text-center flex flex-col gap-3 mb-16 max-w-2xl mx-auto">
          <span className="text-xs font-bold tracking-widest text-primary uppercase">
            Platform Benefits
          </span>
          <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-foreground">
            Travel with complete confidence
          </h2>
          <p className="text-sm sm:text-base text-muted-foreground leading-relaxed">
            Every feature is designed to solve real-world last-mile travel friction, prioritizing absolute safety and ease of use.
          </p>
        </div>

        {/* Grid */}
        <motion.div 
          className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8"
          variants={staggerContainer}
          initial="hidden"
          animate={isInView ? "visible" : "hidden"}
        >
          {features.map((feature, i) => (
            <motion.div 
              key={i} 
              variants={fadeInUp}
              whileHover={{ y: -6 }}
              transition={{ type: "spring", stiffness: 300, damping: 20 }}
            >
              <Card className="h-full border border-border/80 hover:border-primary/30 shadow-sm hover:shadow-md transition-all">
                <CardContent className="flex flex-col items-start gap-4 p-6 pt-8">
                  <motion.div 
                    className="p-3 rounded-2xl bg-primary/10 text-primary"
                    whileHover={{ scale: 1.1 }}
                  >
                    {feature.icon}
                  </motion.div>
                  <h3 className="text-lg font-bold text-foreground">{feature.title}</h3>
                  <p className="text-sm text-muted-foreground leading-relaxed">{feature.description}</p>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </motion.div>
      </div>
    </section>
  )
}
