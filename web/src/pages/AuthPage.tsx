import React from "react"
import { LoginForm } from "@/components/login-form"
import { SignupForm } from "@/components/signup-form"
import { useNavigate } from "react-router-dom"

interface AuthPageProps {
  mode: "login" | "signup"
}

export function AuthPage({ mode }: AuthPageProps) {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen w-screen flex items-center justify-center p-6 bg-background relative overflow-hidden">
      {/* Dot matrix background pattern */}
      <div className="absolute inset-0 opacity-[0.06] bg-[radial-gradient(circle_at_center,var(--foreground)_10%,transparent_50%)] bg-[size:24px_24px] pointer-events-none" />

      {/* Decorative radial gradient glow */}
      <div className="absolute inset-0 opacity-20 bg-[radial-gradient(ellipse_at_top_right,hsl(var(--primary)/0.25),transparent_60%)] pointer-events-none" />

      <div className="w-full max-w-md relative z-10">
        {/* Glow background card outline */}
        <div className="absolute -inset-1 rounded-3xl bg-gradient-to-r from-primary to-accent opacity-20 blur-lg pointer-events-none" />
        
        {mode === "login" ? (
          <LoginForm onSignUpClick={() => navigate("/signup")} />
        ) : (
          <SignupForm onSignInClick={() => navigate("/login")} />
        )}
      </div>
    </div>
  )
}
export default AuthPage
