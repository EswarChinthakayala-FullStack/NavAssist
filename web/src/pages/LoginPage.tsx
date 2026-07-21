import React, { useState } from "react"
import { useNavigate, Link } from "react-router-dom"
import { useAuthStore } from "@/store/auth.store"
import { authService } from "@/services/auth.service"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { EyeIcon, EyeSlashIcon, ShieldIcon, CompassIcon } from "@phosphor-icons/react"

export function LoginPage() {
  const navigate = useNavigate()
  const loginStore = useAuthStore()
  
  const [phone, setPhone] = useState("")
  const [password, setPassword] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [authError, setAuthError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!phone || !password) {
      toast.error("Please fill in all credentials.")
      return
    }

    setLoading(true)
    setAuthError(null)

    try {
      const data = await authService.login(phone, password)
      loginStore.login(data.access_token, data.refresh_token, data.user)
      toast.success("Welcome back! Authentication successful.")
      navigate("/home", { replace: true })
    } catch (err: any) {
      let msg = "Could not validate login details."
      if (err.response?.data?.detail) {
        msg = typeof err.response.data.detail === "string" ? err.response.data.detail : err.response.data.detail[0]?.msg || msg
      }
      setAuthError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="h-screen w-screen bg-background text-foreground flex items-center justify-center p-6 relative overflow-hidden select-none">
      {/* Dot matrix background pattern */}
      <div className="absolute inset-0 opacity-[0.05] bg-[radial-gradient(circle_at_center,var(--foreground)_10%,transparent_50%)] bg-[size:24px_24px] pointer-events-none" />

      {/* Radial glow */}
      <div className="absolute inset-0 opacity-20 bg-[radial-gradient(ellipse_at_top_right,hsl(var(--primary)/0.25),transparent_60%)] pointer-events-none" />

      <div className="max-w-sm w-full bg-card border border-border rounded-3xl p-8 shadow-2xl relative z-10 flex flex-col gap-6 animate-in fade-in zoom-in-95 duration-200">
        <div className="flex flex-col items-center text-center gap-2">
          <div className="w-12 h-12 bg-primary/10 rounded-2xl flex items-center justify-center text-primary mb-2">
            <CompassIcon size={28} weight="fill" />
          </div>
          <h3 className="text-xl font-black tracking-tight">Access Secure Portal</h3>
          <p className="text-[10px] text-muted-foreground">Sign in with your verified registration phone number.</p>
        </div>

        {authError && (
          <div className="p-3 bg-destructive/10 border border-destructive/20 text-destructive text-[10px] font-bold rounded-xl flex gap-2 items-center">
            <ShieldIcon size={16} weight="fill" className="shrink-0" />
            <span>{authError}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-1.5">
            <label className="text-[9px] font-extrabold uppercase tracking-wider text-muted-foreground">Phone Number</label>
            <Input
              type="text"
              required
              disabled={loading}
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="e.g. 9876543210"
              className="rounded-xl h-11 text-xs font-semibold"
            />
          </div>

          <div className="flex flex-col gap-1.5 relative">
            <div className="flex justify-between items-center w-full">
              <label className="text-[9px] font-extrabold uppercase tracking-wider text-muted-foreground">Password</label>
              <Link to="/forgot" className="text-[9px] font-bold text-primary hover:underline">Forgot password?</Link>
            </div>
            <div className="relative">
              <Input
                type={showPassword ? "text" : "password"}
                required
                disabled={loading}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter password details"
                className="rounded-xl h-11 text-xs font-semibold pr-10"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground cursor-pointer"
              >
                {showPassword ? <EyeSlashIcon size={16} /> : <EyeIcon size={16} />}
              </button>
            </div>
          </div>

          <Button
            type="submit"
            disabled={loading}
            className="w-full py-5 rounded-xl font-bold bg-primary text-white text-xs shadow-sm hover:scale-102 transition-all mt-2"
          >
            {loading ? "Authenticating..." : "Login to Portal"}
          </Button>
        </form>

        <div className="text-center mt-2">
          <span className="text-[10px] text-muted-foreground">Don't have an account? </span>
          <Link to="/signup" className="text-[10px] font-extrabold text-primary hover:underline">Sign Up</Link>
        </div>
      </div>
    </div>
  )
}
export default LoginPage
