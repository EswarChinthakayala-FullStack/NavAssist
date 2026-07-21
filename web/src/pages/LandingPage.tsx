import React, { Suspense, lazy, useEffect } from "react"
import { useLocation } from "react-router-dom"
import { Navbar } from "@/components/landing/Navbar"
import { HeroSection } from "@/components/landing/HeroSection"
import { Footer } from "@/components/landing/Footer"

// Lazy load components below the fold for high bundle performance
const TrustStrip = lazy(() => import("@/components/landing/TrustStrip").then(m => ({ default: m.TrustStrip })))
const ProblemSolution = lazy(() => import("@/components/landing/ProblemSolution").then(m => ({ default: m.ProblemSolution })))
const HowItWorks = lazy(() => import("@/components/landing/HowItWorks").then(m => ({ default: m.HowItWorks })))
const FeaturesGrid = lazy(() => import("@/components/landing/FeaturesGrid").then(m => ({ default: m.FeaturesGrid })))
const SafetySection = lazy(() => import("@/components/landing/SafetySection").then(m => ({ default: m.SafetySection })))
const ForAssistantsSection = lazy(() => import("@/components/landing/ForAssistantsSection").then(m => ({ default: m.ForAssistantsSection })))
const TestimonialsCarousel = lazy(() => import("@/components/landing/TestimonialsCarousel").then(m => ({ default: m.TestimonialsCarousel })))
const CoverageSection = lazy(() => import("@/components/landing/CoverageSection").then(m => ({ default: m.CoverageSection })))
const CtaBanner = lazy(() => import("@/components/landing/CtaBanner").then(m => ({ default: m.CtaBanner })))
const FaqAccordion = lazy(() => import("@/components/landing/FaqAccordion").then(m => ({ default: m.FaqAccordion })))

// Skeleton helper matching target height
function SectionSkeleton({ height = "200px" }: { height?: string }) {
  return (
    <div 
      style={{ height }} 
      className="w-full bg-muted/10 animate-pulse flex items-center justify-center border-b"
    >
      <span className="text-xs text-muted-foreground uppercase tracking-widest font-semibold">Loading Section...</span>
    </div>
  )
}

interface LandingPageProps {
  theme: string
  toggleTheme: () => void
}

export function LandingPage({ theme, toggleTheme }: LandingPageProps) {
  const location = useLocation()

  useEffect(() => {
    if (location.hash) {
      const element = document.querySelector(location.hash)
      if (element) {
        const timer = setTimeout(() => {
          element.scrollIntoView({ behavior: "smooth" })
        }, 200)
        return () => clearTimeout(timer)
      }
    }
  }, [location.hash])

  return (
    <div className="w-full overflow-x-hidden bg-background text-foreground">
      {/* 1. Navbar & Hero (Critical bundle, above the fold) */}
      <Navbar theme={theme} toggleTheme={toggleTheme} />
      <HeroSection />

      {/* 2. Below the fold components (Lazy loaded in Suspense) */}
      <Suspense fallback={<SectionSkeleton height="120px" />}>
        <TrustStrip />
      </Suspense>

      <Suspense fallback={<SectionSkeleton height="350px" />}>
        <ProblemSolution />
      </Suspense>

      <Suspense fallback={<SectionSkeleton height="500px" />}>
        <HowItWorks />
      </Suspense>

      <Suspense fallback={<SectionSkeleton height="600px" />}>
        <FeaturesGrid />
      </Suspense>

      <Suspense fallback={<SectionSkeleton height="550px" />}>
        <SafetySection />
      </Suspense>

      <Suspense fallback={<SectionSkeleton height="550px" />}>
        <ForAssistantsSection />
      </Suspense>

      <Suspense fallback={<SectionSkeleton height="450px" />}>
        <TestimonialsCarousel />
      </Suspense>

      <Suspense fallback={<SectionSkeleton height="250px" />}>
        <CoverageSection />
      </Suspense>

      <Suspense fallback={<SectionSkeleton height="280px" />}>
        <CtaBanner />
      </Suspense>

      <Suspense fallback={<SectionSkeleton height="400px" />}>
        <FaqAccordion />
      </Suspense>

      <Footer theme={theme} toggleTheme={toggleTheme} />
    </div>
  )
}
export default LandingPage
