import React, { useState } from "react"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Label } from "@/components/ui/label"
import { Progress } from "@/components/ui/progress"
import { Badge } from "@/components/ui/badge"
import {
  WarningOctagonIcon,
  UploadSimpleIcon,
  TrashIcon,
  CheckCircleIcon,
  ShieldWarningIcon,
  ClockIcon,
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { reportsService } from "@/services/reports.service"
import type { BookingReport } from "@/services/reports.service"
import api from "@/services/api-client"
import { ImageThumbnail } from "@/components/shared/ImageThumbnail"
import { formatDateTimeIST } from "@/lib/utils"

interface ReportRideDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  bookingId: number
}

const CATEGORIES = [
  { value: "passenger_safety", label: "Passenger Safety Issue" },
  { value: "assistant_behavior", label: "Guide/Assistant Behavior" },
  { value: "late_arrival", label: "Late Arrival / Delays" },
  { value: "wrong_route", label: "Wrong Route / Navigation" },
  { value: "fare_issue", label: "Incorrect Fare / Fees" },
  { value: "payment_issue", label: "Payment & Billing Problems" },
  { value: "harassment", label: "Unprofessional Harassment" },
  { value: "emergency", label: "Active Emergency Alert" },
  { value: "vehicle_issue", label: "Vehicle / Equipment Defect" },
  { value: "lost_item", label: "Lost & Found Item" },
  { value: "technical_problem", label: "Technical App Issue" },
  { value: "other", label: "Other General Dispute" },
]

export function ReportRideDialog({
  open,
  onOpenChange,
  bookingId,
}: ReportRideDialogProps) {
  const [category, setCategory] = useState("passenger_safety")
  const [description, setDescription] = useState("")
  const [severity, setSeverity] = useState<"low" | "medium" | "high" | "critical">("medium")
  
  // File uploads
  const [files, setFiles] = useState<{ name: string; url: string; serverUrl?: string }[]>([])
  const [uploading, setUploading] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  // Success ticket tracking state
  const [submittedReport, setSubmittedReport] = useState<BookingReport | null>(null)

  const handleUploadFiles = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = Array.from(e.target.files || [])
    if (selectedFiles.length === 0) return

    if (files.length + selectedFiles.length > 5) {
      toast.error("You can upload a maximum of 5 evidence files.")
      return
    }

    setUploading(true)
    const uploadedList = [...files]

    try {
      for (const file of selectedFiles) {
        // Simple client-side size check (max 5MB)
        if (file.size > 5 * 1024 * 1024) {
          toast.error(`File ${file.name} exceeds 5MB size limit.`)
          continue
        }

        const formData = new FormData()
        formData.append("file", file)

        const res = await api.post("/bookings/upload", formData)

        uploadedList.push({
          name: file.name,
          url: URL.createObjectURL(file),
          serverUrl: res.data.url,
        })
      }
      setFiles(uploadedList)
      toast.success("Evidence files uploaded successfully!")
    } catch (err) {
      toast.error("Failed to upload evidence files.")
    } finally {
      setUploading(false)
    }
  }

  const handleRemoveFile = (index: number) => {
    setFiles((prev) => prev.filter((_, i) => i !== index))
  }

  const handleSubmitReport = async () => {
    if (description.length < 20) {
      toast.error("Please provide a description of at least 20 characters.")
      return
    }

    setSubmitting(true)
    try {
      const evidenceUrls = files.map((f) => (f as any).serverUrl || f.url)
      const report = await reportsService.submitReport(bookingId, {
        category,
        severity,
        description,
        evidence: evidenceUrls,
      })

      setSubmittedReport(report)
      toast.success("Incident report filed successfully!")
    } catch (err: any) {
      toast.error(err.response?.data?.detail || "Failed to submit incident report.")
    } finally {
      setSubmitting(false)
    }
  }

  const handleClose = () => {
    // Reset states
    setCategory("passenger_safety")
    setDescription("")
    setSeverity("medium")
    setFiles([])
    setSubmittedReport(null)
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg p-0 bg-card border border-border/80 rounded-2xl overflow-hidden shadow-2xl flex flex-col max-h-[90vh]">
        
        {/* Header Banner */}
        <div className="bg-gradient-to-r from-red-600/10 via-amber-500/5 to-transparent p-4 border-b border-border/80 flex items-center gap-3 select-none">
          <div className="p-2 bg-red-500/10 rounded-xl text-red-500 border border-red-500/20">
            <WarningOctagonIcon size={22} weight="fill" />
          </div>
          <div className="text-left">
            <DialogTitle className="font-extrabold text-sm text-foreground">
              Report Dispute / Incident
            </DialogTitle>
            <DialogDescription className="text-[11px] text-muted-foreground font-semibold mt-0.5">
              Secure trust audit center for Booking #{bookingId}
            </DialogDescription>
          </div>
        </div>

        {/* Scrollable Form Body */}
        <div className="flex-1 overflow-y-auto p-5 space-y-5 text-left">
          {submittedReport ? (
            /* SUCCESS TIMELINE VIEW */
            <div className="space-y-6 py-4 text-center">
              <div className="inline-flex p-3 bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 rounded-2xl mb-2">
                <CheckCircleIcon size={36} weight="fill" />
              </div>
              <div className="space-y-1">
                <h3 className="font-black text-sm text-foreground">Report Successfully Filed</h3>
                <p className="text-xs text-muted-foreground px-4 leading-relaxed">
                  Your trust case has been logged under audit ticket reference:
                </p>
                <div className="mt-3 inline-block font-mono font-black text-sm text-primary bg-muted/80 px-4 py-2 rounded-xl border border-border">
                  {submittedReport.report_number}
                </div>
              </div>

              {/* Status Timeline Progress */}
              <div className="border border-border/60 bg-muted/20 p-4 rounded-xl space-y-4">
                <div className="flex items-center justify-between text-xs font-bold">
                  <span className="text-muted-foreground uppercase tracking-wider text-[10px]">Current Status</span>
                  <Badge className="bg-red-500/15 text-red-500 font-extrabold text-[10px] rounded-full px-2 py-0.5 border-0 uppercase">
                    {submittedReport.status}
                  </Badge>
                </div>

                <div className="relative pl-6 space-y-4 text-left border-l-2 border-primary/20 ml-2">
                  <div className="relative">
                    <span className="absolute -left-[30px] top-0.5 w-3 h-3 rounded-full bg-primary ring-4 ring-primary/15" />
                    <h5 className="text-xs font-extrabold text-foreground">Submitted & Enqueued</h5>
                    <p className="text-[10px] text-muted-foreground mt-0.5 font-medium">
                      Incident enqueued to the trust dispatch dashboard queue.
                    </p>
                  </div>
                  <div className="relative opacity-60">
                    <span className="absolute -left-[30px] top-0.5 w-3 h-3 rounded-full bg-zinc-600" />
                    <h5 className="text-xs font-extrabold text-muted-foreground">Admin Review</h5>
                    <p className="text-[10px] text-muted-foreground mt-0.5 font-medium">
                      Auditor assigned to verify evidence coordinates and timestamps.
                    </p>
                  </div>
                </div>
              </div>

              {/* Response SLA Warning */}
              <div className="flex items-start gap-3 p-3 bg-amber-500/5 border border-amber-500/15 rounded-xl text-left">
                <ClockIcon size={18} className="text-amber-500 shrink-0 mt-0.5" />
                <div className="space-y-0.5">
                  <h6 className="text-xs font-bold text-amber-500">Estimated SLA Window</h6>
                  <p className="text-[10px] text-muted-foreground leading-relaxed font-semibold">
                    Trust monitors review safety-related incidents within 15 minutes. Billing disputes are resolved within 24 hours.
                  </p>
                </div>
              </div>

              <div className="pt-2">
                <Button onClick={handleClose} className="w-full font-bold text-xs py-4 rounded-xl">
                  Dismiss Ticket
                </Button>
              </div>
            </div>
          ) : (
            /* ACTIVE SUBMISSION FORM VIEW */
            <>
              {/* Category selector */}
              <div className="space-y-2.5">
                <Label className="text-xs font-extrabold text-foreground uppercase tracking-widest">
                  Dispute Category
                </Label>
                <div className="grid grid-cols-2 gap-2">
                  {CATEGORIES.map((cat) => (
                    <button
                      key={cat.value}
                      type="button"
                      onClick={() => setCategory(cat.value)}
                      className={`text-left p-2.5 rounded-xl border text-[11px] font-bold transition-all ${
                        category === cat.value
                          ? "bg-red-500/10 border-red-500 text-red-500 shadow-sm"
                          : "border-border/80 bg-muted/10 text-muted-foreground hover:bg-muted"
                      }`}
                    >
                      {cat.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Severity check */}
              <div className="space-y-2">
                <Label className="text-xs font-extrabold text-foreground uppercase tracking-widest">
                  Severity Priority
                </Label>
                <div className="grid grid-cols-4 gap-2">
                  {(["low", "medium", "high", "critical"] as const).map((sev) => (
                    <button
                      key={sev}
                      type="button"
                      onClick={() => setSeverity(sev)}
                      className={`py-2 rounded-lg border text-[10px] font-extrabold uppercase tracking-wider transition-all ${
                        severity === sev
                          ? sev === "critical"
                            ? "bg-red-600 text-white border-red-600 shadow-md"
                            : sev === "high"
                            ? "bg-amber-500 text-white border-amber-500 shadow-md"
                            : sev === "medium"
                            ? "bg-blue-600 text-white border-blue-600 shadow-md"
                            : "bg-zinc-600 text-white border-zinc-600 shadow-md"
                          : "border-border/80 bg-muted/10 text-muted-foreground hover:bg-muted"
                      }`}
                    >
                      {sev}
                    </button>
                  ))}
                </div>
              </div>

              {/* Description Input */}
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <Label htmlFor="desc" className="text-xs font-extrabold text-foreground uppercase tracking-widest">
                    Provide Incident Description
                  </Label>
                  <span
                    className={`text-[10px] font-black ${
                      description.length >= 20 ? "text-emerald-500" : "text-amber-500"
                    }`}
                  >
                    {description.length}/1000 characters (min 20)
                  </span>
                </div>
                <Textarea
                  id="desc"
                  value={description}
                  onChange={(e) => setDescription(e.target.value.slice(0, 1000))}
                  placeholder="Tell us exactly what happened. Provide timestamps, actions, or route discrepancies..."
                  className="rounded-xl min-h-[100px] border-border/85 bg-muted/20 text-xs font-semibold leading-relaxed"
                />
              </div>

              {/* Attachments Section */}
              <div className="space-y-2.5">
                <Label className="text-xs font-extrabold text-foreground uppercase tracking-widest">
                  Attach Evidence Receipts (Max 5)
                </Label>

                {/* Thumbnail display */}
                {files.length > 0 && (
                  <div className="grid grid-cols-5 gap-2 border border-border/60 p-2.5 rounded-xl bg-muted/20">
                    {files.map((file, i) => (
                      <div key={i} className="relative group rounded-lg overflow-hidden border border-border h-16 w-full">
                        <ImageThumbnail
                          url={file.url}
                          alt="upload"
                          aspectRatio="square"
                          metadata={{
                            title: file.name,
                            documentType: "Report Attachment",
                            uploadedBy: "Reporter",
                            uploadedAt: formatDateTimeIST(new Date()),
                          }}
                        />
                        <button
                          type="button"
                          onClick={() => handleRemoveFile(i)}
                          className="absolute top-1 right-1 bg-red-600 p-1 rounded text-white cursor-pointer z-30 opacity-0 group-hover:opacity-100 transition-opacity"
                        >
                          <TrashIcon size={12} weight="fill" />
                        </button>
                      </div>
                    ))}
                  </div>
                )}

                {/* Drag-drop Upload Button */}
                <div className="relative">
                  <label
                    className={`border-2 border-dashed rounded-xl p-4 flex flex-col items-center justify-center transition-colors cursor-pointer ${
                      uploading
                        ? "border-primary/40 bg-muted/10 cursor-wait"
                        : "border-border/80 hover:border-primary/60 bg-muted/5 hover:bg-muted/10"
                    }`}
                  >
                    <UploadSimpleIcon size={24} className={uploading ? "animate-bounce text-primary" : "text-muted-foreground"} />
                    <span className="text-[11px] font-bold text-foreground mt-2">
                      {uploading ? "Uploading Secure Documents..." : "Select Images/PDF (Max 5MB)"}
                    </span>
                    <input
                      type="file"
                      multiple
                      accept="image/*"
                      className="hidden"
                      disabled={uploading || files.length >= 5}
                      onChange={handleUploadFiles}
                    />
                  </label>
                </div>
              </div>

              {/* Submit Buttons */}
              <div className="pt-1 flex gap-2.5 select-none">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => onOpenChange(false)}
                  disabled={submitting}
                  className="flex-1 py-4 font-bold text-xs rounded-xl"
                >
                  Cancel
                </Button>
                <Button
                  type="button"
                  onClick={handleSubmitReport}
                  disabled={submitting || uploading || description.length < 20}
                  className="flex-1 py-4 font-bold text-xs bg-red-600 hover:bg-red-700 text-white rounded-xl shadow-md"
                >
                  {submitting ? "Submitting Report..." : "File Dispute"}
                </Button>
              </div>
            </>
          )}
        </div>

      </DialogContent>
    </Dialog>
  )
}
export default ReportRideDialog
