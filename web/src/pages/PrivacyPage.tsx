import React from "react"
import { Navbar } from "@/components/landing/Navbar"
import { Footer } from "@/components/landing/Footer"

interface PrivacyPageProps {
  theme: string
  toggleTheme: () => void
}

export function PrivacyPage({ theme, toggleTheme }: PrivacyPageProps) {
  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground">
      <Navbar theme={theme} toggleTheme={toggleTheme} />
      
      <main className="flex-1 max-w-4xl mx-auto px-6 py-32 text-left">
        <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight mb-8">
          Privacy Policy
        </h1>
        <p className="text-sm text-muted-foreground mb-6">
          Last Updated: July 11, 2026
        </p>
        
        <div className="space-y-6 text-sm sm:text-base leading-relaxed text-foreground/90">
          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">1. Information We Collect</h2>
            <p className="mb-3">
              We collect personal information that you provide directly to us when registering, such as your name, phone number, email address, and payment information.
            </p>
            <p>
              For traveler escorts, we collect real-time geolocation coordinates during active trips to provide tracking features and ensure safe transit environments.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">2. How We Use Information</h2>
            <p className="mb-3">
              We use the collected information to facilitate booking matches between travelers and local assistants, manage payments, and verify identities through KYC documentation.
            </p>
            <p>
              We may also use aggregate location patterns to optimize operations and safety metrics at transit hubs. We never sell your personal information to third parties.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">3. Information Sharing & Disclosure</h2>
            <p>
              Your active location coordinates and profile details are shared only with the matched traveler or assistant during an active trip. We may disclose data if required by law or to protect user safety.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">4. Security of Data</h2>
            <p>
              We implement industry-standard encryption protocols to protect your personal details, bank records, and coordinates telemetry. Real-time location history is strictly archived after trip completion.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold mb-3 text-foreground">5. Your Privacy Rights</h2>
            <p>
              You have the right to request deletion of your account and archived trip details at any time by contacting our support team or through your settings dashboard.
            </p>
          </section>
        </div>
      </main>

      <Footer theme={theme} toggleTheme={toggleTheme} />
    </div>
  )
}
export default PrivacyPage
