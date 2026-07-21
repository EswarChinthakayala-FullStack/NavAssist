import React, { useState } from "react"
import { ShareNetworkIcon, CopyIcon, CheckIcon, WhatsappLogoIcon, EnvelopeIcon, ChatsIcon } from "@phosphor-icons/react"
import { Button } from "@/components/ui/button"
import { api } from "@/services/api"
import { toast } from "sonner"

interface ShareTripSheetProps {
  bookingId: number
  isOpen: boolean
  onClose: () => void
}

export function ShareTripSheet({ bookingId, isOpen, onClose }: ShareTripSheetProps) {
  const [loading, setLoading] = useState(false)
  const [shareUrl, setShareUrl] = useState("")
  const [copied, setCopied] = useState(false)
  const [expiryMsg, setExpiryMsg] = useState("")

  const generateLink = async () => {
    setLoading(true)
    try {
      const res = await api.post(`/share/${bookingId}/generate-link`)
      const token = res.data.share_token
      const fullUrl = `${window.location.origin}/track/${token}`
      setShareUrl(fullUrl)
      
      const expiry = new Date(res.data.expires_at)
      setExpiryMsg(`Link active until ${expiry.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`)
      toast.success("Public live tracking link generated successfully!")
    } catch (err: any) {
      const msg = err.response?.data?.detail || "Could not generate tracking link."
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  const handleCopy = () => {
    if (!shareUrl) return
    navigator.clipboard.writeText(shareUrl)
    setCopied(true)
    toast.success("Tracking link copied to clipboard!")
    setTimeout(() => setCopied(false), 2000)
  }

  const handleWhatsappShare = () => {
    if (!shareUrl) return
    const text = encodeURIComponent(`Track my journey live on NavAssist using this link: ${shareUrl}`)
    window.open(`https://api.whatsapp.com/send?text=${text}`, "_blank")
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-end sm:items-center justify-center p-4">
      {/* Click outside to close */}
      <div className="absolute inset-0" onClick={onClose} />

      <div className="bg-card text-foreground border border-border w-full max-w-md rounded-t-3xl sm:rounded-2xl p-6 shadow-2xl relative z-10 animate-in slide-in-from-bottom sm:zoom-in-95 duration-200">
        {/* Handle for mobile visual indicator */}
        <div className="w-12 h-1 bg-border rounded-full mx-auto mb-4 sm:hidden" />

        <div className="flex flex-col gap-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-primary/10 text-primary rounded-xl">
              <ShareNetworkIcon size={20} weight="fill" />
            </div>
            <div>
              <h3 className="font-extrabold text-base leading-tight">Share Active Journey</h3>
              <p className="text-[10px] text-muted-foreground mt-0.5">Let trusted contacts track your location real-time</p>
            </div>
          </div>

          {!shareUrl ? (
            <div className="py-4 text-center">
              <p className="text-xs text-muted-foreground leading-relaxed mb-4">
                Generates a temporary encrypted token links. Anyone with the URL can view your live progress on the map.
              </p>
              <Button
                onClick={generateLink}
                disabled={loading}
                className="w-full py-5 rounded-xl font-bold bg-primary text-white shadow-sm"
              >
                {loading ? "Generating Safe Token..." : "Generate Live Tracking Link"}
              </Button>
            </div>
          ) : (
            <div className="flex flex-col gap-4 py-2">
              <div className="flex items-center gap-2 bg-muted/50 p-2.5 rounded-xl border border-border">
                <input
                  type="text"
                  readOnly
                  value={shareUrl}
                  className="flex-1 text-xs bg-transparent border-0 outline-none text-foreground font-medium truncate"
                />
                <button
                  onClick={handleCopy}
                  className="p-2 hover:bg-muted text-muted-foreground hover:text-foreground rounded-lg transition-all"
                >
                  {copied ? <CheckIcon size={16} className="text-success" /> : <CopyIcon size={16} />}
                </button>
              </div>

              {expiryMsg && (
                <span className="text-[9px] text-warning font-bold bg-warning/5 border border-warning/10 px-2.5 py-1 rounded-md w-fit">
                  {expiryMsg} (2 Hours Expiry)
                </span>
              )}

              <div className="grid grid-cols-2 gap-3 mt-1">
                <Button
                  onClick={handleWhatsappShare}
                  variant="outline"
                  className="py-4 rounded-xl border-border flex items-center justify-center gap-2 hover:bg-muted font-bold text-xs"
                >
                  <WhatsappLogoIcon size={18} weight="fill" className="text-[#25D366]" />
                  WhatsApp
                </Button>
                <Button
                  onClick={() => {
                    const text = encodeURIComponent(`Track my journey live on NavAssist using this link: ${shareUrl}`)
                    window.open(`mailto:?subject=Track My Journey&body=${text}`, "_blank")
                  }}
                  variant="outline"
                  className="py-4 rounded-xl border-border flex items-center justify-center gap-2 hover:bg-muted font-bold text-xs"
                >
                  <EnvelopeIcon size={18} weight="fill" />
                  Email Contact
                </Button>
              </div>

              <Button
                variant="ghost"
                onClick={onClose}
                className="w-full text-xs text-muted-foreground hover:bg-transparent mt-2"
              >
                Close & Keep Tracking
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
export default ShareTripSheet
