import React, { useState, useEffect } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import { couponsService } from "@/services/coupons.service"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { TagIcon, ArrowLeftIcon, InfoIcon, GiftIcon } from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

export interface Coupon {
  id: number
  code: string
  description: string
  discount_type: "flat" | "percentage"
  discount_value: number
  min_booking_amount: number
}

// Custom hook to fetch available coupons as specified by prompt
export function useAvailableCoupons() {
  const [coupons, setCoupons] = useState<Coupon[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchCoupons = async () => {
      setLoading(true)
      try {
        const data = await couponsService.getAvailableCoupons()
        setCoupons(data || [])
      } catch (err) {
        console.error("Failed to fetch coupons:", err)
        // Fallback mock coupons for local offline testing
        setCoupons([
          { id: 1, code: "NAVFIRST", description: "Get flat ₹50 off on your first escort booking request.", discount_type: "flat", discount_value: 50.0, min_booking_amount: 100.0 },
          { id: 2, code: "WIZARD50", description: "Enjoy 10% discount on terminal escort journeys.", discount_type: "percentage", discount_value: 10.0, min_booking_amount: 150.0 },
          { id: 3, code: "SUPERDEAL", description: "Get flat ₹100 off on long distance travel assistance.", discount_type: "flat", discount_value: 100.0, min_booking_amount: 300.0 }
        ])
      } finally {
        setLoading(false)
      }
    }
    fetchCoupons()
  }, [])

  return { coupons, loading }
}

export function DiscountsOffersPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { coupons, loading } = useAvailableCoupons()
  
  // Retrieve current total fare passed from PriceEstimatePage to check eligibility
  const currentFare = Number(location.state?.totalFare || 0)

  const eligibleCoupons = coupons.filter(
    (coupon) => currentFare >= Number(coupon.min_booking_amount)
  )

  const handleApply = (code: string) => {
    // Navigate to /book/apply-coupon prefilled with the code as query param as requested
    navigate(`/book/apply-coupon?code=${encodeURIComponent(code)}`)
  }

  // Render loading skeletons
  const LoadingSkeletons = () => (
    <div className="space-y-4">
      {[1, 2].map((n) => (
        <div key={n} className="w-full p-5 border border-border bg-card/60 rounded-xl flex items-center gap-4 animate-pulse">
          <div className="w-12 h-12 bg-muted rounded-full" />
          <div className="flex-1 space-y-2">
            <div className="h-4 bg-muted rounded w-1/4" />
            <div className="h-3 bg-muted rounded w-3/4" />
            <div className="h-3 bg-muted rounded w-1/2" />
          </div>
        </div>
      ))}
    </div>
  )

  // Empty state if no coupons are eligible for the current fare
  const EmptyState = () => (
    <div className="text-center py-12 border border-dashed border-border rounded-2xl bg-muted/10 space-y-3">
      <div className="p-3 bg-muted/40 rounded-full w-fit mx-auto text-muted-foreground">
        <GiftIcon size={28} />
      </div>
      <div className="text-sm font-extrabold text-foreground">No promo coupons eligible</div>
      <p className="text-xs text-muted-foreground max-w-sm mx-auto px-4">
        Your current trip fare is ₹{currentFare.toFixed(2)}. There are no promotional discounts matching this booking amount threshold.
      </p>
    </div>
  )

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-2xl mx-auto py-4"
    >
      <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-2xl">
        <CardHeader className="border-b border-border/50 bg-muted/20 pb-4">
          <CardTitle className="text-lg font-bold flex items-center gap-2">
            <TagIcon size={20} className="text-primary" />
            Available Promotional Offers
          </CardTitle>
          <CardDescription className="text-xs">
            Choose an active discount coupon code matching your booking fare limit
          </CardDescription>
        </CardHeader>
        <CardContent className="p-6">
          {loading ? (
            <LoadingSkeletons />
          ) : eligibleCoupons.length === 0 ? (
            <EmptyState />
          ) : (
            <div className="grid gap-4">
              {eligibleCoupons.map((coupon) => (
                <div
                  key={coupon.id}
                  className="p-5 border border-border/80 hover:border-primary/40 bg-card rounded-2xl shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 transition-all duration-300 group"
                >
                  <div className="flex gap-4 items-start">
                    <div className="p-3 bg-primary/10 rounded-xl text-primary shrink-0 group-hover:scale-105 transition-transform">
                      <GiftIcon size={22} weight="duotone" />
                    </div>
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="font-mono text-sm font-extrabold text-foreground tracking-wider bg-muted px-2.5 py-0.5 rounded border border-border/60">
                          {coupon.code}
                        </span>
                        <Badge className="bg-success/20 text-success hover:bg-success/20 text-[10px] border-0 font-bold">
                          {coupon.discount_type === "flat"
                            ? `₹${coupon.discount_value} OFF`
                            : `${coupon.discount_value}% OFF`}
                        </Badge>
                      </div>
                      <p className="text-xs text-muted-foreground leading-relaxed">{coupon.description}</p>
                      <div className="flex items-center gap-1 text-[10px] text-muted-foreground font-semibold">
                        <InfoIcon size={12} />
                        <span>Min booking amount: ₹{Number(coupon.min_booking_amount).toFixed(2)}</span>
                      </div>
                    </div>
                  </div>
                  <Button
                    onClick={() => handleApply(coupon.code)}
                    className="w-full sm:w-auto bg-primary text-primary-foreground hover:bg-primary/95 rounded-xl font-bold text-xs px-6 py-4 shadow-sm"
                  >
                    Apply Coupon
                  </Button>
                </div>
              ))}
            </div>
          )}
        </CardContent>
        <CardFooter className="border-t border-border/50 p-6 flex justify-between items-center bg-muted/10">
          <div className="text-xs text-muted-foreground font-semibold">
            Trip Fare: <span className="font-bold text-foreground">₹{currentFare.toFixed(2)}</span>
          </div>
          <Button
            variant="outline"
            onClick={() => navigate("/book/price-estimate")}
            className="rounded-xl py-4 font-bold text-xs gap-1.5 flex items-center justify-center"
          >
            <ArrowLeftIcon size={12} weight="bold" />
            Back to Estimate
          </Button>
        </CardFooter>
      </Card>
    </motion.div>
  )
}
export default DiscountsOffersPage
