import React from "react"

export interface ImageMetadata {
  title?: string
  uploadedAt?: string
  resolution?: string
  fileSize?: string
  uploadedBy?: string
  documentType?: string
}

interface ImageInfoPanelProps {
  metadata?: ImageMetadata
  variant?: "card" | "bar"
}

export function ImageInfoPanel({ metadata, variant = "card" }: ImageInfoPanelProps) {
  if (!metadata) return null

  const items = [
    { label: "Document Type", value: metadata.documentType },
    { label: "Uploaded By", value: metadata.uploadedBy },
    { label: "Uploaded", value: metadata.uploadedAt },
    { label: "Resolution", value: metadata.resolution },
    { label: "File Size", value: metadata.fileSize },
  ].filter((item) => item.value)

  if (items.length === 0) return null

  if (variant === "bar") {
    return (
      <div className="flex flex-wrap items-center gap-x-5 gap-y-1.5 text-[11px] text-zinc-400">
        {items.map((item, idx) => (
          <div key={idx} className="flex items-center gap-1.5">
            <span className="text-zinc-500 font-medium">{item.label}:</span>
            <span className="text-zinc-200 font-bold">{item.value}</span>
          </div>
        ))}
      </div>
    )
  }

  return (
    <div className="bg-zinc-950/80 border border-zinc-900 rounded-xl p-3.5 max-w-sm w-full text-left font-sans shadow-xl">
      {metadata.title && (
        <h4 className="font-extrabold text-[12px] text-white uppercase tracking-wider mb-2.5 border-b border-zinc-900 pb-1.5">
          {metadata.title}
        </h4>
      )}
      <div className="space-y-1.5 text-[11px] leading-relaxed">
        {items.map((item, idx) => (
          <div key={idx} className="flex justify-between gap-4">
            <span className="text-zinc-500 font-semibold">{item.label}</span>
            <span className="text-zinc-200 font-bold">{item.value}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
export default ImageInfoPanel
