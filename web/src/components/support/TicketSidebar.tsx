import React from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  ClockIcon,
  UserCircleIcon,
  ShieldCheckIcon,
  ArrowCircleRightIcon
} from "@phosphor-icons/react"

interface User {
  id: number
  full_name: string
  email?: string
  phone?: string
  role: string
}

interface Ticket {
  id: number
  user_id: number
  subject: string
  description: string
  status: string
  priority: string
  created_at: string
  updated_at: string
  user?: User
}

interface TicketSidebarProps {
  ticket: Ticket
  userRole?: string
  currentUserId?: number
  onStatusChange: (status: string) => void
  formatISTDateTime: (dateStr: string) => string
}

export function TicketSidebar({
  ticket,
  userRole,
  currentUserId,
  onStatusChange,
  formatISTDateTime
}: TicketSidebarProps) {
  const isEditable = userRole === "admin" || ticket.user_id === currentUserId
  const statusList = ["open", "in_progress", "resolved", "closed"]

  return (
    <div className="space-y-5 text-left">
      {/* Ticket Properties & Status Card */}
      <Card className="rounded-2xl border border-border bg-card dark:bg-zinc-900/60 shadow-sm overflow-hidden">
        <CardHeader className="border-b border-border bg-muted/30 dark:bg-zinc-950/40 p-4">
          <CardTitle className="text-xs font-black uppercase tracking-wider text-muted-foreground dark:text-zinc-400 flex items-center gap-2">
            <ClockIcon size={16} className="text-primary" />
            Ticket Properties
          </CardTitle>
        </CardHeader>
        <CardContent className="p-4 space-y-4 text-xs">
          <div className="space-y-1.5">
            <span className="text-muted-foreground font-bold block text-[10px] uppercase tracking-wider">Ticket Priority</span>
            <Badge variant="outline" className={`font-black text-[9px] uppercase rounded-full ${
              ticket.priority === "high" 
                ? "bg-destructive/10 text-destructive border-destructive/25" 
                : ticket.priority === "medium" 
                ? "bg-warning/10 text-warning border-warning/25" 
                : "bg-muted text-muted-foreground border-border"
            }`}>
              {ticket.priority || "medium"}
            </Badge>
          </div>

          {/* Segmented Status Pills */}
          <div className="space-y-2">
            <span className="text-muted-foreground font-bold block text-[10px] uppercase tracking-wider">Status Flow</span>
            {isEditable ? (
              <div className="grid grid-cols-2 gap-1.5 p-1.5 bg-muted dark:bg-zinc-950 rounded-xl border border-border dark:border-zinc-800">
                {statusList.map((st) => {
                  const active = ticket.status === st
                  return (
                    <button
                      key={st}
                      type="button"
                      onClick={() => onStatusChange(st)}
                      className={`relative py-1.5 px-2 rounded-lg text-[10px] font-black uppercase tracking-wider transition-all cursor-pointer text-center ${
                        active
                          ? "bg-primary text-primary-foreground shadow-xs"
                          : "text-muted-foreground hover:text-foreground hover:bg-background dark:hover:bg-zinc-900"
                      }`}
                    >
                      {st.replace("_", " ")}
                    </button>
                  )
                })}
              </div>
            ) : (
              <span className="font-bold text-foreground capitalize block text-sm">{ticket.status}</span>
            )}
          </div>

          <div className="space-y-1">
            <span className="text-muted-foreground font-bold block text-[10px] uppercase tracking-wider">Opened At</span>
            <span className="text-foreground font-mono block text-xs">{formatISTDateTime(ticket.created_at)}</span>
          </div>
        </CardContent>
      </Card>

      {/* Customer Card */}
      {ticket.user && (
        <Card className="rounded-2xl border border-border bg-card dark:bg-zinc-900/60 shadow-sm overflow-hidden">
          <CardHeader className="border-b border-border bg-muted/30 dark:bg-zinc-950/40 p-4">
            <CardTitle className="text-xs font-black uppercase tracking-wider text-muted-foreground dark:text-zinc-400 flex items-center gap-2">
              <UserCircleIcon size={16} className="text-primary" />
              Customer Information
            </CardTitle>
          </CardHeader>
          <CardContent className="p-4 space-y-3 text-xs">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center text-primary font-bold shrink-0">
                {ticket.user.full_name?.charAt(0) || "U"}
              </div>
              <div className="min-w-0">
                <span className="font-extrabold text-foreground block truncate">{ticket.user.full_name}</span>
                <span className="text-muted-foreground font-mono text-[10px] block truncate">{ticket.user.email || ticket.user.phone}</span>
                <div className="flex items-center gap-1.5 mt-1">
                  <Badge variant="outline" className="text-[8px] font-black uppercase tracking-wider rounded-full px-2 py-0.2 border-border bg-muted text-foreground">
                    {ticket.user.role}
                  </Badge>
                  <span className="text-[9px] text-emerald-500 font-semibold flex items-center gap-0.5">
                    <ShieldCheckIcon size={12} /> Verified
                  </span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* GitHub-style Vertical Audit Timeline */}
      <Card className="rounded-2xl border border-border bg-card dark:bg-zinc-900/60 shadow-sm overflow-hidden">
        <CardHeader className="border-b border-border bg-muted/30 dark:bg-zinc-950/40 p-4">
          <CardTitle className="text-xs font-black uppercase tracking-wider text-muted-foreground dark:text-zinc-400 flex items-center gap-2">
            <ArrowCircleRightIcon size={16} className="text-primary" />
            Audit Event Timeline
          </CardTitle>
        </CardHeader>
        <CardContent className="p-4 text-xs">
          <div className="relative pl-4 border-l-2 border-border dark:border-zinc-800 space-y-4">
            <div className="relative">
              <span className="absolute -left-[21px] top-0.5 w-2.5 h-2.5 rounded-full bg-emerald-500 border-2 border-background" />
              <p className="font-bold text-foreground">Ticket Opened</p>
              <p className="text-[10px] text-muted-foreground font-mono">{formatISTDateTime(ticket.created_at)}</p>
            </div>

            {ticket.updated_at !== ticket.created_at && (
              <div className="relative">
                <span className="absolute -left-[21px] top-0.5 w-2.5 h-2.5 rounded-full bg-amber-500 border-2 border-background" />
                <p className="font-bold text-foreground">Status Updated to {ticket.status}</p>
                <p className="text-[10px] text-muted-foreground font-mono">{formatISTDateTime(ticket.updated_at)}</p>
              </div>
            )}

            <div className="relative">
              <span className="absolute -left-[21px] top-0.5 w-2.5 h-2.5 rounded-full bg-primary border-2 border-background" />
              <p className="font-bold text-foreground">Active Support Thread</p>
              <p className="text-[10px] text-muted-foreground">Live communication channel open</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
