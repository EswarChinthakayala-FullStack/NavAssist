import React from "react"
import { UserCircleIcon, HeadsetIcon } from "@phosphor-icons/react"
import { ChatMessage } from "./ChatMessage"

interface User {
  id: number
  full_name: string
  email?: string
  role: string
}

interface Message {
  id: number
  ticket_id: number
  sender_id: number
  message: string
  created_at: string
  sender?: User
}

interface MessageGroupProps {
  sender: User | undefined
  messages: Message[]
  isViewer: boolean
  isTicketCreator: boolean
  renderContentWithImages: (content: string) => React.ReactNode
}

const formatISTTime = (dateStr: string) => {
  if (!dateStr) return ""
  const cleanStr = !dateStr.endsWith("Z") && !dateStr.includes("+") ? dateStr.replace(" ", "T") + "Z" : dateStr
  return new Date(cleanStr).toLocaleTimeString('en-IN', { timeZone: 'Asia/Kolkata', hour: '2-digit', minute: '2-digit' })
}

const getRoleLabel = (role?: string) => {
  if (!role) return "User"
  if (role === "admin") return "Support"
  if (role === "assistant") return "Assistant"
  if (role === "guest") return "Guest"
  return role.charAt(0).toUpperCase() + role.slice(1)
}

export function MessageGroup({
  sender,
  messages,
  isViewer,
  isTicketCreator,
  renderContentWithImages
}: MessageGroupProps) {
  if (!messages || messages.length === 0) return null

  const isAdmin = sender?.role === "admin"
  const senderName = sender?.full_name || (isAdmin ? "Support Agent" : "User")
  const firstMsgTime = formatISTTime(messages[0].created_at)

  return (
    <div className={`flex gap-2.5 items-start my-1 ${
      isViewer ? "flex-row-reverse" : "flex-row"
    }`}>
      {/* Avatar */}
      <div className={`w-7 h-7 rounded-full flex items-center justify-center shrink-0 mt-0.5 ${
        isViewer
          ? "bg-primary text-primary-foreground"
          : isAdmin
          ? "bg-amber-500/15 dark:bg-amber-500/20 text-amber-600 dark:text-amber-400"
          : "bg-muted text-muted-foreground"
      }`}>
        {isAdmin ? (
          <HeadsetIcon size={14} weight="fill" />
        ) : (
          <span className="text-[10px] font-bold leading-none">
            {senderName.charAt(0).toUpperCase()}
          </span>
        )}
      </div>

      {/* Message Stack */}
      <div className={`min-w-0 max-w-[80%] sm:max-w-[70%] flex flex-col ${
        isViewer ? "items-end" : "items-start"
      }`}>
        {/* Compact Header: Name · Role · Time */}
        <div className={`flex items-center gap-1.5 mb-1 px-0.5 ${
          isViewer ? "flex-row-reverse" : "flex-row"
        }`}>
          <span className="text-[12px] font-semibold text-foreground leading-none">
            {senderName}
          </span>
          <span className="text-[10px] text-muted-foreground/60 leading-none">·</span>
          <span className={`text-[10px] font-medium leading-none ${
            isAdmin
              ? "text-amber-600 dark:text-amber-400"
              : sender?.role === "assistant"
              ? "text-blue-600 dark:text-blue-400"
              : "text-muted-foreground"
          }`}>
            {getRoleLabel(sender?.role)}
          </span>
          <span className="text-[10px] text-muted-foreground/60 leading-none">·</span>
          <span className="text-[10px] text-muted-foreground/50 font-normal leading-none">
            {firstMsgTime}
          </span>
        </div>

        {/* Message Bubbles */}
        <div className={`space-y-1 flex flex-col ${
          isViewer ? "items-end" : "items-start"
        }`}>
          {messages.map((msg, index) => (
            <ChatMessage
              key={msg.id || index}
              message={msg}
              isViewer={isViewer}
              isAdmin={isAdmin}
              renderContentWithImages={renderContentWithImages}
            />
          ))}
        </div>
      </div>
    </div>
  )
}
