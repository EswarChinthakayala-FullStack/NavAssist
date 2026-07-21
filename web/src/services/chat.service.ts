import api from "./api-client"

export interface MessageSender {
  id: number
  full_name: string
  role: string
  profile_photo_url?: string
}

export interface BookingMessage {
  id: number
  booking_id: number
  sender_id: number
  message_type: "text" | "image" | "location"
  content?: string
  media_url?: string
  latitude?: string
  longitude?: string
  is_read: boolean
  created_at: string
  sender?: MessageSender
}

export interface ConversationParticipant {
  user_id: number
  name: string
  role: string
  phone_number: string
  avatar_url?: string
  is_online: boolean
  avg_rating: number
  completed_trips: number
}

export const chatService = {
  async getMessages(bookingId: number): Promise<BookingMessage[]> {
    const res = await api.get(`/bookings/${bookingId}/messages`)
    return res.data
  },

  async sendMessage(
    bookingId: number,
    payload: {
      message_type?: "text" | "image" | "location"
      content?: string
      media_url?: string
      latitude?: string
      longitude?: string
    }
  ): Promise<BookingMessage> {
    const res = await api.post(`/bookings/${bookingId}/messages`, payload)
    return res.data
  },

  async markAsRead(messageId: number): Promise<void> {
    await api.patch(`/messages/${messageId}/read`)
  },

  async getConversationParticipant(bookingId: number): Promise<ConversationParticipant> {
    const res = await api.get(`/bookings/${bookingId}/conversation`)
    return res.data
  }
}

export default chatService
