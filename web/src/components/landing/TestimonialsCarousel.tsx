import React, { useState, useRef } from "react"
import { motion, useInView } from "framer-motion"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { CaretLeftIcon, CaretRightIcon, StarIcon } from "@phosphor-icons/react"
import { fadeInUp, staggerContainer } from "@/lib/motion-variants"

export function TestimonialsCarousel() {
  const containerRef = useRef(null)
  const isInView = useInView(containerRef, { once: true, margin: "-100px" })
  const [activeIndex, setActiveIndex] = useState(0)

  const testimonials = [
    {
      quote: "I landed at 11pm not knowing a word of the local language. My assistant was waiting right at the gate with a sign. Genuinely made the trip.",
      name: "Priya S.",
      context: "Solo traveler, Mumbai → Pune",
      avatar: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&q=80&w=80",
      rating: 5
    },
    {
      quote: "My parents are elderly and get overwhelmed at big stations. Now I book them an assistant and track the whole thing from my phone.",
      name: "Arjun K.",
      context: "Family Booker",
      avatar: "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&q=80&w=80",
      rating: 5
    },
    {
      quote: "Flexible enough to fit around my college schedule, and I know every shortcut around the station anyway.",
      name: "Karthik R.",
      context: "NavAssist Assistant",
      avatar: "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&q=80&w=80",
      rating: 5
    }
  ]

  const handleNext = () => {
    setActiveIndex((prev) => (prev + 1) % testimonials.length)
  }

  const handlePrev = () => {
    setActiveIndex((prev) => (prev - 1 + testimonials.length) % testimonials.length)
  }

  return (
    <section 
      ref={containerRef}
      className="py-20 bg-muted/20 border-b"
    >
      <div className="max-w-7xl mx-auto px-6">
        
        {/* Header */}
        <div className="flex flex-row items-end justify-between mb-12">
          <div className="text-left flex flex-col gap-3">
            <span className="text-xs font-bold tracking-widest text-primary uppercase">
              User Reviews
            </span>
            <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-foreground">
              What our community says
            </h2>
          </div>
          
          {/* Controls (Desktop/Tablet) */}
          <div className="flex items-center gap-2">
            <Button variant="outline" size="icon" onClick={handlePrev} className="rounded-full">
              <CaretLeftIcon size={18} weight="bold" />
            </Button>
            <Button variant="outline" size="icon" onClick={handleNext} className="rounded-full">
              <CaretRightIcon size={18} weight="bold" />
            </Button>
          </div>
        </div>

        {/* Swipe Testimonial Cards Layout */}
        <motion.div 
          className="grid grid-cols-1 md:grid-cols-3 gap-8"
          variants={staggerContainer}
          initial="hidden"
          animate={isInView ? "visible" : "hidden"}
        >
          {testimonials.map((t, idx) => {
            const isActive = idx === activeIndex
            return (
              <motion.div 
                key={idx}
                variants={fadeInUp}
                className={`transition-all duration-300 ${
                  isActive 
                    ? "opacity-100 scale-100" 
                    : "opacity-60 md:opacity-100 scale-95 md:scale-100"
                }`}
              >
                <Card className="h-full border border-border/80 shadow-sm hover:shadow-md transition-shadow">
                  <CardContent className="p-6 flex flex-col justify-between h-full gap-6">
                    <div>
                      {/* Rating Stars */}
                      <div className="flex gap-0.5 text-warning mb-4">
                        {[...Array(t.rating)].map((_, i) => (
                          <StarIcon key={i} size={16} weight="fill" />
                        ))}
                      </div>
                      <p className="text-sm sm:text-base text-foreground italic leading-relaxed">
                        "{t.quote}"
                      </p>
                    </div>
                    
                    <div className="flex items-center gap-3 border-t pt-4">
                      <div className="w-10 h-10 rounded-full bg-muted overflow-hidden">
                        <img src={t.avatar} className="object-cover w-full h-full" alt="" />
                      </div>
                      <div>
                        <h4 className="font-bold text-sm text-foreground">{t.name}</h4>
                        <p className="text-[10px] sm:text-xs text-muted-foreground">{t.context}</p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </motion.div>
            )
          })}
        </motion.div>

        {/* Carousel indicators */}
        <div className="flex justify-center gap-1.5 mt-8 md:hidden">
          {testimonials.map((_, i) => (
            <button
              key={i}
              onClick={() => setActiveIndex(i)}
              className={`h-2 rounded-full transition-all duration-300 ${
                i === activeIndex ? "bg-primary w-6" : "bg-border w-2"
              }`}
            />
          ))}
        </div>

      </div>
    </section>
  )
}
