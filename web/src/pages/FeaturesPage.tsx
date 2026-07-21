import React from "react"
import { useNavigate } from "react-router-dom"
import { motion } from "framer-motion"
import { Button } from "@/components/ui/button"
import { ShieldCheckIcon, IdentificationCardIcon, EyeIcon } from "@phosphor-icons/react"
import { fadeInUp } from "@/lib/motion-variants"

export function FeaturesPage() {
  const navigate = useNavigate()

  const items = [
    {
      icon: <IdentificationCardIcon size={22} weight="fill" className="text-primary" />,
      title: "Government KYC Verification",
      desc: "Assistants are bound to verify Aadhaar identity cards before receiving active assignments."
    },
    {
      icon: <ShieldCheckIcon size={22} weight="fill" className="text-success" />,
      title: "Police Record Clearance",
      desc: "Mandatory security audits protect you from bad actors at travel nodes."
    },
    {
      icon: <EyeIcon size={22} weight="fill" className="text-purple-500" />,
      title: "Live GPS Path Audit",
      desc: "Family and friends can view active maps through temporary sharing keys."
    }
  ]

  const handleFinish = () => {
    localStorage.setItem("onboarding_skipped", "true")
    navigate("/login", { replace: true })
  }

  return (
    <div className="h-screen w-screen bg-background text-foreground flex flex-col justify-between p-6 relative overflow-hidden select-none">
      {/* Dot matrix background pattern */}
      <div className="absolute inset-0 opacity-[0.05] bg-[radial-gradient(circle_at_center,var(--foreground)_10%,transparent_50%)] bg-[size:24px_24px] pointer-events-none" />

      {/* Header */}
      <div className="h-10 flex items-center relative z-20">
        <span className="text-[10px] font-black uppercase tracking-widest text-primary">NavAssist Security</span>
      </div>

      {/* Main body */}
      <motion.div
        className="max-w-sm w-full mx-auto flex flex-col gap-6 relative z-10 my-auto"
        initial="hidden"
        animate="visible"
        variants={fadeInUp}
      >
        <div className="flex flex-col gap-1.5 text-center">
          <span className="text-[10px] font-black uppercase tracking-widest text-success shimmer">Certified Secure</span>
          <h3 className="text-lg font-black tracking-tight text-foreground">Vetted by Security Audits</h3>
        </div>

        <div className="flex flex-col gap-4 mt-2">
          {items.map((item, i) => (
            <div key={i} className="flex gap-3.5 items-start p-3 rounded-xl border border-border bg-card/20">
              <div className="p-2 bg-muted rounded-xl shrink-0 mt-0.5 text-primary-foreground">
                {item.icon}
              </div>
              <div>
                <h4 className="text-xs font-bold text-foreground">{item.title}</h4>
                <p className="text-[10px] text-muted-foreground mt-1 leading-relaxed">{item.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </motion.div>

      {/* Bottom controls */}
      <div className="w-full max-w-sm mx-auto relative z-20 pb-4">
        <Button
          onClick={handleFinish}
          className="w-full py-5 rounded-xl font-extrabold bg-primary text-white text-xs shadow-sm hover:scale-102 transition-all"
        >
          Enter Secure Portal
        </Button>
      </div>
    </div>
  )
}
export default FeaturesPage
