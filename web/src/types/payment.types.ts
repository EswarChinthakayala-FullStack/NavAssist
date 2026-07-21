export interface Coupon {
  id: number
  code: string
  discount_percentage: number
  max_discount: number
  min_order_value: number
  expires_at: string
  is_active: boolean
}

export interface WalletTransaction {
  id: number
  wallet_id: number
  amount: number
  transaction_type: "credit" | "debit"
  description: string
  created_at: string
}

export interface Payment {
  id: number
  booking_id: number
  razorpay_order_id: string
  razorpay_payment_id?: string
  razorpay_signature?: string
  amount: number
  status: "pending" | "completed" | "failed"
  created_at: string
  updated_at: string
}
