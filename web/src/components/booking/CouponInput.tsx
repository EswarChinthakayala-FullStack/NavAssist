import React, { useState } from "react"
import { api } from "@/services/api"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { toast } from "sonner"
import { TicketIcon, CheckCircleIcon, XCircleIcon } from "@phosphor-icons/react"

interface CouponInputProps {
  bookingAmount: number
  onCouponApplied: (coupon: { code: string; discount: number; finalAmount: number }) => void
  onCouponRemoved: () => void
}

export function CouponInput({ bookingAmount, onCouponApplied, onCouponRemoved }: CouponInputProps) {
  const [code, setCode] = useState("")
  const [loading, setLoading] = useState(false)
  const [appliedCoupon, setAppliedCoupon] = useState<string | null>(null)
  const [discountVal, setDiscountVal] = useState(0)

  const handleValidate = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!code) return

    setLoading(true)
    try {
      // API endpoints validate coupon: GET/POST /coupons/validate?code={code}&booking_amount={amount}
      // Note: backend coupons.py has: @router.post("/validate")
      // which takes query params code and booking_amount
      const res = await api.post(`/coupons/validate?code=${encodeURIComponent(code)}&booking_amount=${bookingAmount}`)
      if (res.data.valid) {
        const calculatedDiscount = res.data.calculated_discount
        const finalPayable = res.data.final_payable
        
        setAppliedCoupon(code)
        setDiscountVal(calculatedDiscount)
        onCouponApplied({
          code,
          discount: calculatedDiscount,
          finalAmount: finalPayable
        })
        toast.success(`Promo code "${code}" applied successfully! Saved ₹${calculatedDiscount}.`)
      }
    } catch (err) {
      // global api interceptor toast handles errors
      onCouponRemoved()
      setAppliedCoupon(null)
      setDiscountVal(0)
    } finally {
      setLoading(false)
    }
  }

  const handleRemove = () => {
    setAppliedCoupon(null)
    setCode("")
    setDiscountVal(0)
    onCouponRemoved()
    toast.info("Promo code removed.")
  }

  return (
    <div className="grid gap-2 w-full">
      <Label htmlFor="coupon" className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Promotional Discount Coupon</Label>
      
      {appliedCoupon ? (
        <div className="flex items-center justify-between p-3.5 bg-success/15 border border-success/30 rounded-xl">
          <div className="flex items-center gap-2">
            <CheckCircleIcon size={20} className="text-success font-bold" weight="fill" />
            <div>
              <span className="text-xs font-black text-foreground">Code: {appliedCoupon}</span>
              <span className="text-[10px] text-muted-foreground block">Discount: -₹{discountVal.toFixed(2)} applied</span>
            </div>
          </div>
          
          <Button 
            variant="ghost" 
            size="sm" 
            onClick={handleRemove} 
            className="text-xs text-destructive hover:bg-destructive/15 font-bold rounded-lg px-2.5 h-8"
          >
            Remove
          </Button>
        </div>
      ) : (
        <form onSubmit={handleValidate} className="flex gap-2">
          <div className="relative flex-1">
            <Input
              id="coupon"
              value={code}
              onChange={(e) => setCode(e.target.value.toUpperCase())}
              placeholder="ENTER PROMO CODE"
              className="pr-10 font-mono tracking-wider font-extrabold"
              disabled={loading}
            />
            <div className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground">
              <TicketIcon size={18} />
            </div>
          </div>
          
          <Button 
            type="submit" 
            disabled={loading || !code} 
            className="bg-primary text-primary-foreground hover:bg-primary/95 font-bold rounded-xl px-5 h-10"
          >
            {loading ? "Validating..." : "Apply"}
          </Button>
        </form>
      )}
    </div>
  )
}
export default CouponInput
