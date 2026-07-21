import React, { useState } from "react"
import { useNavigate, Link } from "react-router-dom"
import { supportService } from "@/services/support.service"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import {
  PaperPlaneRightIcon,
  ShieldCheckIcon,
  QuestionIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from "@/components/ui/select"

export function FeedbackPage() {
  const navigate = useNavigate()
  const [subject, setSubject] = useState("Trip Issue")
  const [description, setDescription] = useState("")
  const [submitting, setSubmitting] = useState(false)
  const [ticketResult, setTicketResult] = useState<any>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!description.trim()) {
      toast.error("Please explain your feedback details.")
      return
    }

    setSubmitting(true)
    try {
      const ticket = await supportService.createTicket(subject, description)
      setTicketResult(ticket)
      toast.success("Helpdesk ticket opened successfully.")
    } catch (err) {
      console.error(err)
      toast.error("Failed to submit feedback ticket. Standard support channels available.")
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="min-h-[calc(100vh-6rem)] w-full flex items-center justify-center relative overflow-hidden rounded-3xl p-4 md:p-8 select-none">
      {/* Dot matrix background pattern */}
      <div className="absolute inset-0 opacity-[0.05] bg-[radial-gradient(circle_at_center,var(--foreground)_10%,transparent_50%)] bg-[size:24px_24px] pointer-events-none" />

      {/* Radial glow */}
      <div className="absolute inset-0 opacity-20 bg-[radial-gradient(ellipse_at_top_right,hsl(var(--primary)/0.25),transparent_60%)] pointer-events-none" />

      {ticketResult ? (
        <motion.div
          initial={{ opacity: 0, y: 15 }}
          animate={{ opacity: 1, y: 0 }}
          className="max-w-md w-full relative z-10 text-center"
        >
          <Card className="border border-border/80 shadow-2xl backdrop-blur-md rounded-3xl overflow-hidden bg-card/90">
            <div className="bg-gradient-to-b from-success/20 to-transparent p-8 flex justify-center border-b border-border/40">
              <div className="p-4 bg-success/20 text-success rounded-full w-fit shadow-lg">
                <ShieldCheckIcon size={44} weight="fill" className="animate-bounce" />
              </div>
            </div>

            <CardHeader>
              <Badge className="bg-success/20 hover:bg-success/20 text-success border-0 text-[10px] px-3.5 py-1 rounded-full font-bold mx-auto mb-2 tracking-widest uppercase">
                Ticket Opened
              </Badge>
              <CardTitle className="text-lg font-black text-foreground">Support Request Confirmed</CardTitle>
              <CardDescription className="text-xs">
                A support specialist has been assigned to your transit request.
              </CardDescription>
            </CardHeader>

            <CardContent className="p-6 space-y-4">
              <div className="p-4 bg-muted/40 border border-border rounded-2xl text-left space-y-2">
                <div className="flex justify-between items-center text-xs">
                  <span className="font-bold text-muted-foreground">Ticket Reference:</span>
                  <span className="font-mono font-black text-primary">#TKT-{ticketResult.id || "1028"}</span>
                </div>
                <div className="flex justify-between items-center text-xs">
                  <span className="font-bold text-muted-foreground">Subject Group:</span>
                  <span className="font-bold text-foreground">{ticketResult.subject}</span>
                </div>
              </div>
              <p className="text-[11px] text-muted-foreground leading-relaxed pl-1 text-left font-medium">
                We monitor active transit issue reports in real-time. You can track this ticket's resolution updates inside the support panel.
              </p>
            </CardContent>

            <CardFooter className="p-6 border-t border-border/50 bg-muted/10 flex flex-col gap-3">
              <Link to={`/support/tickets/${ticketResult.id}`} className="w-full">
                <Button className="w-full bg-primary text-primary-foreground hover:bg-primary/95 rounded-2xl py-5 font-black text-xs shadow-lg">
                  View Support Ticket Details
                </Button>
              </Link>
              <Button
                variant="outline"
                onClick={() => navigate("/bookings")}
                className="w-full rounded-2xl py-4 font-bold text-xs border-border"
              >
                Done
              </Button>
            </CardFooter>
          </Card>
        </motion.div>
      ) : (
        <motion.div
          initial={{ opacity: 0, y: 15 }}
          animate={{ opacity: 1, y: 0 }}
          className="max-w-md w-full relative z-10 text-left"
        >
          <form onSubmit={handleSubmit}>
            <Card className="border border-border/80 shadow-2xl backdrop-blur-md rounded-3xl overflow-hidden bg-card/90">
              {/* Header Banner */}
              <div className="bg-gradient-to-b from-primary/10 to-transparent p-6 space-y-2 border-b border-border/40 text-center">
                <Badge className="bg-primary/20 hover:bg-primary/20 text-primary border-0 text-[10px] px-3.5 py-1 rounded-full font-bold mx-auto tracking-widest uppercase">
                  Helpdesk Support
                </Badge>
                <CardTitle className="text-lg font-black text-foreground">Report Issue & Feedback</CardTitle>
                <CardDescription className="text-xs">
                  Tell us about app bugs, billing discrepancies, or trip escorts.
                </CardDescription>
              </div>

              <CardContent className="p-6 space-y-5">
                {/* Subject Selector drop down wrapper */}
                <div className="space-y-2 relative">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest pl-1">
                    Select Issue Category
                  </label>
                  <Select value={subject} onValueChange={(val) => setSubject(val || "Trip Issue")}>
                    <SelectTrigger className="w-full h-12 rounded-2xl border border-border px-3.5 text-xs font-bold bg-card cursor-pointer justify-between">
                      <SelectValue placeholder="Select Issue Category" />
                    </SelectTrigger>
                    <SelectContent className="bg-popover border border-border rounded-xl">
                      <SelectItem value="Trip Issue">Trip escort / Guide issue</SelectItem>
                      <SelectItem value="App Bug">Application bug report</SelectItem>
                      <SelectItem value="Billing">Payment or Billing discrepancy</SelectItem>
                      <SelectItem value="Other">General support query</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                {/* Description textarea */}
                <div className="space-y-2">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest pl-1">
                    Explain Issue Context
                  </label>
                  <Textarea
                    placeholder="Explain what happened. Include specific terminal walkway locations or billing details..."
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    rows={5}
                    required
                    className="rounded-2xl border border-border resize-none p-3.5 focus:ring-1 focus:ring-primary text-xs"
                  />
                </div>
              </CardContent>

              <CardFooter className="p-6 border-t border-border/50 bg-muted/10">
                <Button
                  type="submit"
                  disabled={submitting}
                  className="w-full bg-primary text-primary-foreground hover:bg-primary/95 rounded-2xl py-5 font-black text-xs shadow-lg hover:scale-[1.01] transition-all cursor-pointer flex items-center justify-center gap-1.5"
                >
                  <span>Submit Helpdesk Request</span>
                  <PaperPlaneRightIcon size={16} weight="fill" />
                </Button>
              </CardFooter>
            </Card>
          </form>
        </motion.div>
      )}
    </div>
  )
}
export default FeedbackPage
