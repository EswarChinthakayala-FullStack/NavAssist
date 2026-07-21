import { useNavigate } from "react-router-dom"
import { useAuth } from "@/store/auth-context"
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar"
import {
  CaretUpDownIcon,
  UserCircleIcon,
  BellIcon,
  SignOutIcon,
  ShieldCheckIcon,
  HeadsetIcon,
  CompassIcon,
  GearIcon
} from "@phosphor-icons/react"

interface NavUserProps {
  user: {
    name: string
    email: string
    avatar: string
    role: "guest" | "assistant" | "admin"
    isVerified: boolean
  }
}

const roleLabels: Record<string, { label: string; color: string }> = {
  guest: { label: "Passenger", color: "bg-blue-500/15 text-blue-400 border-blue-500/20" },
  assistant: { label: "Guide", color: "bg-emerald-500/15 text-emerald-400 border-emerald-500/20" },
  admin: { label: "Admin", color: "bg-amber-500/15 text-amber-400 border-amber-500/20" },
}

export function NavUser({ user }: NavUserProps) {
  const { isMobile } = useSidebar()
  const { logout } = useAuth()
  const navigate = useNavigate()

  const initials = user.name
    ? user.name
        .trim()
        .split(" ")
        .map((w) => w.charAt(0))
        .join("")
        .toUpperCase()
        .slice(0, 2)
    : "U"

  const roleInfo = roleLabels[user.role] || roleLabels.guest

  const handleLogout = async () => {
    await logout()
    navigate("/login", { replace: true })
  }

  return (
    <SidebarMenu>
      <SidebarMenuItem>
        <DropdownMenu>
          <DropdownMenuTrigger
            render={
              <SidebarMenuButton size="lg" className="aria-expanded:bg-muted" />
            }
          >
            <div className="relative">
              <Avatar>
                {user.avatar && <AvatarImage src={user.avatar} alt={user.name} />}
                <AvatarFallback className="bg-primary/10 text-primary font-bold text-xs uppercase rounded-full">
                  {initials}
                </AvatarFallback>
              </Avatar>
              {user.isVerified && (
                <div className="absolute -bottom-0.5 -right-0.5 w-3.5 h-3.5 rounded-full bg-background flex items-center justify-center">
                  <div className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
                </div>
              )}
            </div>
            <div className="grid flex-1 text-left text-sm leading-tight min-w-0">
              <span className="truncate font-medium">{user.name}</span>
              <span className="truncate text-xs text-muted-foreground">{user.email}</span>
            </div>
            <CaretUpDownIcon className="ml-auto size-4" />
          </DropdownMenuTrigger>

          <DropdownMenuContent
            className="w-64"
            side={isMobile ? "bottom" : "right"}
            align="end"
            sideOffset={4}
          >
            {/* User Info Header */}
            <DropdownMenuGroup>
              <DropdownMenuLabel className="p-0 font-normal">
                <div className="flex items-center gap-3 px-2 py-2.5 text-left text-sm">
                  <div className="relative">
                    <Avatar className="h-10 w-10">
                      {user.avatar && <AvatarImage src={user.avatar} alt={user.name} />}
                      <AvatarFallback className="bg-primary/10 text-primary font-bold text-xs uppercase rounded-full">
                        {initials}
                      </AvatarFallback>
                    </Avatar>
                    {user.isVerified && (
                      <div className="absolute -bottom-0.5 -right-0.5 w-3.5 h-3.5 rounded-full bg-background flex items-center justify-center">
                        <div className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
                      </div>
                    )}
                  </div>
                  <div className="grid flex-1 text-left leading-tight gap-1">
                    <span className="truncate font-semibold text-sm">{user.name}</span>
                    <span className="truncate text-xs text-muted-foreground">{user.email}</span>
                    <div className="flex items-center gap-1.5 mt-0.5">
                      <span className={`text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full border ${roleInfo.color}`}>
                        {roleInfo.label}
                      </span>
                      {user.isVerified && (
                        <span className="text-[10px] font-medium text-emerald-400 flex items-center gap-0.5">
                          <ShieldCheckIcon size={10} weight="fill" />
                          Verified
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </DropdownMenuLabel>
            </DropdownMenuGroup>

            <DropdownMenuSeparator />

            {/* Navigation Items */}
            <DropdownMenuGroup>
              <DropdownMenuItem onClick={() => navigate("/account/profile")}>
                <UserCircleIcon />
                My Profile
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => navigate("/account/saved-locations")}>
                <CompassIcon />
                Saved Locations
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => navigate("/settings")}>
                <GearIcon />
                Preferences & Settings
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => navigate("/notifications")}>
                <BellIcon />
                Notifications
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => navigate("/support")}>
                <HeadsetIcon />
                Help & Support
              </DropdownMenuItem>
            </DropdownMenuGroup>

            <DropdownMenuSeparator />

            {/* Sign Out */}
            <DropdownMenuItem onClick={handleLogout} className="text-destructive focus:text-destructive focus:bg-destructive/10">
              <SignOutIcon />
              Sign Out
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </SidebarMenuItem>
    </SidebarMenu>
  )
}
