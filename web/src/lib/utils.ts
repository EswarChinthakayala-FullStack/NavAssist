import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

// Format number/currency in INR
export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2
  }).format(amount)
}

// Helper to parse dates safely treating naive UTC strings as UTC
export function parseDateIST(dateInput: string | Date): Date {
  if (dateInput instanceof Date) return dateInput
  if (!dateInput) return new Date()
  let str = String(dateInput)
  // If it's an ISO string without timezone indicator ('Z' or '+' or '-'), append 'Z' so JS treats it as UTC
  if (typeof str === "string" && str.includes("T") && !str.endsWith("Z") && !/[+-]\d{2}:\d{2}$/.test(str)) {
    str += "Z"
  }
  return new Date(str)
}

// Format ISO date string into readable local IST formats
export function formatDate(dateString: string | Date): string {
  const date = parseDateIST(dateString)
  if (isNaN(date.getTime())) return ""
  return date.toLocaleDateString("en-IN", {
    timeZone: "Asia/Kolkata",
    day: "numeric",
    month: "short",
    year: "numeric"
  })
}

// Format ISO date string into human friendly IST time
export function formatTime(dateString: string | Date): string {
  const date = parseDateIST(dateString)
  if (isNaN(date.getTime())) return ""
  return date.toLocaleTimeString("en-IN", {
    timeZone: "Asia/Kolkata",
    hour: "2-digit",
    minute: "2-digit",
    hour12: true
  })
}

// Format date and time in IST (Indian Standard Time)
export function formatDateTimeIST(dateString?: string | Date): string {
  if (!dateString) return ""
  const date = parseDateIST(dateString)
  if (isNaN(date.getTime())) return ""
  return date.toLocaleString("en-IN", {
    timeZone: "Asia/Kolkata",
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  })
}

// Format relative time (e.g., "5 mins ago", "Just now")
export function formatRelativeTime(dateString: string | Date): string {
  const date = parseDateIST(dateString)
  if (isNaN(date.getTime())) return ""
  
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  
  if (diffMins < 1) return "Just now"
  if (diffMins < 60) return `${diffMins}m ago`
  
  const diffHours = Math.floor(diffMins / 60)
  if (diffHours < 24) return `${diffHours}h ago`
  
  return formatDate(date)
}

// Parse phone inputs to extract a clean 10-digit Indian mobile number
export function parsePhoneNumber(value: string): string {
  // Strip all non-digits
  let digits = value.replace(/\D/g, "")
  // If it starts with 91 and has more than 10 digits, strip the 91 prefix
  if (digits.startsWith("91") && digits.length > 10) {
    digits = digits.slice(2)
  }
  return digits.slice(0, 10)
}
