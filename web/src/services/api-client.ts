import axios from "axios"
import { toast } from "sonner"

export const API_URL = import.meta.env.VITE_API_URL || "http://127.0.0.1:8000/api/v1"

export const api = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
  },
})

// Add a request interceptor to attach JWT token and handle FormData boundary
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("access_token")
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    if (config.data instanceof FormData && config.headers) {
      if (typeof config.headers.delete === "function") {
        config.headers.delete("Content-Type")
      } else {
        delete config.headers["Content-Type"]
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

let isRefreshing = false
let failedQueue: any[] = []

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

// Add a response interceptor to handle token refresh and errors
api.interceptors.response.use(
  (response) => {
    if (response.data && response.data.success && response.data.message) {
      toast.success(response.data.message)
    }
    return response
  },
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return api(originalRequest)
          })
          .catch((err) => {
            return Promise.reject(err)
          })
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = localStorage.getItem("refresh_token")
      if (refreshToken) {
        try {
          const res = await axios.post(`${API_URL}/auth/refresh-token?refresh_token=${refreshToken}`)
          const { access_token, refresh_token: newRefreshToken } = res.data
          
          localStorage.setItem("access_token", access_token)
          localStorage.setItem("refresh_token", newRefreshToken)

          api.defaults.headers.common["Authorization"] = `Bearer ${access_token}`
          originalRequest.headers.Authorization = `Bearer ${access_token}`
          
          processQueue(null, access_token)
          isRefreshing = false
          return api(originalRequest)
        } catch (refreshError) {
          processQueue(refreshError, null)
          isRefreshing = false
          
          localStorage.removeItem("access_token")
          localStorage.removeItem("refresh_token")
          window.dispatchEvent(new Event("auth_logout"))
          toast.error("Session expired. Please log in again.")
          return Promise.reject(refreshError)
        }
      } else {
        localStorage.removeItem("access_token")
        localStorage.removeItem("refresh_token")
        window.dispatchEvent(new Event("auth_logout"))
      }
    }

    let errorMessage = "An unexpected error occurred"
    let errorData = error.response?.data

    if (errorData instanceof Blob) {
      try {
        const text = await errorData.text()
        errorData = JSON.parse(text)
      } catch (e) {
        console.error("Failed to parse error blob:", e)
      }
    }

    if (errorData?.detail) {
      if (typeof errorData.detail === "string") {
        errorMessage = errorData.detail
      } else if (Array.isArray(errorData.detail)) {
        errorMessage = errorData.detail.map((err: any) => err.msg).join(", ")
      }
    } else if (errorData?.message) {
      errorMessage = errorData.message
    } else if (error.message) {
      errorMessage = error.message
    }

    toast.error(errorMessage)
    return Promise.reject(error)
  }
)

export default api
