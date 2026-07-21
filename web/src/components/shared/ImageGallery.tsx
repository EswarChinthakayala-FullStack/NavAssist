import React, { useState } from "react"
import { ImageThumbnail } from "./ImageThumbnail"
import { ImageViewer } from "./ImageViewer"
import type { ImageMetadata } from "./ImageInfoPanel"

interface ImageGalleryProps {
  urls: string[]
  metadata?: ImageMetadata
  className?: string
  aspectRatio?: "video" | "square" | "portrait"
}

export function ImageGallery({ urls, metadata, className = "grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3", aspectRatio }: ImageGalleryProps) {
  const [viewerOpen, setViewerOpen] = useState(false)
  const [activeIndex, setActiveIndex] = useState(0)

  if (!urls || urls.length === 0) return null

  const handleOpenViewer = (index: number) => {
    setActiveIndex(index)
    setViewerOpen(true)
  }

  return (
    <>
      <div className={className}>
        {urls.map((url, index) => (
          <ImageThumbnail
            key={url + index}
            url={url}
            alt={metadata?.title ? `${metadata.title} - ${index + 1}` : `Gallery Image ${index + 1}`}
            aspectRatio={aspectRatio}
            onClick={() => handleOpenViewer(index)}
          />
        ))}
      </div>

      <ImageViewer
        open={viewerOpen}
        onOpenChange={setViewerOpen}
        urls={urls}
        initialIndex={activeIndex}
        metadata={metadata}
      />
    </>
  )
}
export default ImageGallery
