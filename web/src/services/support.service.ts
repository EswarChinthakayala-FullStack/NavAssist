import api from "./api-client"

export const supportService = {
  async getFaqs() {
    const res = await api.get("/support/faqs")
    return res.data
  },

  async createTicket(subject: string, description: string) {
    const res = await api.post("/support/tickets", { subject, description })
    return res.data
  },

  async listTickets() {
    const res = await api.get("/support/tickets")
    return res.data
  },

  async getTicketDetails(ticketId: number) {
    const res = await api.get(`/support/tickets/${ticketId}`)
    return res.data
  },

  async postTicketMessage(ticketId: number, message: string) {
    const res = await api.post(`/support/tickets/${ticketId}/messages`, { message })
    return res.data
  },

  async updateTicketStatus(ticketId: number, status: string) {
    const res = await api.patch(`/support/tickets/${ticketId}/status`, { status })
    return res.data
  }
}
export default supportService
