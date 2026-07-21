import React, { useRef } from "react"
import { motion, useInView } from "framer-motion"
import { ShieldCheckIcon, ShareNetworkIcon, SirenIcon, MapPinIcon } from "@phosphor-icons/react"
import { fadeInUp } from "@/lib/motion-variants"

export function SafetySection() {
  const containerRef = useRef(null)
  const isInView = useInView(containerRef, { once: true, margin: "-100px" })

  const checklist = [
    {
      icon: <ShieldCheckIcon size={24} weight="fill" className="text-success" />,
      title: "Identity-verified assistants",
      desc: "Aadhaar verification and manual review before anyone can accept a booking."
    },
    {
      icon: <ShareNetworkIcon size={24} weight="fill" className="text-primary-foreground" />,
      title: "Live trip sharing",
      desc: "Family can follow the journey in real time via a private link — no app required."
    },
    {
      icon: <SirenIcon size={24} weight="fill" className="text-destructive" />,
      title: "One-tap SOS",
      desc: "Instantly alerts emergency contacts and our safety team with live location."
    }
  ]

  return (
    <section 
      ref={containerRef}
      id="safety-deepdive"
      className="py-20 bg-secondary text-secondary-foreground border-b relative"
    >
      <div className="max-w-7xl mx-auto px-6 grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
        
        {/* Left: Checklist & Copy */}
        <motion.div 
          className="lg:col-span-7 flex flex-col items-start text-left gap-6"
          initial="hidden"
          animate={isInView ? "visible" : "hidden"}
          variants={fadeInUp}
        >
          <span className="text-xs font-bold tracking-widest text-white shimmer uppercase">
            Safety First
          </span>
          <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight leading-tight">
            Built for peace of mind — yours, and theirs.
          </h2>
          <p className="text-sm sm:text-base text-secondary-foreground/80 leading-relaxed max-w-xl">
            We understand that safety is paramount when traveling alone. Our app-guided escort system guarantees absolute security through rigorous background vetting and real-time oversight.
          </p>

          <div className="flex flex-col gap-6 mt-4 w-full">
            {checklist.map((item, i) => (
              <div key={i} className="flex gap-4 items-start">
                <div className="p-2.5 rounded-xl bg-white/10 flex items-center justify-center shrink-0">
                  {item.icon}
                </div>
                <div>
                  <h4 className="font-bold text-base">{item.title}</h4>
                  <p className="text-sm text-secondary-foreground/70 mt-1 leading-relaxed">{item.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </motion.div>

        {/* Right: Graphic mockup representing location sharing and safety SOS status */}
        <motion.div 
          className="lg:col-span-5 flex justify-center"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={isInView ? { opacity: 1, scale: 1 } : { opacity: 0, scale: 0.95 }}
          transition={{ duration: 0.6, delay: 0.2 }}
        >
          <div className="w-[300px] bg-card text-foreground rounded-[40px] p-6 shadow-2xl border border-white/10 relative overflow-hidden flex flex-col gap-6">
            
            {/* Live radar wave circles */}
            <div className="absolute top-[80px] left-[138px] w-24 h-24 rounded-full border border-destructive/20 animate-ping pointer-events-none" />
            <div className="absolute top-[70px] left-[128px] w-32 h-32 rounded-full border border-destructive/10 animate-pulse pointer-events-none" />

            <div className="text-center pt-2 flex flex-col items-center">
              <div className="w-14 h-14 bg-destructive/10 text-destructive border border-destructive/20 rounded-full flex items-center justify-center animate-bounce mb-3 shadow-inner">
                <SirenIcon size={28} weight="fill" />
              </div>
              <h4 className="text-sm font-extrabold text-destructive tracking-widest uppercase">SOS Broadcast</h4>
              <p className="text-[10px] text-muted-foreground mt-1">Live position shared with 2 contacts</p>
            </div>

            {/* Simulated map route card */}
            <div className="bg-muted/40 border rounded-2xl p-3 flex flex-col gap-2.5">
              <div className="flex justify-between items-center text-[9px] border-b pb-1.5 font-bold">
                <span className="text-muted-foreground">LATENCY</span>
                <span className="text-success flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-success animate-pulse" />
                  0.4s real-time
                </span>
              </div>
              <div className="flex gap-2">
                <div className="p-1.5 h-fit rounded-lg bg-primary/10 text-primary">
                  <MapPinIcon size={14} weight="fill" />
                </div>
                <div className="text-[10px] font-semibold flex-1 leading-snug">
                  <p className="text-foreground">Airport T3 Gate 4</p>
                  <p className="text-muted-foreground text-[8px] mt-0.5">28.5562° N, 77.1001° E</p>
                </div>
              </div>
            </div>

            {/* Sharing link status */}
            <div className="bg-success/5 border border-success/20 rounded-2xl p-3 flex items-center gap-3">
              <div className="w-7 h-7 rounded-full bg-success/10 text-success flex items-center justify-center shrink-0">
                <ShareNetworkIcon size={16} weight="bold" />
              </div>
              <div className="text-[10px]">
                <p className="font-bold text-success">Private link active</p>
                <p className="text-muted-foreground mt-0.5">Guardians are tracking live</p>
              </div>
            </div>

          </div>
        </motion.div>

      </div>
    </section>
  )
}
