import React from "react"
import { Skeleton } from "@/components/ui/skeleton"

// Skeleton for list items (Bookings, Assistants, Coupons)
export function CardListSkeleton({ count = 3 }: { count?: number }) {
  return (
    <div className="flex flex-col gap-4 w-full">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="p-5 rounded-2xl border border-border bg-card/50 flex flex-col gap-4">
          <div className="flex justify-between items-start">
            <div className="flex gap-3 items-center">
              <Skeleton className="w-10 h-10 rounded-full" />
              <div className="flex flex-col gap-1.5">
                <Skeleton className="w-28 h-4 rounded-md" />
                <Skeleton className="w-20 h-3 rounded-md" />
              </div>
            </div>
            <Skeleton className="w-16 h-5 rounded-full" />
          </div>
          <hr className="border-border" />
          <div className="flex justify-between items-center mt-1">
            <Skeleton className="w-24 h-3 rounded-md" />
            <Skeleton className="w-14 h-4 rounded-md" />
          </div>
        </div>
      ))}
    </div>
  )
}

// Skeleton for KYC and Settings Profile update page layout
export function ProfileSkeleton() {
  return (
    <div className="w-full max-w-xl mx-auto flex flex-col gap-6">
      <div className="flex flex-col items-center gap-3">
        <Skeleton className="w-24 h-24 rounded-full" />
        <Skeleton className="w-32 h-5 rounded-md" />
        <Skeleton className="w-48 h-3 rounded-md" />
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="flex flex-col gap-2">
            <Skeleton className="w-16 h-3 rounded-md" />
            <Skeleton className="w-full h-10 rounded-xl" />
          </div>
        ))}
      </div>

      <Skeleton className="w-full h-12 rounded-xl mt-2" />
    </div>
  )
}

// Skeleton for Live Map Tracking layout
export function MapSkeleton() {
  return (
    <div className="w-full h-[300px] sm:h-[400px] rounded-2xl border border-border bg-card/30 relative overflow-hidden flex flex-col justify-end p-4">
      {/* Simulation loading backdrop pulse */}
      <Skeleton className="absolute inset-0 rounded-none w-full h-full" />
      
      {/* Floating control buttons skeletons */}
      <div className="absolute top-4 right-4 flex flex-col gap-2">
        <Skeleton className="w-8 h-8 rounded-lg" />
        <Skeleton className="w-8 h-8 rounded-lg" />
      </div>

      {/* Floating bottom information badge overlay inside map */}
      <div className="relative z-10 p-4 bg-card/85 backdrop-blur-md rounded-xl border border-border flex items-center justify-between w-full shadow-lg">
        <div className="flex gap-2 items-center">
          <Skeleton className="w-8 h-8 rounded-full" />
          <div className="flex flex-col gap-1.5">
            <Skeleton className="w-24 h-3.5 rounded-md" />
            <Skeleton className="w-16 h-2.5 rounded-md" />
          </div>
        </div>
        <Skeleton className="w-16 h-6 rounded-full" />
      </div>
    </div>
  )
}
export default { CardListSkeleton, ProfileSkeleton, MapSkeleton }
