import React from "react"
import {
  X as Cancel01Icon,
  MagnifyingGlassPlus as ZoomInIcon,
  MagnifyingGlassMinus as ZoomOutIcon,
  ArrowCounterClockwise as RotateLeftIcon,
  ArrowClockwise as RotateRightIcon,
  CornersOut as MaximizeIcon,
  CornersIn as MaximizeAndMinimizeIcon,
} from "@phosphor-icons/react"
import { Button } from "@/components/ui/button"

interface ImageToolbarProps {
  onClose: () => void
  onZoomIn?: () => void
  onZoomOut?: () => void
  onResetZoom?: () => void
  onRotateLeft?: () => void
  onRotateRight?: () => void
  onFitScreen?: () => void
  onActualSize?: () => void
}

export function ImageToolbar({
  onClose,
  onZoomIn,
  onZoomOut,
  onResetZoom,
  onRotateLeft,
  onRotateRight,
  onFitScreen,
  onActualSize,
}: ImageToolbarProps) {
  return (
    <div className="flex items-center gap-1.5 p-2 bg-zinc-950/80 border border-zinc-900 rounded-2xl shadow-2xl backdrop-blur-md shrink-0">
      {onZoomOut && (
        <Button
          size="icon"
          variant="ghost"
          onClick={onZoomOut}
          className="w-9 h-9 text-zinc-400 hover:text-white rounded-xl hover:bg-zinc-900 cursor-pointer"
          title="Zoom Out"
        >
          <ZoomOutIcon size={18} />
        </Button>
      )}
      {onZoomIn && (
        <Button
          size="icon"
          variant="ghost"
          onClick={onZoomIn}
          className="w-9 h-9 text-zinc-400 hover:text-white rounded-xl hover:bg-zinc-900 cursor-pointer"
          title="Zoom In"
        >
          <ZoomInIcon size={18} />
        </Button>
      )}
      {onResetZoom && (
        <Button
          size="icon"
          variant="ghost"
          onClick={onResetZoom}
          className="w-9 h-9 text-zinc-400 hover:text-white rounded-xl hover:bg-zinc-900 cursor-pointer"
          title="Reset Zoom"
        >
          <MaximizeAndMinimizeIcon size={18} />
        </Button>
      )}
      {onRotateLeft && (
        <Button
          size="icon"
          variant="ghost"
          onClick={onRotateLeft}
          className="w-9 h-9 text-zinc-400 hover:text-white rounded-xl hover:bg-zinc-900 cursor-pointer"
          title="Rotate Left"
        >
          <RotateLeftIcon size={18} />
        </Button>
      )}
      {onRotateRight && (
        <Button
          size="icon"
          variant="ghost"
          onClick={onRotateRight}
          className="w-9 h-9 text-zinc-400 hover:text-white rounded-xl hover:bg-zinc-900 cursor-pointer"
          title="Rotate Right"
        >
          <RotateRightIcon size={18} />
        </Button>
      )}
      {onFitScreen && (
        <Button
          size="icon"
          variant="ghost"
          onClick={onFitScreen}
          className="w-9 h-9 text-zinc-400 hover:text-white rounded-xl hover:bg-zinc-900 cursor-pointer"
          title="Fit to Screen"
        >
          <MaximizeIcon size={18} />
        </Button>
      )}

      <span className="w-[1px] h-6 bg-zinc-900 mx-1.5" />

      <Button
        size="icon"
        variant="ghost"
        onClick={onClose}
        className="w-9 h-9 text-zinc-400 hover:text-red-500 rounded-xl hover:bg-red-500/10 cursor-pointer transition-colors"
        title="Close"
      >
        <Cancel01Icon size={18} />
      </Button>
    </div>
  )
}
export default ImageToolbar
