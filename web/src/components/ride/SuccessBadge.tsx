import React from "react"
import { motion } from "framer-motion"
import { CheckCircleIcon } from "@phosphor-icons/react"

export function SuccessBadge() {
  return (
    <motion.div
      initial={{ scale: 0.8, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      transition={{
        type: "spring",
        stiffness: 260,
        damping: 20,
        delay: 0.1,
      }}
      className="inline-flex items-center gap-2 bg-emerald-500/10 border border-emerald-500/30 px-4 py-1.5 rounded-full shadow-[0_0_15px_rgba(16,185,129,0.15)] text-emerald-500 select-none animate-pulse"
    >
      <CheckCircleIcon size={18} weight="fill" className="shrink-0" />
      <span className="text-[11px] font-black uppercase tracking-wider">Ride Completed Successfully</span>
    </motion.div>
  )
}

export default SuccessBadge
