import React from "react"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { ImageIcon, PaperPlaneRightIcon, XIcon } from "@phosphor-icons/react"

interface AttachmentItem {
  url: string
  previewUrl?: string
}

interface MessageComposerProps {
  replyMessage: string
  setReplyMessage: (msg: string) => void
  attachments: AttachmentItem[]
  setAttachments: React.Dispatch<React.SetStateAction<AttachmentItem[]>>
  uploadingImage: boolean
  sending: boolean
  handleUploadTicketImage: (e: React.ChangeEvent<HTMLInputElement>) => void
  handleSendReply: (e?: React.FormEvent) => void
}

export function MessageComposer({
  replyMessage,
  setReplyMessage,
  attachments,
  setAttachments,
  uploadingImage,
  sending,
  handleUploadTicketImage,
  handleSendReply
}: MessageComposerProps) {
  return (
    <div className="p-4 border-t border-border bg-card/80 dark:bg-zinc-950/80 shrink-0">
      <form onSubmit={handleSendReply} className="w-full space-y-3">
        <div className="border border-border dark:border-zinc-800 focus-within:border-primary/80 focus-within:ring-1 focus-within:ring-primary/20 rounded-2xl bg-background dark:bg-zinc-900/60 overflow-hidden transition-all shadow-md">
          {/* Attached Images Preview Strip */}
          {attachments.length > 0 && (
            <div className="p-3 border-b border-border dark:border-zinc-800/60 bg-muted/40 dark:bg-zinc-950/40 flex flex-wrap gap-2.5 items-center">
              {attachments.map((item, idx) => (
                <div key={idx} className="relative group w-14 h-14 rounded-xl overflow-hidden border border-border dark:border-zinc-700/80 shadow-xs shrink-0 bg-background dark:bg-zinc-950">
                  <img
                    src={item.previewUrl || item.url}
                    alt="Attachment preview"
                    className="w-full h-full object-cover"
                  />
                  <button
                    type="button"
                    onClick={() => setAttachments((prev) => prev.filter((_, i) => i !== idx))}
                    className="absolute top-1 right-1 p-1 bg-black/80 hover:bg-black text-white rounded-full transition-colors cursor-pointer z-30 shadow-xs"
                    title="Remove attachment"
                  >
                    <XIcon size={10} weight="bold" />
                  </button>
                </div>
              ))}
              {uploadingImage && (
                <div className="w-14 h-14 rounded-xl border border-dashed border-primary/50 bg-primary/5 flex items-center justify-center text-primary animate-pulse">
                  <div className="w-4 h-4 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                </div>
              )}
            </div>
          )}

          {/* Main Textarea */}
          <Textarea
            value={replyMessage}
            onChange={(e) => setReplyMessage(e.target.value)}
            placeholder={uploadingImage ? "Uploading evidence attachment..." : "Type your reply message..."}
            disabled={uploadingImage}
            rows={2}
            className="w-full min-h-[64px] max-h-[140px] resize-none border-0 focus-visible:ring-0 focus-visible:outline-none bg-transparent p-3.5 text-xs sm:text-sm leading-relaxed text-foreground dark:text-zinc-100 placeholder:text-muted-foreground"
            onKeyDown={(e) => {
              if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault()
                handleSendReply()
              }
            }}
          />

          {/* Bottom Control Bar */}
          <div className="px-3 py-2.5 bg-muted/30 dark:bg-zinc-950/50 border-t border-border/40 dark:border-zinc-800/40 flex items-center justify-between gap-2">
            <label
              className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-border dark:border-zinc-800 bg-card dark:bg-zinc-900 hover:bg-muted dark:hover:bg-zinc-800 text-foreground dark:text-zinc-200 text-xs font-bold cursor-pointer transition-colors ${
                uploadingImage ? "opacity-50 cursor-not-allowed" : ""
              }`}
              title="Attach Evidence Image"
            >
              <ImageIcon size={16} className="text-primary" />
              <span>{uploadingImage ? "Uploading..." : "Attach Image"}</span>
              <input
                type="file"
                accept="image/*"
                className="hidden"
                disabled={uploadingImage || sending}
                onChange={handleUploadTicketImage}
              />
            </label>

            <div className="flex items-center gap-3">
              <span className="hidden sm:inline-block text-[10px] text-muted-foreground font-medium">
                <kbd className="font-mono bg-muted dark:bg-zinc-800 px-1.5 py-0.5 rounded border border-border dark:border-zinc-700 text-[9px] text-foreground dark:text-zinc-300">Enter</kbd> to send
              </span>
              <Button
                type="submit"
                disabled={(!replyMessage.trim() && attachments.length === 0) || sending || uploadingImage}
                className="rounded-xl px-4 py-2 text-xs font-black bg-primary text-primary-foreground hover:bg-primary/95 flex items-center gap-2 cursor-pointer shadow-xs transition-all"
              >
                <span>{sending ? "Sending..." : "Send Response"}</span>
                <PaperPlaneRightIcon size={14} weight="fill" />
              </Button>
            </div>
          </div>
        </div>
      </form>
    </div>
  )
}
