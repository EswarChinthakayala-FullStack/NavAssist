import React from "react"
import { Skeleton } from "@/components/ui/skeleton"

export function ImageSkeleton() {
  return (
    <div className="w-full h-full min-h-[100px] flex items-center justify-center bg-zinc-900 rounded-lg">
      <Skeleton className="w-full h-full rounded-lg animate-pulse bg-zinc-800" />
    </div>
  )
}
export default ImageSkeleton
