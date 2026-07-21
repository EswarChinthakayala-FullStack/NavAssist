import React, { useEffect, useState } from "react"
import Lightbox from "yet-another-react-lightbox"
import Zoom from "yet-another-react-lightbox/plugins/zoom"
import Download from "yet-another-react-lightbox/plugins/download"
import Inline from "yet-another-react-lightbox/plugins/inline"
import "yet-another-react-lightbox/styles.css"

import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog"
import { MobileImageDrawer } from "./MobileImageDrawer"
import { ImageInfoPanel } from "./ImageInfoPanel"
import type { ImageMetadata } from "./ImageInfoPanel"
import { ImageDownloadButton } from "./ImageDownloadButton"
import { ImageToolbar } from "./ImageToolbar"
import { getFullImageUrl } from "./ImageThumbnail"

interface ImageViewerProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  url?: string
  urls?: string[]
  initialIndex?: number
  metadata?: ImageMetadata
}

// Custom hook to detect mobile screen width
function useMediaQuery(query: string) {
  const [matches, setMatches] = useState(false)
  useEffect(() => {
    const media = window.matchMedia(query)
    setMatches(media.matches)
    const listener = (e: MediaQueryListEvent) => setMatches(e.matches)
    media.addEventListener("change", listener)
    return () => media.removeEventListener("change", listener)
  }, [query])
  return matches
}

export function ImageViewer({
  open,
  onOpenChange,
  url,
  urls,
  initialIndex = 0,
  metadata,
}: ImageViewerProps) {
  const isMobile = useMediaQuery("(max-width: 768px)")
  const [index, setIndex] = useState(initialIndex)
  const [rotation, setRotation] = useState(0)

  useEffect(() => {
    if (open) {
      setIndex(initialIndex)
      setRotation(0) // Reset rotation when opening
    }
  }, [open, initialIndex])

  const slides = urls ? urls.map((u) => ({ src: getFullImageUrl(u) })) : url ? [{ src: getFullImageUrl(url) }] : []

  // Ensure lock body scroll is active when lightbox is open
  useEffect(() => {
    if (open) {
      document.body.style.overflow = "hidden"
    } else {
      document.body.style.overflow = ""
    }
    return () => {
      document.body.style.overflow = ""
    }
  }, [open])

  // Custom filename resolver
  const getFilename = () => {
    const currentUrl = urls?.[index] || url || ""
    const parts = currentUrl.split("/")
    return parts[parts.length - 1] || "download.png"
  }

  const currentMetadata: ImageMetadata | undefined = metadata
    ? {
        ...metadata,
        title: urls && urls.length > 1 ? `${metadata.title || "Image"} (${index + 1} / ${urls.length})` : metadata.title,
      }
    : undefined

  const renderLightbox = () => (
    <div className="w-full h-full relative flex-1 bg-black flex flex-col overflow-hidden text-white select-none [&_.yarl\_\_toolbar]:!hidden [&_.yarl\_\_container]:!bg-black">
      {/* TOP HEADER BAR */}
      <div className="w-full bg-zinc-950/90 border-b border-zinc-900 px-4 py-2.5 flex items-center justify-between z-50 shrink-0 gap-3 backdrop-blur-md">
        {/* Title / Counter */}
        <div className="flex items-center gap-2 min-w-0">
          <h3 className="font-extrabold text-xs text-white uppercase tracking-wider truncate">
            {currentMetadata?.title || "Image Viewer"}
          </h3>
          {slides.length > 1 && (
            <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-zinc-800 text-zinc-200 border border-zinc-700">
              {index + 1} / {slides.length}
            </span>
          )}
        </div>

        {/* Toolbar Action Triggers */}
        <div className="flex items-center gap-1">
          <ImageToolbar
            onClose={() => onOpenChange(false)}
            onRotateLeft={() => setRotation((r) => r - 90)}
            onRotateRight={() => setRotation((r) => r + 90)}
            onResetZoom={() => setRotation(0)}
          />
        </div>
      </div>

      {/* CENTER IMAGE VIEWPORT */}
      <div className="flex-1 w-full min-h-0 relative bg-black flex items-center justify-center overflow-hidden">
        <Lightbox
          index={index}
          slides={slides}
          on={{ view: ({ index: i }) => {
            setIndex(i)
            setRotation(0) // Reset rotation on slide change
          } }}
          plugins={[Zoom, Download, Inline]}
          zoom={{
            maxZoomPixelRatio: 5,
            scrollToZoom: true,
          }}
          carousel={{
            preload: 2,
          }}
          render={{
            buttonPrev: slides.length <= 1 ? () => null : undefined,
            buttonNext: slides.length <= 1 ? () => null : undefined,
            slide: ({ slide }) => (
              <div className="w-full h-full flex items-center justify-center overflow-hidden p-2 sm:p-4">
                <img
                  src={slide.src}
                  style={{
                    transform: `rotate(${rotation}deg)`,
                    transition: "transform 0.25s cubic-bezier(0.4, 0, 0.2, 1)",
                    maxWidth: "100%",
                    maxHeight: "100%",
                    objectFit: "contain",
                  }}
                  alt=""
                />
              </div>
            )
          }}
        />
      </div>

      {/* BOTTOM METADATA & DOWNLOAD FOOTER BAR */}
      <div className="w-full bg-zinc-950/90 border-t border-zinc-900 px-4 py-2.5 flex flex-wrap items-center justify-between gap-3 z-50 shrink-0 backdrop-blur-md">
        {/* Horizontal metadata bar */}
        <div className="flex-1 min-w-0">
          <ImageInfoPanel metadata={currentMetadata} variant="bar" />
        </div>

        {/* Download Button */}
        {(urls?.[index] || url) && (
          <ImageDownloadButton url={urls?.[index] || url || ""} filename={getFilename()} />
        )}
      </div>
    </div>
  )

  if (isMobile) {
    return (
      <MobileImageDrawer open={open} onOpenChange={onOpenChange}>
        {renderLightbox()}
      </MobileImageDrawer>
    )
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-6xl w-[95vw] h-[90vh] bg-black p-0 border border-zinc-900 rounded-2xl overflow-hidden shadow-2xl flex flex-col focus:outline-none">
        <DialogHeader className="sr-only">
          <DialogTitle>Fullscreen Image Viewer</DialogTitle>
          <DialogDescription>View user uploaded images with advanced controls</DialogDescription>
        </DialogHeader>
        {renderLightbox()}
      </DialogContent>
    </Dialog>
  )
}
export default ImageViewer
