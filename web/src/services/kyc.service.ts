import api from "./api-client"

export const kycService = {
  async uploadDocuments(aadhaarNumber: string, docFront: File, docBack: File) {
    const formData = new FormData()
    formData.append("aadhaar_number", aadhaarNumber)
    formData.append("doc_front", docFront)
    formData.append("doc_back", docBack)

    const res = await api.post("/kyc/documents", formData, {
      headers: {
        "Content-Type": "multipart/form-data"
      }
    })
    return res.data
  },

  async getKycStatus() {
    const res = await api.get("/kyc/status")
    return res.data
  }
}
export default kycService
