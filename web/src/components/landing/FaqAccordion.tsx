import React, { useRef } from "react"
import { motion, useInView } from "framer-motion"
import {
  Accordion,
  AccordionItem,
  AccordionTrigger,
  AccordionContent
} from "@/components/ui/accordion"
import { fadeInUp } from "@/lib/motion-variants"

export function FaqAccordion() {
  const containerRef = useRef(null)
  const isInView = useInView(containerRef, { once: true, margin: "-100px" })

  const faqs = [
    {
      q: "How does NavAssist verify assistants?",
      a: "Every assistant submits Aadhaar identification, bank details, and is manually reviewed before their account is activated. Ongoing trust scores are based on completed trips and real-time user ratings."
    },
    {
      q: "What if my assistant doesn't show up?",
      a: "Bookings are automatically re-matched if an assistant doesn't accept within the timeout window, and unassigned bookings are never charged. You will be notified instantly."
    },
    {
      q: "Can my family track my trip without the app?",
      a: "Yes. You can generate a private live-tracking link from the app and share it via SMS or WhatsApp; no download or account is required to view it."
    },
    {
      q: "How do I get paid as an assistant?",
      a: "Earnings are settled weekly to your linked bank account or UPI ID. You can view your detailed payouts history anytime in your earnings dashboard."
    },
    {
      q: "Which cities is NavAssist available in?",
      a: "NavAssist is currently live at over 40+ railway stations and airports across metro cities including New Delhi, Mumbai, Bengaluru, Chennai, Pune, Kolkata, and Hyderabad. Check the Coverage section for the full list."
    }
  ]

  return (
    <section 
      ref={containerRef}
      id="faq"
      className="py-20 bg-background border-b"
    >
      <div className="max-w-3xl mx-auto px-6">
        
        {/* Header */}
        <div className="text-center flex flex-col gap-3 mb-12">
          <span className="text-xs font-bold tracking-widest text-primary uppercase">
            Got Questions?
          </span>
          <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-foreground">
            Frequently Asked Questions
          </h2>
        </div>

        {/* Accordion List */}
        <motion.div 
          initial="hidden"
          animate={isInView ? "visible" : "hidden"}
          variants={fadeInUp}
          className="border rounded-2xl p-6 bg-card"
        >
          <Accordion className="w-full">
            {faqs.map((faq, idx) => (
              <AccordionItem key={idx} value={`item-${idx}`} className="border-b py-1 last:border-0">
                <AccordionTrigger className="text-sm sm:text-base font-bold text-foreground hover:no-underline">
                  {faq.q}
                </AccordionTrigger>
                <AccordionContent className="text-xs sm:text-sm text-muted-foreground leading-relaxed pt-2">
                  {faq.a}
                </AccordionContent>
              </AccordionItem>
            ))}
          </Accordion>
        </motion.div>

      </div>
    </section>
  )
}
