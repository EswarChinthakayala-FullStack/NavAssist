import api from "./api-client"

export interface BookingReport {
  id: number
  report_number: string
  booking_id: number
  reporter_id: number
  against_user_id?: number
  category: string
  severity: "low" | "medium" | "high" | "critical"
  status: "submitted" | "under_review" | "waiting_response" | "resolved" | "closed" | "rejected"
  description: string
  evidence_json?: { files: string[] }
  assigned_admin_id?: number
  resolution_notes?: string
  resolution_time?: string
  created_at: string
  updated_at: string
}

export const reportsService = {
  async submitReport(
    bookingId: number,
    payload: {
      category: string
      severity: "low" | "medium" | "high" | "critical"
      description: string
      evidence?: string[]
      against_user_id?: number
      gps_coordinates?: string
    }
  ): Promise<BookingReport> {
    const res = await api.post(`/bookings/${bookingId}/report`, payload)
    return res.data
  },

  async getBookingReports(bookingId: number): Promise<BookingReport[]> {
    const res = await api.get(`/bookings/${bookingId}/reports`)
    return res.data
  },

  async listAllReports(): Promise<BookingReport[]> {
    const res = await api.get(`/admin/reports/list`)
    return res.data
  },

  async resolveReport(
    reportId: number,
    status: string,
    notes: string
  ): Promise<BookingReport> {
    const res = await api.patch(`/admin/reports/${reportId}/resolve`, { status, notes })
    return res.data
  }
}

export default reportsService
