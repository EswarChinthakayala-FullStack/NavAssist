import api from "./api-client"

export const authService = {
  async sendOtp(phone: string) {
    const res = await api.post("/auth/send-otp", { phone })
    return res.data
  },

  async verifyOtp(phone: string, code: string) {
    const res = await api.post("/auth/verify-otp", { phone, code })
    return res.data
  },

  async login(phone: string, password_raw: string) {
    const res = await api.post("/auth/token", { phone, password: password_raw })
    return res.data
  },

  async signup(data: any) {
    const res = await api.post("/auth/signup", data)
    return res.data
  },

  async googleLogin(idToken: string) {
    const res = await api.post("/auth/google", { id_token: idToken })
    return res.data
  },

  async verifyEmail(email: string, code: string) {
    const res = await api.post("/auth/verify/email", { email, code })
    return res.data
  },

  async verifyPhone(phone: string, code: string) {
    const res = await api.post("/auth/verify/phone", { phone, code })
    return res.data
  },

  async resendEmailOtp(email: string) {
    const res = await api.post(`/auth/verify/resend-email?email=${encodeURIComponent(email)}`)
    return res.data
  },

  async logout() {
    const res = await api.post("/auth/logout")
    return res.data
  }
}
export default authService
