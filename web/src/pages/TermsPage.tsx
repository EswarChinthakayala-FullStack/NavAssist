import React from "react"
import { Navbar } from "@/components/landing/Navbar"
import { Footer } from "@/components/landing/Footer"

interface TermsPageProps {
  theme: string
  toggleTheme: () => void
}

export function TermsPage({ theme, toggleTheme }: TermsPageProps) {
  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground">
      <Navbar theme={theme} toggleTheme={toggleTheme} />
      
      <main className="flex-1 max-w-4xl mx-auto px-6 py-32 text-left">
        <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight mb-8">
          Terms of Service
        </h1>
        <p className="text-sm text-muted-foreground mb-6">
          Last Updated: July 11, 2026
        </p>
        
        <div className="space-y-6 text-sm sm:text-base leading-relaxed text-foreground/90">
          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">1. User Agreements & Eligibility</h2>
            <p className="mb-3">
              By registering an account with NavAssist, you agree to comply with these Terms of Service. You must be at least 18 years of age to register or use our platform.
            </p>
            <p>
              Assistants are independent contractors and must submit valid government-issued identifiers (such as Aadhaar cards) for KYC checks before being matched.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">2. Escort Booking & Service Rules</h2>
            <p className="mb-3">
              NavAssist acts as a matchmaking and safety monitoring platform connecting travelers with assistants. We do not provide transit escort services directly.
            </p>
            <p>
              Users must communicate through the in-app chat systems and meet only at designated station/airport hubs.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">3. Safety Protocols & SOS Actions</h2>
            <p>
              Both travelers and assistants must follow our safety guidelines. In the event of an emergency, you may trigger the in-app SOS alarm, which logs active GPS telemetry and alerts your designated emergency contacts.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">4. Payment Terms & settlements</h2>
            <p>
              Travelers agree to pay the fees quoted at the time of booking. Payouts to assistants are managed weekly and settled directly to linked bank accounts, minus platform matching commission fees.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">5. Limitation of Liability</h2>
            <p>
              NavAssist is not liable for disputes, property loss, or injuries arising from matches, except as mandated under local public transportation safety laws.
            </p>
          </section>
        </div>
      </main>

      <Footer theme={theme} toggleTheme={toggleTheme} />
    </div>
  )
}
export default TermsPage
