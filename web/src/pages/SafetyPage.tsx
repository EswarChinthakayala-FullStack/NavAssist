import React, { useState, useEffect } from "react"
import { api } from "@/services/api"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { toast } from "sonner"
import {
  SirenIcon,
  PlusIcon,
  TrashIcon,
  ShieldCheckIcon,
  UserPlusIcon,
  PhoneCallIcon,
  WarningCircleIcon
} from "@phosphor-icons/react"
import { motion } from "framer-motion"

interface Contact {
  id: number
  name: string
  phone: string
}

export function SafetyPage() {
  const [contacts, setContacts] = useState<Contact[]>([])
  const [name, setName] = useState("")
  const [phone, setPhone] = useState("")
  
  const [sosActive, setSosActive] = useState(false)
  const [loading, setLoading] = useState(true)
  const [activeBookingId, setActiveBookingId] = useState<number | null>(null)

  const fetchContacts = async () => {
    try {
      const res = await api.get("/users/me/emergency-contacts")
      setContacts(res.data)
    } catch (err) {
      console.warn("Failed to load emergency contacts:", err)
    } finally {
      setLoading(false)
    }
  }

  const fetchActiveBooking = async () => {
    try {
      const res = await api.get("/bookings/")
      const active = res.data.find(
        (b: any) => b.status === "PENDING" || b.status === "ACCEPTED" || b.status === "STARTED"
      )
      if (active) {
        setActiveBookingId(active.id)
      } else {
        setActiveBookingId(null)
      }
    } catch (err) {}
  }

  useEffect(() => {
    fetchContacts()
    fetchActiveBooking()
  }, [])

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim() || !phone.trim()) return
    
    const formattedPhone = phone.startsWith("+") ? phone : `+91${phone}`
    
    try {
      const res = await api.post("/users/me/emergency-contacts", {
        name,
        phone: formattedPhone
      })
      toast.success("Emergency contact registered successfully.")
      setContacts(prev => [...prev, res.data])
      setName("")
      setPhone("")
    } catch (err) {}
  }

  const handleDelete = async (id: number) => {
    try {
      await api.delete(`/users/me/emergency-contacts/${id}`)
      toast.success("Emergency contact removed.")
      setContacts(prev => prev.filter(c => c.id !== id))
    } catch (err) {}
  }

  const triggerSos = async () => {
    if (!activeBookingId) {
      toast.error("SOS alerts can only be triggered during active guidance journeys.")
      return
    }

    setSosActive(true)
    
    let lat = 28.6139
    let lon = 77.2090
    
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          lat = position.coords.latitude
          lon = position.coords.longitude
          await sendSosApiCall(lat, lon)
        },
        async () => {
          await sendSosApiCall(lat, lon)
        }
      )
    } else {
      await sendSosApiCall(lat, lon)
    }
  }

  const sendSosApiCall = async (latitude: number, longitude: number) => {
    try {
      await api.post("/sos/trigger", {
        booking_id: activeBookingId,
        latitude,
        longitude
      })
      toast.success("Emergency SOS triggered! Live coordinate telemetry links dispatched to guardians.")
    } catch (err) {
      setSosActive(false)
    }
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-6xl mx-auto py-2 relative space-y-6 text-left"
    >
      {/* SOS Active Flashing Overlay */}
      {sosActive && (
        <div className="fixed inset-0 z-50 bg-destructive/20 backdrop-blur-md flex items-center justify-center pointer-events-none animate-pulse">
          <div className="bg-destructive text-destructive-foreground font-bold px-8 py-4 rounded-full text-lg uppercase tracking-widest shadow-2xl flex items-center gap-2 border-2 border-white animate-bounce">
            <SirenIcon size={24} weight="fill" />
            SOS Active & Dispatched
          </div>
        </div>
      )}

      {/* Header Banner */}
      <div className="bg-gradient-to-r from-destructive/10 via-primary/5 to-transparent border border-border/80 p-6 rounded-2xl shadow-sm">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <div className="flex items-center gap-2">
              <Badge className="bg-destructive/20 hover:bg-destructive/20 text-destructive border-0 text-[10px] px-3.5 py-1 rounded-full font-bold uppercase tracking-wider">
                Emergency Panel
              </Badge>
              <Badge variant="outline" className="font-bold text-[10px] uppercase tracking-wider rounded-full">
                Encrypted Sync
              </Badge>
            </div>
            <h3 className="font-black text-2xl mt-3 tracking-tight text-foreground">Safety & SOS Console</h3>
            <p className="text-xs text-muted-foreground mt-1">Manage emergency contact guardians and dispatch real-time GPS telemetry distress signals.</p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Side: Management and Contacts (7 columns) */}
        <div className="lg:col-span-7 space-y-6">
          <Card className="rounded-2xl border border-border/80 shadow-md">
            <CardHeader className="pb-3 border-b border-border/40">
              <CardTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
                <PhoneCallIcon size={18} className="text-primary" />
                Registered Emergency Guardians
              </CardTitle>
              <CardDescription className="text-xs">
                These numbers will receive instant SMS notifications containing live map tracking links during SOS triggers.
              </CardDescription>
            </CardHeader>
            <CardContent className="pt-6">
              {loading ? (
                <div className="text-center py-10 flex flex-col items-center justify-center gap-2">
                  <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                  <span className="text-xs text-muted-foreground">Loading safety contacts...</span>
                </div>
              ) : contacts.length > 0 ? (
                <div className="divide-y divide-border/40">
                  {contacts.map(contact => (
                    <div key={contact.id} className="flex items-center justify-between py-3.5 first:pt-0 last:pb-0">
                      <div>
                        <h4 className="font-bold text-xs text-foreground">{contact.name}</h4>
                        <span className="text-[11px] text-muted-foreground font-mono mt-0.5 block">{contact.phone}</span>
                      </div>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleDelete(contact.id)}
                        className="text-destructive hover:bg-destructive/10 rounded-full hover:scale-105 active:scale-95 transition-transform"
                      >
                        <TrashIcon size={18} />
                      </Button>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8 border-2 border-dashed border-border/80 rounded-2xl">
                  <WarningCircleIcon size={32} className="text-muted-foreground/60 mx-auto mb-2" />
                  <p className="text-xs text-muted-foreground font-semibold">No emergency contacts registered yet.</p>
                </div>
              )}
            </CardContent>
          </Card>

          <Card className="rounded-2xl border border-border/80 shadow-md">
            <CardHeader className="pb-3 border-b border-border/40">
              <CardTitle className="text-xs font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
                <UserPlusIcon size={18} className="text-primary" />
                Register New Guardian
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-6">
              <form onSubmit={handleAdd} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <Label htmlFor="name" className="text-[10px] font-bold uppercase tracking-wider pl-1">Name / Relation</Label>
                    <Input
                      id="name"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      placeholder="e.g. Brother, Parent"
                      required
                      className="rounded-xl border border-border/80 p-3 text-xs"
                    />
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="phone" className="text-[10px] font-bold uppercase tracking-wider pl-1">10-Digit Mobile Number</Label>
                    <Input
                      id="phone"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      placeholder="9876543210"
                      required
                      className="rounded-xl border border-border/80 p-3 text-xs"
                    />
                  </div>
                </div>
                <Button type="submit" className="w-full h-11 text-xs font-black shadow-lg bg-primary text-primary-foreground hover:bg-primary/90 rounded-xl flex items-center justify-center gap-2 cursor-pointer hover:scale-[1.01] transition-transform">
                  <PlusIcon size={16} weight="bold" />
                  Register Guardian Contact
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>

        {/* Right Side: Emergency Trigger Panic Button (5 columns) */}
        <div className="lg:col-span-5 h-full">
          <Card className="border border-destructive/25 bg-destructive/5 rounded-2xl h-full flex flex-col justify-between shadow-md min-h-[380px]">
            <CardHeader className="text-center pb-0">
              <div className="w-16 h-16 bg-destructive/10 text-destructive rounded-full flex items-center justify-center mx-auto mb-4 border border-destructive/20 shadow-inner">
                <SirenIcon size={32} weight="fill" className="animate-pulse" />
              </div>
              <CardTitle className="text-lg font-black text-destructive tracking-widest uppercase">Emergency Trigger</CardTitle>
              <CardDescription className="text-xs leading-relaxed text-destructive/80 mt-2">
                Pressing the distress button below immediately dispatches your current physical coordinates stream link to all registered guardians.
              </CardDescription>
            </CardHeader>
            <CardContent className="text-center py-8">
              <Button
                onClick={triggerSos}
                disabled={sosActive}
                className="w-48 h-48 rounded-full bg-destructive text-destructive-foreground hover:bg-destructive/90 text-sm font-black shadow-2xl transition-all hover:scale-105 active:scale-95 border-8 border-background cursor-pointer uppercase tracking-widest"
              >
                {sosActive ? "Dispatched" : "Trigger SOS"}
              </Button>
            </CardContent>
            <CardFooter className="text-[10px] text-center text-muted-foreground block pb-6 px-6 border-t border-destructive/10 pt-4">
              {!activeBookingId ? (
                <span className="text-destructive font-bold flex items-center justify-center gap-1">
                  <WarningCircleIcon size={14} />
                  Requires an active journey booking to trigger
                </span>
              ) : (
                "Encrypts live telemetry payload and initiates SMS alerts stream."
              )}
            </CardFooter>
          </Card>
        </div>
      </div>
    </motion.div>
  )
}
export default SafetyPage
