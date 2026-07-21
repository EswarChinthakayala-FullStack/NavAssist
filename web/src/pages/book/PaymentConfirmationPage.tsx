import React, { useState, useEffect } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import { paymentsService } from "@/services/payments.service"
import { bookingsService } from "@/services/bookings.service"
import { useBookingDraftStore } from "@/store/booking-draft.store"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { CheckCircleIcon, WarningIcon, CalendarBlankIcon, ReceiptIcon, MoneyIcon } from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

interface ReceiptData {
  booking_id: number
  booking_code?: string
  receipt_number?: string
  invoice_number?: string
  payment_method?: string
  payment_status?: string
  fare_amount?: number
  tax_inr?: number
  total_payable?: number
  date?: string
  amount?: number
  status?: string
  created_at?: string
  payment_id?: string
}

function useVerifyAndFetchReceipt() {
  const [loading, setLoading] = useState(true)
  const [success, setSuccess] = useState(false)
  const [receipt, setReceipt] = useState<ReceiptData | null>(null)
  const [errorMsg, setErrorMsg] = useState("")

  const process = async (params: {
    bookingId: number
    razorpay_order_id?: string
    razorpay_payment_id?: string
    razorpay_signature?: string
    method?: string
  }) => {
    setLoading(true)
    setErrorMsg("")
    try {
      // For cash and wallet, the payment state was already saved by the create-order endpoint
      // For online, the verify endpoint was already called in PaymentMethodPage's handler callback
      // This page only needs to fetch the receipt/booking details

      // Fetch real booking details for receipt display
      let bookingData: any = null
      try {
        bookingData = await bookingsService.getBooking(params.bookingId)
      } catch (bookingErr) {
        console.warn("Failed to fetch booking for receipt:", bookingErr)
      }

      // Fetch real receipt from backend
      try {
        const receiptData = await paymentsService.getReceipt(params.bookingId)
        setReceipt({
          booking_id: params.bookingId,
          booking_code: receiptData.booking_code,
          receipt_number: receiptData.receipt_number,
          invoice_number: receiptData.invoice_number,
          payment_method: params.method || receiptData.payment_method || "online",
          payment_status: receiptData.payment_status || "completed",
          fare_amount: receiptData.fare_amount,
          tax_inr: receiptData.tax_inr,
          total_payable: receiptData.total_payable,
          date: receiptData.date || bookingData?.created_at,
          amount: receiptData.total_payable || receiptData.fare_amount,
        })
      } catch (receiptErr) {
        console.warn("Receipt fetch failed, using booking data:", receiptErr)
        // Fallback to booking data if receipt endpoint fails
        const fare = Number(bookingData?.final_fare || bookingData?.fare_estimate || bookingData?.fare_amount || 0)
        setReceipt({
          booking_id: params.bookingId,
          booking_code: bookingData?.booking_code,
          payment_method: params.method || bookingData?.payment_method || "online",
          payment_status: params.method === "cash" ? "pending" : (bookingData?.payment_status || "completed"),
          fare_amount: fare,
          total_payable: fare,
          amount: fare,
          date: bookingData?.created_at,
        })
      }

      setSuccess(true)
    } catch (err: any) {
      console.error("Payment confirmation error:", err)
      setErrorMsg(err.response?.data?.detail || err.message || "Payment confirmation failed.")
      setSuccess(false)
    } finally {
      setLoading(false)
    }
  }

  return { process, loading, success, receipt, errorMsg }
}

export function PaymentConfirmationPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const resetDraft = useBookingDraftStore((state) => state.resetDraft)
  
  const { process, loading, success, receipt, errorMsg } = useVerifyAndFetchReceipt()
  const [bookingId, setBookingId] = useState<number | null>(null)

  useEffect(() => {
    const stateParams = location.state
    const sessionStr = sessionStorage.getItem("active_booking_id")
    const resolvedId = stateParams?.bookingId || (sessionStr ? parseInt(sessionStr) : null)

    if (!resolvedId) {
      toast.error("No active payment transaction found to confirm.")
      navigate("/book/pickup")
      return
    }
    setBookingId(resolvedId)

    process({
      bookingId: resolvedId,
      razorpay_order_id: stateParams?.razorpay_order_id,
      razorpay_payment_id: stateParams?.razorpay_payment_id,
      razorpay_signature: stateParams?.razorpay_signature,
      method: stateParams?.method
    })
  }, [location.state, navigate])

  useEffect(() => {
    if (success) {
      resetDraft()
    }
  }, [success, resetDraft])

  useEffect(() => {
    if (success && bookingId) {
      const timer = setTimeout(() => {
        navigate(`/trip/${bookingId}/assigned`)
      }, 3000)
      return () => clearTimeout(timer)
    }
  }, [success, bookingId, navigate])

  const handleManualRedirect = () => {
    if (bookingId) {
      navigate(`/trip/${bookingId}/assigned`)
    } else {
      navigate("/bookings")
    }
  }

  const handleRetryPayment = () => {
    if (bookingId) {
      navigate("/book/payment-method", { state: { bookingId } })
    } else {
      navigate("/book/pickup")
    }
  }

  const circleVariants = {
    hidden: { pathLength: 0, opacity: 0 },
    visible: { pathLength: 1, opacity: 1, transition: { duration: 0.5, ease: "easeOut" as const } }
  }

  const checkVariants = {
    hidden: { pathLength: 0, opacity: 0 },
    visible: { pathLength: 1, opacity: 1, transition: { duration: 0.4, delay: 0.4, ease: "easeInOut" as const } }
  }

  if (loading) {
    return (
      <div className="h-screen w-screen flex flex-col items-center justify-center bg-background text-foreground gap-4">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-muted-foreground">Confirming payment status...</span>
      </div>
    )
  }

  if (!success) {
    return (
      <motion.div
        initial={{ opacity: 0, scale: 0.98 }}
        animate={{ opacity: 1, scale: 1 }}
        className="max-w-md mx-auto py-8 text-center"
      >
        <Card className="border border-destructive/20 bg-destructive/5 shadow-lg rounded-2xl p-6 space-y-4">
          <div className="p-4 bg-destructive/15 rounded-full w-fit mx-auto text-destructive animate-pulse">
            <WarningIcon size={40} weight="duotone" />
          </div>
          <CardHeader className="pb-2">
            <CardTitle className="text-xl font-bold text-destructive">Payment Confirmation Failed</CardTitle>
            <CardDescription className="text-xs text-muted-foreground mt-1.5">
              We couldn't confirm your payment status. Your money is safe — if deducted, it will be refunded automatically.
            </CardDescription>
          </CardHeader>
          <CardContent className="text-xs text-muted-foreground bg-background/50 border border-border/80 rounded-xl p-3 text-left leading-relaxed">
            <p className="font-semibold text-foreground">Error Details:</p>
            <p className="mt-1 font-mono text-[11px] text-destructive-foreground">{errorMsg || "Checkout session verification timed out."}</p>
          </CardContent>
          <CardFooter className="flex gap-3 justify-center pt-2">
            <Button
              variant="outline"
              onClick={() => navigate("/book/payment-method")}
              className="rounded-xl py-4 flex-1 cursor-pointer font-bold text-xs"
            >
              Change Method
            </Button>
            <Button
              onClick={handleRetryPayment}
              className="bg-primary text-primary-foreground hover:bg-primary/95 rounded-xl py-4 flex-1 cursor-pointer font-extrabold text-xs"
            >
              Retry Payment
            </Button>
          </CardFooter>
        </Card>
      </motion.div>
    )
  }

  const isCash = location.state?.method === "cash"
  const displayAmount = receipt?.total_payable || receipt?.fare_amount || receipt?.amount || 0

  return (
    <div className="h-full flex items-center justify-center py-6 w-full">
      <Card className="max-w-md w-full border border-border/85 shadow-xl backdrop-blur-md rounded-3xl overflow-hidden text-left">
        {/* Success animation panel */}
        <div className={`p-8 flex justify-center border-b border-border/50 ${isCash ? "bg-emerald-500/10" : "bg-gradient-to-b from-success/20 to-transparent"}`}>
          {isCash ? (
            <div className="p-4 bg-emerald-500/20 text-emerald-500 rounded-full animate-bounce">
              <MoneyIcon size={52} weight="duotone" />
            </div>
          ) : (
            <svg className="w-20 h-20 text-success" viewBox="0 0 100 100" fill="none">
              <motion.circle
                cx="50"
                cy="50"
                r="45"
                stroke="currentColor"
                strokeWidth="5"
                variants={circleVariants}
                initial="hidden"
                animate="visible"
              />
              <motion.path
                d="M30 52 L45 65 L70 38"
                stroke="currentColor"
                strokeWidth="6"
                strokeLinecap="round"
                strokeLinejoin="round"
                variants={checkVariants}
                initial="hidden"
                animate="visible"
              />
            </svg>
          )}
        </div>

        <CardHeader className="text-center pb-2">
          <CardTitle className="text-xl font-black text-foreground">
            {isCash ? "Cash Payment Mode Selected!" : "Payment Received Successfully!"}
          </CardTitle>
          <CardDescription className="text-xs text-muted-foreground mt-1">
            {isCash
              ? "Please pay physical cash to your verified escort guide upon arrival."
              : `Transaction processed and linked to booking reference ID #${bookingId}`}
          </CardDescription>
        </CardHeader>

        {/* Invoice details list */}
        <CardContent className="p-6 space-y-4">
          <div className="border border-border/80 bg-muted/20 p-4 rounded-2xl text-xs space-y-3.5 leading-relaxed">
            <div className="flex justify-between items-center text-muted-foreground">
              <span>Receipt Date</span>
              <span className="font-bold text-foreground flex items-center gap-1">
                <CalendarBlankIcon size={14} />
                {receipt?.date ? new Date(receipt.date).toLocaleString() : new Date().toLocaleString()}
              </span>
            </div>

            {receipt?.receipt_number && (
              <div className="flex justify-between items-center text-muted-foreground">
                <span>Receipt #</span>
                <span className="font-mono text-[10px] bg-muted px-2 py-0.5 border rounded text-foreground font-semibold">
                  {receipt.receipt_number}
                </span>
              </div>
            )}

            <div className="flex justify-between items-center text-muted-foreground">
              <span>Payment Mode</span>
              <span className="font-mono text-[10px] bg-muted px-2 py-0.5 border rounded text-foreground font-semibold uppercase">
                {isCash ? "Cash on Collection" : "Online Gateway"}
              </span>
            </div>

            <div className="flex justify-between items-center text-muted-foreground">
              <span>Status</span>
              <span className={`font-extrabold uppercase flex items-center gap-0.5 ${isCash ? "text-amber-500" : "text-success"}`}>
                <CheckCircleIcon size={14} weight="fill" />
                {isCash ? "Pending Cash Confirmation" : "Captured"}
              </span>
            </div>

            {receipt?.tax_inr != null && receipt.tax_inr > 0 && (
              <>
                <div className="border-t border-dashed border-border pt-3 flex justify-between items-center text-muted-foreground">
                  <span>Base Fare</span>
                  <span className="font-bold text-foreground">₹{Number(receipt.fare_amount || 0).toFixed(2)}</span>
                </div>
                <div className="flex justify-between items-center text-muted-foreground">
                  <span>Tax (GST 18%)</span>
                  <span className="font-bold text-foreground">₹{Number(receipt.tax_inr).toFixed(2)}</span>
                </div>
              </>
            )}

            <div className="border-t border-dashed border-border pt-3 flex justify-between items-center text-sm font-bold text-foreground">
              <span>{isCash ? "Amount Due" : "Amount Paid"}</span>
              <span className="text-primary font-black text-base">₹{Number(displayAmount).toFixed(2)}</span>
            </div>
          </div>

          <p className="text-[10px] text-muted-foreground text-center animate-pulse">
            Redirecting to guide assignment console in 3s...
          </p>
        </CardContent>

        <CardFooter className="p-6 border-t border-border/50 bg-muted/10">
          <Button
            onClick={handleManualRedirect}
            className="w-full bg-primary text-primary-foreground hover:bg-primary/95 rounded-2xl py-5 font-black text-xs shadow-lg hover:scale-[1.01] transition-all cursor-pointer flex items-center justify-center gap-1.5"
          >
            <ReceiptIcon size={16} weight="bold" />
            <span>Go to Active Trip</span>
          </Button>
        </CardFooter>
      </Card>
    </div>
  )
}
export default PaymentConfirmationPage
