import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useAuth } from "@/store/auth-context"
import { useUiStore } from "@/store/ui.store"
import { useTheme } from "next-themes"
import { useMutation, useQueryClient } from "@tanstack/react-query"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Switch } from "@/components/ui/switch"
import { Badge } from "@/components/ui/badge"
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogCancel,
  AlertDialogAction
} from "@/components/ui/alert-dialog"
import {
  SignOutIcon,
  MoonIcon,
  SunIcon,
  ShieldCheckIcon,
  QuestionIcon,
  HeadsetIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

export function SettingsPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { user, logout } = useAuth()

  const { theme, setTheme: setNextTheme } = useTheme()
  const { setTheme: setUiTheme } = useUiStore()

  const [darkMode, setDarkMode] = useState(theme === "dark")
  const [logoutOpen, setLogoutOpen] = useState(false)

  useEffect(() => {
    setDarkMode(theme === "dark")
  }, [theme])

  const updateSettingsMutation = useMutation({
    mutationFn: async (payload: any) => {
      return new Promise((resolve) => setTimeout(() => resolve(payload), 300))
    },
    onSuccess: () => {
      toast.success("Preferences updated!")
    }
  })

  const handleThemeChange = (checked: boolean) => {
    setDarkMode(checked)
    const val = checked ? "dark" : "light"
    setNextTheme(val)
    setUiTheme(val)
    updateSettingsMutation.mutate({ dark_mode: checked })
  }

  const confirmLogout = async () => {
    try {
      await logout()
      queryClient.clear()
      toast.success("Logged out successfully.")
      navigate("/welcome", { replace: true })
    } catch (err) {
      console.error(err)
      toast.error("Logout failed. Please try again.")
    }
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-5xl mx-auto py-4 space-y-6 text-left"
    >
      {/* Header Banner */}
      <div className="bg-card border border-border/80 p-6 rounded-2xl shadow-sm">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <div className="flex items-center gap-2">
              <Badge className="bg-primary/10 text-primary border-0 text-[10px] px-3.5 py-1 rounded-full font-bold uppercase tracking-wider">
                System Preferences
              </Badge>
              <Badge variant="outline" className="font-bold text-[10px] uppercase tracking-wider rounded-full border-border/80">
                SSL Secured Session
              </Badge>
            </div>
            <h3 className="font-black text-2xl mt-3 tracking-tight text-foreground">Account & System Settings</h3>
            <p className="text-xs text-muted-foreground mt-1">Manage display theme preferences, security properties, and session controls.</p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Left Column: Interface & Security */}
        <div className="space-y-6">
          {/* Display & Theme Card */}
          <Card className="border border-border/80 shadow-sm rounded-2xl overflow-hidden bg-card">
            <CardHeader className="pb-3 border-b border-border/40">
              <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-2">
                {darkMode ? <MoonIcon size={18} className="text-primary" /> : <SunIcon size={18} className="text-primary" />}
                Interface Appearance
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <h5 className="text-sm font-bold text-foreground">Dark Theme</h5>
                  <p className="text-xs text-muted-foreground">Toggle dark mode interface on this device</p>
                </div>
                <Switch
                  checked={darkMode}
                  onCheckedChange={handleThemeChange}
                />
              </div>
            </CardContent>
          </Card>

          {/* Account Identity & Security Properties */}
          <Card className="border border-border/80 shadow-sm rounded-2xl overflow-hidden bg-card">
            <CardHeader className="pb-3 border-b border-border/40">
              <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-2">
                <ShieldCheckIcon size={18} className="text-emerald-500" />
                Security Overview
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-6 space-y-4 text-xs">
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground font-semibold">Account Role</span>
                <Badge variant="outline" className="font-mono font-bold uppercase text-[10px] px-2.5 py-0.5 rounded-full">
                  {user?.role || "User"}
                </Badge>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground font-semibold">Phone Verified</span>
                <span className="font-bold text-emerald-500 flex items-center gap-1">
                  ✓ Verified
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground font-semibold">Encryption Protocol</span>
                <span className="font-mono font-semibold text-foreground">TLS 1.3 / AES-256</span>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right Column: Help Resources & Session Revocation */}
        <div className="space-y-6">
          {/* Help & Support Card */}
          <Card className="border border-border/80 shadow-sm rounded-2xl overflow-hidden bg-card">
            <CardHeader className="pb-3 border-b border-border/40">
              <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-2">
                <QuestionIcon size={18} className="text-primary" />
                Support & Resources
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-6 space-y-4">
              <p className="text-xs text-muted-foreground leading-relaxed">
                Need help with your bookings, account settings, or disputes? Connect directly with our support desk.
              </p>
              <Button
                variant="outline"
                onClick={() => navigate("/support")}
                className="w-full text-xs font-bold py-2.5 rounded-xl border border-border flex items-center justify-center gap-2 cursor-pointer hover:bg-muted/50"
              >
                <HeadsetIcon size={16} />
                <span>Open Support & FAQs Hub</span>
              </Button>
            </CardContent>
          </Card>

          {/* Session Revocation / Logout */}
          <Card className="border border-destructive/20 bg-destructive/5 shadow-sm rounded-2xl overflow-hidden">
            <CardHeader className="pb-3 border-b border-destructive/10">
              <CardTitle className="text-xs font-bold text-destructive uppercase tracking-wider flex items-center gap-2">
                <SignOutIcon size={18} />
                Session Control
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-6">
              <p className="text-xs text-muted-foreground leading-relaxed mb-4">
                Revoke current authentication session tokens and log out of this device.
              </p>
              <Button
                onClick={() => setLogoutOpen(true)}
                className="w-full bg-destructive text-destructive-foreground hover:bg-destructive/90 py-5 font-black text-xs rounded-xl shadow-xs cursor-pointer hover:scale-[1.01] transition-transform"
              >
                Sign Out of Account
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Logout confirmation AlertDialog */}
      <AlertDialog open={logoutOpen} onOpenChange={setLogoutOpen}>
        <AlertDialogContent className="max-w-xs sm:max-w-sm rounded-2xl border border-destructive/20 bg-card p-6">
          <AlertDialogHeader className="space-y-3 text-center sm:text-left">
            <div className="p-3.5 bg-destructive/10 text-destructive rounded-full w-fit mx-auto sm:mx-0">
              <SignOutIcon size={32} weight="fill" />
            </div>
            <div className="space-y-1">
              <AlertDialogTitle className="text-base font-bold text-destructive">Confirm Logout Request?</AlertDialogTitle>
              <AlertDialogDescription className="text-xs text-muted-foreground leading-relaxed">
                Are you sure you want to log out? This clears all active session tokens and requires logging in again on your next visit.
              </AlertDialogDescription>
            </div>
          </AlertDialogHeader>
          <AlertDialogFooter className="flex flex-col sm:flex-row gap-2 mt-4">
            <AlertDialogCancel className="w-full sm:w-auto rounded-xl py-2.5 font-bold text-xs">
              Cancel
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={confirmLogout}
              className="w-full sm:w-auto bg-destructive text-destructive-foreground hover:bg-destructive/95 rounded-xl py-2.5 px-6 font-extrabold text-xs shadow-sm"
            >
              Confirm Logout
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </motion.div>
  )
}
export default SettingsPage
