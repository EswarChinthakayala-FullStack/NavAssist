import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { couponsService } from "@/services/coupons.service"
import { useBookingDraftStore } from "@/store/booking-draft.store"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { TagIcon, ArrowLeftIcon, CheckCircleIcon, WarningCircleIcon, GiftIcon } from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

export function ApplyCouponPage() {
  const navigate = useNavigate()
  const { setCoupon } = useBookingDraftStore()

  const [code, setCode] = useState("")
  const [loading, setLoading] = useState(false)
  
  // Validation status states
  const [status, setStatus] = useState<{
    type: "success" | "error" | null
    message: string
    discount?: number
  }>({ type: null, message: "" })

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCode(e.target.value.toUpperCase())
    // Reset status when user starts typing new code
    if (status.type) {
      setStatus({ type: null, message: "" })
    }
  }

  const handleApply = async (e: React.FormEvent) => {
    e.preventDefault()
    if (code.trim().length < 2) {
      setStatus({ type: "error", message: "Coupon code must be at least 2 characters long." })
      return
    }

    setLoading(true)
    setStatus({ type: null, message: "" })

    try {
      // Validate against backend with temporary booking total to check rules
      const res = await couponsService.validateCoupon(code, 150.00)

      if (res.valid) {
        setStatus({
          type: "success",
          message: `Promo code successfully applied!`,
          discount: Number(res.calculated_discount || res.discount_value || 50.00)
        })

        // Save Coupon in Zustand Draft store
        setCoupon({
          id: 0,
          code: res.coupon_code || code,
          discount_percentage: res.discount_type === "percentage" ? Number(res.discount_value) : 0,
          max_discount: Number(res.calculated_discount || 50.00),
          min_order_value: 0.0,
          expires_at: new Date(Date.now() + 86400000).toISOString(),
          is_active: true
        })

        toast.success(`Coupon "${res.coupon_code}" applied successfully!`)

        // Navigate back to price-estimate page after brief delay to show success row
        setTimeout(() => {
          navigate("/book/price-estimate")
        }, 1200)

      } else {
        setStatus({
          type: "error",
          message: res.message || "Promo coupon code is invalid or expired."
        })
      }
    } catch (err: any) {
      console.error(err)
      const errorDetail = err.response?.data?.detail || err.message || "Validation failed"
      setStatus({
        type: "error",
        message: `Error: ${errorDetail}`
      })
    } finally {
      setLoading(false)
    }
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-md mx-auto py-4"
    >
      <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-2xl">
        <CardHeader className="border-b border-border/50 bg-muted/20 pb-4">
          <CardTitle className="text-lg font-bold flex items-center gap-2">
            <TagIcon size={20} className="text-primary" />
            Apply Coupon Code
          </CardTitle>
          <CardDescription className="text-xs">
            Type your promotional discount code below to reduce your booking fare
          </CardDescription>
        </CardHeader>
        <CardContent className="p-6 space-y-4">
          {/* Coupon Input Form */}
          <form onSubmit={handleApply} className="flex gap-2">
            <div className="flex-1">
              <Input
                placeholder="ENTER PROMO CODE"
                value={code}
                onChange={handleInputChange}
                className="font-mono uppercase tracking-wider text-sm rounded-xl py-5"
                disabled={loading || status.type === "success"}
              />
            </div>
            <Button
              type="submit"
              disabled={loading || !code.trim() || status.type === "success"}
              className="bg-primary text-primary-foreground hover:bg-primary/95 rounded-xl font-bold px-6 py-5 cursor-pointer shrink-0"
            >
              {loading ? "Verifying..." : "Apply"}
            </Button>
          </form>

          {/* Validation Result Notification Banner */}
          {status.type === "success" && (
            <motion.div
              initial={{ opacity: 0, y: 5 }}
              animate={{ opacity: 1, y: 0 }}
              className="p-3 bg-success/10 border border-success/20 rounded-xl text-xs text-success font-semibold flex items-start gap-2"
            >
              <CheckCircleIcon size={16} className="text-success shrink-0 mt-0.5" />
              <div>
                <p>{status.message}</p>
                {status.discount && (
                  <p className="mt-1 font-bold text-success">
                    Discount Saved: ₹{status.discount.toFixed(2)}
                  </p>
                )}
                <p className="text-[10px] text-success/80 mt-0.5">Redirecting back to estimate details...</p>
              </div>
            </motion.div>
          )}

          {status.type === "error" && (
            <motion.div
              initial={{ opacity: 0, y: 5 }}
              animate={{ opacity: 1, y: 0 }}
              className="p-3 bg-destructive/10 border border-destructive/20 rounded-xl text-xs text-destructive font-semibold flex items-start gap-2"
            >
              <WarningCircleIcon size={16} className="text-destructive shrink-0 mt-0.5" />
              <div>
                <p>{status.message}</p>
              </div>
            </motion.div>
          )}

          {/* Prompt card detailing mock/test coupon codes */}
          <div className="bg-muted/40 border border-border/80 p-4 rounded-2xl space-y-2">
            <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest flex items-center gap-1">
              <GiftIcon size={14} />
              Quick-Test Promo Codes
            </span>
            <ul className="text-xs text-muted-foreground space-y-1 bg-background/50 p-2.5 rounded-xl border border-border/40 font-mono">
              <li>• <span className="font-extrabold text-foreground">NAVFIRST</span> - Flat ₹50 discount</li>
              <li>• <span className="font-extrabold text-foreground">WIZARD50</span> - 10% discount</li>
            </ul>
          </div>
        </CardContent>
        <CardFooter className="border-t border-border/50 p-6 flex justify-start bg-muted/10">
          <Button
            variant="outline"
            onClick={() => navigate("/book/price-estimate")}
            className="rounded-xl py-4 font-bold text-xs gap-1.5 flex items-center justify-center cursor-pointer"
          >
            <ArrowLeftIcon size={12} weight="bold" />
            Back to Estimate
          </Button>
        </CardFooter>
      </Card>
    </motion.div>
  )
}
export default ApplyCouponPage
