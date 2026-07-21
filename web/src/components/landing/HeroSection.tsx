import React from "react"
import { motion } from "framer-motion"
import { useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { ShieldCheckIcon, StarIcon, ClockIcon, CompassIcon, MapPinIcon } from "@phosphor-icons/react"
import { fadeInUp, staggerContainer } from "@/lib/motion-variants"

export function HeroSection() {
  const navigate = useNavigate()

  return (
    <section className="relative min-h-[95vh] pt-24 pb-12 flex items-center overflow-hidden bg-[radial-gradient(ellipse_at_top_right,hsl(var(--primary)/0.06),transparent_60%)]">
      <div className="max-w-7xl mx-auto px-6 w-full grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
        
        {/* Left Column: Copy & CTAs */}
        <motion.div 
          className="lg:col-span-7 flex flex-col items-start text-left gap-6"
          variants={staggerContainer}
          initial="hidden"
          animate="visible"
        >
          <motion.div variants={fadeInUp}>
            <Badge className="bg-accent/40 text-accent-foreground border-accent hover:bg-accent/50 px-3 py-1 rounded-full text-xs font-semibold flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-success animate-pulse" />
              Now live at 40+ stations, airports & bus stands
            </Badge>
          </motion.div>

          <motion.h1 
            className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight leading-none text-foreground"
            variants={fadeInUp}
          >
            Never navigate a <span className="text-primary">new city</span> alone.
          </motion.h1>

          <motion.p 
            className="text-lg sm:text-xl text-muted-foreground leading-relaxed max-w-xl"
            variants={fadeInUp}
          >
            Book a verified local assistant to receive you at the station, airport or bus stand — and guide you safely to where you're going.
          </motion.p>

          <motion.div 
            className="flex flex-col sm:flex-row gap-4 w-full sm:w-auto"
            variants={fadeInUp}
          >
            <Button 
              onClick={() => navigate("/dashboard")} 
              className="bg-primary text-primary-foreground hover:bg-primary/95 text-base px-8 py-6 rounded-xl shadow-md font-bold"
            >
              Book an Assistant
            </Button>
            <Button 
              onClick={() => navigate("/dashboard")} 
              variant="outline"
              className="text-base px-8 py-6 rounded-xl border-border hover:bg-muted font-bold"
            >
              Become an Assistant
            </Button>
          </motion.div>

          <motion.div 
            className="flex flex-wrap items-center gap-x-6 gap-y-2 mt-4 text-sm text-muted-foreground border-t pt-6 w-full"
            variants={fadeInUp}
          >
            <div className="flex items-center gap-1.5 font-medium">
              <ShieldCheckIcon size={18} className="text-success" weight="fill" />
              Aadhaar-verified
            </div>
            <div className="flex items-center gap-1.5 font-medium">
              <StarIcon size={18} className="text-warning" weight="fill" />
              4.8 avg rating
            </div>
            <div className="flex items-center gap-1.5 font-medium">
              <ClockIcon size={18} className="text-primary" weight="fill" />
              Live tracking
            </div>
          </motion.div>
        </motion.div>

        {/* Right Column: Interactive Phone Mockup Frame */}
        <motion.div 
          className="lg:col-span-5 flex justify-center relative select-none"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.6, delay: 0.2 }}
        >
          {/* Animated Float Container */}
          <motion.div
            className="relative"
            animate={{ y: [0, -10, 0] }}
            transition={{ duration: 6, repeat: Infinity, ease: "easeInOut" }}
          >
            {/* Phone Bezel Frame */}
            <div className="relative w-[300px] h-[600px] rounded-[48px] border-[12px] border-secondary/30 bg-card shadow-2xl overflow-hidden flex flex-col justify-between">
              
              {/* Phone Camera Notch */}
              <div className="absolute top-0 left-1/2 -translate-x-1/2 w-32 h-6 bg-secondary/30 rounded-b-2xl z-20 flex items-center justify-center">
                <div className="w-3 h-3 rounded-full bg-black/40" />
              </div>

              {/* Mock App Interface Screen */}
              <div className="flex-1 flex flex-col justify-between p-4 pt-10 bg-gradient-to-b from-primary/5 via-background to-background relative overflow-hidden">
                {/* Mock Map Vector Grid Background */}
                <div className="absolute inset-0 opacity-10 bg-[linear-gradient(to_right,#e5e7eb_1px,transparent_1px),linear-gradient(to_bottom,#e5e7eb_1px,transparent_1px)] bg-[size:16px_16px]" />
                
                {/* Simulated GPS Coordinate path */}
                <svg className="absolute inset-0 w-full h-full pointer-events-none opacity-40">
                  <path d="M 60 120 Q 200 180 180 340 T 120 480" fill="none" stroke="hsl(var(--primary))" strokeWidth="4" strokeDasharray="6 4" />
                </svg>

                {/* Simulated Markers */}
                <div className="absolute top-[110px] left-[52px] w-6 h-6 rounded-full bg-primary/20 flex items-center justify-center animate-ping">
                  <div className="w-3 h-3 rounded-full bg-primary" />
                </div>
                <div className="absolute top-[470px] left-[110px] w-6 h-6 rounded-full bg-success/20 flex items-center justify-center">
                  <div className="w-3.5 h-3.5 rounded-full bg-success border-2 border-white flex items-center justify-center text-[8px] text-white font-bold" />
                </div>

                {/* Top Nav Header */}
                <div className="bg-card/75 backdrop-blur-md border p-3 rounded-2xl flex items-center justify-between shadow-sm z-10">
                  <div className="flex items-center gap-2">
                    <div className="w-7 h-7 rounded-full bg-primary/10 text-primary flex items-center justify-center">
                      <CompassIcon size={16} weight="bold" />
                    </div>
                    <div>
                      <h4 className="text-[10px] font-bold text-foreground">Trip to Hotel</h4>
                      <span className="text-[8px] text-muted-foreground">ETA 8 mins</span>
                    </div>
                  </div>
                  <Badge className="bg-success/15 text-success hover:bg-success/25 border-0 text-[8px] px-1.5 py-0.5 font-bold">LIVE</Badge>
                </div>

                {/* Bottom Ride Card */}
                <div className="bg-card/95 backdrop-blur-md border p-4 rounded-3xl shadow-lg flex flex-col gap-3 z-10">
                  <div className="flex justify-between items-center border-b pb-2.5">
                    <div className="flex items-center gap-2">
                      <div className="w-9 h-9 rounded-full bg-muted overflow-hidden">
                        <img src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=80" className="object-cover w-full h-full" alt="" />
                      </div>
                      <div>
                        <h4 className="text-xs font-bold">Ramesh Kumar</h4>
                        <span className="text-[9px] text-muted-foreground">⭐ 4.9 · Verified</span>
                      </div>
                    </div>
                    <div className="text-right">
                      <h4 className="text-xs font-extrabold text-primary">₹122.00</h4>
                      <span className="text-[8px] text-muted-foreground">UPI Paid</span>
                    </div>
                  </div>

                  <div className="flex gap-2">
                    <div className="flex flex-col items-center gap-1.5 pt-0.5">
                      <div className="w-1.5 h-1.5 rounded-full bg-primary" />
                      <div className="w-0.5 h-6 bg-border" />
                      <div className="w-1.5 h-1.5 rounded-full bg-success" />
                    </div>
                    <div className="flex flex-col gap-1 text-[9px] text-muted-foreground flex-1">
                      <p className="font-semibold text-foreground truncate">Airport Terminal 3</p>
                      <div className="h-2" />
                      <p className="font-semibold text-foreground truncate">Aerocity Hotel Hub</p>
                    </div>
                  </div>
                </div>

              </div>
            </div>

            {/* Overlapping Floating Badges */}
            <motion.div
              className="absolute -top-4 -left-12 bg-card/95 backdrop-blur border p-3 rounded-2xl shadow-lg flex items-center gap-2.5 z-30"
              animate={{ y: [0, 8, 0] }}
              transition={{ duration: 4, repeat: Infinity, ease: "easeInOut", delay: 1 }}
            >
              <div className="p-2 rounded-full bg-success/10 text-success">
                <ShieldCheckIcon size={20} weight="fill" />
              </div>
              <div>
                <h4 className="text-xs font-bold text-foreground">Verified assistant</h4>
                <p className="text-[10px] text-muted-foreground mt-0.5">Aadhaar trust checked</p>
              </div>
            </motion.div>

            <motion.div
              className="absolute bottom-16 -right-12 bg-card/95 backdrop-blur border p-3 rounded-2xl shadow-lg flex items-center gap-2.5 z-30"
              animate={{ y: [0, -8, 0] }}
              transition={{ duration: 5, repeat: Infinity, ease: "easeInOut", delay: 0.5 }}
            >
              <div className="p-2 rounded-full bg-primary/10 text-primary">
                <ClockIcon size={20} weight="fill" />
              </div>
              <div>
                <h4 className="text-xs font-bold text-foreground">Live Telemetry</h4>
                <p className="text-[10px] text-muted-foreground mt-0.5">Tracking location active</p>
              </div>
            </motion.div>
          </motion.div>
        </motion.div>

      </div>
    </section>
  )
}
