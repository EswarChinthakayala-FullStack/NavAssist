import React from "react"
import { createBrowserRouter, Navigate, Outlet, useLocation, useNavigate } from "react-router-dom"
import { useAuth } from "@/store/auth-context"
import { SidebarProvider, SidebarTrigger, SidebarInset } from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/app-sidebar"
import { Separator } from "@/components/ui/separator"
import { Button } from "@/components/ui/button"
import { SunIcon, MoonIcon } from "@phosphor-icons/react"
import { useTheme } from "next-themes"
import { toast } from "sonner"
import { pageTransition } from "@/lib/motion-variants"
import { motion } from "framer-motion"
import { ErrorBoundary } from "@/components/feedback/ErrorBoundary"

// Page imports
import { LandingPage } from "@/pages/LandingPage"
import { DashboardPage } from "@/pages/DashboardPage"
import { AdminConsolePage } from "@/pages/AdminConsolePage"
import { BookingsPage } from "@/pages/BookingsPage"
import { KycPage } from "@/pages/KycPage"
import { SafetyPage } from "@/pages/SafetyPage"
import { SettingsPage } from "@/pages/SettingsPage"
import { PrivacyPage } from "@/pages/PrivacyPage"
import { TermsPage } from "@/pages/TermsPage"
import { RefundPage } from "@/pages/RefundPage"
import { SitemapPage } from "@/pages/SitemapPage"
import { PublicTrackingPage } from "@/pages/PublicTrackingPage"
import { AuthPage } from "@/pages/AuthPage"
import { OtpVerificationPage } from "@/pages/OtpVerificationPage"
import { LocationPermissionPage } from "@/pages/LocationPermissionPage"
import { NotificationsPage } from "@/pages/NotificationsPage"
import { SupportPage } from "@/pages/SupportPage"
import { AssistantProfilePage } from "@/pages/AssistantProfilePage"
import { HomeDashboardPage } from "@/pages/HomeDashboardPage"
import { BookAssistantPage } from "@/pages/book/BookAssistantPage"
import { PickupLocationPage } from "@/pages/book/PickupLocationPage"
import { DestinationPage } from "@/pages/book/DestinationPage"
import { ScheduleDateTimePage } from "@/pages/book/ScheduleDateTimePage"
import { AvailableAssistantsPage } from "@/pages/book/AvailableAssistantsPage"
import { BookConfirmPage } from "@/pages/book/BookConfirmPage"
import { BookingSummaryPage } from "@/pages/book/BookingSummaryPage"
import { PriceEstimatePage } from "@/pages/book/PriceEstimatePage"
import { DiscountsOffersPage } from "@/pages/book/DiscountsOffersPage"
import { ApplyCouponPage } from "@/pages/book/ApplyCouponPage"
import { PaymentMethodPage } from "@/pages/book/PaymentMethodPage"
import { PaymentConfirmationPage } from "@/pages/book/PaymentConfirmationPage"
import { AssistantAssignedPage } from "@/pages/trip/AssistantAssignedPage"
import { AssistantEnRoutePage } from "@/pages/trip/AssistantEnRoutePage"
import { SosButton } from "@/components/booking/SosButton"
import { LiveTrackingPage } from "@/pages/trip/LiveTrackingPage"
import { GuestPickedUpPage } from "@/pages/trip/GuestPickedUpPage"
import { NavigationRoutePage } from "@/pages/trip/NavigationRoutePage"
import { JourneyProgressPage } from "@/pages/trip/JourneyProgressPage"
import { EmergencySosPage } from "@/pages/trip/EmergencySosPage"
import { RateAssistantPage } from "@/pages/trip/RateAssistantPage"
import { FeedbackPage } from "@/pages/support/FeedbackPage"
import { TripHistoryPage } from "@/pages/trip/TripHistoryPage"
import { SavedLocationsPage } from "@/pages/account/SavedLocationsPage"
import { UserProfilePage } from "@/pages/account/UserProfilePage"
import { HelpSupportPage } from "@/pages/support/HelpSupportPage"
import { TicketDetailPage } from "@/pages/support/TicketDetailPage"
import { NotFoundPage } from "@/pages/NotFoundPage"
import { RideCompletedPage } from "@/pages/ride/RideCompletedPage"
import { RideCancelledPage } from "@/pages/ride/RideCancelledPage"

// ─── Role type ────────────────────────────────────────────────────
type UserRole = "guest" | "assistant" | "admin"

// ─── Role Guard ───────────────────────────────────────────────────
// Wraps child routes and redirects users whose role is not in allowedRoles.
function RoleGuard({ allowedRoles }: { allowedRoles: UserRole[] }) {
  const { user } = useAuth()
  const location = useLocation()

  if (!user) return null

  if (!allowedRoles.includes(user.role)) {
    // Determine the correct home for the user's role
    const roleHome: Record<UserRole, string> = {
      guest: "/home",
      assistant: "/dashboard",
      admin: "/admin",
    }
    const target = roleHome[user.role] || "/home"

    toast.error("Access denied — you don't have permission to view that page.")
    return <Navigate to={target} state={{ from: location }} replace />
  }

  return <Outlet />
}

// ─── Protected Full Page Layout (No Sidebar/Header) ───────────────
function ProtectedFullPageLayout() {
  const { isAuthenticated, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <div className="h-screen w-screen flex flex-col items-center justify-center bg-background text-foreground gap-4">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-semibold tracking-wider text-muted-foreground">Initializing session...</span>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return <Outlet />
}

// ─── Role-Aware Index Redirect ────────────────────────────────────
function RoleIndexRedirect() {
  const { user } = useAuth()

  if (!user) return <Navigate to="/login" replace />

  const roleHome: Record<UserRole, string> = {
    guest: "/home",
    assistant: "/dashboard",
    admin: "/admin",
  }

  return <Navigate to={roleHome[user.role] || "/home"} replace />
}

// ─── Protected Layout ─────────────────────────────────────────────
function AppLayout() {
  const { theme, setTheme } = useTheme()
  const { isAuthenticated, loading, user } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <div className="h-screen w-screen flex flex-col items-center justify-center bg-background text-foreground gap-4">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-semibold tracking-wider text-muted-foreground">Initializing session...</span>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (isAuthenticated && user && (!user.is_phone_verified || !user.is_email_verified)) {
    return <Navigate to="/otp-verification" replace />
  }

  const getPageTitle = () => {
    const p = location.pathname

    if (p.startsWith("/assistant/")) return "Guide Profile"
    if (p.startsWith("/admin")) return "Admin Panel"
    if (p.startsWith("/book")) return "Book a Guide"
    if (p.startsWith("/trip")) return "Live Journey Tracking"

    switch (p) {
      case "/home":
        return "Passenger Home"
      case "/dashboard":
        return "Guide Console"
      case "/trips":
      case "/bookings":
        return "My Bookings"
      case "/kyc":
        return "KYC Verification"
      case "/safety":
        return "Safety Contacts & SOS"
      case "/settings":
        return "Profile & Settings"
      case "/notifications":
        return "Notifications"
      case "/support":
        return "Help & Support"
      default:
        return "NavAssist"
    }
  }

  const toggleTheme = () => {
    setTheme(theme === "dark" ? "light" : "dark")
  }

  // Role label for the header
  const roleBadge = user?.role === "admin"
    ? "Admin"
    : user?.role === "assistant"
    ? "Guide"
    : "Passenger"

  return (
    <SidebarProvider>
      <div className="flex h-screen w-screen overflow-hidden bg-background text-foreground">
        <AppSidebar />
        <SidebarInset className="flex flex-col flex-1 overflow-hidden">
          <header className="flex h-16 shrink-0 items-center justify-between gap-2 border-b px-6 bg-card/50 backdrop-blur-md">
            <div className="flex items-center gap-2">
              <SidebarTrigger />
              <Separator orientation="vertical" className="mr-2 h-4" />
              <span className="text-sm font-medium text-muted-foreground">{getPageTitle()}</span>
            </div>
            
            <div className="flex items-center gap-4">
              <span className="hidden sm:inline-flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-muted-foreground bg-muted/60 px-2.5 py-1 rounded-full border border-border/50">
                {roleBadge}
              </span>
              <Button variant="ghost" size="icon" onClick={toggleTheme} className="rounded-full">
                {theme === "light" ? <MoonIcon size={20} /> : <SunIcon size={20} />}
              </Button>
              <div className="h-2.5 w-2.5 rounded-full bg-success animate-pulse" />
              <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground hidden sm:inline-block">GPS Live Sync</span>
            </div>
          </header>

          <main className="flex-1 overflow-y-auto p-6 bg-background/90 relative">
            <motion.div
              key={location.pathname.split("/")[1] || ""}
              initial="initial"
              animate="animate"
              exit="exit"
              variants={pageTransition}
              className="h-full w-full"
            >
              <ErrorBoundary>
                <Outlet />
              </ErrorBoundary>
            </motion.div>
            {location.pathname.startsWith("/trip/") && !location.pathname.endsWith("/sos-active") && <SosButton />}
          </main>
        </SidebarInset>
      </div>
    </SidebarProvider>
  )
}

// ─── Public Page Wrapper ──────────────────────────────────────────
function PublicPageWrapper({ component: Component }: { component: React.ComponentType<any> }) {
  const { theme, setTheme } = useTheme()
  const toggleTheme = () => setTheme(theme === "dark" ? "light" : "dark")
  return <Component theme={theme} toggleTheme={toggleTheme} />
}

// ─── Public-Only Guard ────────────────────────────────────────────
function PublicOnlyRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, loading, user } = useAuth()

  if (loading) {
    return (
      <div className="h-screen w-screen flex flex-col items-center justify-center bg-background text-foreground gap-4">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-semibold tracking-wider text-muted-foreground">Initializing session...</span>
      </div>
    )
  }

  if (isAuthenticated && user) {
    const roleHome: Record<UserRole, string> = {
      guest: "/home",
      assistant: "/dashboard",
      admin: "/admin",
    }
    return <Navigate to={roleHome[user.role] || "/home"} replace />
  }

  return <>{children}</>
}

// ─── Wildcard Redirect ────────────────────────────────────────────
function WildcardRedirect() {
  const { user } = useAuth()

  const roleHome: Record<UserRole, string> = {
    guest: "/home",
    assistant: "/dashboard",
    admin: "/admin",
  }

  return <Navigate to={user ? (roleHome[user.role] || "/home") : "/landing"} replace />
}

// ─── Root Path Resolver ──────────────────────────────────────────
function RootPathResolver() {
  const { isAuthenticated, loading, user } = useAuth()

  if (loading) {
    return (
      <div className="h-screen w-screen flex flex-col items-center justify-center bg-background text-foreground gap-4">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-semibold tracking-wider text-muted-foreground">Initializing session...</span>
      </div>
    )
  }

  if (isAuthenticated && user) {
    const roleHome: Record<UserRole, string> = {
      guest: "/home",
      assistant: "/dashboard",
      admin: "/admin",
    }
    return <Navigate to={roleHome[user.role] || "/home"} replace />
  }

  return <PublicPageWrapper component={LandingPage} />
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ROUTER DEFINITION
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
export const router = createBrowserRouter([
  // ── Public Pages ────────────────────────────────────────────────
  {
    path: "/",
    element: <RootPathResolver />,
  },
  {
    path: "/landing",
    element: <PublicPageWrapper component={LandingPage} />,
  },
  {
    path: "/privacy",
    element: <PublicPageWrapper component={PrivacyPage} />,
  },
  {
    path: "/terms",
    element: <PublicPageWrapper component={TermsPage} />,
  },
  {
    path: "/refund-policy",
    element: <PublicPageWrapper component={RefundPage} />,
  },
  {
    path: "/sitemap",
    element: <PublicPageWrapper component={SitemapPage} />,
  },
  {
    path: "/login",
    element: (
      <PublicOnlyRoute>
        <AuthPage mode="login" />
      </PublicOnlyRoute>
    ),
  },
  {
    path: "/signup",
    element: (
      <PublicOnlyRoute>
        <AuthPage mode="signup" />
      </PublicOnlyRoute>
    ),
  },
  {
    path: "/otp-verification",
    element: <OtpVerificationPage />,
  },
  {
    path: "/auth",
    element: <Navigate to="/login" replace />,
  },
  {
    path: "/track/:token",
    element: <PublicTrackingPage />,
  },
  {
    path: "/share/:token",
    element: <PublicTrackingPage />,
  },

  // ── Protected App Routes ────────────────────────────────────────
  {
    path: "/",
    element: <AppLayout />,
    children: [
      // ── Shared Routes (all authenticated roles) ─────────────────
      {
        path: "settings",
        element: <SettingsPage />,
      },
      {
        path: "notifications",
        element: <NotificationsPage />,
      },
      {
        path: "support",
        element: <HelpSupportPage />,
      },
      {
        path: "account/saved-locations",
        element: <SavedLocationsPage />,
      },
      {
        path: "account/profile",
        element: <UserProfilePage />,
      },
      {
        path: "safety",
        element: <SafetyPage />,
      },
      {
        path: "bookings",
        element: <BookingsPage />,
      },
      {
        path: "trips",
        element: <TripHistoryPage />,
      },
      {
        path: "trip/:bookingId/assigned",
        element: <AssistantAssignedPage />,
      },
      {
        path: "trip/:bookingId/enroute",
        element: <AssistantEnRoutePage />,
      },
      {
        path: "trip/:bookingId/tracking",
        element: <LiveTrackingPage />,
      },
      {
        path: "trip/tracking/:bookingId",
        element: <LiveTrackingPage />,
      },
      {
        path: "trip/:bookingId/picked-up",
        element: <GuestPickedUpPage />,
      },
      {
        path: "trip/:bookingId/route",
        element: <NavigationRoutePage />,
      },
      {
        path: "trip/:bookingId/progress",
        element: <JourneyProgressPage />,
      },
      {
        path: "trip/:bookingId/rate",
        element: <RateAssistantPage />,
      },
      {
        path: "trip/:bookingId/sos-active",
        element: <EmergencySosPage />,
      },
      {
        path: "ride/payment/:bookingId",
        element: <PaymentMethodPage />,
      },
      {
        path: "support/feedback",
        element: <FeedbackPage />,
      },
      {
        path: "support/tickets/:id",
        element: <TicketDetailPage />,
      },
      {
        path: "location-permission",
        element: <LocationPermissionPage />,
      },
      {
        path: "assistant/:assistantId",
        element: <AssistantProfilePage />,
      },

      // ── Guest-Only Routes ───────────────────────────────────────
      {
        element: <RoleGuard allowedRoles={["guest"]} />,
        children: [
          {
            path: "home",
            element: <HomeDashboardPage />,
          },
          {
            path: "book",
            element: <BookAssistantPage />,
            children: [
              {
                path: "pickup",
                element: <PickupLocationPage />,
              },
              {
                path: "destination",
                element: <DestinationPage />,
              },
              {
                path: "schedule",
                element: <ScheduleDateTimePage />,
              },
              {
                path: "assistants",
                element: <AvailableAssistantsPage />,
              },
              {
                path: "confirm",
                element: <BookingSummaryPage />,
              },
              {
                path: "summary",
                element: <BookingSummaryPage />,
              },
              {
                path: "price-estimate",
                element: <PriceEstimatePage />,
              },
              {
                path: "offers",
                element: <DiscountsOffersPage />,
              },
              {
                path: "apply-coupon",
                element: <ApplyCouponPage />,
              },
              {
                path: "payment-method",
                element: <PaymentMethodPage />,
              },
              {
                path: "payment-confirmation",
                element: <PaymentConfirmationPage />,
              },
            ],
          },
        ],
      },

      // ── Assistant-Only Routes ───────────────────────────────────
      {
        element: <RoleGuard allowedRoles={["assistant"]} />,
        children: [
          {
            path: "dashboard",
            element: <DashboardPage />,
          },
          {
            path: "kyc",
            element: <KycPage />,
          },
        ],
      },

      // ── Admin-Only Routes ───────────────────────────────────────
      {
        element: <RoleGuard allowedRoles={["admin"]} />,
        children: [
          {
            path: "admin",
            element: <AdminConsolePage />,
          },
        ],
      },
    ],
  },
  
  // ── Protected Full Page Routes (No Sidebar/Header) ──────────────
  {
    path: "/ride",
    element: <ProtectedFullPageLayout />,
    children: [
      {
        path: "completed/:bookingId",
        element: <RideCompletedPage />,
      },
      {
        path: "cancelled/:bookingId",
        element: <RideCancelledPage />,
      },
    ],
  },

  // ── Wildcard ────────────────────────────────────────────────────
  {
    path: "*",
    element: <NotFoundPage />,
  },
])
