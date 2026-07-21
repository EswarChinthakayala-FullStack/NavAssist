import React, { useEffect, useState } from "react"
import { supportService } from "@/services/support.service"
import { ChatCircleDotsIcon, QuestionIcon, PlusIcon, SirenIcon } from "@phosphor-icons/react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { toast } from "sonner"

interface FaqItem {
  id: number
  question: string
  answer: string
}

export function SupportPage() {
  const [faqs, setFaqs] = useState<FaqItem[]>([])
  const [loadingFaqs, setLoadingFaqs] = useState(true)
  const [subject, setSubject] = useState("")
  const [description, setDescription] = useState("")
  const [submitting, setSubmitting] = useState(false)

  const fetchFaqs = async () => {
    try {
      const res = await supportService.getFaqs()
      setFaqs(res)
    } catch (err) {
      // Fallback local support data if endpoint fails
      setFaqs([
        { id: 1, question: "How does NavAssist keep me secure?", answer: "NavAssist vets all local assistants with identity Aadhaar documentation and police record clearances before they can join active listings." },
        { id: 2, question: "What should I do during an emergency?", answer: "Press the red Siren SOS button on the bottom of the map tracking window. It triggers location broadcasts to emergency contacts and alerts our support office." },
        { id: 3, question: "How is fare estimate calculated?", answer: "Estimates are calculated using a baseline base fee plus coordinates distance rate, duration travel metrics, surge flags, and coupon validation codes." }
      ])
    } finally {
      setLoadingFaqs(false)
    }
  }

  const handleCreateTicket = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      await supportService.createTicket(subject, description)
      toast.success("Support ticket created successfully!")
      setSubject("")
      setDescription("")
    } catch (err) {
      // Handled globally
    } finally {
      setSubmitting(false)
    }
  }

  useEffect(() => {
    fetchFaqs()
  }, [])

  return (
    <div className="max-w-xl mx-auto py-2 flex flex-col gap-8">
      {/* Help header */}
      <div>
        <h3 className="font-extrabold text-base leading-tight">Help & Support Desk</h3>
        <p className="text-[10px] text-muted-foreground mt-0.5">Access platform FAQs or open direct communication tickets with the support audit center.</p>
      </div>

      {/* Emergency contacts card */}
      <div className="p-4 bg-destructive/10 border border-destructive/20 rounded-xl flex gap-3.5 items-start">
        <div className="p-2.5 bg-destructive/25 text-destructive rounded-lg animate-pulse shrink-0">
          <SirenIcon size={20} weight="fill" />
        </div>
        <div>
          <h4 className="text-xs font-black uppercase tracking-wider text-destructive">24/7 Security Hotline</h4>
          <p className="text-[10px] text-muted-foreground mt-1 leading-relaxed">
            Need urgent assistance? Trigger the SOS panic switch inside your active trips map, or reach our emergency support direct phone number at <span className="font-extrabold text-destructive">+91 1800-456-7890</span>.
          </p>
        </div>
      </div>

      {/* FAQs lists */}
      <div>
        <div className="flex items-center gap-2 mb-4">
          <QuestionIcon size={18} className="text-primary" />
          <h4 className="text-xs font-black uppercase tracking-wider text-muted-foreground">Frequently Asked Questions</h4>
        </div>
        
        <div className="flex flex-col gap-3">
          {faqs.map((faq) => (
            <div key={faq.id} className="p-4 rounded-xl border border-border bg-card/30">
              <h5 className="text-xs font-bold text-foreground leading-snug">{faq.question}</h5>
              <p className="text-[10px] text-muted-foreground leading-relaxed mt-1.5">{faq.answer}</p>
            </div>
          ))}
        </div>
      </div>

      <hr className="border-border" />

      {/* Create Ticket */}
      <form onSubmit={handleCreateTicket} className="flex flex-col gap-4">
        <div className="flex items-center gap-2">
          <ChatCircleDotsIcon size={18} className="text-primary" />
          <h4 className="text-xs font-black uppercase tracking-wider text-muted-foreground">Submit Support Request</h4>
        </div>

        <div className="flex flex-col gap-1.5">
          <label htmlFor="subject" className="text-[10px] font-extrabold uppercase tracking-wider text-muted-foreground">Subject</label>
          <Input
            id="subject"
            type="text"
            required
            value={subject}
            onChange={(e) => setSubject(e.target.value)}
            placeholder="Describe the issue in a few words"
            disabled={submitting}
            className="rounded-xl h-10 text-xs font-semibold"
          />
        </div>

        <div className="flex flex-col gap-1.5">
          <label htmlFor="description" className="text-[10px] font-extrabold uppercase tracking-wider text-muted-foreground">Description Details</label>
          <Textarea
            id="description"
            required
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Provide complete details about the issue or question..."
            disabled={submitting}
            className="rounded-xl min-h-[100px] text-xs font-semibold"
          />
        </div>

        <Button
          type="submit"
          disabled={submitting}
          className="w-full py-5 rounded-xl font-bold bg-primary text-white text-xs shadow-sm hover:scale-102 transition-all"
        >
          {submitting ? "Submitting Ticket..." : "Submit Ticket"}
        </Button>
      </form>
    </div>
  )
}
export default SupportPage
