import React, { useState, useEffect } from "react"
import { api } from "@/services/api"
import { useAuth } from "@/store/auth-context"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { toast } from "sonner"
import {
  ShieldCheckIcon,
  ClockIcon,
  XCircleIcon,
  UploadSimpleIcon,
  FileIcon,
  ArrowRightIcon
} from "@phosphor-icons/react"

export function KycPage() {
  const { user } = useAuth()
  
  const [kycStatus, setKycStatus] = useState<"DRAFT" | "NOT_SUBMITTED" | "PENDING" | "APPROVED" | "REJECTED">("DRAFT")
  const [aadhaarNumber, setAadhaarNumber] = useState("")
  const [docFront, setDocFront] = useState<File | null>(null)
  const [docBack, setDocBack] = useState<File | null>(null)
  
  const [reviewNotes, setReviewNotes] = useState("")
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const fetchKycStatus = async () => {
    if (user?.role !== "assistant") return
    setLoading(true)
    try {
      const res = await api.get("/kyc/status")
      const rawStatus = (res.data.verification_status || res.data.status || "DRAFT").toUpperCase()
      const status = (rawStatus === "NOT_SUBMITTED" || rawStatus === "NONE") ? "DRAFT" : rawStatus
      setKycStatus(status as any)
      if (res.data.review_notes) {
        setReviewNotes(res.data.review_notes)
      }
      if (res.data.aadhaar_number) {
        setAadhaarNumber(res.data.aadhaar_number)
      }
    } catch (err) {
      // If 404, default to DRAFT so input form is visible
      setKycStatus("DRAFT")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (user) {
      fetchKycStatus()
    }
  }, [user])

  const handleBecomeAssistant = async () => {
    setLoading(true)
    try {
      await api.post("/assistants/apply")
      toast.success("Profile role upgraded to Assistant! Please submit your KYC details.")
      // Force reload page session values
      window.location.reload()
    } catch (err) {
      // Handled globally
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (aadhaarNumber.length !== 12) {
      toast.error("Aadhaar Number must be exactly 12 digits.")
      return
    }
    if (!docFront || !docBack) {
      toast.error("Please upload front and back side photographs of your Aadhaar card.")
      return
    }

    setSubmitting(true)
    try {
      const formData = new FormData()
      formData.append("aadhaar_number", aadhaarNumber)
      formData.append("doc_front", docFront)
      formData.append("doc_back", docBack)

      await api.post("/kyc/documents", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      })

      toast.success("KYC documents submitted successfully for verification review!")
      fetchKycStatus()
    } catch (err) {
      // Handled globally
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-20 gap-3">
        <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
        <span className="text-xs text-muted-foreground">Loading KYC details...</span>
      </div>
    )
  }

  // Guest Role view layout
  if (user?.role !== "assistant") {
    return (
      <div className="max-w-6xl mx-auto py-2 space-y-6">
        {/* Header Banner */}
        <div className="bg-gradient-to-r from-primary/10 via-primary/5 to-transparent border border-border/80 p-6 rounded-2xl shadow-sm">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <div className="text-left">
              <div className="flex items-center gap-2">
                <Badge className="bg-primary/20 hover:bg-primary/20 text-primary border-0 text-[10px] px-3.5 py-1 rounded-full font-bold uppercase tracking-wider">
                  Account Trust
                </Badge>
              </div>
              <h3 className="font-black text-2xl mt-3 tracking-tight text-foreground">Identity & Verification</h3>
              <p className="text-xs text-muted-foreground mt-1">Credentials and safety verification parameters for traveler profile accounts.</p>
            </div>
          </div>
        </div>

        {/* 2-Column Grid Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Main Info (8 Columns) */}
          <div className="lg:col-span-8 space-y-6">
            <Card className="border border-success/30 bg-success/5 rounded-2xl overflow-hidden shadow-sm text-left">
              <CardHeader className="flex flex-row items-center gap-4">
                <div className="w-12 h-12 bg-success/10 text-success rounded-full flex items-center justify-center shrink-0">
                  <ShieldCheckIcon size={28} weight="fill" />
                </div>
                <div>
                  <CardTitle className="text-lg font-bold text-success">Guest Account Verified</CardTitle>
                  <CardDescription className="text-xs text-success/80">Your profile is authenticated via phone OTP.</CardDescription>
                </div>
              </CardHeader>
              <CardContent className="text-xs text-muted-foreground leading-relaxed pt-2">
                As a traveler guest, your identity is verified using OTP validation on your phone. You can immediately request verified local assistants and dispatch emergency alerts. No additional documents are required.
              </CardContent>
            </Card>

            <Card className="rounded-2xl border border-border shadow-md bg-gradient-to-r from-primary/5 to-transparent text-left">
              <CardHeader>
                <CardTitle className="text-base font-bold">Become a Verified Assistant</CardTitle>
                <CardDescription>Earn money by guiding and receiving travelers at platforms and terminals.</CardDescription>
              </CardHeader>
              <CardContent className="text-xs text-muted-foreground leading-relaxed">
                By applying to become a local escort assistant, you must submit government Aadhaar details for police verification. Once approved by the administrator, you can toggle online availability and receive passenger dispatch bookings!
              </CardContent>
              <CardFooter className="pt-2">
                <Button onClick={handleBecomeAssistant} className="w-full h-11 text-xs font-black shadow-lg bg-primary text-primary-foreground hover:bg-primary/90 rounded-xl flex items-center justify-center gap-2 cursor-pointer">
                  Apply to Become Assistant
                  <ArrowRightIcon size={16} />
                </Button>
              </CardFooter>
            </Card>
          </div>

          {/* Guidelines Sidebar (4 Columns) */}
          <div className="lg:col-span-4 space-y-6 text-left">
            <Card className="border border-border/80 shadow-md rounded-2xl overflow-hidden bg-card">
              <CardHeader className="pb-3 border-b border-border/40">
                <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                  <ShieldCheckIcon size={18} className="text-primary" />
                  Trust & Security
                </CardTitle>
              </CardHeader>
              <CardContent className="pt-6 space-y-4">
                <div className="space-y-3 text-xs">
                  <div className="p-3.5 bg-muted/40 border border-border rounded-xl">
                    <h5 className="font-bold text-foreground">Data Encryption</h5>
                    <p className="text-[10px] text-muted-foreground mt-1">All personal and document coordinates are encrypted at rest and in transit.</p>
                  </div>
                  <div className="p-3.5 bg-muted/40 border border-border rounded-xl">
                    <h5 className="font-bold text-foreground">Verification Tiers</h5>
                    <p className="text-[10px] text-muted-foreground mt-1">Travelers verify with phone credentials. Assistants undergo full government identification checks.</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-6xl mx-auto py-2 space-y-6">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-primary/10 via-primary/5 to-transparent border border-border/80 p-6 rounded-2xl shadow-sm">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div className="text-left">
            <Badge className="bg-primary/20 hover:bg-primary/20 text-primary border-0 text-[10px] px-3.5 py-1 rounded-full font-bold uppercase tracking-wider">
              Identity Verification
            </Badge>
            <h3 className="font-black text-2xl mt-3 tracking-tight text-foreground">Assistant Verification Profile</h3>
            <p className="text-xs text-muted-foreground mt-1">Government credentials and verification status parameters are mandatory for local helper escorts.</p>
          </div>
        </div>
      </div>

      {/* 2-Column Grid Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left column: Status displays & Upload Form (8 Columns) */}
        <div className="lg:col-span-8 space-y-6 text-left">
          {/* APPROVED State */}
          {kycStatus === "APPROVED" && (
            <Card className="border border-success/30 bg-success/5 rounded-2xl shadow-sm">
              <CardHeader className="flex flex-row items-center gap-4">
                <div className="w-12 h-12 bg-success/10 text-success rounded-full flex items-center justify-center shrink-0">
                  <ShieldCheckIcon size={28} weight="fill" />
                </div>
                <div>
                  <CardTitle className="text-lg font-bold text-success">Identity Approved</CardTitle>
                  <CardDescription className="text-xs text-success/80">Your profile has passed security and background trust review.</CardDescription>
                </div>
              </CardHeader>
              <CardContent className="text-xs text-muted-foreground leading-relaxed pt-2">
                Your verification status is active. You can now toggle online availability in the marketplace and accept passenger dispatches. If you update account credentials, security parameters may reset.
              </CardContent>
            </Card>
          )}

          {/* PENDING State */}
          {kycStatus === "PENDING" && (
            <Card className="border border-warning/30 bg-warning/5 rounded-2xl shadow-sm">
              <CardHeader className="flex flex-row items-center gap-4">
                <div className="w-12 h-12 bg-warning/10 text-warning rounded-full flex items-center justify-center shrink-0 animate-pulse">
                  <ClockIcon size={28} />
                </div>
                <div>
                  <CardTitle className="text-lg font-bold text-warning">KYC Review Pending</CardTitle>
                  <CardDescription className="text-xs text-warning/80">Our safety administrators are reviewing your submission details.</CardDescription>
                </div>
              </CardHeader>
              <CardContent className="text-xs text-muted-foreground leading-relaxed pt-2">
                Identity background audits usually finalize within 10–15 minutes. We will verify coordinate references and validate Aadhaar records.
              </CardContent>
            </Card>
          )}

          {/* REJECTED State */}
          {kycStatus === "REJECTED" && (
            <Card className="border border-destructive/30 bg-destructive/5 rounded-2xl shadow-sm">
              <CardHeader className="flex flex-row items-center gap-4">
                <div className="w-12 h-12 bg-destructive/10 text-destructive rounded-full flex items-center justify-center shrink-0">
                  <XCircleIcon size={28} weight="fill" />
                </div>
                <div>
                  <CardTitle className="text-lg font-bold text-destructive">KYC Application Rejected</CardTitle>
                  <CardDescription className="text-xs text-destructive/80">Submission documents did not pass background audit criteria.</CardDescription>
                </div>
              </CardHeader>
              <CardContent className="text-xs text-muted-foreground leading-relaxed pt-2">
                <div className="p-3.5 bg-destructive/10 border border-destructive/20 rounded-xl mb-3 text-destructive font-semibold">
                  Reason: {reviewNotes || "Uploaded photographs were unclear. Please resubmit."}
                </div>
                Please update your credentials and submit again for re-evaluation.
              </CardContent>
            </Card>
          )}

          {/* Upload Form (Visible if DRAFT, NOT_SUBMITTED, or REJECTED) */}
          {(kycStatus === "DRAFT" || kycStatus === "NOT_SUBMITTED" || kycStatus === "REJECTED") && (
            <Card className="rounded-2xl border border-border shadow-sm">
              <CardHeader>
                <CardTitle className="text-base font-bold">Submit Identity Credentials</CardTitle>
                <CardDescription>Upload clear photographic images of front/back government documents.</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="grid gap-5">
                  <div className="space-y-1">
                    <Label htmlFor="aadhaar" className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider pl-0.5">Aadhaar Number (12 Digits)</Label>
                    <Input
                      id="aadhaar"
                      value={aadhaarNumber}
                      onChange={(e) => setAadhaarNumber(e.target.value.replace(/\D/g, '').slice(0, 12))}
                      placeholder="0000 0000 0000"
                      required
                      className="rounded-xl h-10 text-xs font-semibold mt-1"
                    />
                  </div>
                  
                  <div className="grid grid-cols-2 gap-4">
                    {/* Front Upload */}
                    <div className="relative border-2 border-dashed border-border rounded-xl p-4 text-center cursor-pointer hover:bg-muted/40 transition-colors flex flex-col items-center justify-center min-h-[140px]">
                      <input
                        type="file"
                        accept="image/*"
                        onChange={(e) => setDocFront(e.target.files?.[0] || null)}
                        className="absolute inset-0 opacity-0 cursor-pointer"
                        required
                      />
                      <div className="text-primary/70 mb-2">
                        <UploadSimpleIcon size={24} />
                      </div>
                      <span className="text-xs text-muted-foreground font-bold">Upload Doc Front</span>
                      {docFront && (
                        <span className="text-[10px] text-success font-semibold mt-2 flex items-center gap-1">
                          <FileIcon size={12} />
                          {docFront.name.slice(0, 15)}...
                        </span>
                      )}
                    </div>

                    {/* Back Upload */}
                    <div className="relative border-2 border-dashed border-border rounded-xl p-4 text-center cursor-pointer hover:bg-muted/40 transition-colors flex flex-col items-center justify-center min-h-[140px]">
                      <input
                        type="file"
                        accept="image/*"
                        onChange={(e) => setDocBack(e.target.files?.[0] || null)}
                        className="absolute inset-0 opacity-0 cursor-pointer"
                        required
                      />
                      <div className="text-primary/70 mb-2">
                        <UploadSimpleIcon size={24} />
                      </div>
                      <span className="text-xs text-muted-foreground font-bold">Upload Doc Back</span>
                      {docBack && (
                        <span className="text-[10px] text-success font-semibold mt-2 flex items-center gap-1">
                          <FileIcon size={12} />
                          {docBack.name.slice(0, 15)}...
                        </span>
                      )}
                    </div>
                  </div>

                  <Button type="submit" disabled={submitting} className="w-full h-11 text-xs font-black shadow-lg bg-primary text-primary-foreground hover:bg-primary/90 rounded-xl mt-2 cursor-pointer flex items-center justify-center gap-1.5">
                    {submitting ? "Submitting documents..." : "Submit Documents for Verification"}
                  </Button>
                </form>
              </CardContent>
            </Card>
          )}
        </div>

        {/* Right column: FAQ & Document guidelines (4 Columns) */}
        <div className="lg:col-span-4 space-y-6 text-left">
          <Card className="border border-border/80 shadow-md rounded-2xl overflow-hidden bg-card">
            <CardHeader className="pb-3 border-b border-border/40">
              <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                <ShieldCheckIcon size={18} className="text-primary" />
                Submission Guidelines
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-6 space-y-4">
              <div className="space-y-3 text-xs leading-relaxed">
                <div className="p-3.5 bg-muted/40 border border-border rounded-xl">
                  <h5 className="font-bold text-foreground">Clear Legibility</h5>
                  <p className="text-[10px] text-muted-foreground mt-1">Make sure the text and numbers on your government ID card are completely readable. Blur or low lighting will result in verification delays or rejection.</p>
                </div>
                <div className="p-3.5 bg-muted/40 border border-border rounded-xl">
                  <h5 className="font-bold text-foreground">Edges & Framing</h5>
                  <p className="text-[10px] text-muted-foreground mt-1">The uploaded photos must show all four corners of the physical document card. Avoid cropping out text or identifiers.</p>
                </div>
                <div className="p-3.5 bg-muted/40 border border-border rounded-xl">
                  <h5 className="font-bold text-foreground">Privacy Shield</h5>
                  <p className="text-[10px] text-muted-foreground mt-1">Your documents are processed securely solely for trust purposes. They are never shared publicly or exposed to passengers.</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
export default KycPage
