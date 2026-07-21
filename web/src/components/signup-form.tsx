import React, { useState } from "react"
import { cn, parsePhoneNumber } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldDescription, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { useAuth } from "@/store/auth-context"
import { useNavigate, Link } from "react-router-dom"
import { toast } from "sonner"
import { Label } from "@/components/ui/label"
import { UserIcon, ShieldCheckIcon, EyeIcon, EyeSlashIcon } from "@phosphor-icons/react"

interface SignupFormProps extends React.ComponentProps<"div"> {
  onSignInClick: () => void
}

export function SignupForm({ className, onSignInClick, ...props }: SignupFormProps) {
  const { signup } = useAuth()
  const navigate = useNavigate()

  const [name, setName] = useState("")
  const [email, setEmail] = useState("")
  const [phone, setPhone] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [role, setRole] = useState<"guest" | "assistant">("guest")
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (password.length < 8) {
      toast.error("Password must be at least 8 characters long.")
      return
    }

    if (password !== confirmPassword) {
      toast.error("Passwords do not match.")
      return
    }

    setLoading(true)
    const formattedPhone = phone.startsWith("+") ? phone : `+91${phone}`
    try {
      const success = await signup({
        name,
        email,
        phone: formattedPhone,
        password,
        role
      })
      if (success) {
        toast.success("Account registered successfully! Please verify your account.")
        navigate("/otp-verification")
      }
    } catch (err) {
      // handled globally
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={cn("flex flex-col gap-4", className)} {...props}>
      <Card className="rounded-2xl border border-border shadow-xl backdrop-blur-md bg-card/90">
        <CardHeader className="text-center pt-5 pb-2">
          <CardTitle className="text-xl font-bold tracking-tight">Create your account</CardTitle>
          <CardDescription className="text-[10px] mt-0.5">
            Register your credentials to get started with NavAssist
          </CardDescription>
        </CardHeader>
        <CardContent className="px-5 pb-4">
          <form onSubmit={handleSubmit}>
            <FieldGroup className="gap-2.5">
              {/* Role Toggle Selector */}
              <div className="grid grid-cols-2 gap-1.5 p-0.5 bg-muted rounded-xl border mb-0.5">
                <button
                  type="button"
                  onClick={() => setRole("guest")}
                  className={cn(
                    "py-1.5 text-[11px] font-bold rounded-lg transition-all flex items-center justify-center gap-1",
                    role === "guest" ? "bg-card text-foreground shadow-sm" : "text-muted-foreground hover:text-foreground"
                  )}
                >
                  <UserIcon size={12} />
                  Passenger
                </button>
                <button
                  type="button"
                  onClick={() => setRole("assistant")}
                  className={cn(
                    "py-1.5 text-[11px] font-bold rounded-lg transition-all flex items-center justify-center gap-1",
                    role === "assistant" ? "bg-card text-foreground shadow-sm" : "text-muted-foreground hover:text-foreground"
                  )}
                >
                  <ShieldCheckIcon size={12} />
                  Escort Guide
                </button>
              </div>

              <Field>
                <FieldLabel htmlFor="name" className="text-xs">Full Name</FieldLabel>
                <Input
                  id="name"
                  type="text"
                  placeholder="John Doe"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                  className="mt-0.5 h-9 text-xs"
                  disabled={loading}
                />
              </Field>

              {/* Grid 2-column: Phone & Email */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <Field>
                  <FieldLabel htmlFor="phone" className="text-xs">Phone Number</FieldLabel>
                  <div className="relative mt-0.5">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs font-semibold text-muted-foreground">+91</span>
                    <Input
                      id="phone"
                      type="tel"
                      value={phone}
                      onChange={(e) => setPhone(parsePhoneNumber(e.target.value))}
                      placeholder="9876543210"
                      required
                      className="pl-12 h-9 text-xs"
                      disabled={loading}
                    />
                  </div>
                </Field>

                <Field>
                  <FieldLabel htmlFor="email" className="text-xs">Email address (Optional)</FieldLabel>
                  <Input
                    id="email"
                    type="email"
                    placeholder="m@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="mt-0.5 h-9 text-xs"
                    disabled={loading}
                  />
                </Field>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <Field>
                  <FieldLabel htmlFor="password" className="text-xs">Password</FieldLabel>
                  <div className="relative mt-0.5">
                    <Input
                      id="password"
                      type={showPassword ? "text" : "password"}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="••••••••"
                      required
                      className="pr-8 h-9 text-xs"
                      disabled={loading}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground cursor-pointer flex items-center justify-center h-4 w-4"
                    >
                      {showPassword ? <EyeSlashIcon size={15} /> : <EyeIcon size={15} />}
                    </button>
                  </div>
                </Field>
                <Field>
                  <FieldLabel htmlFor="confirm-password" className="text-xs">Confirm Password</FieldLabel>
                  <div className="relative mt-0.5">
                    <Input
                      id="confirm-password"
                      type={showConfirmPassword ? "text" : "password"}
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="••••••••"
                      required
                      className="pr-8 h-9 text-xs"
                      disabled={loading}
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground cursor-pointer flex items-center justify-center h-4 w-4"
                    >
                      {showConfirmPassword ? <EyeSlashIcon size={15} /> : <EyeIcon size={15} />}
                    </button>
                  </div>
                </Field>
              </div>
              <span className="text-[9px] text-muted-foreground font-semibold -mt-1.5 block">Must be at least 8 characters long.</span>

              <Field className="mt-1">
                <Button type="submit" disabled={loading} className="w-full h-9 font-bold rounded-xl shadow-sm">
                  {loading ? "Registering..." : "Create Account"}
                </Button>
                <FieldDescription className="text-center text-xs mt-2">
                  Already have an account?{" "}
                  <button type="button" onClick={onSignInClick} className="text-primary font-bold hover:underline">
                    Sign in
                  </button>
                </FieldDescription>
              </Field>
            </FieldGroup>
          </form>
        </CardContent>
      </Card>
      
      <FieldDescription className="px-6 text-center text-[9px]">
        By clicking continue, you agree to our <Link to="/terms" className="underline">Terms of Service</Link> and{" "}
        <Link to="/privacy" className="underline">Privacy Policy</Link>.
      </FieldDescription>
    </div>
  )
}
export default SignupForm
