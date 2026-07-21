import React from "react"
import { ChatCircleDotsIcon } from "@phosphor-icons/react"
import { Button } from "@/components/ui/button"

interface EmptyConversationProps {
  onFocusInput?: () => void
}

export function EmptyConversation({ onFocusInput }: EmptyConversationProps) {
  return (
    <div className="py-12 px-4 flex flex-col items-center justify-center text-center space-y-3">
      <div className="w-14 h-14 rounded-2xl bg-muted dark:bg-zinc-800/80 border border-border dark:border-zinc-700/80 flex items-center justify-center text-primary shadow-md">
        <ChatCircleDotsIcon size={32} weight="duotone" />
      </div>
      <h4 className="font-extrabold text-sm text-foreground">No message replies yet</h4>
      <p className="text-xs text-muted-foreground max-w-sm leading-relaxed">
        Start the conversation below by sending a message or attaching supporting evidence images.
      </p>
      {onFocusInput && (
        <Button
          onClick={onFocusInput}
          variant="outline"
          className="rounded-xl text-xs font-bold border-border bg-card text-foreground hover:bg-muted"
        >
          Type First Reply
        </Button>
      )}
    </div>
  )
}
