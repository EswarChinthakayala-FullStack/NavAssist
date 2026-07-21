import React from "react"
import { useNavigate, useLocation } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { SunIcon, MoonIcon, TwitterLogoIcon, InstagramLogoIcon, LinkedinLogoIcon } from "@phosphor-icons/react"
import { AppLogo } from "@/components/ui/app-logo"

interface FooterProps {
  theme: string
  toggleTheme: () => void
}

export function Footer({ theme, toggleTheme }: FooterProps) {
  const navigate = useNavigate()
  const location = useLocation()

  const sections = [
    {
      title: "Product",
      links: [
        { name: "How it Works", href: "#how-it-works" },
        { name: "Safety & SOS", href: "#safety-deepdive" },
        { name: "Become an Assistant", href: "#for-assistants" }
      ]
    },
    {
      title: "Legal",
      links: [
        { name: "Privacy Policy", href: "/privacy" },
        { name: "Terms of Service", href: "/terms" },
        { name: "Refund Policy", href: "/refund-policy" }
      ]
    }
  ]

  const handleNavClick = (e: React.MouseEvent<HTMLAnchorElement>, href: string) => {
    if (href.startsWith("#") && href.length > 1) {
      e.preventDefault()
      if (location.pathname === "/" || location.pathname === "/landing") {
        const targetElement = document.querySelector(href)
        if (targetElement) {
          targetElement.scrollIntoView({ behavior: "smooth" })
        }
      } else {
        navigate({ pathname: "/", hash: href })
      }
    } else if (href.startsWith("/")) {
      e.preventDefault()
      navigate(href)
      window.scrollTo(0, 0)
    }
  }

  return (
    <footer className="w-full bg-muted/60 border-t pt-16 pb-8">
      <div className="max-w-7xl mx-auto px-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
        
        {/* Brand column */}
        <div className="lg:col-span-2 flex flex-col items-start gap-4 text-left">
          <div className="flex items-center gap-2 cursor-pointer" onClick={() => navigate("/")}>
            <AppLogo size="sm" animated={false} />
          </div>
          <p className="text-sm text-muted-foreground leading-relaxed max-w-sm">
            Connecting travelers with verified local helpers at airports, railway stations and metro hubs for secure, stress-free last-mile escort.
          </p>
        </div>

        {/* Section columns */}
        {sections.map((section, idx) => (
          <div key={idx} className="text-left flex flex-col gap-4">
            <h4 className="text-xs font-bold uppercase tracking-wider text-foreground">
              {section.title}
            </h4>
            <div className="flex flex-col gap-2.5">
              {section.links.map((link, linkIdx) => (
                <a
                  key={linkIdx}
                  href={link.href}
                  onClick={(e) => handleNavClick(e, link.href)}
                  className="text-sm text-muted-foreground hover:text-foreground transition-colors"
                >
                  {link.name}
                </a>
              ))}
            </div>
          </div>
        ))}
      </div>

      {/* Bottom Bar */}
      <div className="max-w-7xl mx-auto px-6 border-t pt-8 mt-12 flex flex-col sm:flex-row items-center justify-between gap-4">
        <span className="text-xs text-muted-foreground text-center">
          © {new Date().getFullYear()} NavAssist Technologies Pvt. Ltd. All rights reserved.
        </span>

        {/* Secondary controls (Theme toggle) */}
        <div className="flex items-center gap-4">
          <span className="text-xs text-muted-foreground font-semibold uppercase tracking-wider">Device Sync Status: Online</span>
          <Button variant="ghost" size="icon" onClick={toggleTheme} className="rounded-full">
            {theme === "light" ? <MoonIcon size={16} /> : <SunIcon size={16} />}
          </Button>
        </div>
      </div>
    </footer>
  )
}
