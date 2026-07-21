"use client"

import * as React from "react"

import { NavMain } from "@/components/nav-main"
import { NavProjects } from "@/components/nav-projects"
import { NavUser } from "@/components/nav-user"
import { TeamSwitcher } from "@/components/team-switcher"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarRail,
} from "@/components/ui/sidebar"
import { Separator } from "@/components/ui/separator"
import {
  GearIcon,
  MapTrifoldIcon,
  ShieldCheckIcon,
  SirenIcon,
  HouseIcon,
  SuitcaseIcon,
  UsersIcon,
  ChartBarIcon,
  TicketIcon,
  HeadsetIcon,
  NavigationArrowIcon,
  ClockCounterClockwiseIcon,
  MapPinIcon,
  BellIcon,
  UserCircleIcon,
  MagnifyingGlassIcon,
  CurrencyDollarIcon,
  IdentificationCardIcon,
  GaugeIcon,
  ListChecksIcon,
  FlagBannerIcon,
  WalletIcon,
  BookOpenIcon,
} from "@phosphor-icons/react"
import { AppLogo } from "@/components/ui/app-logo"

import { useAuth } from "@/store/auth-context"

// ── Role branding ────────────────────────────────────────────────
const roleBranding: Record<string, { plan: string }> = {
  guest:     { plan: "Passenger Mode" },
  assistant: { plan: "Guide Mode" },
  admin:     { plan: "Admin Mode" },
}

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  const { user } = useAuth()
  const role = (user?.role || "guest") as "guest" | "assistant" | "admin"

  const branding = roleBranding[role] || roleBranding.guest

  const teamData = [
    {
      name: "NavAssist",
      logo: <AppLogo variant="icon" animated={false} />,
      plan: branding.plan,
    },
  ]

  // ═══════════════════════════════════════════════════════════════
  //  GUEST (Passenger) Navigation
  // ═══════════════════════════════════════════════════════════════
  const guestNav = [
    {
      title: "Travel",
      url: "#",
      icon: <SuitcaseIcon weight="duotone" />,
      isActive: true,
      items: [
        { title: "Home Map",        url: "/home",                   icon: <HouseIcon size={15} weight="duotone" /> },
        { title: "Book a Guide",    url: "/book/pickup",            icon: <NavigationArrowIcon size={15} weight="duotone" /> },
        { title: "Trip History",    url: "/trips",                  icon: <ClockCounterClockwiseIcon size={15} weight="duotone" /> },
        { title: "Saved Locations", url: "/account/saved-locations", icon: <MapPinIcon size={15} weight="duotone" /> },
      ],
    },
    {
      title: "Safety & Help",
      url: "#",
      icon: <SirenIcon weight="duotone" />,
      items: [
        { title: "Safety & SOS",    url: "/safety",  icon: <SirenIcon size={15} weight="duotone" /> },
        { title: "Help & Support",  url: "/support",  icon: <HeadsetIcon size={15} weight="duotone" /> },
        { title: "Report Feedback", url: "/support/feedback", icon: <FlagBannerIcon size={15} weight="duotone" /> },
      ],
    },
    {
      title: "Account",
      url: "#",
      icon: <GearIcon weight="duotone" />,
      items: [
        { title: "My Profile",     url: "/account/profile",  icon: <UserCircleIcon size={15} weight="duotone" /> },
        { title: "Preferences",    url: "/settings",         icon: <GearIcon size={15} weight="duotone" /> },
        { title: "Notifications",  url: "/notifications",    icon: <BellIcon size={15} weight="duotone" /> },
      ],
    },
  ]

  const guestQuickActions = [
    { name: "Book a Guide",     url: "/book/pickup", icon: <NavigationArrowIcon size={16} weight="fill" />, badge: "New" },
    { name: "Home Map",         url: "/home",        icon: <HouseIcon size={16} weight="fill" /> },
    { name: "Emergency SOS",    url: "/safety",      icon: <SirenIcon size={16} weight="fill" /> },
  ]

  // ═══════════════════════════════════════════════════════════════
  //  ASSISTANT (Guide) Navigation
  // ═══════════════════════════════════════════════════════════════
  const assistantNav = [
    {
      title: "Guide Services",
      url: "#",
      icon: <NavigationArrowIcon weight="duotone" />,
      isActive: true,
      items: [
        { title: "Dashboard",        url: "/dashboard",  icon: <GaugeIcon size={15} weight="duotone" /> },
        { title: "Job History",       url: "/trips",      icon: <ClockCounterClockwiseIcon size={15} weight="duotone" /> },
        { title: "Active Bookings",   url: "/bookings",   icon: <ListChecksIcon size={15} weight="duotone" /> },
      ],
    },
    {
      title: "Verification",
      url: "#",
      icon: <ShieldCheckIcon weight="duotone" />,
      items: [
        { title: "KYC Identity",     url: "/kyc",    icon: <IdentificationCardIcon size={15} weight="duotone" /> },
        { title: "Safety & SOS",     url: "/safety", icon: <SirenIcon size={15} weight="duotone" /> },
      ],
    },
    {
      title: "Account",
      url: "#",
      icon: <GearIcon weight="duotone" />,
      items: [
        { title: "My Profile",     url: "/account/profile",  icon: <UserCircleIcon size={15} weight="duotone" /> },
        { title: "Preferences",    url: "/settings",         icon: <GearIcon size={15} weight="duotone" /> },
        { title: "Notifications",  url: "/notifications",    icon: <BellIcon size={15} weight="duotone" /> },
        { title: "Help & Support", url: "/support",          icon: <HeadsetIcon size={15} weight="duotone" /> },
      ],
    },
  ]

  const assistantQuickActions = [
    { name: "Guide Dashboard", url: "/dashboard", icon: <GaugeIcon size={16} weight="fill" /> },
    { name: "KYC Verification", url: "/kyc",      icon: <ShieldCheckIcon size={16} weight="fill" />, badge: "Required" },
    { name: "Emergency SOS",    url: "/safety",    icon: <SirenIcon size={16} weight="fill" /> },
  ]

  // ═══════════════════════════════════════════════════════════════
  //  ADMIN Navigation
  // ═══════════════════════════════════════════════════════════════
  const adminNav = [
    {
      title: "Administration",
      url: "#",
      icon: <ChartBarIcon weight="duotone" />,
      isActive: true,
      items: [
        { title: "Admin Console",    url: "/admin?tab=overview",    icon: <GaugeIcon size={15} weight="duotone" /> },
        { title: "User Management",  url: "/admin?tab=users",       icon: <UsersIcon size={15} weight="duotone" /> },
        { title: "KYC Queue",        url: "/admin?tab=kyc",         icon: <ShieldCheckIcon size={15} weight="duotone" /> },
      ],
    },
    {
      title: "Support & Ops",
      url: "#",
      icon: <HeadsetIcon weight="duotone" />,
      items: [
        { title: "Support Tickets",  url: "/support",           icon: <TicketIcon size={15} weight="duotone" /> },
        { title: "Report Feedback",  url: "/support/feedback",  icon: <FlagBannerIcon size={15} weight="duotone" /> },
      ],
    },
    {
      title: "Account",
      url: "#",
      icon: <GearIcon weight="duotone" />,
      items: [
        { title: "My Profile",     url: "/account/profile",  icon: <UserCircleIcon size={15} weight="duotone" /> },
        { title: "Preferences",    url: "/settings",         icon: <GearIcon size={15} weight="duotone" /> },
        { title: "Notifications",  url: "/notifications",    icon: <BellIcon size={15} weight="duotone" /> },
      ],
    },
  ]

  const adminQuickActions = [
    { name: "Admin Console",    url: "/admin",   icon: <ChartBarIcon size={16} weight="fill" /> },
    { name: "Support Tickets",  url: "/support", icon: <TicketIcon size={16} weight="fill" /> },
  ]

  // ── Select config by role ──────────────────────────────────────
  const navItems = role === "admin" ? adminNav : role === "assistant" ? assistantNav : guestNav
  const quickActions = role === "admin" ? adminQuickActions : role === "assistant" ? assistantQuickActions : guestQuickActions
  const navLabel = role === "admin" ? "Admin Portal" : role === "assistant" ? "Guide Portal" : "Passenger Portal"

  // ── User object for footer ─────────────────────────────────────
  const currentUser = {
    name: user?.full_name || "NavAssist User",
    email: user?.email || "user@navassist.in",
    avatar: user?.profile_photo_url || "",
    role: role,
    isVerified: !!(user?.is_phone_verified && user?.is_email_verified),
  }

  return (
    <Sidebar collapsible="icon" {...props}>
      <SidebarHeader>
        <TeamSwitcher teams={teamData} />
      </SidebarHeader>
      <SidebarContent>
        <NavMain items={navItems} label={navLabel} />
        <Separator className="mx-3 opacity-50" />
        <NavProjects projects={quickActions} label="Quick Actions" />
      </SidebarContent>
      <SidebarFooter>
        <NavUser user={currentUser} />
      </SidebarFooter>
      <SidebarRail />
    </Sidebar>
  )
}
