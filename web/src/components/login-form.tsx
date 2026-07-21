import React, { useState } from "react"
import { cn, parsePhoneNumber } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldDescription, FieldGroup, FieldLabel, FieldSeparator } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { useAuth } from "@/store/auth-context"
import { useNavigate, Link } from "react-router-dom"
import { toast } from "sonner"
import { Badge } from "@/components/ui/badge"
import { api } from "@/services/api"
import { PhoneIcon, KeyIcon, ShieldCheckIcon, EyeIcon, EyeSlashIcon } from "@phosphor-icons/react"

interface LoginFormProps extends React.ComponentProps<"div"> {
  onSignUpClick: () => void
}

export function LoginForm({ className, onSignUpClick, ...props }: LoginFormProps) {
  const { login, sendOtp, verifyOtp } = useAuth()
  const navigate = useNavigate()

  const [loginMethod, setLoginMethod] = useState<"otp" | "password">("otp")
  const [phone, setPhone] = useState("")
  const [password, setPassword] = useState("")
  
  // OTP flow states
  const [otpSent, setOtpSent] = useState(false)
  const [otpCode, setOtpCode] = useState("")
  const [debugOtp, setDebugOtp] = useState("")
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const handleSendOtp = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!phone) {
      toast.error("Please enter a valid phone number.")
      return
    }

    setLoading(true)
    const formattedPhone = phone.startsWith("+") ? phone : `+91${phone}`
    try {
      const res = await sendOtp(formattedPhone)
      if (res.success) {
        setOtpSent(true)
        if (res.debug_otp) {
          setDebugOtp(res.debug_otp)
        }
      }
    } finally {
      setLoading(false)
    }
  }

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!otpCode) return

    setLoading(true)
    const formattedPhone = phone.startsWith("+") ? phone : `+91${phone}`
    try {
      const res = await verifyOtp(formattedPhone, otpCode)
      if (res.success) {
        if (res.registered) {
          navigate("/dashboard")
        } else {
          // If not registered, carry phone details to register screen
          toast.info("Phone verified! Proceed to create your profile.")
          onSignUpClick()
        }
      }
    } finally {
      setLoading(false)
    }
  }

  const handlePasswordLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!phone || !password) return

    setLoading(true)
    const formattedPhone = phone.startsWith("+") ? phone : `+91${phone}`
    try {
      const success = await login(formattedPhone, password)
      if (success) {
        navigate("/dashboard")
      }
    } finally {
      setLoading(false)
    }
  }



  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      <Card className="rounded-2xl border border-border shadow-xl backdrop-blur-md bg-card/90">
        <CardHeader className="text-center pt-8">
          <CardTitle className="text-2xl font-bold tracking-tight">Welcome back</CardTitle>
          <CardDescription className="text-xs mt-1">
            Access your passenger console or guide profile session
          </CardDescription>
        </CardHeader>
        <CardContent className="px-6 pb-6">
          <FieldGroup className="gap-5">


            {/* Tab Swapper */}
            {!otpSent && (
              <div className="grid grid-cols-2 gap-2 p-1 bg-muted rounded-xl border">
                <button
                  type="button"
                  onClick={() => setLoginMethod("otp")}
                  className={cn(
                    "py-2 text-xs font-bold rounded-lg transition-all",
                    loginMethod === "otp" ? "bg-card text-foreground shadow-sm" : "text-muted-foreground hover:text-foreground"
                  )}
                >
                  Simulated OTP
                </button>
                <button
                  type="button"
                  onClick={() => setLoginMethod("password")}
                  className={cn(
                    "py-2 text-xs font-bold rounded-lg transition-all",
                    loginMethod === "password" ? "bg-card text-foreground shadow-sm" : "text-muted-foreground hover:text-foreground"
                  )}
                >
                  Password ID
                </button>
              </div>
            )}

            {/* Tab Contents: OTP Verification */}
            {loginMethod === "otp" && (
              <form onSubmit={otpSent ? handleVerifyOtp : handleSendOtp} className="grid gap-4">
                {otpSent && debugOtp && (
                  <div className="p-3 bg-accent/15 border border-accent rounded-xl text-center">
                    <span className="text-[10px] font-bold text-accent-foreground uppercase tracking-wider mr-1">Debug OTP:</span>
                    <Badge variant="secondary" className="font-mono text-xs tracking-widest px-2 py-0.5">{debugOtp}</Badge>
                  </div>
                )}

                <div>
                  <FieldLabel htmlFor="phone">{otpSent ? "Verify OTP Code" : "Phone Number"}</FieldLabel>
                  {otpSent ? (
                    <Input
                      id="otp"
                      value={otpCode}
                      onChange={(e) => setOtpCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                      placeholder="000000"
                      required
                      className="mt-1.5 text-center font-mono text-lg tracking-widest"
                      disabled={loading}
                    />
                  ) : (
                    <div className="relative mt-1.5">
                      <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs font-semibold text-muted-foreground">+91</span>
                      <Input
                        id="phone"
                        type="tel"
                        value={phone}
                        onChange={(e) => setPhone(parsePhoneNumber(e.target.value))}
                        placeholder="9876543210"
                        required
                        className="pl-12"
                        disabled={loading}
                      />
                    </div>
                  )}
                </div>

                <Button type="submit" disabled={loading} className="w-full py-5 font-bold rounded-xl mt-1 shadow-sm">
                  {loading ? "Processing..." : otpSent ? "Verify & Continue" : "Send One-Time Password"}
                </Button>
                
                {otpSent && (
                  <Button 
                    type="button" 
                    variant="ghost" 
                    onClick={() => {
                      setOtpSent(false)
                      setOtpCode("")
                    }} 
                    className="w-full text-xs text-muted-foreground hover:bg-transparent"
                  >
                    Change Phone Number
                  </Button>
                )}
              </form>
            )}

            {/* Tab Contents: Password Authentication */}
            {loginMethod === "password" && (
              <form onSubmit={handlePasswordLogin} className="grid gap-4">
                <div>
                  <FieldLabel htmlFor="phone-pass">Phone Number</FieldLabel>
                  <div className="relative mt-1.5">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs font-semibold text-muted-foreground">+91</span>
                    <Input
                      id="phone-pass"
                      type="tel"
                      value={phone}
                      onChange={(e) => setPhone(parsePhoneNumber(e.target.value))}
                      placeholder="9876543210"
                      required
                      className="pl-12"
                      disabled={loading}
                    />
                  </div>
                </div>

                <div>
                  <div className="flex items-center justify-between">
                    <FieldLabel htmlFor="password">Password</FieldLabel>
                    <a href="#" className="text-[10px] text-primary font-bold hover:underline">Forgot?</a>
                  </div>
                  <div className="relative mt-1.5">
                    <Input
                      id="password"
                      type={showPassword ? "text" : "password"}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="••••••••"
                      required
                      className="pr-10"
                      disabled={loading}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground cursor-pointer flex items-center justify-center h-5 w-5"
                    >
                      {showPassword ? <EyeSlashIcon size={18} /> : <EyeIcon size={18} />}
                    </button>
                  </div>
                </div>

                <Button type="submit" disabled={loading} className="w-full py-5 font-bold rounded-xl mt-1 shadow-sm">
                  {loading ? "Signing in..." : "Log In"}
                </Button>
              </form>
            )}

            <Field>
              <FieldDescription className="text-center text-xs mt-2">
                Don&apos;t have an account?{" "}
                <button type="button" onClick={onSignUpClick} className="text-primary font-bold hover:underline">
                  Sign up
                </button>
              </FieldDescription>
            </Field>
          </FieldGroup>
        </CardContent>
      </Card>
      
      <FieldDescription className="px-6 text-center text-[10px]">
        By clicking continue, you agree to our <Link to="/terms" className="underline">Terms of Service</Link> and{" "}
        <Link to="/privacy" className="underline">Privacy Policy</Link>.
      </FieldDescription>
    </div>
  )
}
export default LoginForm
