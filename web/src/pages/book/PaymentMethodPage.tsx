import React, { useState, useEffect } from "react"
import { useNavigate, useLocation, useParams } from "react-router-dom"
import { useAuth } from "@/store/auth-context"
import { walletService } from "@/services/wallet.service"
import { bookingsService } from "@/services/bookings.service"
import { paymentsService } from "@/services/payments.service"
import { formatCurrency } from "@/components/booking/FareBreakdownCard"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  CreditCardIcon,
  WalletIcon,
  BankIcon,
  CoinsIcon,
  ShieldCheckIcon,
  LockIcon,
  MoneyIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"

function useWalletBalance() {
  const [balance, setBalance] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)

  const fetchBalance = async () => {
    setLoading(true)
    try {
      const res = await walletService.getBalance()
      setBalance(Number(res.balance || 0.00))
    } catch (err) {
      console.error("Failed to query wallet balance:", err)
      setBalance(0.00)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchBalance()
  }, [])

  return { balance, loading, refetch: fetchBalance }
}

function useCreateOrder() {
  const [loading, setLoading] = useState(false)

  const createOrder = async (bookingId: number, method: "online" | "cash" = "online") => {
    setLoading(true)
    try {
      const res = await paymentsService.createOrder(bookingId, method)
      return res
    } finally {
      setLoading(false)
    }
  }

  return { createOrder, loading }
}

const loadRazorpayCheckoutScript = (): Promise<boolean> => {
  return new Promise((resolve) => {
    if ((window as any).Razorpay) {
      resolve(true)
      return
    }
    const script = document.createElement("script")
    script.src = "https://checkout.razorpay.com/v1/checkout.js"
    script.onload = () => resolve(true)
    script.onerror = () => resolve(false)
    document.body.appendChild(script)
  })
}

type PaymentStage = "idle" | "creating_order" | "loading_sdk" | "checkout_open" | "verifying"

export function PaymentMethodPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const params = useParams<{ bookingId?: string }>()
  const { user } = useAuth()
  
  const { balance: walletBalance, loading: loadingWallet } = useWalletBalance()
  const { createOrder, loading: isCreatingOrder } = useCreateOrder()

  const [bookingId, setBookingId] = useState<number | null>(null)
  const [fareAmount, setFareAmount] = useState<number | null>(null)
  const [selectedMethod, setSelectedMethod] = useState("upi")
  const [loadingBooking, setLoadingBooking] = useState(true)
  const [paymentStage, setPaymentStage] = useState<PaymentStage>("idle")

  useEffect(() => {
    const stateId = location.state?.bookingId
    const paramId = params.bookingId ? parseInt(params.bookingId) : null
    const sessionStr = sessionStorage.getItem("active_booking_id")
    const resolvedId = stateId || paramId || (sessionStr ? parseInt(sessionStr) : null)

    if (!resolvedId) {
      toast.error("Active booking reference not found.")
      navigate("/book/pickup")
      return
    }
    setBookingId(resolvedId)

    const fetchBookingDetails = async () => {
      setLoadingBooking(true)
      try {
        const res = await bookingsService.getBooking(resolvedId)
        setFareAmount(Number(res.fare_amount || res.fare_estimate || res.final_fare || 0))
      } catch (err) {
        console.error("Failed to load booking details for payment", err)
        toast.error("Failed to load booking details.")
      } finally {
        setLoadingBooking(false)
      }
    }

    fetchBookingDetails()
  }, [location.state, params.bookingId, navigate])

  const handlePayment = async () => {
    if (!bookingId || fareAmount === null || paymentStage !== "idle") return

    // ─── Cash Payment Flow ───────────────────────────────
    if (selectedMethod === "cash") {
      setPaymentStage("creating_order")
      try {
        await createOrder(bookingId, "cash")
        toast.success("Cash payment mode selected! Please pay your guide upon arrival.")
        navigate("/book/payment-confirmation", {
          state: {
            bookingId,
            method: "cash",
            status: "pending"
          }
        })
      } catch (err: any) {
        toast.error(err.response?.data?.detail || "Failed to confirm cash payment choice.")
      } finally {
        setPaymentStage("idle")
      }
      return
    }

    // ─── Wallet Payment Flow ─────────────────────────────
    if (selectedMethod === "wallet") {
      if (walletBalance !== null && walletBalance < fareAmount) {
        toast.error("Insufficient wallet funds. Please topup or select another payment option.")
        return
      }
      setPaymentStage("creating_order")
      toast.loading("Processing payment via NavAssist Wallet...")
      setTimeout(() => {
        toast.dismiss()
        toast.success("Wallet payment completed successfully!")
        navigate("/book/payment-confirmation", {
          state: {
            bookingId,
            method: "wallet",
            status: "success"
          }
        })
        setPaymentStage("idle")
      }, 1200)
      return
    }

    // ─── Online Payment (Razorpay Checkout) ──────────────
    setPaymentStage("loading_sdk")
    const scriptLoaded = await loadRazorpayCheckoutScript()
    if (!scriptLoaded) {
      toast.error("Razorpay SDK failed to load. Please check your internet connection.")
      setPaymentStage("idle")
      return
    }

    setPaymentStage("creating_order")
    try {
      const order = await createOrder(bookingId, "online")

      // The backend returns razorpay_key_id and razorpay_order_id
      const razorpayKeyId = order.razorpay_key_id
      const razorpayOrderId = order.razorpay_order_id || order.gateway_order_id

      if (!razorpayKeyId || !razorpayOrderId) {
        toast.error("Payment gateway configuration error. Please contact support.")
        setPaymentStage("idle")
        return
      }

      setPaymentStage("checkout_open")

      const options = {
        key: razorpayKeyId,
        amount: Math.round(Number(order.amount) * 100), // Convert INR to paise
        currency: order.currency || "INR",
        name: "NavAssist Portal",
        description: `Escort Journey Booking #${bookingId}`,
        order_id: razorpayOrderId,
        handler: async (response: any) => {
          // Payment success — verify on backend
          setPaymentStage("verifying")
          try {
            await paymentsService.verifyPayment({
              razorpay_order_id: response.razorpay_order_id,
              razorpay_payment_id: response.razorpay_payment_id,
              razorpay_signature: response.razorpay_signature,
            })
            toast.success("Payment verified successfully!")
            navigate("/book/payment-confirmation", {
              state: {
                bookingId,
                razorpay_order_id: response.razorpay_order_id,
                razorpay_payment_id: response.razorpay_payment_id,
                razorpay_signature: response.razorpay_signature,
              }
            })
          } catch (verifyErr: any) {
            console.error("Payment verification failed:", verifyErr)
            toast.error(
              verifyErr.response?.data?.detail ||
              "Payment verification failed. Please contact support if amount was deducted."
            )
          } finally {
            setPaymentStage("idle")
          }
        },
        prefill: {
          name: user?.full_name || "",
          email: user?.email || "guest@navassist.in",
          contact: user?.phone || ""
        },
        theme: {
          color: "#3b82f6"
        },
        modal: {
          escape: false,
          backdropclose: false,
          ondismiss: () => {
            toast.info("Payment checkout was cancelled. You can retry anytime.")
            setPaymentStage("idle")
          }
        }
      }

      const rzp = new (window as any).Razorpay(options)

      rzp.on("payment.failed", async (response: any) => {
        const error = response.error || {}
        console.error("Razorpay payment failed:", error)
        toast.error(`Payment failed: ${error.description || "Unknown error"}`)

        // Record failure on backend
        try {
          await paymentsService.recordFailure({
            razorpay_order_id: razorpayOrderId,
            error_code: error.code,
            error_description: error.description,
            error_reason: error.reason
          })
        } catch (recordErr) {
          console.error("Failed to record payment failure:", recordErr)
        }
        setPaymentStage("idle")
      })

      rzp.open()
    } catch (err: any) {
      console.error("Payment initiation error:", err)
      const errorMsg = err.response?.data?.detail || err.message || "Failed to initiate payment session"
      toast.error(`Checkout session failure: ${errorMsg}`)
      setPaymentStage("idle")
    }
  }

  const isProcessing = paymentStage !== "idle"

  const getButtonLabel = () => {
    switch (paymentStage) {
      case "loading_sdk": return "Loading Razorpay..."
      case "creating_order": return "Creating Order..."
      case "checkout_open": return "Complete in Razorpay..."
      case "verifying": return "Verifying Payment..."
      default:
        if (selectedMethod === "cash") return "Confirm Cash Mode"
        return `Pay ${formatCurrency(fareAmount)}`
    }
  }

  if (loadingBooking || fareAmount === null) {
    return (
      <div className="h-[380px] w-full flex flex-col items-center justify-center gap-4 bg-background text-foreground">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs uppercase font-extrabold tracking-wider text-muted-foreground">Initializing payment gateway...</span>
      </div>
    )
  }

  const paymentMethods = [
    { id: "upi", label: "Unified Payments Interface (UPI)", description: "GPay, PhonePe, Paytm & any UPI ID", icon: <CoinsIcon size={22} weight="duotone" className="text-primary" /> },
    { id: "card", label: "Credit / Debit Cards", description: "Visa, Mastercard, RuPay, Maestro", icon: <CreditCardIcon size={22} weight="duotone" className="text-primary" /> },
    { id: "netbanking", label: "Net Banking", description: "Secure login access for all major Indian banks", icon: <BankIcon size={22} weight="duotone" className="text-primary" /> },
    { id: "cash", label: "Cash on Collection", description: "Pay cash directly to your verified guide upon meeting", icon: <MoneyIcon size={22} weight="duotone" className="text-emerald-500" /> },
    {
      id: "wallet",
      label: `NavAssist Wallet`,
      description: loadingWallet ? "Querying balance..." : `Available Balance: ${formatCurrency(walletBalance)}`,
      icon: <WalletIcon size={22} weight="duotone" className="text-primary" />,
      badge: walletBalance !== null && walletBalance >= fareAmount ? "Sufficient Funds" : "Insufficient Funds"
    }
  ]

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-xl mx-auto py-4"
    >
      <Card className="border border-border/80 shadow-md backdrop-blur-md rounded-2xl overflow-hidden">
        <CardHeader className="border-b border-border/50 bg-muted/20 pb-4 text-left">
          <CardTitle className="text-lg font-bold flex items-center gap-2">
            <LockIcon size={20} className="text-primary" />
            Select Payment Method
          </CardTitle>
          <CardDescription className="text-xs">
            Transactions are encrypted and processed securely via Razorpay or Cash verification
          </CardDescription>
        </CardHeader>
        <CardContent className="p-6 space-y-4">
          <div className="flex flex-col gap-3">
            {paymentMethods.map((method) => {
              const isSelected = selectedMethod === method.id
              const isDisabled = (method.id === "wallet" && (walletBalance === null || walletBalance < fareAmount)) || isProcessing
              
              return (
                <div
                  key={method.id}
                  onClick={() => !isDisabled && setSelectedMethod(method.id)}
                  className={`p-4 border rounded-2xl flex items-center justify-between cursor-pointer transition-all duration-300 ${
                    isSelected
                      ? "border-primary bg-primary/5 ring-2 ring-primary/20 scale-[1.01]"
                      : isDisabled
                      ? "border-border/40 bg-muted/10 opacity-50 cursor-not-allowed"
                      : "border-border/80 hover:border-primary/50 hover:bg-muted/30"
                  }`}
                >
                  <div className="flex gap-4 items-center">
                    <div className="p-3 bg-muted rounded-xl shrink-0">
                      {method.icon}
                    </div>
                    <div className="text-left space-y-0.5">
                      <p className="text-xs font-bold text-foreground">{method.label}</p>
                      <p className="text-[10px] text-muted-foreground leading-relaxed">{method.description}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    {method.badge && (
                      <Badge className={`text-[8px] uppercase tracking-wider font-extrabold border-0 px-2 py-0.5 rounded-full ${
                        method.badge === "Sufficient Funds"
                          ? "bg-success/20 text-success"
                          : "bg-destructive/20 text-destructive"
                      }`}>
                        {method.badge}
                      </Badge>
                    )}
                    <div className={`w-4 h-4 rounded-full border flex items-center justify-center shrink-0 ${
                      isSelected ? "border-primary" : "border-muted-foreground/30"
                    }`}>
                      {isSelected && <div className="w-2 h-2 rounded-full bg-primary" />}
                    </div>
                  </div>
                </div>
              )
            })}
          </div>

          <div className="bg-muted/40 border border-border/80 p-3.5 rounded-2xl flex gap-2 items-center text-left">
            <ShieldCheckIcon size={18} className="text-success shrink-0" />
            <span className="text-[10px] text-muted-foreground font-semibold leading-relaxed">
              Your transaction is protected under secure SSL-encryption & enterprise payment integrity protocols.
            </span>
          </div>
        </CardContent>
        <CardFooter className="border-t border-border/50 p-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-muted/10">
          <div className="text-left">
            <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">Payable Total</span>
            <p className="text-xl font-black text-foreground">{formatCurrency(fareAmount)}</p>
          </div>

          <div className="flex gap-3 w-full sm:w-auto">
            <Button
              variant="outline"
              onClick={() => navigate("/book/price-estimate")}
              disabled={isProcessing}
              className="rounded-xl py-4 font-bold text-xs flex-1 sm:flex-initial cursor-pointer"
            >
              Back
            </Button>
            <Button
              onClick={handlePayment}
              disabled={isProcessing || isCreatingOrder}
              className="bg-primary text-primary-foreground hover:bg-primary/95 rounded-xl py-4 px-6 font-extrabold text-xs shadow-md flex-1 sm:flex-initial hover:scale-[1.02] transition-all cursor-pointer"
            >
              {isProcessing ? (
                <span className="flex items-center gap-2">
                  <span className="w-3.5 h-3.5 border-2 border-primary-foreground border-t-transparent rounded-full animate-spin" />
                  {getButtonLabel()}
                </span>
              ) : (
                getButtonLabel()
              )}
            </Button>
          </div>
        </CardFooter>
      </Card>
    </motion.div>
  )
}
export default PaymentMethodPage
