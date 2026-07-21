import React, { useState } from "react"
import { useNavigate, Link } from "react-router-dom"
import { authService } from "@/services/auth.service"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { CompassIcon } from "@phosphor-icons/react"
import { signupSchema } from "@/lib/validators"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from "@/components/ui/select"

export function SignupPage() {
  const navigate = useNavigate()
  
  const [name, setName] = useState("")
  const [email, setEmail] = useState("")
  const [phone, setPhone] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [role, setRole] = useState<"guest" | "assistant">("guest")
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrors({})
    setLoading(true)

    // Run frontend validations
    const valResult = signupSchema.safeParse({ name, email, phone, password, confirmPassword, role })
    if (!valResult.success) {
      const errMap: Record<string, string> = {}
      valResult.error.issues.forEach((err: any) => {
        if (err.path[0]) {
          errMap[err.path[0].toString()] = err.message
        }
      })
      setErrors(errMap)
      setLoading(false)
      toast.error("Please resolve registration field errors.")
      return
    }

    try {
      // Call auth service signup
      await authService.signup({ name, email, phone, password, role })
      
      // Automatically call send OTP code
      await authService.sendOtp(phone)
      
      toast.success("Account created! Passcode OTP code sent to your phone.")
      
      // Navigate to otp-verification passing state parameters
      navigate("/otp-verification", { state: { phone } })
    } catch (err: any) {
      let msg = "Could not create user account."
      if (err.response?.data?.detail) {
        msg = typeof err.response.data.detail === "string" ? err.response.data.detail : err.response.data.detail[0]?.msg || msg
      }
      toast.error(msg)
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

      <div className="max-w-md w-full bg-card border border-border rounded-3xl p-8 shadow-2xl relative z-10 flex flex-col gap-5 max-h-[90vh] overflow-y-auto animate-in fade-in zoom-in-95 duration-200">
        <div className="flex flex-col items-center text-center gap-1.5">
          <div className="w-10 h-10 bg-primary/10 rounded-2xl flex items-center justify-center text-primary mb-1">
            <CompassIcon size={24} weight="fill" />
          </div>
          <h3 className="text-lg font-black tracking-tight">Create User Account</h3>
          <p className="text-[10px] text-muted-foreground">Register your secure travelers profile below.</p>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-3">
          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1">
              <label className="text-[9px] font-extrabold uppercase tracking-wider text-muted-foreground">Full Name</label>
              <Input
                type="text"
                disabled={loading}
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="John Doe"
                className="rounded-xl h-10 text-xs font-semibold"
              />
              {errors.name && <span className="text-[9px] text-destructive font-bold">{errors.name}</span>}
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-[9px] font-extrabold uppercase tracking-wider text-muted-foreground">User Role</label>
              <Select disabled={loading} value={role} onValueChange={(val) => setRole((val || "guest") as any)}>
                <SelectTrigger className="flex h-10 w-full rounded-xl border border-input bg-card px-3 text-xs font-semibold shadow-sm justify-between">
                  <SelectValue placeholder="Select Role" />
                </SelectTrigger>
                <SelectContent className="bg-popover border border-border rounded-xl">
                  <SelectItem value="guest">Traveler (Guest)</SelectItem>
                  <SelectItem value="assistant">Escort (Assistant)</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1">
              <label className="text-[9px] font-extrabold uppercase tracking-wider text-muted-foreground">Email</label>
              <Input
                type="email"
                disabled={loading}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@domain.com"
                className="rounded-xl h-10 text-xs font-semibold"
              />
              {errors.email && <span className="text-[9px] text-destructive font-bold">{errors.email}</span>}
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-[9px] font-extrabold uppercase tracking-wider text-muted-foreground">Phone Number</label>
              <Input
                type="text"
                disabled={loading}
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="10-digit number"
                className="rounded-xl h-10 text-xs font-semibold"
              />
              {errors.phone && <span className="text-[9px] text-destructive font-bold">{errors.phone}</span>}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1">
              <label className="text-[9px] font-extrabold uppercase tracking-wider text-muted-foreground">Password</label>
              <Input
                type="password"
                disabled={loading}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Min 8 characters"
                className="rounded-xl h-10 text-xs font-semibold"
              />
              {errors.password && <span className="text-[9px] text-destructive font-bold">{errors.password}</span>}
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-[9px] font-extrabold uppercase tracking-wider text-muted-foreground">Confirm</label>
              <Input
                type="password"
                disabled={loading}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Confirm password"
                className="rounded-xl h-10 text-xs font-semibold"
              />
              {errors.confirmPassword && <span className="text-[9px] text-destructive font-bold">{errors.confirmPassword}</span>}
            </div>
          </div>

          <Button
            type="submit"
            disabled={loading}
            className="w-full py-5 rounded-xl font-bold bg-primary text-white text-xs shadow-sm hover:scale-102 transition-all mt-3"
          >
            {loading ? "Registering..." : "Create Secure Account"}
          </Button>
        </form>

        <div className="text-center mt-1">
          <span className="text-[10px] text-muted-foreground">Already registered? </span>
          <Link to="/login" className="text-[10px] font-extrabold text-primary hover:underline">Log In</Link>
        </div>
      </div>
    </div>
  )
}
export default SignupPage
