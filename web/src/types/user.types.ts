export interface SavedLocation {
  id: number
  user_id: number
  name: string
  address: string
  latitude: number
  longitude: number
  created_at: string
}

export interface EmergencyContact {
  id: number
  user_id: number
  name: string
  phone: string
  relationship: string
  is_active: boolean
  created_at: string
}

export interface User {
  id: number
  name: string
  email: string
  phone: string
  role: "guest" | "assistant" | "admin"
  is_active: boolean
  is_phone_verified: boolean
  is_email_verified: boolean
  created_at: string
  updated_at: string
  avatar_url?: string
}
