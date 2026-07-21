import { useAuthStore } from "@/store/auth.store"

export function useAuth() {
  const { user, accessToken, isAuthenticated, login, logout, updateUser } = useAuthStore()

  const isGuest = user?.role === "guest"
  const isAssistant = user?.role === "assistant"
  const isAdmin = user?.role === "admin"

  // Safe name resolution wrapper
  const userFullName = user?.name || ""
  const userPhone = user?.phone || ""

  return {
    user,
    accessToken,
    isAuthenticated,
    isGuest,
    isAssistant,
    isAdmin,
    userFullName,
    userPhone,
    login,
    logout,
    updateUser
  }
}
export default useAuth
