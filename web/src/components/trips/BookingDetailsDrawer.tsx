import React from "react"
import { Drawer, DrawerContent } from "@/components/ui/drawer"
import { BookingDetailsContent, type BookingDetailsData } from "./BookingDetailsContent"

interface BookingDetailsDrawerProps {
  isOpen: boolean
  onClose: () => void
  booking: BookingDetailsData | null
  loading?: boolean
}

export function BookingDetailsDrawer({
  isOpen,
  onClose,
  booking,
  loading,
}: BookingDetailsDrawerProps) {
  return (
    <Drawer
      open={isOpen}
      onOpenChange={(open) => !open && onClose()}
      showSwipeHandle={true}
      swipeDirection="down"
    >
      <DrawerContent className="w-full max-h-[92vh] h-[92vh] rounded-t-3xl border-t border-border/80 bg-background/98 backdrop-blur-md p-0 overflow-hidden shadow-2xl">
        <BookingDetailsContent booking={booking} loading={loading} onClose={onClose} />
      </DrawerContent>
    </Drawer>
  )
}
export default BookingDetailsDrawer
