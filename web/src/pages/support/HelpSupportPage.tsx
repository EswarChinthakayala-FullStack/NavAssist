import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { supportService } from "@/services/support.service"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import {
  Accordion,
  AccordionItem,
  AccordionTrigger,
  AccordionContent
} from "@/components/ui/accordion"
import {
  HeadsetIcon,
  MagnifyingGlassIcon,
  TicketIcon,
  ClockAfternoonIcon,
  CircleIcon,
  CaretRightIcon,
  ChatCircleTextIcon,
  QuestionIcon
} from "@phosphor-icons/react"
import { toast } from "sonner"
import { motion } from "framer-motion"
import { useAuth } from "@/store/auth-context"

const formatISTDate = (dateStr: string) => {
  if (!dateStr) return ""
  const cleanStr = !dateStr.endsWith("Z") && !dateStr.includes("+") ? dateStr.replace(" ", "T") + "Z" : dateStr
  return new Date(cleanStr).toLocaleDateString('en-IN', { timeZone: 'Asia/Kolkata' })
}

function useFaqs() {
  const [faqs, setFaqs] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await supportService.getFaqs()
        setFaqs(res || [])
      } catch (err) {
        console.warn("Failed to load FAQs:", err)
      } finally {
        setLoading(false)
      }
    }
    fetch()
  }, [])

  return { faqs, loading }
}

function useTickets() {
  const [tickets, setTickets] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  const reload = async () => {
    try {
      const res = await supportService.listTickets()
      setTickets(res || [])
    } catch (err) {
      console.warn("Failed to load tickets:", err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    reload()
  }, [])

  return { tickets, loading, reload }
}

export function HelpSupportPage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const { faqs, loading: loadingFaqs } = useFaqs()
  const { tickets, loading: loadingTickets } = useTickets()

  const [search, setSearch] = useState("")

  const filteredFaqs = faqs.filter(
    (faq: any) =>
      faq.question.toLowerCase().includes(search.toLowerCase()) ||
      faq.answer.toLowerCase().includes(search.toLowerCase())
  )

  const groupedFaqs = filteredFaqs.reduce((acc: any, faq: any) => {
    const cat = faq.category || "General FAQs"
    if (!acc[cat]) acc[cat] = []
    acc[cat].push(faq)
    return acc
  }, {})

  const getTicketStatusBadge = (status: string) => {
    const s = status.toLowerCase()
    if (s === "open" || s === "active" || s === "pending") {
      return (
        <Badge className="bg-primary/10 text-primary hover:bg-primary/10 border-0 text-[9px] px-2 py-0.5 rounded-full font-bold">
          Open
        </Badge>
      )
    }
    return (
      <Badge className="bg-muted text-muted-foreground hover:bg-muted border-0 text-[9px] px-2 py-0.5 rounded-full font-bold">
        Closed
      </Badge>
    )
  }

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
                Support Hub
              </Badge>
              <Badge variant="outline" className="font-bold text-[10px] uppercase tracking-wider rounded-full">
                24/7 Desk Active
              </Badge>
            </div>
            <h3 className="font-black text-2xl mt-3 tracking-tight text-foreground">Help & Support Console</h3>
            <p className="text-xs text-muted-foreground mt-1">Explore helper tutorial guides, read FAQ solutions, or submit tickets for verification or payments.</p>
          </div>
          <Button
            onClick={() => navigate("/support/feedback")}
            className="bg-primary text-primary-foreground hover:bg-primary/95 rounded-xl py-4.5 px-6 font-extrabold text-xs shadow-md shrink-0 flex items-center gap-1.5 cursor-pointer hover:scale-[1.01] transition-transform"
          >
            <ChatCircleTextIcon size={16} weight="bold" />
            Create Support Ticket
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Side: FAQs & Search (8 columns) */}
        <div className="lg:col-span-8 space-y-6">
          {/* Search Bar */}
          <div className="relative text-left">
            <Input
              placeholder="Search help topics, answers, or questions..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="rounded-xl border border-border pl-10 pr-4 py-3.5 text-xs font-semibold"
            />
            <div className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none">
              <MagnifyingGlassIcon size={16} />
            </div>
          </div>

          <div className="space-y-4">
            <h4 className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground pl-1 flex items-center gap-1">
              <QuestionIcon size={14} />
              Frequently Asked Questions
            </h4>

            {loadingFaqs ? (
              <div className="h-[200px] w-full flex items-center justify-center bg-card border rounded-2xl">
                <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
              </div>
            ) : filteredFaqs.length === 0 ? (
              <Card className="border border-border/60 p-8 text-center rounded-2xl bg-card">
                <p className="text-xs text-muted-foreground">No matching FAQ topics found.</p>
              </Card>
            ) : (
              <div className="space-y-4">
                {Object.keys(groupedFaqs).map((catName) => (
                  <Card key={catName} className="border border-border/80 rounded-2xl overflow-hidden bg-card shadow-sm">
                    <CardHeader className="bg-muted/15 p-4 border-b border-border/40">
                      <h5 className="text-[10px] font-black uppercase tracking-widest text-primary leading-none">
                        {catName}
                      </h5>
                    </CardHeader>
                    <CardContent className="p-4">
                      <Accordion className="w-full">
                        {groupedFaqs[catName].map((faq: any) => (
                          <AccordionItem key={faq.id} value={`faq-${faq.id}`} className="border-b border-border/40 last:border-b-0">
                            <AccordionTrigger className="text-xs font-bold text-foreground py-3 text-left hover:text-primary transition-colors">
                              {faq.question}
                            </AccordionTrigger>
                            <AccordionContent className="text-[11px] text-muted-foreground leading-relaxed pb-4 pl-1">
                              {faq.answer}
                            </AccordionContent>
                          </AccordionItem>
                        ))}
                      </Accordion>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right Side: Active Support Tickets (4 columns) */}
        <div className="lg:col-span-4 space-y-4">
          <h4 className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground pl-1 flex items-center gap-1">
            <TicketIcon size={14} />
            {user?.role === "admin" ? "All Support Tickets" : "My Support Tickets"}
          </h4>

          {loadingTickets ? (
            <div className="h-[150px] w-full flex items-center justify-center bg-card border rounded-2xl">
              <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
            </div>
          ) : tickets.length === 0 ? (
            <Card className="border border-border/80 p-8 text-center rounded-2xl bg-card shadow-sm">
              <TicketIcon size={24} className="text-muted-foreground/45 mx-auto mb-2" />
              <p className="text-xs text-muted-foreground">
                {user?.role === "admin" ? "No support tickets found in the queue." : "You do not have any active support tickets."}
              </p>
            </Card>
          ) : (
            <div className="flex flex-col gap-3">
              {tickets.map((ticket) => (
                <div
                  key={ticket.id}
                  onClick={() => navigate(`/support/tickets/${ticket.id}`)}
                  className="p-4 bg-card border border-border/80 rounded-2xl flex items-center justify-between gap-4 cursor-pointer hover:bg-muted/30 transition-all shadow-sm"
                >
                  <div className="space-y-1 min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-foreground truncate">
                        {ticket.subject}
                      </span>
                      {getTicketStatusBadge(ticket.status)}
                    </div>
                    <p className="text-[10px] text-muted-foreground leading-tight truncate">
                      Ref ID: #TKT-{ticket.id} • {formatISTDate(ticket.created_at)} {ticket.user ? `• By: ${ticket.user.full_name} (${ticket.user.role})` : ""}
                    </p>
                  </div>
                  <CaretRightIcon size={14} className="text-muted-foreground shrink-0" />
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </motion.div>
  )
}
export default HelpSupportPage
