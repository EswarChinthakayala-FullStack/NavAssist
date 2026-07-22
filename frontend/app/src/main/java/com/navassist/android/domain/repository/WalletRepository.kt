package com.navassist.android.domain.repository

import com.navassist.android.domain.model.WalletTransaction

data class WalletBalanceInfo(
    val availableBalance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val cashbackBalance: Double = 0.0,
    val currency: String = "INR",
    val lastUpdated: String = "Just now"
)

data class WalletTopupOrder(
    val success: Boolean = true,
    val razorpayOrderId: String? = null,
    val razorpayKeyId: String? = null,
    val amount: Double = 0.0,
    val currency: String = "INR"
)

interface WalletRepository {
    suspend fun getBalance(): Result<Double>
    suspend fun getFullBalanceInfo(): Result<WalletBalanceInfo>
    suspend fun getTransactions(): Result<List<WalletTransaction>>
    suspend fun getTransactionsFiltered(filterType: String? = null, searchQuery: String? = null): Result<List<WalletTransaction>>
    suspend fun addFunds(amount: Double): Result<Double>
    suspend fun initiateTopupOrder(amount: Double): Result<WalletTopupOrder>
    suspend fun verifyTopupPayment(razorpayOrderId: String, razorpayPaymentId: String, razorpaySignature: String): Result<Boolean>
}
