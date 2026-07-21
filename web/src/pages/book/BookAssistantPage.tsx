import React from "react"
import { Outlet, useLocation, Navigate } from "react-router-dom"
import { BookingStepper } from "@/components/booking/BookingStepper"

export function BookAssistantPage() {
  const location = useLocation()

  // If path is exactly /book, redirect to the first step (/book/pickup)
  if (location.pathname === "/book" || location.pathname === "/book/") {
    return <Navigate to="/book/pickup" replace />
  }

  // Map sub-routes to step numbers for the stepper
  const getStepIndex = () => {
    if (location.pathname.includes("/book/pickup")) return 0
    if (location.pathname.includes("/book/destination")) return 1
    if (location.pathname.includes("/book/schedule")) return 2
    if (location.pathname.includes("/book/assistants")) return 3
    if (
      location.pathname.includes("/book/confirm") ||
      location.pathname.includes("/book/summary") ||
      location.pathname.includes("/book/price-estimate") ||
      location.pathname.includes("/book/offers") ||
      location.pathname.includes("/book/apply-coupon") ||
      location.pathname.includes("/book/payment-method") ||
      location.pathname.includes("/book/payment-confirmation")
    ) return 4
    return 0
  }

  const steps = [
    { title: "Pickup", description: "Where to meet", path: "/book/pickup" },
    { title: "Destination", description: "Where to go", path: "/book/destination" },
    { title: "Schedule", description: "When to start", path: "/book/schedule" },
    { title: "Assistants", description: "Choose guide", path: "/book/assistants" },
    { title: "Confirm", description: "Finalize booking", path: "/book/confirm" },
  ]

  return (
    <div className="max-w-6xl mx-auto flex flex-col gap-6 py-2">
      {/* Centered Stepper */}
      <div className="bg-card border border-border/80 rounded-2xl p-4 shadow-md backdrop-blur-md">
        <BookingStepper currentStep={getStepIndex()} steps={steps} />
      </div>

      {/* Main wizard step content */}
      <div className="flex-1 min-h-[500px]">
        <Outlet />
      </div>
    </div>
  )
}
export default BookAssistantPage
