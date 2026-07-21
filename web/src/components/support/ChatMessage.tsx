import React from "react"
import { motion } from "framer-motion"

interface Message {
  id: number
  ticket_id: number
  sender_id: number
  message: string
  created_at: string
}

interface ChatMessageProps {
  message: Message
  isViewer: boolean
  isAdmin: boolean
  renderContentWithImages: (content: string) => React.ReactNode
}

export function ChatMessage({
  message,
  isViewer,
  isAdmin,
  renderContentWithImages
}: ChatMessageProps) {
  const trimmed = (message.message || "").trim()
  const isOnlyAttachment = (trimmed.startsWith("[Attachment]") || trimmed.startsWith("/static/")) && !trimmed.includes("\n")

  if (isOnlyAttachment) {
    return (
      <motion.div
        initial={{ opacity: 0, y: 6 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.15 }}
        className="w-fit max-w-full rounded-2xl overflow-hidden shadow-sm"
      >
        {renderContentWithImages(message.message)}
      </motion.div>
    )
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 6 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.15 }}
      className={`w-fit max-w-full p-3.5 sm:p-4 rounded-2xl shadow-xs text-left transition-all break-words ${
        isViewer
          ? "bg-primary text-primary-foreground border border-primary/30 rounded-tr-xs"
          : isAdmin
          ? "bg-amber-500/10 border border-amber-500/25 text-foreground rounded-tl-xs"
          : "bg-card dark:bg-zinc-900 border border-border dark:border-zinc-800 text-foreground dark:text-zinc-100 rounded-tl-xs shadow-xs"
      }`}
    >
      {renderContentWithImages(message.message)}
    </motion.div>
  )
}
