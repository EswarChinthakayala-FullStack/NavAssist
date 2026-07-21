import React, { useState } from "react"
import { DownloadSimpleIcon } from "@phosphor-icons/react"
import { Button } from "@/components/ui/button"
import { toast } from "sonner"
import { getFullImageUrl } from "./ImageThumbnail"

interface ImageDownloadButtonProps {
  url: string
  filename?: string
  className?: string
}

export function ImageDownloadButton({ url, filename = "image.png", className }: ImageDownloadButtonProps) {
  const [downloading, setDownloading] = useState(false)

  const handleDownload = async (e: React.MouseEvent) => {
    e.stopPropagation()
    setDownloading(true)
    try {
      const response = await fetch(getFullImageUrl(url))
      if (!response.ok) throw new Error("Network response was not ok")
      const blob = await response.blob()
      const blobUrl = window.URL.createObjectURL(blob)
      
      const link = document.createElement("a")
      link.href = blobUrl
      link.download = filename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(blobUrl)
      toast.success("Image downloaded successfully!")
    } catch (err) {
      console.error(err)
      // Fallback: Open in new tab
      window.open(url, "_blank")
    } finally {
      setDownloading(false)
    }
  }

  return (
    <Button
      size="sm"
      variant="outline"
      onClick={handleDownload}
      disabled={downloading}
      className={`text-[11px] font-bold px-3 py-1.5 h-8 border-zinc-800 bg-zinc-900/60 hover:bg-zinc-800 text-white rounded-lg flex items-center gap-1.5 cursor-pointer shrink-0 ${className}`}
    >
      <DownloadSimpleIcon size={14} />
      <span>{downloading ? "Downloading..." : "Download"}</span>
    </Button>
  )
}
export default ImageDownloadButton
