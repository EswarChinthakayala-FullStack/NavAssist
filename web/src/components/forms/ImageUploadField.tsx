import React, { useState, useRef } from "react"
import { CloudArrowUpIcon, FileImageIcon, TrashIcon } from "@phosphor-icons/react"
import { cn } from "@/lib/utils"

interface ImageUploadFieldProps {
  label: string
  onChange: (file: File | null) => void
  disabled?: boolean
  className?: string
  accept?: string
}

export function ImageUploadField({
  label,
  onChange,
  disabled = false,
  className,
  accept = "image/*"
}: ImageUploadFieldProps) {
  const [dragActive, setDragActive] = useState(false)
  const [filePreview, setFilePreview] = useState<string | null>(null)
  const [fileName, setFileName] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleFile = (file: File | null) => {
    if (!file) {
      setFilePreview(null)
      setFileName(null)
      onChange(null)
      return
    }

    setFileName(file.name)
    onChange(file)

    // Generate local preview if it's an image
    if (file.type.startsWith("image/")) {
      const url = URL.createObjectURL(file)
      setFilePreview(url)
    } else {
      setFilePreview(null)
    }
  }

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true)
    } else if (e.type === "dragleave") {
      setDragActive(false)
    }
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setDragActive(false)

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFile(e.dataTransfer.files[0])
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    e.preventDefault()
    if (e.target.files && e.target.files[0]) {
      handleFile(e.target.files[0])
    }
  }

  const handleClear = (e: React.MouseEvent) => {
    e.stopPropagation()
    handleFile(null)
    if (fileInputRef.current) {
      fileInputRef.current.value = ""
    }
  }

  return (
    <div className={cn("flex flex-col gap-1.5 w-full", className)}>
      <span className="text-[10px] font-extrabold uppercase tracking-wider text-muted-foreground">
        {label}
      </span>

      <div
        onDragEnter={handleDrag}
        onDragOver={handleDrag}
        onDragLeave={handleDrag}
        onDrop={handleDrop}
        onClick={() => !disabled && fileInputRef.current?.click()}
        className={cn(
          "relative border-2 border-dashed border-border rounded-2xl p-5 flex flex-col items-center justify-center text-center cursor-pointer transition-all duration-200 min-h-[140px] bg-card hover:bg-muted/30 hover:border-primary/50",
          dragActive && "border-primary bg-primary/5",
          disabled && "opacity-50 pointer-events-none bg-muted"
        )}
      >
        <input
          ref={fileInputRef}
          type="file"
          accept={accept}
          onChange={handleChange}
          disabled={disabled}
          className="hidden"
        />

        {filePreview ? (
          <div className="flex flex-col items-center gap-3 w-full">
            <div className="relative group w-24 h-16 rounded-lg overflow-hidden border border-border shadow-inner">
              <img src={filePreview} alt="Upload preview" className="w-full h-full object-cover" />
              <button
                type="button"
                onClick={handleClear}
                className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center text-white cursor-pointer"
              >
                <TrashIcon size={18} weight="fill" />
              </button>
            </div>
            <span className="text-[9px] text-muted-foreground font-semibold truncate max-w-[200px]">
              {fileName}
            </span>
          </div>
        ) : fileName ? (
          <div className="flex flex-col items-center gap-2">
            <FileImageIcon size={28} className="text-primary" />
            <span className="text-xs font-bold text-foreground truncate max-w-[200px]">
              {fileName}
            </span>
            <button
              type="button"
              onClick={handleClear}
              className="text-[10px] text-destructive font-extrabold hover:underline"
            >
              Remove file
            </button>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-2">
            <div className="p-2.5 rounded-full bg-primary/10 text-primary">
              <CloudArrowUpIcon size={24} />
            </div>
            <div>
              <p className="text-xs font-extrabold text-foreground">Click to upload or drag & drop</p>
              <p className="text-[10px] text-muted-foreground mt-1">PNG, JPG, or PDF up to 5MB</p>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
export default ImageUploadField
