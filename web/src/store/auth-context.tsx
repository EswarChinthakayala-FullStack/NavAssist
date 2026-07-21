import React, { createContext, useContext, useState, useEffect } from "react"
import { api } from "@/services/api"
import { toast } from "sonner"

interface GuestProfile {
  user_id: number
  name: string
  profile_picture_url?: string | null
}

interface AssistantProfile {
  user_id: number
  name: string
  profile_picture_url?: string | null
  kyc_status: string
  online_status: string
}

interface User {
  id: number
  phone: string
  email: string | null
  role: "guest" | "assistant" | "admin"
  full_name: string
  profile_photo_url?: string | null
  is_phone_verified: boolean
  is_email_verified: boolean
  guest?: GuestProfile | null
  assistant?: AssistantProfile | null
}

interface AuthContextType {
  user: User | null
  loading: boolean
  isAuthenticated: boolean
  userFullName: string
  userPhone: string
  sendOtp: (phone: string) => Promise<{ success: boolean; debug_otp?: string }>
  verifyOtp: (phone: string, otp: string) => Promise<{ success: boolean; registered: boolean }>
  login: (phone: string, password: string) => Promise<boolean>
  signup: (data: { name: string; email: string; phone: string; password?: string; role: "guest" | "assistant" }) => Promise<boolean>
  logout: () => Promise<void>
  updateProfile: (data: { full_name?: string; email?: string; profile_photo_url?: string }) => Promise<boolean>
  fetchProfile: () => Promise<any>
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  const fetchProfile = async () => {
    try {
      const res = await api.get("/users/me")
      setUser(res.data)
      return res.data
    } catch (err) {
      setUser(null)
      localStorage.removeItem("access_token")
      localStorage.removeItem("refresh_token")
    }
  }

  // Load user profile on startup if access token exists
  useEffect(() => {
    const token = localStorage.getItem("access_token")
    if (token) {
      fetchProfile().finally(() => setLoading(false))
    } else {
      setLoading(false)
    }

    // Handle token expiration/forced logout from axios interceptor
    const handleForcedLogout = () => {
      setUser(null)
    }
    window.addEventListener("auth_logout", handleForcedLogout)
    return () => {
      window.removeEventListener("auth_logout", handleForcedLogout)
    }
  }, [])

  const sendOtp = async (phone: string) => {
    try {
      const res = await api.post("/auth/otp/send", { phone })
      if (res.data.success) {
        toast.success(`OTP sent successfully! (Code: ${res.data.debug_otp})`)
        return { success: true, debug_otp: res.data.debug_otp }
      }
      return { success: false }
    } catch (err) {
      return { success: false }
    }
  }

  const verifyOtp = async (phone: string, otp: string) => {
    try {
      const res = await api.post("/auth/otp/verify", { phone, otp })
      if (res.data.success) {
        if (res.data.registered && res.data.tokens) {
          const { access_token, refresh_token } = res.data.tokens
          localStorage.setItem("access_token", access_token)
          localStorage.setItem("refresh_token", refresh_token)
          await fetchProfile()
          toast.success("Welcome back!")
        }
        return { success: true, registered: res.data.registered }
      }
      return { success: false, registered: false }
    } catch (err) {
      return { success: false, registered: false }
    }
  }

  const login = async (phone: string, password: string) => {
    try {
      const res = await api.post("/auth/login", { phone, password })
      const { access_token, refresh_token, debug_otp, debug_email_code } = res.data
      localStorage.setItem("access_token", access_token)
      localStorage.setItem("refresh_token", refresh_token)
      await fetchProfile()
      if (debug_otp) sessionStorage.setItem("debug_otp", debug_otp)
      if (debug_email_code) sessionStorage.setItem("debug_email_code", debug_email_code)
      toast.success("Logged in successfully!")
      return true
    } catch (err) {
      return false
    }
  }

  const signup = async (data: { name: string; email: string; phone: string; password?: string; role: "guest" | "assistant" }) => {
    try {
      // Backend signup endpoint expects: phone, password, role, email, name
      const payload = {
        phone: data.phone,
        password: data.password || "DefaultPassword@123",
        role: data.role,
        email: data.email,
        name: data.name
      }
      const res = await api.post("/auth/signup", payload)
      const { access_token, refresh_token, debug_otp, debug_email_code } = res.data
      localStorage.setItem("access_token", access_token)
      localStorage.setItem("refresh_token", refresh_token)
      await fetchProfile()
      if (debug_otp) sessionStorage.setItem("debug_otp", debug_otp)
      if (debug_email_code) sessionStorage.setItem("debug_email_code", debug_email_code)
      toast.success("Registration successful!")
      return true
    } catch (err) {
      return false
    }
  }

  const logout = async () => {
    try {
      // Call backend logout to revoke token (it expects auth token so if expired it might fail, we catch error and proceed to clear local anyway)
      await api.post("/auth/logout").catch(() => {})
    } finally {
      localStorage.removeItem("access_token")
      localStorage.removeItem("refresh_token")
      setUser(null)
      toast.success("Logged out successfully.")
    }
  }

  const updateProfile = async (data: { full_name?: string; email?: string; profile_photo_url?: string }) => {
    try {
      const payload: any = {
        full_name: data.full_name,
        email: data.email
      }
      if (data.profile_photo_url !== undefined) {
        payload.profile_photo_url = data.profile_photo_url
      }
      const res = await api.patch("/users/me", payload)
      setUser(res.data)
      toast.success("Profile updated successfully!")
      return true
    } catch (err) {
      return false
    }
  }

  const userFullName = user?.full_name || (user?.role === "assistant" ? user?.assistant?.name : user?.guest?.name) || ""
  const userPhone = user?.phone || ""

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAuthenticated: !!user,
        userFullName,
        userPhone,
        sendOtp,
        verifyOtp,
        login,
        signup,
        logout,
        updateProfile,
        fetchProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}
