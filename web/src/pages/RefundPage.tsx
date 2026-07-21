import React from "react"
import { Navbar } from "@/components/landing/Navbar"
import { Footer } from "@/components/landing/Footer"

interface RefundPageProps {
  theme: string
  toggleTheme: () => void
}

export function RefundPage({ theme, toggleTheme }: RefundPageProps) {
  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground">
      <Navbar theme={theme} toggleTheme={toggleTheme} />
      
      <main className="flex-1 max-w-4xl mx-auto px-6 py-32 text-left">
        <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight mb-8">
          Refund Policy
        </h1>
        <p className="text-sm text-muted-foreground mb-6">
          Last Updated: July 11, 2026
        </p>
        
        <div className="space-y-6 text-sm sm:text-base leading-relaxed text-foreground/90">
          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">1. Cancellations by Travelers</h2>
            <p className="mb-3">
              If you cancel a booking before an assistant accepts it, no fee is charged.
            </p>
            <p>
              If you cancel after an assistant has accepted and is en route, a small cancellation fee (up to 20% of the booking value) may apply to compensate the assistant.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">2. Assistant No-Shows</h2>
            <p>
              If your matched assistant does not arrive within the timeout window, the booking is cancelled automatically, and any pre-authorized payments are refunded in full. No fees will be charged.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">3. Disputes & Resolution</h2>
            <p>
              If a trip is marked complete but was not finished, or if you encountered issues during transit, you may submit a dispute through the Bookings overview dashboard within 24 hours.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">4. Refund processing Times</h2>
            <p>
              Approved refunds are credited back to the original payment source (UPI, net banking, or credit/debit card) within 5 to 7 business days, depending on your banking institution.
            </p>
          </section>
        </div>
      </main>

      <Footer theme={theme} toggleTheme={toggleTheme} />
    </div>
  )
}
export default RefundPage
