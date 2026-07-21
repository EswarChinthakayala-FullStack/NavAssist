import React, { useEffect, useState } from "react"
import { BookingDetailsSheet } from "./BookingDetailsSheet"
import { BookingDetailsDrawer } from "./BookingDetailsDrawer"
import type { BookingDetailsData } from "./BookingDetailsContent"

interface BookingDetailsResponsiveProps {
  isOpen: boolean
  onClose: () => void
  booking: BookingDetailsData | null
  loading?: boolean
}

export function BookingDetailsResponsive({
  isOpen,
  onClose,
  booking,
  loading,
}: BookingDetailsResponsiveProps) {
  const [isDesktop, setIsDesktop] = useState<boolean>(() => {
    if (typeof window !== "undefined") {
      return window.innerWidth >= 768
    }
    return true
  })

  useEffect(() => {
    const handleResize = () => {
      setIsDesktop(window.innerWidth >= 768)
    }

    window.addEventListener("resize", handleResize)
    return () => window.removeEventListener("resize", handleResize)
  }, [])

  if (isDesktop) {
    return <BookingDetailsSheet isOpen={isOpen} onClose={onClose} booking={booking} loading={loading} />
  }

  return <BookingDetailsDrawer isOpen={isOpen} onClose={onClose} booking={booking} loading={loading} />
}
export default BookingDetailsResponsive
