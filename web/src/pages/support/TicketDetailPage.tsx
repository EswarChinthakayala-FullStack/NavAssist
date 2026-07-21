import React, { useState, useEffect, useRef, useMemo } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { supportService } from "@/services/support.service"
import { useAuth } from "@/store/auth-context"
import { Card, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger
} from "@/components/ui/sheet"
import {
  ArrowLeftIcon,
  ChatCircleDotsIcon,
  SlidersIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"
import api from "@/services/api-client"
import { ImageThumbnail } from "@/components/shared/ImageThumbnail"

import { MessageGroup } from "@/components/support/MessageGroup"
import { MessageComposer } from "@/components/support/MessageComposer"
import { TicketSidebar } from "@/components/support/TicketSidebar"
import { EmptyConversation } from "@/components/support/EmptyConversation"

const formatISTDateTime = (dateStr: string) => {
  if (!dateStr) return ""
  const cleanStr = !dateStr.endsWith("Z") && !dateStr.includes("+") ? dateStr.replace(" ", "T") + "Z" : dateStr
  return new Date(cleanStr).toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' })
}

const formatDateHeader = (dateStr: string) => {
  if (!dateStr) return ""
  const cleanStr = !dateStr.endsWith("Z") && !dateStr.includes("+") ? dateStr.replace(" ", "T") + "Z" : dateStr
  const date = new Date(cleanStr)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (date.toDateString() === today.toDateString()) return "Today"
  if (date.toDateString() === yesterday.toDateString()) return "Yesterday"
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

interface AttachmentItem {
  url: string
  previewUrl?: string
}

interface GroupedMessageCluster {
  id: string
  sender: any
  isViewer: boolean
  isTicketCreator: boolean
  messages: any[]
}

interface DateGroup {
  dateLabel: string
  clusters: GroupedMessageCluster[]
}

export function TicketDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const ticketId = id ? parseInt(id, 10) : NaN

  const [ticket, setTicket] = useState<any>(null)
  const [messages, setMessages] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  
  const [replyMessage, setReplyMessage] = useState("")
  const [attachments, setAttachments] = useState<AttachmentItem[]>([])
  const [sending, setSending] = useState(false)
  const [uploadingImage, setUploadingImage] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement | null>(null)

  const handleUploadTicketImage = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    if (!file.type.startsWith("image/")) {
      toast.error("Please select a valid image file (PNG, JPG, WEBP, GIF, SVG).")
      return
    }

    const localObjectUrl = URL.createObjectURL(file)
    setUploadingImage(true)
    try {
      const formData = new FormData()
      formData.append("file", file)
      const res = await api.post("/bookings/upload", formData)
      const serverUrl = res.data.url
      setAttachments((prev) => [...prev, { url: serverUrl, previewUrl: localObjectUrl }])
      toast.success("Image attachment added to response!")
    } catch (err: any) {
      toast.error(err?.response?.data?.detail || "Failed to upload image attachment.")
    } finally {
      setUploadingImage(false)
      if (e.target) e.target.value = ""
    }
  }

  const renderContentWithImages = (content: string) => {
    if (!content) return null
    const urlRegex = /(https?:\/\/[^\s]+|\/static\/[^\s]+)/gi
    const rawMatches = content.match(urlRegex) || []
    
    const imageUrls: string[] = []
    rawMatches.forEach((m) => {
      let cleanUrl = m.replace(/^[-\s]+/, "").trim()
      cleanUrl = cleanUrl.replace(/[.,;)\s'"]+$/, "")
      if (/\.(png|jpg|jpeg|webp|gif|svg)$/i.test(cleanUrl) || cleanUrl.includes("/static/")) {
        if (!imageUrls.includes(cleanUrl)) imageUrls.push(cleanUrl)
      }
    })

    const textLines = content.split("\n").filter((line) => {
      const trimmed = line.trim()
      if (trimmed.startsWith("- /static/") || trimmed.startsWith("[Attachment] /static/") || (trimmed.includes("/static/") && trimmed.length < 120)) {
        return false
      }
      return true
    }).join("\n").trim()

    return (
      <div className="space-y-2.5">
        {textLines && <p className="text-xs sm:text-sm leading-relaxed whitespace-pre-wrap">{textLines}</p>}
        {imageUrls.length > 0 && (
          <div className="flex flex-wrap gap-3 pt-1.5">
            {imageUrls.map((url, idx) => (
              <div key={idx} className="w-56 sm:w-64 max-w-full rounded-2xl overflow-hidden border border-border dark:border-zinc-700/60 shadow-md shrink-0 bg-card dark:bg-zinc-950">
                <ImageThumbnail
                  url={url}
                  alt="Evidence Attachment"
                  aspectRatio="video"
                  metadata={{
                    title: "Ticket Attachment",
                    documentType: "Ticket Evidence Document",
                  }}
                />
              </div>
            ))}
          </div>
        )}
      </div>
    )
  }

  const fetchDetails = async () => {
    if (isNaN(ticketId)) return
    try {
      const res = await supportService.getTicketDetails(ticketId)
      setTicket(res.ticket)
      setMessages(res.messages || [])
    } catch (err) {
      toast.error("Failed to load support ticket details.")
      navigate("/support")
    } finally {
      setLoading(false)
    }
  }

  const handleSendReply = async (e?: React.FormEvent) => {
    if (e) e.preventDefault()
    if ((!replyMessage.trim() && attachments.length === 0) || sending || isNaN(ticketId)) return

    let finalContent = replyMessage.trim()
    if (attachments.length > 0) {
      const attachmentText = attachments.map((item) => `[Attachment] ${item.url}`).join("\n")
      finalContent = finalContent ? `${finalContent}\n\n${attachmentText}` : attachmentText
    }

    setSending(true)
    try {
      const newMsg = await supportService.postTicketMessage(ticketId, finalContent)
      setMessages((prev) => [...prev, newMsg])
      setReplyMessage("")
      setAttachments([])
      
      if (ticket?.status === "closed" || ticket?.status === "resolved") {
        fetchDetails()
      }
    } catch (err) {
      toast.error("Failed to deliver message thread response.")
    } finally {
      setSending(false)
    }
  }

  const handleStatusChange = async (newStatus: string) => {
    if (isNaN(ticketId)) return
    try {
      const updatedTicket = await supportService.updateTicketStatus(ticketId, newStatus)
      setTicket(updatedTicket)
      toast.success(`Ticket status updated to ${newStatus}.`)
    } catch (err) {
      toast.error("Failed to update ticket status.")
    }
  }

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }, [messages])

  useEffect(() => {
    fetchDetails()
  }, [ticketId])

  const dateGroups: DateGroup[] = useMemo(() => {
    if (!ticket) return []

    const initialMsg = {
      id: -1,
      ticket_id: ticket.id,
      sender_id: ticket.user_id,
      message: ticket.description,
      created_at: ticket.created_at,
      sender: ticket.user
    }

    const allMsgs = [initialMsg, ...messages]
    const dateMap = new Map<string, GroupedMessageCluster[]>()

    allMsgs.forEach((msg) => {
      const dHeader = formatDateHeader(msg.created_at) || "Conversation History"
      if (!dateMap.has(dHeader)) {
        dateMap.set(dHeader, [])
      }

      const clusters = dateMap.get(dHeader)!
      const lastCluster = clusters[clusters.length - 1]

      const isViewer = msg.sender_id === user?.id
      const isTicketCreator = msg.sender_id === ticket.user_id

      const msgTime = new Date(msg.created_at).getTime()
      const lastMsgTime = lastCluster && lastCluster.messages.length > 0
        ? new Date(lastCluster.messages[lastCluster.messages.length - 1].created_at).getTime()
        : 0

      if (
        lastCluster &&
        lastCluster.sender?.id === msg.sender_id &&
        Math.abs(msgTime - lastMsgTime) < 300000
      ) {
        lastCluster.messages.push(msg)
      } else {
        clusters.push({
          id: `cluster-${msg.id}-${msg.created_at}`,
          sender: msg.sender,
          isViewer,
          isTicketCreator,
          messages: [msg]
        })
      }
    })

    return Array.from(dateMap.entries()).map(([dateLabel, clusters]) => ({
      dateLabel,
      clusters
    }))
  }, [ticket, messages, user?.id])

  const getTicketStatusBadge = (status: string) => {
    const s = status?.toLowerCase() || ""
    if (s === "open") {
      return (
        <Badge className="bg-emerald-500/10 text-emerald-500 border-emerald-500/20 text-[10px] px-3 py-0.5 rounded-full font-bold uppercase tracking-wider">
          Active Thread
        </Badge>
      )
    }
    if (s === "in_progress") {
      return (
        <Badge className="bg-amber-500/10 text-amber-500 border-amber-500/20 text-[10px] px-3 py-0.5 rounded-full font-bold uppercase tracking-wider">
          Under Review
        </Badge>
      )
    }
    return (
      <Badge className="bg-muted text-muted-foreground border border-border text-[10px] px-3 py-0.5 rounded-full font-bold uppercase tracking-wider">
        Closed
      </Badge>
    )
  }

  if (loading) {
    return (
      <div className="min-h-[450px] w-full flex items-center justify-center">
        <div className="flex flex-col items-center gap-3 text-muted-foreground">
          <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
          <span className="text-xs font-semibold">Loading support thread...</span>
        </div>
      </div>
    )
  }

  if (!ticket) {
    return (
      <div className="min-h-[450px] w-full flex flex-col items-center justify-center gap-4 text-center">
        <ChatCircleDotsIcon size={48} className="text-muted-foreground/50" />
        <h3 className="font-extrabold text-lg text-foreground">Support Ticket Not Found</h3>
        <Button onClick={() => navigate("/support")} className="rounded-xl font-bold text-xs bg-primary text-primary-foreground">
          Back to Support Hub
        </Button>
      </div>
    )
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-7xl mx-auto px-4 md:px-6 py-4 space-y-5 text-left"
    >
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border pb-4">
        <div className="flex items-center gap-3 min-w-0">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate("/support")}
            className="rounded-xl border border-border shrink-0 hover:bg-muted text-foreground"
          >
            <ArrowLeftIcon size={16} />
          </Button>
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <span className="text-xs font-mono font-black text-primary">#TKT-{ticket.id}</span>
              {getTicketStatusBadge(ticket.status)}
            </div>
            <h1 className="text-lg sm:text-xl font-black mt-1 text-foreground tracking-tight truncate">
              {ticket.subject}
            </h1>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Mobile Sheet Trigger for Ticket Details */}
          <div className="lg:hidden">
            <Sheet>
              <SheetTrigger>
                <Button variant="outline" className="rounded-xl text-xs font-bold border-border bg-card text-foreground">
                  <SlidersIcon size={16} className="mr-1.5" /> Ticket Details
                </Button>
              </SheetTrigger>
              <SheetContent side="right" className="bg-background border-border text-foreground p-6 overflow-y-auto w-full sm:max-w-md">
                <SheetHeader className="p-0 pb-4 border-b border-border mb-4">
                  <SheetTitle className="text-foreground font-extrabold text-base">Support Audit Details</SheetTitle>
                </SheetHeader>
                <TicketSidebar
                  ticket={ticket}
                  userRole={user?.role}
                  currentUserId={user?.id}
                  onStatusChange={handleStatusChange}
                  formatISTDateTime={formatISTDateTime}
                />
              </SheetContent>
            </Sheet>
          </div>

          <div className="hidden sm:block text-right text-[10px] text-muted-foreground shrink-0">
            <p className="font-mono">Opened: {formatISTDateTime(ticket.created_at)}</p>
            {ticket.user && (
              <p className="font-bold text-foreground mt-0.5">
                By {ticket.user.full_name} ({ticket.user.role})
              </p>
            )}
          </div>
        </div>
      </div>

      {/* 3-Column Responsive Grid Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Main Conversation Box (8 Columns on Desktop) */}
        <div className="lg:col-span-8 flex flex-col gap-4 min-w-0">
          <Card className="rounded-2xl border border-border bg-card flex flex-col overflow-hidden h-[640px] shadow-md">
            <CardHeader className="bg-muted/30 p-4 border-b border-border shrink-0 flex flex-row items-center justify-between">
              <CardTitle className="text-xs font-black uppercase tracking-wider text-muted-foreground flex items-center gap-2">
                <ChatCircleDotsIcon size={16} className="text-primary" />
                Conversation History
              </CardTitle>
              <Badge variant="outline" className="text-[9px] font-mono uppercase tracking-wider px-2.5 py-0.5 rounded-full border-border bg-muted text-muted-foreground">
                {messages.length + 1} Messages
              </Badge>
            </CardHeader>

            {/* Scrollable Conversation Stream */}
            <div className="flex-1 min-h-0 overflow-y-auto bg-background/50 p-4">
              {dateGroups.length === 0 ? (
                <EmptyConversation />
              ) : (
                <div className="flex flex-col gap-5">
                  {dateGroups.map((group, groupIdx) => (
                    <div key={groupIdx} className="space-y-4">
                      {/* Date Divider */}
                      <div className="relative flex items-center justify-center my-2">
                        <div className="absolute inset-0 flex items-center">
                          <div className="w-full border-t border-border" />
                        </div>
                        <span className="relative px-3 py-1 bg-card border border-border rounded-full text-[9px] font-bold font-mono text-muted-foreground uppercase tracking-widest shadow-xs">
                          {group.dateLabel}
                        </span>
                      </div>

                      {/* Group Clusters */}
                      {group.clusters.map((cluster) => (
                        <MessageGroup
                          key={cluster.id}
                          sender={cluster.sender}
                          messages={cluster.messages}
                          isViewer={cluster.isViewer}
                          isTicketCreator={cluster.isTicketCreator}
                          renderContentWithImages={renderContentWithImages}
                        />
                      ))}
                    </div>
                  ))}
                  <div ref={messagesEndRef} />
                </div>
              )}
            </div>

            {/* ChatGPT Style Floating Input Composer */}
            <MessageComposer
              replyMessage={replyMessage}
              setReplyMessage={setReplyMessage}
              attachments={attachments}
              setAttachments={setAttachments}
              uploadingImage={uploadingImage}
              sending={sending}
              handleUploadTicketImage={handleUploadTicketImage}
              handleSendReply={handleSendReply}
            />
          </Card>
        </div>

        {/* Sidebar Audit Panel (4 Columns on Desktop) */}
        <div className="hidden lg:block lg:col-span-4 sticky top-6 self-start min-w-0">
          <TicketSidebar
            ticket={ticket}
            userRole={user?.role}
            currentUserId={user?.id}
            onStatusChange={handleStatusChange}
            formatISTDateTime={formatISTDateTime}
          />
        </div>
      </div>
    </motion.div>
  )
}
export default TicketDetailPage
