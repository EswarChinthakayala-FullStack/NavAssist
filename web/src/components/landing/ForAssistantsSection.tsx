import React, { useRef } from "react"
import { motion, useInView } from "framer-motion"
import { useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { CashRegisterIcon, CalendarIcon, WalletIcon } from "@phosphor-icons/react"
import { fadeInUp, staggerContainer } from "@/lib/motion-variants"

export function ForAssistantsSection() {
  const navigate = useNavigate()
  const containerRef = useRef(null)
  const isInView = useInView(containerRef, { once: true, margin: "-100px" })

  const stats = [
    {
      icon: <WalletIcon size={22} className="text-primary" />,
      value: "₹500+",
      label: "Avg. per trip"
    },
    {
      icon: <CalendarIcon size={22} className="text-primary" />,
      value: "Flexible",
      label: "Set own hours"
    },
    {
      icon: <CashRegisterIcon size={22} className="text-primary" />,
      value: "Weekly",
      label: "Direct payouts"
    }
  ]

  return (
    <section 
      ref={containerRef}
      id="for-assistants"
      className="py-20 bg-background border-b"
    >
      <div className="max-w-7xl mx-auto px-6 grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
        
        {/* Left: Stat Card composition graphic */}
        <motion.div 
          className="lg:col-span-5 order-2 lg:order-1 flex justify-center"
          initial={{ opacity: 0, x: -30 }}
          animate={isInView ? { opacity: 1, x: 0 } : { opacity: 0, x: -30 }}
          transition={{ duration: 0.6 }}
        >
          <div className="relative w-[320px] h-[360px] bg-gradient-to-tr from-primary/10 via-background to-background rounded-[32px] border p-6 flex flex-col justify-center items-center gap-6 shadow-md overflow-hidden">
            <div className="absolute inset-0 opacity-5 bg-[linear-gradient(to_right,#000_1px,transparent_1px),linear-gradient(to_bottom,#000_1px,transparent_1px)] bg-[size:14px_14px] dark:opacity-20 dark:bg-[linear-gradient(to_right,#fff_1px,transparent_1px),linear-gradient(to_bottom,#fff_1px,transparent_1px)]" />
            
            {/* Visual Vector Ring */}
            <div className="w-24 h-24 rounded-full bg-primary/10 flex items-center justify-center text-primary border border-primary/20 shadow-inner">
              <CashRegisterIcon size={44} weight="bold" />
            </div>

            <div className="text-center">
              <h4 className="text-lg font-bold text-foreground">Local Knowledge Value</h4>
              <p className="text-xs text-muted-foreground mt-1 max-w-[200px]">Help commuters navigate the terminals and train routes safely.</p>
            </div>
            
            <div className="bg-card border rounded-2xl p-3 flex items-center gap-3 w-full shadow-sm">
              <div className="w-2.5 h-2.5 rounded-full bg-success animate-pulse" />
              <span className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">Vetting applications open</span>
            </div>
          </div>
        </motion.div>

        {/* Right: Copy & Stats strip */}
        <motion.div 
          className="lg:col-span-7 order-1 lg:order-2 flex flex-col items-start text-left gap-6"
          initial="hidden"
          animate={isInView ? "visible" : "hidden"}
          variants={staggerContainer}
        >
          <motion.span variants={fadeInUp} className="text-xs font-bold tracking-widest text-primary uppercase">
            Earn with NavAssist
          </motion.span>
          
          <motion.h2 variants={fadeInUp} className="text-3xl sm:text-4xl font-extrabold tracking-tight text-foreground leading-tight">
            Know your city? Turn it into income.
          </motion.h2>
          
          <motion.p variants={fadeInUp} className="text-sm sm:text-base text-muted-foreground leading-relaxed max-w-xl">
            Set your own hours, work near the stations and landmarks you already know, and get paid securely after every trip. Join as a verified NavAssist assistant in minutes.
          </motion.p>

          <motion.div 
            className="grid grid-cols-3 gap-4 w-full border-t border-b py-6 my-2"
            variants={fadeInUp}
          >
            {stats.map((stat, i) => (
              <div key={i} className="text-center">
                <div className="w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center mx-auto mb-2 text-primary">
                  {stat.icon}
                </div>
                <h4 className="font-extrabold text-sm sm:text-base text-foreground">{stat.value}</h4>
                <p className="text-[10px] sm:text-xs text-muted-foreground mt-0.5">{stat.label}</p>
              </div>
            ))}
          </motion.div>

          <motion.div variants={fadeInUp} className="w-full sm:w-auto">
            <Button 
              onClick={() => navigate("/dashboard")} 
              className="bg-primary hover:bg-primary/95 text-primary-foreground font-bold px-8 py-6 rounded-xl shadow-md w-full sm:w-auto text-sm"
            >
              Apply to Become an Assistant
            </Button>
          </motion.div>
        </motion.div>

      </div>
    </section>
  )
}
