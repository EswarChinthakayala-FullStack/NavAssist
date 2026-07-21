import React, { useState, useEffect } from "react"
import { useSearchParams } from "react-router-dom"
import { api } from "@/services/api"
import { toast } from "sonner"
import {
  UsersIcon,
  IdentificationCardIcon,
  TicketIcon,
  ShieldCheckIcon,
  UserCircleIcon,
  CircleIcon,
  CheckCircleIcon,
  XCircleIcon,
  ClockIcon,
  WarningIcon,
  CurrencyDollarIcon,
  ChartBarIcon,
  MagnifyingGlassIcon,
  ActivityIcon,
} from "@phosphor-icons/react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { ImageViewer } from "@/components/shared/ImageViewer"
import { ImageThumbnail } from "@/components/shared/ImageThumbnail"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

interface AdminStats {
  total_registered_users: number
  total_bookings_processed: number
  pending_kyc_reviews: number
  open_tickets_count: number
}

interface UserProfile {
  id: number
  full_name: string
  email: string
  phone_number: string
  role: string
  is_email_verified: boolean
  is_phone_verified: boolean
  status: string
  created_at: string
}

interface AssistantDocument {
  id: number
  doc_type: string
  file_url: string
  verified: boolean
  uploaded_at: string
}

interface AssistantProfile {
  id: number
  user_id: number
  bio: string | null
  experience_years: number
  verification_status: string
  aadhaar_masked: string | null
  trust_score: number
  avg_rating: number
  total_trips: number
  is_online: boolean
  user?: UserProfile
  documents?: AssistantDocument[]
}

export function AdminConsolePage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const activeTab = searchParams.get("tab") || "overview"

  const getDocUrl = (fileUrl: string) => {
    if (!fileUrl) return ""
    if (fileUrl.startsWith("http")) return fileUrl
    if (fileUrl.startsWith("/static")) return `http://localhost:8000${fileUrl}`
    const safeFilename = fileUrl.replace("/", "_")
    return `http://localhost:8000/static/${safeFilename}`
  }

  // Overview stats
  const [stats, setStats] = useState<AdminStats | null>(null)
  const [statsLoading, setStatsLoading] = useState(false)

  // Users management
  const [users, setUsers] = useState<UserProfile[]>([])
  const [usersLoading, setUsersLoading] = useState(false)
  const [userSearch, setUserSearch] = useState("")

  // KYC queue
  const [kycQueue, setKycQueue] = useState<AssistantProfile[]>([])
  const [kycLoading, setKycLoading] = useState(false)
  const [selectedKyc, setSelectedKyc] = useState<AssistantProfile | null>(null)
  
  // Rejection modal
  const [isRejectOpen, setIsRejectOpen] = useState(false)
  const [rejectReason, setRejectReason] = useState("")
  const [rejectingId, setRejectingId] = useState<number | null>(null)

  // Image viewer dialog states
  const [isViewerOpen, setIsViewerOpen] = useState(false)
  const [viewerImage, setViewerImage] = useState("")
  const [viewerTitle, setViewerTitle] = useState("")

  // Fetch Stats
  const fetchStats = async () => {
    setStatsLoading(true)
    try {
      const res = await api.get("/admin/dashboard/stats")
      setStats(res.data)
    } catch (err) {
      toast.error("Failed to load dashboard metrics.")
    } finally {
      setStatsLoading(false)
    }
  }

  // Fetch Users
  const fetchUsers = async () => {
    setUsersLoading(true)
    try {
      const res = await api.get("/admin/users")
      setUsers(res.data)
    } catch (err) {
      toast.error("Failed to load users database.")
    } finally {
      setUsersLoading(false)
    }
  }

  // Fetch KYC Queue
  const fetchKycQueue = async () => {
    setKycLoading(true)
    try {
      const res = await api.get("/admin/assistants/pending-kyc")
      setKycQueue(res.data)
      // Automatically select first item if none is selected
      if (res.data.length > 0) {
        setSelectedKyc(res.data[0])
      } else {
        setSelectedKyc(null)
      }
    } catch (err) {
      toast.error("Failed to fetch pending KYC queue.")
    } finally {
      setKycLoading(false)
    }
  }

  useEffect(() => {
    if (activeTab === "overview") {
      fetchStats()
    } else if (activeTab === "users") {
      fetchUsers()
    } else if (activeTab === "kyc") {
      fetchKycQueue()
    }
  }, [activeTab])

  // Update user status
  const handleUpdateUserStatus = async (userId: number, newStatus: string) => {
    try {
      await api.patch(`/admin/users/${userId}/status`, { status: newStatus })
      toast.success("User account status updated successfully.")
      fetchUsers() // Reload user list
      fetchStats() // Update user stats count
    } catch (err) {
      toast.error("Failed to update user status.")
    }
  }

  // Approve KYC application
  const handleApproveKyc = async (assistantId: number) => {
    try {
      await api.patch(`/kyc/admin/${assistantId}/approve`)
      toast.success("KYC application approved successfully.")
      fetchKycQueue() // Refresh queue
      fetchStats() // Update count
    } catch (err) {
      toast.error("Failed to approve KYC application.")
    }
  }

  // Open Rejection Dialog
  const openRejectionDialog = (assistantId: number) => {
    setRejectingId(assistantId)
    setRejectReason("")
    setIsRejectOpen(true)
  }

  // Submit Rejection
  const handleRejectKyc = async () => {
    if (!rejectingId) return
    if (!rejectReason || rejectReason.trim().length < 3) {
      toast.error("Please provide a valid reason (minimum 3 characters) for rejection.")
      return
    }

    try {
      await api.patch(`/kyc/admin/${rejectingId}/reject`, { reason: rejectReason })
      toast.success("KYC application rejected successfully.")
      setIsRejectOpen(false)
      fetchKycQueue()
      fetchStats()
    } catch (err) {
      toast.error("Failed to reject KYC application.")
    }
  }

  // Filter users based on search query
  const filteredUsers = users.filter((u) => {
    const q = userSearch.toLowerCase()
    return (
      u.full_name?.toLowerCase().includes(q) ||
      u.email?.toLowerCase().includes(q) ||
      u.phone_number?.includes(q) ||
      u.role?.toLowerCase().includes(q)
    );
  })

  const setTab = (tabName: string) => {
    setSearchParams({ tab: tabName })
  }

  return (
    <div className="flex flex-col gap-6 h-full w-full max-w-7xl mx-auto p-1 animate-fade-in">
      {/* Header section with styling */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b pb-4">
        <div>
          <h1 className="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-primary to-purple-400 bg-clip-text text-transparent">
            Administration Portal
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Perform KYC audits, manage user accounts, and review platform engagement metrics.
          </p>
        </div>

        {/* Tab switch buttons */}
        <div className="flex items-center gap-1.5 bg-card/60 p-1 rounded-xl border self-start md:self-auto">
          <Button
            variant={activeTab === "overview" ? "default" : "ghost"}
            size="sm"
            onClick={() => setTab("overview")}
            className="rounded-lg font-bold text-xs"
          >
            <ChartBarIcon className="mr-1.5" size={15} />
            Overview
          </Button>
          <Button
            variant={activeTab === "users" ? "default" : "ghost"}
            size="sm"
            onClick={() => setTab("users")}
            className="rounded-lg font-bold text-xs"
          >
            <UsersIcon className="mr-1.5" size={15} />
            Users
          </Button>
          <Button
            variant={activeTab === "kyc" ? "default" : "ghost"}
            size="sm"
            onClick={() => setTab("kyc")}
            className="rounded-lg font-bold text-xs relative"
          >
            <ShieldCheckIcon className="mr-1.5" size={15} />
            KYC Queue
            {stats && stats.pending_kyc_reviews > 0 && (
              <span className="absolute -top-1 -right-1 flex h-4 w-4 items-center justify-center rounded-full bg-destructive text-[9px] font-bold text-destructive-foreground animate-pulse shadow-sm">
                {stats.pending_kyc_reviews}
              </span>
            )}
          </Button>
        </div>
      </div>

      {/* OVERVIEW TAB */}
      {activeTab === "overview" && (
        <div className="flex flex-col gap-6">
          {statsLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              {[1, 2, 3, 4].map((i) => (
                <div key={i} className="h-32 rounded-2xl bg-card/50 border animate-pulse" />
              ))}
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
              {/* Card 1 */}
              <Card className="rounded-2xl border bg-card/50 backdrop-blur-sm relative overflow-hidden group hover:shadow-md transition-all">
                <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
                  <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
                    Registered Users
                  </CardTitle>
                  <UsersIcon size={24} className="text-primary opacity-80" weight="duotone" />
                </CardHeader>
                <CardContent>
                  <div className="text-3xl font-extrabold">{stats?.total_registered_users ?? 0}</div>
                  <p className="text-[10px] text-muted-foreground mt-1">Platform user accounts</p>
                </CardContent>
              </Card>

              {/* Card 2 */}
              <Card className="rounded-2xl border bg-card/50 backdrop-blur-sm relative overflow-hidden group hover:shadow-md transition-all">
                <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
                  <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
                    Bookings Processed
                  </CardTitle>
                  <TicketIcon size={24} className="text-secondary opacity-80" weight="duotone" />
                </CardHeader>
                <CardContent>
                  <div className="text-3xl font-extrabold">{stats?.total_bookings_processed ?? 0}</div>
                  <p className="text-[10px] text-muted-foreground mt-1">Total trips registered</p>
                </CardContent>
              </Card>

              {/* Card 3 */}
              <Card className="rounded-2xl border bg-card/50 backdrop-blur-sm relative overflow-hidden group hover:shadow-md transition-all">
                <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
                  <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
                    Pending KYC Reviews
                  </CardTitle>
                  <ShieldCheckIcon size={24} className="text-warning opacity-80" weight="duotone" />
                </CardHeader>
                <CardContent>
                  <div className="text-3xl font-extrabold text-warning">
                    {stats?.pending_kyc_reviews ?? 0}
                  </div>
                  <p className="text-[10px] text-muted-foreground mt-1">Awaiting identity approval</p>
                </CardContent>
              </Card>

              {/* Card 4 */}
              <Card className="rounded-2xl border bg-card/50 backdrop-blur-sm relative overflow-hidden group hover:shadow-md transition-all">
                <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
                  <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
                    Open Support Tickets
                  </CardTitle>
                  <ActivityIcon size={24} className="text-destructive opacity-80" weight="duotone" />
                </CardHeader>
                <CardContent>
                  <div className="text-3xl font-extrabold text-destructive">
                    {stats?.open_tickets_count ?? 0}
                  </div>
                  <p className="text-[10px] text-muted-foreground mt-1">Awaiting support resolution</p>
                </CardContent>
              </Card>
            </div>
          )}

          {/* Quick Info Grid */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Card className="col-span-2 rounded-2xl border bg-card/50">
              <CardHeader>
                <CardTitle className="text-base font-bold">Admin Activity Console</CardTitle>
                <CardDescription>Verify guides, configure service settings, and access audits.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="p-4 rounded-xl border bg-background/50 flex flex-col gap-2">
                  <h4 className="text-xs font-bold flex items-center gap-1.5 text-primary">
                    <CheckCircleIcon size={16} /> KYC Verification Checklist
                  </h4>
                  <p className="text-xs text-muted-foreground leading-relaxed">
                    Make sure to cross-examine uploaded Aadhaar photo card details with the assistant's profile information before approving them. Checked profiles are granted online Guide capability.
                  </p>
                </div>
                <div className="p-4 rounded-xl border bg-background/50 flex flex-col gap-2">
                  <h4 className="text-xs font-bold flex items-center gap-1.5 text-warning">
                    <WarningIcon size={16} /> Suspension Warning
                  </h4>
                  <p className="text-xs text-muted-foreground leading-relaxed">
                    Suspending a user account terminates their active sessions immediately and blocks future login attempts. Perform suspensions only for policy violations or fraudulent alerts.
                  </p>
                </div>
              </CardContent>
            </Card>

            <Card className="rounded-2xl border bg-card/50">
              <CardHeader>
                <CardTitle className="text-base font-bold">Quick Actions</CardTitle>
                <CardDescription>Shortcut commands.</CardDescription>
              </CardHeader>
              <CardContent className="flex flex-col gap-3">
                <Button
                  onClick={() => setTab("kyc")}
                  className="rounded-xl w-full justify-start py-5 font-bold text-xs"
                  variant="outline"
                >
                  <ShieldCheckIcon className="mr-2" size={16} />
                  Review KYC Submissions
                </Button>
                <Button
                  onClick={() => setTab("users")}
                  className="rounded-xl w-full justify-start py-5 font-bold text-xs"
                  variant="outline"
                >
                  <UsersIcon className="mr-2" size={16} />
                  Manage User Accounts
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      )}

      {/* USER MANAGEMENT TAB */}
      {activeTab === "users" && (
        <Card className="rounded-2xl border bg-card/50 overflow-hidden">
          <CardHeader className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b bg-muted/20">
            <div>
              <CardTitle className="text-lg font-bold">User Management</CardTitle>
              <CardDescription>Suspend accounts or review credentials.</CardDescription>
            </div>
            
            <div className="relative w-full md:w-80">
              <MagnifyingGlassIcon className="absolute left-3 top-2.5 text-muted-foreground" size={16} />
              <Input
                type="text"
                placeholder="Search by name, email, role..."
                value={userSearch}
                onChange={(e) => setUserSearch(e.target.value)}
                className="pl-9 h-9 rounded-xl border bg-background/50 text-xs focus-visible:ring-1"
              />
            </div>
          </CardHeader>
          <CardContent className="p-0 overflow-x-auto">
            {usersLoading ? (
              <div className="p-8 flex flex-col items-center justify-center gap-2 text-muted-foreground">
                <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                <span className="text-xs">Loading user list...</span>
              </div>
            ) : filteredUsers.length === 0 ? (
              <div className="p-12 text-center text-xs text-muted-foreground">
                No users found matching your search.
              </div>
            ) : (
              <table className="w-full text-left text-xs border-collapse">
                <thead>
                  <tr className="bg-muted/10 border-b text-muted-foreground font-bold">
                    <th className="p-4">User</th>
                    <th className="p-4">Email</th>
                    <th className="p-4">Phone</th>
                    <th className="p-4">Role</th>
                    <th className="p-4">Status</th>
                    <th className="p-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border/60">
                  {filteredUsers.map((u) => (
                    <tr key={u.id} className="hover:bg-muted/5 transition-colors">
                      <td className="p-4 font-bold flex items-center gap-2.5">
                        <UserCircleIcon size={24} className="text-muted-foreground" />
                        {u.full_name || "Unregistered"}
                      </td>
                      <td className="p-4 text-muted-foreground">{u.email || "-"}</td>
                      <td className="p-4 text-muted-foreground">{u.phone_number}</td>
                      <td className="p-4">
                        <Badge
                          variant="outline"
                          className={
                            u.role === "admin"
                              ? "bg-purple-500/10 text-purple-400 border-purple-500/25"
                              : u.role === "assistant"
                              ? "bg-blue-500/10 text-blue-400 border-blue-500/25"
                              : "bg-emerald-500/10 text-emerald-400 border-emerald-500/25"
                          }
                        >
                          {u.role}
                        </Badge>
                      </td>
                      <td className="p-4">
                        {u.status === "active" ? (
                          <span className="flex items-center gap-1 text-emerald-500">
                            <CircleIcon size={8} weight="fill" /> Active
                          </span>
                        ) : (
                          <span className="flex items-center gap-1 text-destructive font-bold">
                            <CircleIcon size={8} weight="fill" /> Suspended
                          </span>
                        )}
                      </td>
                      <td className="p-4 text-right">
                        {u.email === "admin@navassist.in" ? (
                          <span className="text-[10px] text-muted-foreground italic mr-2">Protected</span>
                        ) : (
                          <div className="flex justify-end">
                            <Select
                              value={u.status}
                              onValueChange={(value) => handleUpdateUserStatus(u.id, value || "")}
                            >
                              <SelectTrigger className="h-8 w-28 text-[10px] font-semibold bg-background/50 border-border/80 rounded-lg">
                                <SelectValue placeholder="Status" />
                              </SelectTrigger>
                              <SelectContent className="text-[10px] font-semibold">
                                <SelectItem value="active">Active</SelectItem>
                                <SelectItem value="suspended">Suspended</SelectItem>
                              </SelectContent>
                            </Select>
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </CardContent>
        </Card>
      )}

      {/* KYC QUEUE TAB */}
      {activeTab === "kyc" && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 h-full">
          {/* Sidebar Queue List */}
          <div className="col-span-1 lg:col-span-4 flex flex-col gap-4">
            <Card className="rounded-2xl border bg-card/50 overflow-hidden h-full flex flex-col">
              <CardHeader className="border-b pb-4 bg-muted/20">
                <CardTitle className="text-base font-bold">Pending Audits</CardTitle>
                <CardDescription>Awaiting verification reviews.</CardDescription>
              </CardHeader>
              <CardContent className="p-0 overflow-y-auto flex-1 max-h-[500px]">
                {kycLoading ? (
                  <div className="p-6 text-center text-xs text-muted-foreground flex flex-col items-center justify-center gap-2">
                    <div className="w-5 h-5 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                    <span>Loading queue...</span>
                  </div>
                ) : kycQueue.length === 0 ? (
                  <div className="p-8 text-center text-xs text-muted-foreground flex flex-col items-center justify-center gap-2">
                    <CheckCircleIcon size={32} className="text-emerald-500" />
                    <span>Queue Empty! No pending KYCs.</span>
                  </div>
                ) : (
                  <div className="divide-y divide-border/60">
                    {kycQueue.map((item) => {
                      const isActive = selectedKyc?.id === item.id
                      return (
                        <div
                          key={item.id}
                          onClick={() => setSelectedKyc(item)}
                          className={`p-4 flex flex-col gap-1 cursor-pointer transition-colors ${
                            isActive ? "bg-primary/5 border-l-2 border-l-primary" : "hover:bg-muted/5"
                          }`}
                        >
                          <div className="flex items-center justify-between">
                            <span className="font-bold text-xs">
                              {item.user?.full_name || "Guide Application"}
                            </span>
                            <span className="text-[9px] text-muted-foreground flex items-center gap-1">
                              <ClockIcon /> Pending
                            </span>
                          </div>
                          <span className="text-[10px] text-muted-foreground truncate">
                            {item.user?.email || "No Email Address"}
                          </span>
                          <span className="text-[9px] text-muted-foreground mt-1">
                            Experience: {item.experience_years} Years
                          </span>
                        </div>
                      )
                    })}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* KYC Details Viewer */}
          <div className="col-span-1 lg:col-span-8">
            {selectedKyc ? (
              <Card className="rounded-2xl border bg-card/50 overflow-hidden flex flex-col gap-6">
                <CardHeader className="border-b pb-4 bg-muted/20 flex flex-row items-center justify-between">
                  <div>
                    <CardTitle className="text-base font-bold">Application Details</CardTitle>
                    <CardDescription>
                      Review identity document credentials and background summary.
                    </CardDescription>
                  </div>
                  <Badge variant="outline" className="bg-warning/10 text-warning border-warning/25 animate-pulse">
                    Awaiting Review
                  </Badge>
                </CardHeader>
                
                <CardContent className="space-y-6 pt-2">
                  {/* Bio & Details Grid */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6 bg-background/40 p-4 rounded-2xl border">
                    <div className="space-y-2">
                      <h4 className="text-xs font-bold text-primary uppercase tracking-wider">Applicant Info</h4>
                      <div className="space-y-1 text-xs">
                        <p><strong className="text-muted-foreground">Full Name:</strong> {selectedKyc.user?.full_name}</p>
                        <p><strong className="text-muted-foreground">Email:</strong> {selectedKyc.user?.email}</p>
                        <p><strong className="text-muted-foreground">Phone:</strong> {selectedKyc.user?.phone_number}</p>
                        <p><strong className="text-muted-foreground">Experience:</strong> {selectedKyc.experience_years} Years</p>
                      </div>
                    </div>
                    
                    <div className="space-y-2">
                      <h4 className="text-xs font-bold text-primary uppercase tracking-wider">Aadhaar Credentials</h4>
                      <div className="space-y-1 text-xs">
                        <p><strong className="text-muted-foreground">Masked UID:</strong> {selectedKyc.aadhaar_masked || "Not Masked"}</p>
                        <p className="italic text-muted-foreground mt-1">"{selectedKyc.bio || "No biography provided."}"</p>
                      </div>
                    </div>
                  </div>

                  {/* Document Photos Showcase */}
                  <div className="space-y-3">
                    <h4 className="text-xs font-bold text-foreground">Identity Verification Images</h4>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {/* Front Photo */}
                      <div className="flex flex-col gap-1.5">
                        <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Front Aadhaar Image</span>
                        <div className="h-44 border border-zinc-800 bg-zinc-950 rounded-xl overflow-hidden flex items-center justify-center relative group">
                          {selectedKyc.documents?.find((d) => d.doc_type === "aadhaar_front")?.file_url ? (
                            <ImageThumbnail
                              url={getDocUrl(selectedKyc.documents?.find((d) => d.doc_type === "aadhaar_front")?.file_url || "")}
                              alt="Aadhaar Card Front"
                              aspectRatio="video"
                              onClick={() => {
                                setViewerImage(getDocUrl(selectedKyc.documents?.find((d) => d.doc_type === "aadhaar_front")?.file_url || ""))
                                setViewerTitle("Front Aadhaar Image")
                                setIsViewerOpen(true)
                              }}
                            />
                          ) : (
                            <span className="text-[10px] text-muted-foreground">No Front Image Uploaded</span>
                          )}
                        </div>
                      </div>

                      {/* Back Photo */}
                      <div className="flex flex-col gap-1.5">
                        <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Back Aadhaar Image</span>
                        <div className="h-44 border border-zinc-800 bg-zinc-950 rounded-xl overflow-hidden flex items-center justify-center relative group">
                          {selectedKyc.documents?.find((d) => d.doc_type === "aadhaar_back")?.file_url ? (
                            <ImageThumbnail
                              url={getDocUrl(selectedKyc.documents?.find((d) => d.doc_type === "aadhaar_back")?.file_url || "")}
                              alt="Aadhaar Card Back"
                              aspectRatio="video"
                              onClick={() => {
                                setViewerImage(getDocUrl(selectedKyc.documents?.find((d) => d.doc_type === "aadhaar_back")?.file_url || ""))
                                setViewerTitle("Back Aadhaar Image")
                                setIsViewerOpen(true)
                              }}
                            />
                          ) : (
                            <span className="text-[10px] text-muted-foreground">No Back Image Uploaded</span>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Audit Review Actions */}
                  <div className="flex items-center justify-end gap-3 border-t pt-4">
                    <Button
                      variant="outline"
                      onClick={() => openRejectionDialog(selectedKyc.user_id)}
                      className="rounded-xl border-destructive/35 text-destructive hover:bg-destructive/10 font-bold text-xs"
                    >
                      <XCircleIcon className="mr-1.5" size={16} />
                      Reject Application
                    </Button>
                    
                    <Button
                      onClick={() => handleApproveKyc(selectedKyc.user_id)}
                      className="rounded-xl bg-success text-success-foreground hover:bg-success/90 font-bold text-xs"
                    >
                      <CheckCircleIcon className="mr-1.5" size={16} />
                      Approve & Verify Guide
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ) : (
              <Card className="rounded-2xl border bg-card/50 p-12 text-center text-xs text-muted-foreground flex flex-col items-center justify-center gap-3">
                <ShieldCheckIcon size={44} className="opacity-40" />
                <span>Select a pending application in the queue to perform audits.</span>
              </Card>
            )}
          </div>
        </div>
      )}

      {/* REJECTION REASON MODAL DIALOG */}
      {isRejectOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-card border rounded-2xl p-6 shadow-2xl max-w-md w-full mx-4 flex flex-col gap-4">
            <div>
              <h3 className="text-base font-bold text-foreground">Reject KYC Application</h3>
              <p className="text-xs text-muted-foreground mt-0.5">
                Specify the audit reason explaining why this application was rejected. This reason is logged and shared with the guide.
              </p>
            </div>
            
            <Input
              type="text"
              placeholder="e.g. Aadhaar photo is blurred, details do not match profile..."
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              className="text-xs rounded-xl focus-visible:ring-1"
            />
            
            <div className="flex items-center justify-end gap-2.5 mt-2">
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setIsRejectOpen(false)}
                className="rounded-lg text-xs"
              >
                Cancel
              </Button>
              <Button
                variant="destructive"
                size="sm"
                onClick={handleRejectKyc}
                className="rounded-lg text-xs font-bold"
              >
                Confirm Rejection
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* DETAILED IMAGE VIEWER DIALOG */}
      <ImageViewer
        open={isViewerOpen}
        onOpenChange={setIsViewerOpen}
        url={viewerImage}
        metadata={{
          title: viewerTitle,
          documentType: "KYC Verification Document",
          uploadedBy: selectedKyc?.user?.full_name || "Assistant Guide",
        }}
      />
    </div>
  )
}
export default AdminConsolePage
