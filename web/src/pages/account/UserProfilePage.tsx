import React, { useState, useEffect } from "react"
import { useAuth } from "@/store/auth-context"
import { usersService } from "@/services/users.service"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import {
  UserIcon,
  ShieldCheckIcon,
  CameraIcon,
  SpinnerIcon,
  DeviceMobileIcon,
  EnvelopeIcon,
  UserFocusIcon,
  BriefcaseIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"
import { ImageThumbnail } from "@/components/shared/ImageThumbnail"

export function UserProfilePage() {
  const { user, updateProfile } = useAuth()
  
  const [fullName, setFullName] = useState("")
  const [email, setEmail] = useState("")
  const [phone, setPhone] = useState("")
  const [avatar, setAvatar] = useState("")

  const [saving, setSaving] = useState(false)

  // Initialize fields
  useEffect(() => {
    if (user) {
      setFullName(user.full_name || "")
      setEmail(user.email || "")
      setPhone(user.phone || "")
      setAvatar(user.profile_photo_url || "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=facearea&facepad=2&w=256&h=256&q=80")
    }
  }, [user])

  const handleAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      const reader = new FileReader()
      reader.onloadend = () => {
        const img = new Image()
        img.onload = () => {
          const canvas = document.createElement("canvas")
          const maxDim = 200
          let width = img.width
          let height = img.height

          if (width > height) {
            if (width > maxDim) {
              height = Math.round((height * maxDim) / width)
              width = maxDim
            }
          } else {
            if (height > maxDim) {
              width = Math.round((width * maxDim) / height)
              height = maxDim
            }
          }

          canvas.width = width
          canvas.height = height

          const ctx = canvas.getContext("2d")
          if (ctx) {
            ctx.drawImage(img, 0, 0, width, height)
            const compressedBase64 = canvas.toDataURL("image/jpeg", 0.7)
            setAvatar(compressedBase64)
            toast.success("Profile photo optimized for database storage!")
          } else {
            setAvatar(reader.result as string)
            toast.success("Profile photo uploaded to preview!")
          }
        }
        img.src = reader.result as string
      }
      reader.readAsDataURL(file)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!fullName.trim()) {
      toast.error("Full name cannot be empty.")
      return
    }

    setSaving(true)
    try {
      const success = await updateProfile({
        full_name: fullName,
        email: email,
        profile_photo_url: avatar
      })
      if (success) {
        toast.success("Profile saved successfully!")
      } else {
        toast.error("Failed to update profile attributes.")
      }
    } catch (err) {
      console.error(err)
      toast.error("Failed to update profile attributes.")
    } finally {
      setSaving(false)
    }
  }

  const initials = fullName
    ? fullName
        .trim()
        .split(" ")
        .map((w) => w.charAt(0))
        .join("")
        .toUpperCase()
        .slice(0, 2)
    : "U"

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-6xl mx-auto py-2 space-y-6 text-left"
    >
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-primary/10 via-primary/5 to-transparent border border-border/80 p-6 rounded-2xl shadow-sm">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <div className="flex items-center gap-2">
              <Badge className="bg-primary/20 hover:bg-primary/20 text-primary border-0 text-[10px] px-3.5 py-1 rounded-full font-bold uppercase tracking-wider">
                Identity Profile
              </Badge>
              <Badge variant="outline" className="font-bold text-[10px] uppercase tracking-wider rounded-full text-success border-success/30 bg-success/5">
                Role: {user?.role || "Passenger"}
              </Badge>
            </div>
            <h3 className="font-black text-2xl mt-3 tracking-tight text-foreground">User Profile Settings</h3>
            <p className="text-xs text-muted-foreground mt-1">Manage your personal display identity profile details and metadata options.</p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Side: Avatar Card (4 columns) */}
        <div className="lg:col-span-4 space-y-6">
          <Card className="border border-border/80 shadow-md rounded-2xl overflow-hidden text-center bg-card">
            <div className="h-20 bg-gradient-to-r from-primary/25 via-primary/10 to-transparent" />
            <CardContent className="relative -mt-10 pb-6 flex flex-col items-center gap-4">
              {/* Profile image with camera upload button */}
              <div className="relative group">
                <div className="w-24 h-24 rounded-full overflow-hidden border-4 border-background bg-muted shadow-lg flex items-center justify-center">
                  {avatar ? (
                    <ImageThumbnail
                      url={avatar}
                      alt={fullName}
                      aspectRatio="circle"
                      metadata={{
                        title: "User Profile Photo",
                        uploadedAt: "Latest Upload",
                        documentType: "Profile Avatar",
                        uploadedBy: fullName,
                      }}
                    />
                  ) : (
                    <span className="text-xl font-bold text-primary">{initials}</span>
                  )}
                </div>
                <label className="absolute bottom-0 right-0 p-2 bg-primary text-primary-foreground rounded-full shadow-lg cursor-pointer hover:scale-105 transition-transform border border-background">
                  <CameraIcon size={14} weight="fill" />
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleAvatarChange}
                    className="hidden"
                  />
                </label>
              </div>

              <div>
                <h4 className="font-black text-base text-foreground leading-tight">{fullName || "NavAssist User"}</h4>
                <p className="text-[10px] text-muted-foreground font-mono mt-1 uppercase tracking-wider">Ref ID: #USR-{user?.id || "N/A"}</p>
              </div>

              <div className="flex flex-wrap justify-center gap-1.5 pt-1">
                {user?.is_phone_verified && (
                  <Badge className="bg-success/15 text-success hover:bg-success/15 border-0 text-[8px] px-2 py-0.5 rounded-full font-bold">
                    Phone Verified
                  </Badge>
                )}
                {user?.is_email_verified && (
                  <Badge className="bg-success/15 text-success hover:bg-success/15 border-0 text-[8px] px-2 py-0.5 rounded-full font-bold">
                    Email Verified
                  </Badge>
                )}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right Side: Profile Editable Form (8 columns) */}
        <div className="lg:col-span-8">
          <form onSubmit={handleSubmit}>
            <Card className="border border-border/80 shadow-md rounded-2xl overflow-hidden bg-card h-full flex flex-col justify-between">
              <CardHeader className="pb-3 border-b border-border/40">
                <CardTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
                  <UserFocusIcon size={18} className="text-primary" />
                  Account Details Info
                </CardTitle>
                <CardDescription className="text-xs">
                  Update your public name display and manage read-only verification credentials.
                </CardDescription>
              </CardHeader>

              <CardContent className="pt-6 space-y-4">
                {/* Editable Full Name */}
                <div className="space-y-1.5">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest pl-1">
                    Display Name
                  </label>
                  <div className="relative">
                    <Input
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      placeholder="Enter full name..."
                      required
                      className="rounded-xl border border-border p-3 text-xs pl-10 font-semibold"
                    />
                    <div className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted-foreground">
                      <UserIcon size={16} />
                    </div>
                  </div>
                </div>

                {/* Read-Only Verified Email */}
                <div className="space-y-1.5">
                  <div className="flex justify-between items-center pl-1">
                    <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">
                      Email Address
                    </label>
                    {user?.is_email_verified && (
                      <Badge className="bg-success/20 text-success border-0 text-[8px] px-2 py-0.5 rounded-full font-bold flex items-center gap-0.5">
                        <ShieldCheckIcon size={10} weight="fill" />
                        Verified
                      </Badge>
                    )}
                  </div>
                  <div className="relative">
                    <Input
                      value={email}
                      disabled
                      className="rounded-xl border border-border/80 bg-muted/30 p-3 text-xs pl-10 font-bold select-none text-muted-foreground"
                    />
                    <div className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted-foreground">
                      <EnvelopeIcon size={16} />
                    </div>
                  </div>
                </div>

                {/* Read-Only Verified Phone */}
                <div className="space-y-1.5">
                  <div className="flex justify-between items-center pl-1">
                    <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">
                      Mobile Number
                    </label>
                    {user?.is_phone_verified && (
                      <Badge className="bg-success/20 text-success border-0 text-[8px] px-2 py-0.5 rounded-full font-bold flex items-center gap-0.5">
                        <ShieldCheckIcon size={10} weight="fill" />
                        Verified
                      </Badge>
                    )}
                  </div>
                  <div className="relative">
                    <Input
                      value={phone}
                      disabled
                      className="rounded-xl border border-border/80 bg-muted/30 p-3 text-xs pl-10 font-bold select-none text-muted-foreground"
                    />
                    <div className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted-foreground">
                      <DeviceMobileIcon size={16} />
                    </div>
                  </div>
                </div>
              </CardContent>

              <CardFooter className="p-6 border-t border-border/40 bg-muted/10">
                <Button
                  type="submit"
                  disabled={saving}
                  className="w-full bg-primary text-primary-foreground hover:bg-primary/95 rounded-xl py-5 font-black text-xs shadow-md cursor-pointer hover:scale-[1.01] transition-transform flex items-center justify-center gap-1.5"
                >
                  {saving ? (
                    <>
                      <SpinnerIcon size={16} className="animate-spin" />
                      <span>Saving Updates...</span>
                    </>
                  ) : (
                    <span>Save Profile Changes</span>
                  )}
                </Button>
              </CardFooter>
            </Card>
          </form>
        </div>
      </div>
    </motion.div>
  )
}
export default UserProfilePage
