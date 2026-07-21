import { Link, useNavigate } from "react-router-dom"
import { motion, type Variants } from "framer-motion"
import { ArrowLeft } from "lucide-react"
import { Button } from "@/components/ui/button"
import { AppLogo } from "@/components/ui/app-logo"
import { LostPinIllustration } from "@/components/illustrations/LostPinIllustration"
import { useAuth } from "@/store/auth-context"

const containerVariants: Variants = {
  hidden: {},
  show: {
    transition: { staggerChildren: 0.1, delayChildren: 0.2 },
  },
}

const itemVariants: Variants = {
  hidden: { opacity: 0, y: 16 },
  show: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.4, ease: "easeOut" as const },
  },
}

/**
 * Catch-all 404 route.
 *
 * Renders outside the normal app chrome (no AppShell / TopBar / BottomNavBar) —
 * this is an escape hatch, not a tab. Registered as the final `path: "*"` entry
 * in the router, outside both ProtectedRoute and PublicOnlyRoute, so it renders
 * regardless of auth state.
 */
export function NotFoundPage() {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()

  // An unauthenticated visitor hitting a bad link should land on the public
  // entry point, not get bounced by ProtectedRoute when we send them to /home.
  const handleBackHome = () => navigate(isAuthenticated ? "/home" : "/")

  return (
    <div className="relative flex min-h-screen flex-col items-center justify-center bg-background px-4 text-center">
      <div className="absolute left-6 top-6">
        <AppLogo variant="icon" size="md" animated={false} />
      </div>

      <div className="mb-8 h-[180px] w-[240px] sm:h-[240px] sm:w-[320px]">
        <LostPinIllustration size={320} className="h-full w-full" />
      </div>

      <motion.div
        variants={containerVariants}
        initial="hidden"
        animate="show"
        className="flex flex-col items-center"
      >
        <motion.p
          variants={itemVariants}
          className="text-sm font-medium tracking-wide text-muted-foreground"
        >
          404 — ROUTE NOT FOUND
        </motion.p>

        <motion.h1
          variants={itemVariants}
          className="mt-3 text-3xl font-bold text-foreground md:text-4xl"
        >
          Looks like this destination doesn&apos;t exist.
        </motion.h1>

        <motion.p
          variants={itemVariants}
          className="mx-auto mt-3 max-w-md text-base leading-relaxed text-muted-foreground"
        >
          The page you&apos;re looking for may have moved, or the link might be
          off by a stop. Let&apos;s get you back on route.
        </motion.p>

        <motion.div
          variants={itemVariants}
          className="mt-8 flex flex-col gap-3 sm:flex-row"
        >
          <Button
            size="lg"
            className="w-full sm:w-auto cursor-pointer"
            onClick={handleBackHome}
          >
            Back to Home
          </Button>
          <Button
            size="lg"
            variant="ghost"
            className="w-full sm:w-auto cursor-pointer"
            onClick={() => navigate(-1)}
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Go Back
          </Button>
        </motion.div>

        <motion.p
          variants={itemVariants}
          className="mt-6 text-xs text-muted-foreground"
        >
          Lost for another reason?{" "}
          <Link
            to="/support"
            className="underline underline-offset-2 hover:text-foreground transition-colors"
          >
            Contact Support
          </Link>
        </motion.p>
      </motion.div>
    </div>
  )
}

export default NotFoundPage