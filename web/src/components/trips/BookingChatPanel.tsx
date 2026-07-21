import React, { useState, useEffect, useRef } from "react"
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetDescription,
} from "@/components/ui/sheet"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar"
import {
  PaperPlaneRightIcon,
  MapPinIcon,
  ImageIcon,
  PhoneIcon,
  CircleIcon,
  CheckIcon,
  ChecksIcon,
  X as XIcon,
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { chatService } from "@/services/chat.service"
import type { BookingMessage, ConversationParticipant } from "@/services/chat.service"
import api from "@/services/api-client"
import { ImageThumbnail } from "@/components/shared/ImageThumbnail"
import { formatDateTimeIST, formatTime } from "@/lib/utils"

interface BookingChatPanelProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  bookingId: number
  currentUserId: number
}

export function BookingChatPanel({
  open,
  onOpenChange,
  bookingId,
  currentUserId,
}: BookingChatPanelProps) {
  const [messages, setMessages] = useState<BookingMessage[]>([])
  const [participant, setParticipant] = useState<ConversationParticipant | null>(null)
  const [inputText, setInputText] = useState("")
  const [uploading, setUploading] = useState(false)
  const [loading, setLoading] = useState(false)
  const socketRef = useRef<WebSocket | null>(null)
  const scrollRef = useRef<HTMLDivElement | null>(null)

  // Fetch initial chat context
  useEffect(() => {
    if (!open || !bookingId) return

    let isMounted = true

    const loadContext = async () => {
      setLoading(true)
      try {
        const [participantData, messagesData] = await Promise.all([
          chatService.getConversationParticipant(bookingId),
          chatService.getMessages(bookingId),
        ])
        if (isMounted) {
          setParticipant(participantData)
          setMessages(messagesData)
          
          // Mark received messages as read
          const unread = messagesData.filter((m) => !m.is_read && m.sender_id !== currentUserId)
          for (const m of unread) {
            await chatService.markAsRead(m.id)
          }
        }
      } catch (err: any) {
        console.error("Failed to load chat history:", err)
      } finally {
        if (isMounted) setLoading(false)
      }
    }

    loadContext()

    // Establish WebSocket Connection for real-time sync
    const wsProto = window.location.protocol === "https:" ? "wss:" : "ws:"
    const host = window.location.hostname === "localhost" ? "localhost:8000" : window.location.host
    const token = localStorage.getItem("access_token")
    const wsUrl = `${wsProto}//${host}/ws/tracking/${bookingId}?token=${encodeURIComponent(token || "")}`

    const ws = new WebSocket(wsUrl)
    socketRef.current = ws

    ws.onmessage = (event) => {
      try {
        const payload = JSON.parse(event.data)
        if (payload.event === "chat:message") {
          const newMsg: BookingMessage = {
            id: payload.id,
            booking_id: payload.booking_id,
            sender_id: payload.sender_id,
            message_type: payload.message_type as any,
            content: payload.message,
            media_url: payload.media_url,
            latitude: payload.latitude,
            longitude: payload.longitude,
            is_read: false,
            created_at: payload.timestamp,
            sender: {
              id: payload.sender_id,
              full_name: payload.sender_name,
              role: payload.sender_role,
            },
          }

          if (isMounted) {
            setMessages((prev) => {
              if (prev.some((m) => m.id === newMsg.id)) return prev
              return [...prev, newMsg]
            })

            // Auto-mark as read in database
            if (payload.sender_id !== currentUserId) {
              chatService.markAsRead(newMsg.id)
            }
          }
        }
      } catch (e) {
        // Ignore parsing errors for other websocket events
      }
    }

    return () => {
      isMounted = false
      if (ws) ws.close()
    }
  }, [open, bookingId, currentUserId])

  // Scroll to bottom helper
  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollIntoView({ behavior: "smooth" })
    }
  }, [messages])

  const handleSendText = async () => {
    if (!inputText.trim()) return
    const textToSend = inputText.trim()
    setInputText("")

    try {
      const response = await chatService.sendMessage(bookingId, {
        message_type: "text",
        content: textToSend,
      })
      setMessages((prev) => [...prev, response])
    } catch (e) {
      toast.error("Failed to send message")
    }
  }

  const handleShareLocation = () => {
    if (!navigator.geolocation) {
      toast.error("Geolocation is not supported by your browser")
      return
    }

    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        try {
          const response = await chatService.sendMessage(bookingId, {
            message_type: "location",
            content: "Shared current location coordinates",
            latitude: pos.coords.latitude.toString(),
            longitude: pos.coords.longitude.toString(),
          })
          setMessages((prev) => [...prev, response])
          toast.success("Location coordinates shared!")
        } catch (e) {
          toast.error("Failed to share location coordinates")
        }
      },
      () => {
        toast.error("Please enable browser location permissions to share your position")
      }
    )
  }

  const handleUploadImage = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    setUploading(true)
    const formData = new FormData()
    formData.append("file", file)

    try {
      const uploadRes = await api.post("/bookings/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      })
      const imageUrl = uploadRes.data.url

      const response = await chatService.sendMessage(bookingId, {
        message_type: "image",
        content: `Sent image: ${file.name}`,
        media_url: imageUrl,
      })

      setMessages((prev) => [...prev, response])
      toast.success("Image attachment sent successfully!")
    } catch (err) {
      toast.error("Image upload failed. Please try again.")
    } finally {
      setUploading(false)
    }
  }

  const handleCall = () => {
    if (participant?.phone_number) {
      window.location.href = `tel:${participant.phone_number}`
    } else {
      toast.info("Calling escort assistant guide...")
    }
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent
        side="right"
        showCloseButton={false}
        className="w-full sm:max-w-md p-0 flex flex-col h-full bg-card text-card-foreground border-l border-border overflow-hidden shadow-2xl focus:outline-none"
      >
        {/* HEADER BAR */}
        <SheetHeader className="p-3.5 border-b border-border bg-card flex flex-row items-center gap-3 space-y-0 shrink-0 select-none">
          <Avatar className="w-10 h-10 border border-border shadow-xs shrink-0">
            <AvatarImage src={participant?.avatar_url} alt={participant?.name} className="object-cover" />
            <AvatarFallback className="font-extrabold text-xs bg-muted text-foreground">
              {participant?.name?.slice(0, 2).toUpperCase() || "GA"}
            </AvatarFallback>
          </Avatar>

          <div className="flex-1 min-w-0 text-left">
            <div className="flex items-center gap-1.5">
              <SheetTitle className="font-extrabold text-sm text-foreground truncate">
                {participant?.name || "Assistant Guide"}
              </SheetTitle>
              {participant?.is_online ? (
                <span className="w-2 h-2 rounded-full bg-emerald-500 block ring-4 ring-emerald-500/20 shrink-0" />
              ) : (
                <span className="w-2 h-2 rounded-full bg-muted-foreground/40 block shrink-0" />
              )}
            </div>
            <SheetDescription className="text-[10px] text-muted-foreground font-bold uppercase tracking-wider mt-0.5">
              {participant?.role === "assistant" ? "Escort Assistant Guide" : "Passenger Guest"}
            </SheetDescription>
          </div>

          {/* Action Buttons: CALL & CLOSE */}
          <div className="flex items-center gap-1.5 shrink-0">
            <Button
              size="icon"
              variant="ghost"
              onClick={handleCall}
              className="w-9 h-9 rounded-full bg-muted hover:bg-muted/80 text-foreground border border-border shrink-0 cursor-pointer"
              title="Call Assistant"
            >
              <PhoneIcon size={16} weight="fill" />
            </Button>

            <Button
              size="icon"
              variant="ghost"
              onClick={() => onOpenChange(false)}
              className="w-9 h-9 rounded-full bg-muted hover:bg-muted/80 text-foreground border border-border shrink-0 cursor-pointer"
              title="Close Chat"
            >
              <XIcon size={16} />
            </Button>
          </div>
        </SheetHeader>

        {/* CHAT MESSAGES SCROLL AREA */}
        <div className="flex-1 min-h-0 w-full overflow-y-auto bg-muted/20 p-4 scrollbar-thin scrollbar-thumb-border">
          {loading ? (
            <div className="flex flex-col items-center justify-center h-full space-y-2 py-20 text-muted-foreground text-xs font-semibold">
              <span className="animate-spin rounded-full h-5 w-5 border-2 border-foreground border-t-transparent" />
              <span>Syncing secure conversation thread...</span>
            </div>
          ) : messages.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full py-20 text-muted-foreground text-xs text-center px-6">
              <div className="p-3 bg-muted rounded-2xl border border-border mb-2.5">
                <CircleIcon size={24} className="text-foreground" />
              </div>
              <span className="font-bold text-foreground text-sm">Secure Chat Initiated</span>
              <span className="mt-1 leading-relaxed text-muted-foreground">
                Messages are encrypted end-to-end and saved securely for this booking.
              </span>
            </div>
          ) : (
            <div className="space-y-3.5 pb-2 text-left">
              {messages.map((msg, index) => {
                const isMe = msg.sender_id === currentUserId
                return (
                  <div
                    key={msg.id || index}
                    className={`flex flex-col max-w-[85%] ${isMe ? "ml-auto items-end" : "mr-auto items-start"}`}
                  >
                    {/* Bubble Content */}
                    <div
                      className={`px-3.5 py-2.5 rounded-2xl text-xs leading-relaxed font-medium shadow-xs ${
                        isMe
                          ? "bg-foreground text-background font-semibold rounded-tr-none"
                          : "bg-card text-card-foreground border border-border/80 rounded-tl-none"
                      }`}
                    >
                      {msg.message_type === "text" && <p>{msg.content}</p>}

                      {msg.message_type === "image" && msg.media_url && (
                        <div className="space-y-1.5 w-52">
                          <ImageThumbnail
                            url={msg.media_url}
                            alt="Attachment"
                            aspectRatio="video"
                            metadata={{
                              title: "Chat Attachment",
                              uploadedAt: formatDateTimeIST(msg.created_at),
                              documentType: "Chat Image",
                              uploadedBy: isMe ? "Owner" : (participant?.name || "Guide"),
                            }}
                          />
                          {msg.content && <p className="text-[10px] opacity-90 mt-1">{msg.content}</p>}
                        </div>
                      )}

                      {msg.message_type === "location" && msg.latitude && msg.longitude && (
                        <div className="space-y-2">
                          <div className="flex items-center gap-1.5 font-bold">
                            <MapPinIcon size={14} weight="fill" />
                            <span>Shared Location Coordinates</span>
                          </div>
                          <a
                            href={`https://www.google.com/maps/search/?api=1&query=${msg.latitude},${msg.longitude}`}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="inline-block bg-muted text-[10px] text-foreground font-bold px-2.5 py-1.5 rounded-lg border border-border hover:bg-muted/80 transition-colors mt-1"
                          >
                            Open in Google Maps
                          </a>
                        </div>
                      )}
                    </div>

                    {/* Meta info (Time & delivery status) */}
                    <div className="flex items-center gap-1 mt-1 text-[9px] text-muted-foreground select-none">
                      <span>{formatTime(msg.created_at)}</span>
                      {isMe && (
                        msg.is_read ? (
                          <ChecksIcon size={12} className="text-emerald-500" />
                        ) : (
                          <CheckIcon size={12} />
                        )
                      )}
                    </div>
                  </div>
                )
              })}
              <div ref={scrollRef} />
            </div>
          )}
        </div>

        {/* INPUT CONTROLLER FOOTER */}
        <div className="p-3 bg-card border-t border-border flex items-center gap-2 shrink-0 z-20">
          <Button
            size="icon"
            variant="ghost"
            onClick={handleShareLocation}
            className="rounded-xl w-9 h-9 bg-muted border border-border text-foreground hover:bg-muted/80 shrink-0 cursor-pointer"
            title="Share Location"
          >
            <MapPinIcon size={16} weight="bold" />
          </Button>

          <label className="rounded-xl w-9 h-9 bg-muted border border-border flex items-center justify-center text-foreground hover:bg-muted/80 shrink-0 cursor-pointer transition-colors" title="Upload Image">
            <ImageIcon size={16} weight="bold" />
            <input
              type="file"
              accept="image/*"
              className="hidden"
              disabled={uploading}
              onChange={handleUploadImage}
            />
          </label>

          <div className="flex-1 relative flex items-center">
            <Input
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") handleSendText()
              }}
              placeholder={uploading ? "Uploading attachment..." : "Type secure message..."}
              disabled={uploading}
              className="rounded-xl pr-10 border-border bg-muted/40 text-foreground placeholder:text-muted-foreground text-xs focus-visible:ring-foreground"
            />
            <Button
              size="icon"
              variant="ghost"
              onClick={handleSendText}
              disabled={uploading || !inputText.trim()}
              className="absolute right-1 w-7 h-7 rounded-lg text-foreground hover:bg-transparent hover:opacity-75 disabled:opacity-30 cursor-pointer"
            >
              <PaperPlaneRightIcon size={15} weight="fill" />
            </Button>
          </div>
        </div>
      </SheetContent>
    </Sheet>
  )
}
export default BookingChatPanel
