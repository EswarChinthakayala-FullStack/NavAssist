import React, { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import {
  BellIcon,
  CheckIcon,
  CheckSquareOffsetIcon,
  InfoIcon,
  CaretRightIcon,
  SlidersHorizontalIcon,
  StarIcon,
  SirenIcon,
  NavigationArrowIcon,
  CurrencyInrIcon,
  UserIcon,
  UserCheckIcon,
} from "@phosphor-icons/react"
import { notificationsService } from "@/services/notifications.service"
import { toast } from "sonner"
import { EmptyState } from "@/components/feedback/EmptyState"
import { CardListSkeleton } from "@/components/feedback/LoadingSkeletons"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"

interface NotificationItem {
  id: number
  title: string
  message: string
  is_read: boolean
  created_at: string
  data?: {
    route?: string
    booking_id?: number
    rating?: number
    reviewer_name?: string
    review_text?: string
  }
}

function useFcmToken() {
  useEffect(() => {
    const register = async () => {
      try {
        const dummyToken = "fcm_token_web_" + Math.random().toString(36).substr(2, 9).toUpperCase()
        await notificationsService.registerDeviceToken(dummyToken, "web")
      } catch (err) {
        console.warn("Push token registration failure", err)
      }
    }
    register()
  }, [])
}

function formatNotificationTime(dateStr?: string): string {
  if (!dateStr) return ""
  try {
    let s = dateStr.trim()
    if (!s.endsWith("Z") && !/[+-]\d{2}:?\d{2}$/.test(s)) {
      s += "Z"
    }
    const d = new Date(s)
    if (isNaN(d.getTime())) return dateStr
    return d.toLocaleTimeString("en-IN", {
      timeZone: "Asia/Kolkata",
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    }).toLowerCase()
  } catch {
    return dateStr || ""
  }
}

export function NotificationsPage() {
  const navigate = useNavigate()
  const [notifications, setNotifications] = useState<NotificationItem[]>([])
  const [loading, setLoading] = useState(true)
  const [searchFilter, setSearchFilter] = useState("")
  const [statusFilter, setStatusFilter] = useState<"all" | "unread">("all")

  useFcmToken()

  const fetchNotifications = async () => {
    try {
      const res = await notificationsService.listNotifications()
      setNotifications(res || [])
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleMarkRead = async (id: number, route?: string) => {
    try {
      await notificationsService.markAsRead(id)
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, is_read: true } : n))
      )
      toast.success("Notification marked as read")
      
      if (route) {
        navigate(route)
      }
    } catch (err) {
      console.error(err)
    }
  }

  const handleMarkAllRead = async () => {
    try {
      await notificationsService.markAllAsRead()
      setNotifications((prev) => prev.map((n) => ({ ...n, is_read: true })))
      toast.success("All notifications marked as read")
    } catch (err) {
      console.error(err)
      toast.error("Failed to mark notifications as read.")
    }
  }

  useEffect(() => {
    fetchNotifications()
  }, [])

  const groupNotifications = (items: NotificationItem[]) => {
    const today: NotificationItem[] = []
    const yesterday: NotificationItem[] = []
    const earlier: NotificationItem[] = []

    const now = new Date()
    const todayStr = now.toDateString()

    const yesterdayDate = new Date(now)
    yesterdayDate.setDate(yesterdayDate.getDate() - 1)
    const yesterdayStr = yesterdayDate.toDateString()

    items.forEach((item) => {
      let s = item.created_at.trim()
      if (!s.endsWith("Z") && !/[+-]\d{2}:?\d{2}$/.test(s)) {
        s += "Z"
      }
      const itemDate = new Date(s)
      const itemDateStr = itemDate.toDateString()

      if (itemDateStr === todayStr) {
        today.push(item)
      } else if (itemDateStr === yesterdayStr) {
        yesterday.push(item)
      } else {
        earlier.push(item)
      }
    })

    return { today, yesterday, earlier }
  }

  const filteredNotifications = notifications.filter((n) => {
    const title = n?.title || ""
    const message = n?.message || ""
    const reviewer = n?.data?.reviewer_name || ""
    const query = (searchFilter || "").toLowerCase()

    const matchesSearch =
      title.toLowerCase().includes(query) ||
      message.toLowerCase().includes(query) ||
      reviewer.toLowerCase().includes(query)
    const matchesStatus = statusFilter === "all" || !n?.is_read
    return matchesSearch && matchesStatus
  })

  const grouped = groupNotifications(filteredNotifications)

  const getNotificationIcon = (item: NotificationItem) => {
    const titleLower = (item?.title || "").toLowerCase()
    const msgLower = (item?.message || "").toLowerCase()

    if (titleLower.includes("rating") || titleLower.includes("star") || msgLower.includes("rated")) {
      return (
        <div className="p-2.5 rounded-xl bg-amber-500/15 text-amber-500 shrink-0 mt-0.5 shadow-xs">
          <StarIcon size={20} weight="fill" />
        </div>
      )
    }
    if (titleLower.includes("sos") || titleLower.includes("emergency") || titleLower.includes("alert")) {
      return (
        <div className="p-2.5 rounded-xl bg-red-500/15 text-red-500 shrink-0 mt-0.5 shadow-xs">
          <SirenIcon size={20} weight="fill" />
        </div>
      )
    }
    if (titleLower.includes("booking") || titleLower.includes("trip") || titleLower.includes("assigned")) {
      return (
        <div className="p-2.5 rounded-xl bg-primary/15 text-primary shrink-0 mt-0.5 shadow-xs">
          <NavigationArrowIcon size={20} weight="bold" />
        </div>
      )
    }
    if (titleLower.includes("payout") || titleLower.includes("payment") || titleLower.includes("earned")) {
      return (
        <div className="p-2.5 rounded-xl bg-emerald-500/15 text-emerald-500 shrink-0 mt-0.5 shadow-xs">
          <CurrencyInrIcon size={20} weight="bold" />
        </div>
      )
    }

    return (
      <div className={`p-2.5 rounded-xl shrink-0 mt-0.5 shadow-xs ${
        !item.is_read ? "bg-primary/10 text-primary" : "bg-muted text-muted-foreground"
      }`}>
        <InfoIcon size={20} weight="fill" />
      </div>
    )
  }

  const getNotificationRoute = (item: NotificationItem) => {
    if (item.data?.route) return item.data.route

    const titleLower = (item.title || "").toLowerCase()
    const msgLower = (item.message || "").toLowerCase()

    if (
      titleLower.includes("rating") ||
      titleLower.includes("star") ||
      msgLower.includes("rated") ||
      titleLower.includes("completed") ||
      msgLower.includes("completed") ||
      titleLower.includes("payout") ||
      titleLower.includes("payment")
    ) {
      return "/bookings"
    }

    if (item.data?.booking_id) {
      return `/trip/${item.data.booking_id}/tracking`
    }

    return undefined
  }

  const renderGroup = (title: string, list: NotificationItem[]) => {
    if (list.length === 0) return null

    return (
      <div className="space-y-3 text-left">
        <h4 className="text-[10px] font-extrabold uppercase tracking-widest text-muted-foreground pl-1">
          {title} ({list.length})
        </h4>
        <div className="flex flex-col gap-3">
          {list.map((item) => {
            const reviewerName = item.data?.reviewer_name
            const ratingScore = item.data?.rating
            const route = getNotificationRoute(item)

            return (
              <div
                key={item.id}
                onClick={() => handleMarkRead(item.id, route)}
                className={`p-4 rounded-2xl border transition-all relative cursor-pointer hover:shadow-md group ${
                  !item.is_read
                    ? "border-primary/30 bg-card dark:bg-card/90 shadow-sm pl-5"
                    : "border-border/80 bg-card/50 opacity-90 hover:opacity-100"
                }`}
              >
                {/* Unread Accent Bar */}
                {!item.is_read && (
                  <div className="absolute left-0 top-0 bottom-0 w-1 bg-primary rounded-l-2xl" />
                )}

                <div className="flex gap-3.5 items-start">
                  {/* Category Icon */}
                  {getNotificationIcon(item)}

                  {/* Body Content */}
                  <div className="flex-1 min-w-0 text-left space-y-1">
                    <div className="flex justify-between items-start gap-2">
                      <h4 className={`text-xs ${!item.is_read ? "font-black text-foreground" : "font-bold text-foreground/80"}`}>
                        {item.title}
                      </h4>
                      <span className="text-[10px] text-muted-foreground shrink-0 font-semibold font-mono">
                        {formatNotificationTime(item.created_at)}
                      </span>
                    </div>

                    <p className="text-xs leading-relaxed text-muted-foreground font-medium">
                      {item.message}
                    </p>

                    {/* Reviewer / Details Tag */}
                    {(reviewerName || ratingScore) && (
                      <div className="flex items-center gap-2 pt-1 flex-wrap">
                        {reviewerName && (
                          <Badge variant="outline" className="text-[10px] font-bold bg-muted/40 border-border/80 text-foreground flex items-center gap-1 rounded-md px-2 py-0.5">
                            <UserCheckIcon size={12} className="text-primary" />
                            <span>Reviewed by: {reviewerName}</span>
                          </Badge>
                        )}
                        {ratingScore && (
                          <Badge className="text-[10px] font-extrabold bg-amber-500/15 text-amber-600 dark:text-amber-400 border border-amber-500/30 flex items-center gap-1 rounded-md px-2 py-0.5">
                            <StarIcon size={11} weight="fill" />
                            <span>{ratingScore} Stars</span>
                          </Badge>
                        )}
                      </div>
                    )}

                    {/* Action Route Link */}
                    {route && (
                      <div className="pt-1">
                        <span className="text-[10px] font-extrabold text-primary inline-flex items-center gap-1 group-hover:translate-x-0.5 transition-transform">
                          <span>View Details</span>
                          <CaretRightIcon size={12} weight="bold" />
                        </span>
                      </div>
                    )}
                  </div>

                  {/* Mark as read checkmark */}
                  {!item.is_read && (
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation()
                        handleMarkRead(item.id)
                      }}
                      className="p-1.5 hover:bg-primary/10 text-muted-foreground hover:text-primary rounded-xl transition-all self-center cursor-pointer shrink-0"
                      title="Mark as read"
                    >
                      <CheckIcon size={16} weight="bold" />
                    </button>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-6xl mx-auto py-4 space-y-6">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-primary/15 via-primary/5 to-transparent border border-border/80 p-6 rounded-3xl shadow-sm text-left">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <Badge className="bg-primary/20 hover:bg-primary/20 text-primary border-0 text-[10px] px-3.5 py-1 rounded-full font-bold uppercase tracking-wider">
                Alert Feed
              </Badge>
              <Badge variant="outline" className="font-bold text-[10px] uppercase tracking-wider rounded-full">
                Realtime Updates
              </Badge>
            </div>
            <h3 className="font-black text-xl sm:text-2xl tracking-tight text-foreground pt-1">
              Notification Activity Log
            </h3>
            <p className="text-xs text-muted-foreground">
              Updates on escort assignments, guest ratings, payment payouts, and safety alerts.
            </p>
          </div>
          
          {notifications.some((n) => !n.is_read) && (
            <Button
              variant="outline"
              size="sm"
              onClick={handleMarkAllRead}
              className="text-xs border-primary/30 hover:bg-primary/10 text-primary flex items-center gap-1.5 cursor-pointer rounded-xl py-5 px-4 font-bold shadow-xs shrink-0"
            >
              <CheckSquareOffsetIcon size={16} weight="bold" />
              <span>Mark All Read</span>
            </Button>
          )}
        </div>
      </div>

      {/* Grid columns layout */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left column: main notifications list */}
        <div className="lg:col-span-8 space-y-6">
          {loading ? (
            <CardListSkeleton count={4} />
          ) : filteredNotifications.length === 0 ? (
            <EmptyState
              wide={true}
              title={searchFilter ? "No matching alerts" : "All caught up"}
              description={searchFilter ? "Try searching for a different keyword or updating status filters." : "Your notification log is clear. We'll alert you here as new ratings and ride requests arrive."}
              icon={<BellIcon size={40} weight="light" className="text-muted-foreground" />}
            />
          ) : (
            <div className="space-y-6">
              {renderGroup("Today", grouped.today)}
              {renderGroup("Yesterday", grouped.yesterday)}
              {renderGroup("Earlier History", grouped.earlier)}
            </div>
          )}
        </div>

        {/* Right column: filters and stats */}
        <div className="lg:col-span-4 space-y-6 text-left">
          {/* Summary Metrics */}
          <Card className="border border-border/80 shadow-md rounded-2xl overflow-hidden bg-card">
            <CardHeader className="pb-3 border-b border-border/40">
              <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                <InfoIcon size={18} className="text-primary" />
                Summary Metrics
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-5">
              <div className="grid grid-cols-2 gap-3">
                <div className="p-3.5 bg-muted/30 border border-border/80 rounded-xl text-center">
                  <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Unread</span>
                  <h3 className="text-2xl font-black text-primary mt-0.5">{notifications.filter(n => !n.is_read).length}</h3>
                </div>
                <div className="p-3.5 bg-muted/30 border border-border/80 rounded-xl text-center">
                  <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Total</span>
                  <h3 className="text-2xl font-black text-foreground mt-0.5">{notifications.length}</h3>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Quick Filters */}
          <Card className="border border-border/80 shadow-md rounded-2xl overflow-hidden bg-card">
            <CardHeader className="pb-3 border-b border-border/40">
              <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                <SlidersHorizontalIcon size={18} className="text-primary" />
                Quick Filters
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-5 space-y-4">
              <div className="space-y-1">
                <label className="text-[10px] font-extrabold uppercase tracking-wider text-muted-foreground pl-0.5">Search Alerts</label>
                <Input
                  type="text"
                  placeholder="Filter by keyword or guest..."
                  value={searchFilter}
                  onChange={(e) => setSearchFilter(e.target.value)}
                  className="rounded-xl h-10 text-xs font-semibold"
                />
              </div>

              <div className="space-y-1.5 pt-1">
                <label className="text-[10px] font-extrabold uppercase tracking-wider text-muted-foreground pl-0.5">Filter Status</label>
                <div className="flex flex-col gap-2">
                  <Button
                    variant={statusFilter === "all" ? "default" : "outline"}
                    onClick={() => setStatusFilter("all")}
                    className="w-full text-xs font-bold rounded-xl justify-start h-9 cursor-pointer"
                  >
                    All Notifications
                  </Button>
                  <Button
                    variant={statusFilter === "unread" ? "default" : "outline"}
                    onClick={() => setStatusFilter("unread")}
                    className="w-full text-xs font-bold rounded-xl justify-start h-9 cursor-pointer"
                  >
                    Unread Only
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}

export default NotificationsPage
