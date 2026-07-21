import React, { useState } from "react"
import { motion, AnimatePresence } from "framer-motion"
import { MagnifyingGlassIcon } from "@phosphor-icons/react"
import { ImageSkeleton } from "./ImageSkeleton"
import { ImageErrorFallback } from "./ImageErrorFallback"
import { ImageViewer } from "./ImageViewer"
import type { ImageMetadata } from "./ImageInfoPanel"
import { API_URL } from "@/services/api-client"

export const getBackendHost = (): string => {
  if (API_URL) {
    const origin = API_URL.replace(/\/api\/v1\/?$/, "")
    if (origin) return origin
  }
  const isLocal = window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1"
  return isLocal ? "http://127.0.0.1:8000" : `${window.location.protocol}//${window.location.host}`
}

export function getFullImageUrl(url: string): string {
  if (!url) return ""
  let clean = url.trim()
  if (clean.startsWith("http://") || clean.startsWith("https://") || clean.startsWith("data:") || clean.startsWith("blob:")) return clean

  const backendHost = getBackendHost()
  
  if (clean.startsWith("/")) {
    return `${backendHost}${clean}`
  }
  if (clean.startsWith("static/")) {
    return `${backendHost}/${clean}`
  }
  return `${backendHost}/static/${clean.replace(/^\/+/, "")}`
}

interface ImageThumbnailProps {
  url: string
  alt?: string
  aspectRatio?: "video" | "square" | "portrait" | "circle"
  metadata?: ImageMetadata
  onClick?: () => void
}

export function ImageThumbnail({
  url,
  alt = "Preview image",
  aspectRatio = "video",
  metadata,
  onClick,
}: ImageThumbnailProps) {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [standaloneOpen, setStandaloneOpen] = useState(false)

  const aspectClass =
    aspectRatio === "circle"
      ? "aspect-square rounded-full"
      : aspectRatio === "square"
      ? "aspect-square rounded-xl"
      : aspectRatio === "portrait"
      ? "aspect-[3/4] rounded-xl"
      : "aspect-video rounded-xl"

  const handleRetry = () => {
    setError(false)
    setLoading(true)
  }

  const handleThumbnailClick = (e: React.MouseEvent) => {
    e.stopPropagation()
    if (onClick) {
      onClick()
    } else {
      setStandaloneOpen(true)
    }
  }

  return (
    <>
      <div className="relative w-full overflow-hidden select-none">
        {error ? (
          <div onClick={handleThumbnailClick} className="cursor-pointer">
            <ImageErrorFallback onRetry={handleRetry} />
          </div>
        ) : (
          <motion.div
            onClick={handleThumbnailClick}
            whileHover={!loading ? { scale: 1.03 } : {}}
            className={`relative w-full ${aspectClass} border border-zinc-800 bg-zinc-950 shadow-sm cursor-pointer overflow-hidden transition-colors duration-200 hover:border-amber-500 hover:shadow-lg`}
          >
            {/* Loading Indicator */}
            {loading && (
              <div className="absolute inset-0 z-10">
                <ImageSkeleton />
              </div>
            )}

            {/* Blur placeholder and actual image */}
            <img
              src={getFullImageUrl(url)}
              alt={alt}
              loading="lazy"
              onLoad={() => setLoading(false)}
              onError={() => {
                setLoading(false)
                setError(true)
              }}
              className={`w-full h-full object-cover transition-all duration-300 ${
                loading ? "blur-md scale-95 opacity-50" : "blur-0 scale-100 opacity-100"
              }`}
            />

            {/* Hover overlay */}
            {!loading && (
              <motion.div
                initial={{ opacity: 0 }}
                whileHover={{ opacity: 1 }}
                transition={{ duration: 0.15 }}
                className="absolute inset-0 bg-black/60 flex flex-col items-center justify-center gap-1.5 z-20"
              >
                <div className="p-2 bg-white/10 rounded-xl text-white border border-white/20 shadow-xs">
                  <MagnifyingGlassIcon size={18} />
                </div>
                <span className="text-[10px] font-black text-white uppercase tracking-wider">
                  View Image
                </span>
              </motion.div>
            )}
          </motion.div>
        )}
      </div>

      {/* Standalone full-screen viewer if click is not handled externally */}
      {!onClick && (
        <ImageViewer
          open={standaloneOpen}
          onOpenChange={setStandaloneOpen}
          url={url}
          metadata={metadata}
        />
      )}
    </>
  )
}
export default ImageThumbnail
