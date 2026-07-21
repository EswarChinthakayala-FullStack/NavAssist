import api from "./api-client"

export const walletService = {
  async getBalance() {
    const res = await api.get("/wallet/balance")
    return res.data
  },

  async topUp(amount: number) {
    const res = await api.post("/wallet/topup", { amount })
    return res.data
  },

  async getTransactions() {
    const res = await api.get("/wallet/transactions")
    return res.data
  }
}
export default walletService
