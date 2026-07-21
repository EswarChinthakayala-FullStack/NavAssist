import React, { useState, useEffect } from "react"
import { motion, AnimatePresence } from "framer-motion"
import { useNavigate, useLocation } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { Sheet, SheetContent, SheetTrigger, SheetHeader, SheetTitle } from "@/components/ui/sheet"
import { SunIcon, MoonIcon, ListIcon } from "@phosphor-icons/react"
import { AppLogo } from "@/components/ui/app-logo"

interface NavbarProps {
  theme: string
  toggleTheme: () => void
}

export function Navbar({ theme, toggleTheme }: NavbarProps) {
  const [isScrolled, setIsScrolled] = useState(false)
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 40)
    }
    window.addEventListener("scroll", handleScroll)
    return () => window.removeEventListener("scroll", handleScroll)
  }, [])

  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth >= 1024) {
        setIsMenuOpen(false)
      }
    }
    window.addEventListener("resize", handleResize)
    return () => window.removeEventListener("resize", handleResize)
  }, [])

  const navLinks = [
    { name: "How it Works", href: "#how-it-works" },
    { name: "Safety & SOS", href: "#safety-deepdive" },
    { name: "Become an Assistant", href: "#for-assistants" },
    { name: "FAQ", href: "#faq" }
  ]

  const handleNavClick = (e: React.MouseEvent<HTMLAnchorElement>, href: string) => {
    e.preventDefault()
    setIsMenuOpen(false)
    if (location.pathname === "/" || location.pathname === "/landing") {
      const targetElement = document.querySelector(href)
      if (targetElement) {
        targetElement.scrollIntoView({ behavior: "smooth" })
      }
    } else {
      navigate({ pathname: "/", hash: href })
    }
  }

  return (
    <motion.header
      className={`fixed top-0 left-0 right-0 z-40 transition-colors duration-300 ${
        isScrolled
          ? "bg-background/80 backdrop-blur-md border-b border-border"
          : "bg-transparent border-b border-transparent"
      }`}
      initial={{ y: -64 }}
      animate={{ y: 0 }}
      transition={{ duration: 0.4 }}
    >
      <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
        {/* Left: Logo */}
        <div className="flex items-center gap-2 cursor-pointer" onClick={() => navigate("/")}>
          <AppLogo size="sm" />
        </div>

        {/* Center: Nav links (desktop) */}
        <nav className="hidden lg:flex items-center gap-8">
          {navLinks.map((link) => (
            <a
              key={link.name}
              href={link.href}
              onClick={(e) => handleNavClick(e, link.href)}
              className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
            >
              {link.name}
            </a>
          ))}
        </nav>

        {/* Right: Actions */}
        <div className="hidden lg:flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={toggleTheme} className="rounded-full">
            {theme === "light" ? <MoonIcon size={20} /> : <SunIcon size={20} />}
          </Button>
          <Button variant="ghost" onClick={() => navigate("/dashboard")}>
            Log In
          </Button>
          <Button onClick={() => navigate("/dashboard")} className="bg-primary text-primary-foreground hover:bg-primary/90">
            Get Started
          </Button>
        </div>

        {/* Mobile Hamburger menu */}
        <div className="flex lg:hidden items-center gap-3">
          <Button variant="ghost" size="icon" onClick={toggleTheme} className="rounded-full">
            {theme === "light" ? <MoonIcon size={18} /> : <SunIcon size={18} />}
          </Button>

          <Sheet open={isMenuOpen} onOpenChange={setIsMenuOpen}>
            <SheetTrigger render={<Button variant="ghost" size="icon" />}>
              <ListIcon size={22} />
            </SheetTrigger>
            <SheetContent side="right" className="w-[300px] flex flex-col justify-between p-6">
              <div className="flex flex-col">
                <SheetHeader className="text-left pb-4 border-b p-0">
                  <SheetTitle className="flex items-center gap-2 font-bold">
                    <AppLogo size="sm" animated={false} />
                  </SheetTitle>
                </SheetHeader>
                <div className="flex flex-col gap-2 mt-6">
                  {navLinks.map((link) => (
                    <a
                      key={link.name}
                      href={link.href}
                      onClick={(e) => handleNavClick(e, link.href)}
                      className="text-base font-semibold text-muted-foreground hover:text-foreground hover:bg-muted/45 px-4 py-3 rounded-xl transition-all duration-200 text-left"
                    >
                      {link.name}
                    </a>
                  ))}
                </div>
              </div>
              <div className="flex flex-col gap-3">
                <Button variant="outline" className="w-full" onClick={() => navigate("/dashboard")}>
                  Log In
                </Button>
                <Button className="w-full bg-primary text-primary-foreground hover:bg-primary/90" onClick={() => navigate("/dashboard")}>
                  Get Started
                </Button>
              </div>
            </SheetContent>
          </Sheet>
        </div>
      </div>
    </motion.header>
  )
}
