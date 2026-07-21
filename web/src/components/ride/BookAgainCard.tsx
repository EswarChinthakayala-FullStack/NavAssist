import React from "react"
import { useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { ArrowClockwiseIcon } from "@phosphor-icons/react"
import { motion } from "framer-motion"

export function BookAgainCard() {
  const navigate = useNavigate()

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6, ease: "easeOut", delay: 0.4 }}
      className="w-full"
    >
      <Button
        onClick={() => navigate("/home")}
        className="w-full bg-primary hover:bg-primary/95 text-primary-foreground font-extrabold text-xs py-6 rounded-xl flex items-center justify-center gap-2 shadow-lg shadow-primary/10 cursor-pointer transition-all hover:scale-[1.01] active:scale-98"
      >
        <ArrowClockwiseIcon size={16} weight="bold" />
        <span>Book Another Ride</span>
      </Button>
    </motion.div>
  )
}

export default BookAgainCard
