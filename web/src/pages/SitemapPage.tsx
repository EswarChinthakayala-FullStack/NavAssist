import React from "react"
import { useNavigate } from "react-router-dom"
import { Navbar } from "@/components/landing/Navbar"
import { Footer } from "@/components/landing/Footer"
import { Button } from "@/components/ui/button"
import { FileTextIcon, HouseIcon, UserIcon } from "@phosphor-icons/react"

interface SitemapPageProps {
  theme: string
  toggleTheme: () => void
}

export function SitemapPage({ theme, toggleTheme }: SitemapPageProps) {
  const navigate = useNavigate()

  const links = [
    {
      title: "Public Marketing Site",
      items: [
        { name: "Home Landing Page", path: "/", icon: <HouseIcon size={16} /> },
        { name: "Privacy Policy", path: "/privacy", icon: <FileTextIcon size={16} /> },
        { name: "Terms of Service", path: "/terms", icon: <FileTextIcon size={16} /> },
        { name: "Refund Policy", path: "/refund-policy", icon: <FileTextIcon size={16} /> },
      ]
    },
    {
      title: "Authenticated Portal Pages",
      items: [
        { name: "Dashboard Overview", path: "/dashboard", icon: <UserIcon size={16} /> },
        { name: "My Bookings History", path: "/bookings", icon: <UserIcon size={16} /> },
        { name: "KYC Verification", path: "/kyc", icon: <UserIcon size={16} /> },
        { name: "Safety SOS Panel", path: "/safety", icon: <UserIcon size={16} /> },
        { name: "Profile & Settings", path: "/settings", icon: <UserIcon size={16} /> },
      ]
    }
  ]

  const handleLinkClick = (path: string) => {
    navigate(path)
    window.scrollTo(0, 0)
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground">
      <Navbar theme={theme} toggleTheme={toggleTheme} />
      
      <main className="flex-1 max-w-4xl mx-auto px-6 py-32 text-left w-full">
        <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight mb-8">
          Sitemap
        </h1>
        <p className="text-sm text-muted-foreground mb-12">
          Directory structure of all active endpoints and pages on the NavAssist platform.
        </p>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {links.map((group, idx) => (
            <div key={idx} className="border rounded-2xl p-6 bg-card">
              <h2 className="text-lg font-bold mb-4 text-foreground">
                {group.title}
              </h2>
              <ul className="space-y-3">
                {group.items.map((item, itemIdx) => (
                  <li key={itemIdx}>
                    <button
                      onClick={() => handleLinkClick(item.path)}
                      className="flex items-center gap-3 text-sm text-muted-foreground hover:text-foreground transition-colors w-full text-left"
                    >
                      <div className="w-8 h-8 rounded-lg bg-primary/5 flex items-center justify-center text-primary">
                        {item.icon}
                      </div>
                      <span className="font-semibold">{item.name}</span>
                    </button>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </main>

      <Footer theme={theme} toggleTheme={toggleTheme} />
    </div>
  )
}
export default SitemapPage
