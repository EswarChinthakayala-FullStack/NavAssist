import React from "react"
import {
  Drawer,
  DrawerContent,
  DrawerHeader,
  DrawerTitle,
  DrawerDescription,
} from "@/components/ui/drawer"

interface MobileImageDrawerProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  children: React.ReactNode
}

export function MobileImageDrawer({ open, onOpenChange, children }: MobileImageDrawerProps) {
  return (
    <Drawer open={open} onOpenChange={onOpenChange}>
      <DrawerContent className="bg-black border-t border-zinc-900 h-[92vh] flex flex-col p-0 text-white rounded-t-2xl outline-none overflow-hidden">
        <DrawerHeader className="sr-only">
          <DrawerTitle>Image View</DrawerTitle>
          <DrawerDescription>Full screen touch responsive image drawer</DrawerDescription>
        </DrawerHeader>
        <div className="flex-1 flex flex-col justify-between overflow-hidden relative select-none">
          {children}
        </div>
      </DrawerContent>
    </Drawer>
  )
}
export default MobileImageDrawer
