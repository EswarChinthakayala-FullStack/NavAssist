import { create } from "zustand"
import type { User } from "@/types/user.types"

interface AuthState {
  user: User | null
  accessToken: string | null
  isAuthenticated: boolean
  login: (accessToken: string, refreshToken: string, user: User) => void
  logout: () => void
  updateUser: (user: User) => void
}

export const useAuthStore = create<AuthState>((set) => {
  // Try to load initial values from localStorage
  const savedToken = localStorage.getItem("access_token")
  let savedUser: User | null = null
  try {
    const rawUser = localStorage.getItem("user_profile")
    if (rawUser) {
      savedUser = JSON.parse(rawUser)
    }
  } catch (err) {
    // Ignore invalid JSON format
  }

  return {
    user: savedUser,
    accessToken: savedToken,
    isAuthenticated: !!savedToken,
    login: (accessToken, refreshToken, user) => {
      localStorage.setItem("access_token", accessToken)
      localStorage.setItem("refresh_token", refreshToken)
      localStorage.setItem("user_profile", JSON.stringify(user))
      set({ accessToken, user, isAuthenticated: true })
    },
    logout: () => {
      localStorage.removeItem("access_token")
      localStorage.removeItem("refresh_token")
      localStorage.removeItem("user_profile")
      set({ accessToken: null, user: null, isAuthenticated: false })
    },
    updateUser: (user) => {
      localStorage.setItem("user_profile", JSON.stringify(user))
      set({ user })
    }
  }
})

export default useAuthStore
