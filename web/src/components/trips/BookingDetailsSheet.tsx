import React from "react"
import { Sheet, SheetContent } from "@/components/ui/sheet"
import { BookingDetailsContent, type BookingDetailsData } from "./BookingDetailsContent"

interface BookingDetailsSheetProps {
  isOpen: boolean
  onClose: () => void
  booking: BookingDetailsData | null
  loading?: boolean
}

export function BookingDetailsSheet({
  isOpen,
  onClose,
  booking,
  loading,
}: BookingDetailsSheetProps) {
  return (
    <Sheet open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <SheetContent
        side="right"
        showCloseButton={false}
        className="w-[50vw] min-w-[50vw] max-w-[50vw] h-screen p-0 bg-background/95 backdrop-blur-md border-l border-border/80 shadow-2xl overflow-hidden"
      >
        <BookingDetailsContent booking={booking} loading={loading} onClose={onClose} />
      </SheetContent>
    </Sheet>
  )
}
export default BookingDetailsSheet
