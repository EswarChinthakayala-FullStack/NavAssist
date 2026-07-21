import React from "react"
import { useNavigate } from "react-router-dom"
import { motion } from "framer-motion"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { CompassIcon, NavigationArrowIcon, SirenIcon, CreditCardIcon } from "@phosphor-icons/react"
import { fadeInUp, staggerContainer } from "@/lib/motion-variants"

export function FeatureOverviewPage() {
  const navigate = useNavigate()

  const features = [
    {
      icon: <CompassIcon size={24} className="text-primary" />,
      title: "Assistant Booking",
      description: "Book verified local guides at key transit docks in seconds."
    },
    {
      icon: <NavigationArrowIcon size={24} className="text-success" />,
      title: "Live GPS Tracking",
      description: "Real-time OSRM map rendering updates coordinates instantly."
    },
    {
      icon: <SirenIcon size={24} className="text-destructive animate-pulse" />,
      title: "Safety SOS Alerts",
      description: "One-tap emergency broadcast to close safety contacts."
    },
    {
      icon: <CreditCardIcon size={24} className="text-purple-500" />,
      title: "Secure Payments",
      description: "Hassle-free Razorpay checkout order flows built right in."
    }
  ]

  return (
    <div className="h-screen w-screen bg-background text-foreground flex flex-col justify-between p-6 relative overflow-hidden select-none">
      {/* Dot matrix background pattern */}
      <div className="absolute inset-0 opacity-[0.05] bg-[radial-gradient(circle_at_center,var(--foreground)_10%,transparent_50%)] bg-[size:24px_24px] pointer-events-none" />

      {/* Header */}
      <div className="h-10 flex items-center relative z-20">
        <span className="text-[10px] font-black uppercase tracking-widest text-primary">NavAssist Spec Sheet</span>
      </div>

      {/* Grid container */}
      <motion.div
        className="max-w-sm w-full mx-auto flex flex-col gap-6 relative z-10 my-auto"
        initial="hidden"
        animate="visible"
        variants={staggerContainer}
      >
        <div className="flex flex-col gap-1 text-center">
          <span className="text-[10px] font-black uppercase tracking-widest text-primary">Core Modules</span>
          <h3 className="text-lg font-black tracking-tight text-foreground">Feature Overview Matrix</h3>
        </div>

        <div className="grid grid-cols-2 gap-4 mt-2">
          {features.map((feature, i) => (
            <motion.div key={i} variants={fadeInUp}>
              <Card className="p-4 flex flex-col gap-2.5 items-start bg-card/25 border-border hover:border-primary/40 transition-all select-none">
                <div className="p-2 bg-muted rounded-xl text-primary-foreground">
                  {feature.icon}
                </div>
                <div>
                  <h4 className="text-xs font-bold text-foreground leading-normal">{feature.title}</h4>
                  <p className="text-[9px] text-muted-foreground leading-normal mt-1">{feature.description}</p>
                </div>
              </Card>
            </motion.div>
          ))}
        </div>
      </motion.div>

      {/* Bottom controls */}
      <div className="w-full max-w-sm mx-auto relative z-20 pb-4">
        <Button
          onClick={() => navigate("/signup")}
          className="w-full py-5 rounded-xl font-extrabold bg-primary text-white text-xs shadow-sm hover:scale-102 transition-all"
        >
          Continue to Registration
        </Button>
      </div>
    </div>
  )
}
export default FeatureOverviewPage
