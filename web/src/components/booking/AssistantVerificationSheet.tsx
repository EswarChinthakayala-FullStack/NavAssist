import React from "react"
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription } from "@/components/ui/sheet"
import { CheckCircle, Clock, ShieldCheck, Star } from "@phosphor-icons/react"

interface AssistantVerificationSheetProps {
  isOpen: boolean
  onClose: () => void
  assistantName: string
  verificationStatus: string
  totalTrips: number
  rating: number
}

export function AssistantVerificationSheet({
  isOpen,
  onClose,
  assistantName,
  verificationStatus,
  totalTrips,
  rating,
}: AssistantVerificationSheetProps) {
  const isApproved = verificationStatus === "APPROVED"

  return (
    <Sheet open={isOpen} onOpenChange={(open) => !open && onClose()}>
      {/* Slide-up on mobile, slide-left on desktop (handled by shadcn default SheetContent style) */}
      <SheetContent side="right" className="w-full sm:max-w-none md:w-1/2 md:max-w-[50vw] bg-card/95 border-l border-border/80 backdrop-blur-md p-4 sm:p-6 md:p-8 flex flex-col justify-between overflow-y-auto">
        <div className="space-y-6">
          <SheetHeader className="text-left">
            <div className="p-3 rounded-2xl w-fit bg-primary/10 text-primary mb-2">
              <ShieldCheck size={28} weight="fill" />
            </div>
            <SheetTitle className="text-lg font-black text-foreground">
              Verification & Trust Report
            </SheetTitle>
            <SheetDescription className="text-xs text-muted-foreground leading-normal">
              Background verification records and platform trust indicators for <strong>{assistantName}</strong>.
            </SheetDescription>
          </SheetHeader>

          {/* Verification Steps Rows */}
          <div className="space-y-4 pt-2">
            {/* 1. Aadhaar / ID Check */}
            <div className="flex gap-4 p-4 rounded-xl border border-border bg-muted/20 select-none">
              <div className="mt-0.5 text-success">
                <CheckCircle size={22} weight="fill" />
              </div>
              <div className="space-y-1">
                <h4 className="font-extrabold text-xs text-foreground">Government ID Verified</h4>
                <p className="text-[10px] text-muted-foreground leading-normal">
                  Aadhaar identification card and police background reports checked and approved on file.
                </p>
              </div>
            </div>

            {/* 2. Phone Check */}
            <div className="flex gap-4 p-4 rounded-xl border border-border bg-muted/20 select-none">
              <div className="mt-0.5 text-success">
                <CheckCircle size={22} weight="fill" />
              </div>
              <div className="space-y-1">
                <h4 className="font-extrabold text-xs text-foreground">Contact Verified</h4>
                <p className="text-[10px] text-muted-foreground leading-normal">
                  OTP verified phone registration, email validation, and active contact numbers verified.
                </p>
              </div>
            </div>

            {/* 3. Trust Score Check */}
            <div className="flex gap-4 p-4 rounded-xl border border-border bg-muted/20 select-none">
              <div className="mt-0.5 text-success">
                <CheckCircle size={22} weight="fill" />
              </div>
              <div className="space-y-1">
                <h4 className="font-extrabold text-xs text-foreground">High Trust Standing</h4>
                <p className="text-[10px] text-muted-foreground leading-normal">
                  Platform reputation index is safe. Review sentiment score is high with no user complaints.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Small Stat Row at bottom */}
        <div className="border-t border-border pt-4 grid grid-cols-2 gap-4 select-none">
          <div className="p-3.5 rounded-xl border border-border/80 bg-muted/10 text-center">
            <span className="text-[9px] font-black uppercase text-muted-foreground tracking-wider block">Total Escorts</span>
            <span className="text-base font-black text-foreground mt-1 block">{totalTrips}</span>
          </div>

          <div className="p-3.5 rounded-xl border border-border/80 bg-muted/10 text-center">
            <span className="text-[9px] font-black uppercase text-muted-foreground tracking-wider block">Average Rating</span>
            <span className="text-base font-black text-warning mt-1 block flex items-center justify-center gap-1">
              <Star size={16} weight="fill" />
              {rating.toFixed(1)}
            </span>
          </div>
        </div>
      </SheetContent>
    </Sheet>
  )
}
export default AssistantVerificationSheet
