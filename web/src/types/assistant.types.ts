export type VerificationStatus = "PENDING" | "APPROVED" | "REJECTED" | "NOT_SUBMITTED"

export interface AssistantProfile {
  id: number
  user_id: number
  name: string
  aadhaar_number?: string
  verification_status: VerificationStatus
  is_online: boolean
  rating: number
  total_trips: number
  doc_front_url?: string
  doc_back_url?: string
  profile_photo_url?: string
  created_at: string
}

export interface AssistantDocument {
  id: number
  assistant_id: number
  document_type: "aadhaar" | "police_clearance"
  document_url: string
  uploaded_at: string
}
