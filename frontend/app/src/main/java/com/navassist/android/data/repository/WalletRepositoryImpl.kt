package com.navassist.android.data.repository

import com.navassist.android.data.remote.api.PaymentsApi
import com.navassist.android.data.remote.api.WalletApi
import com.navassist.android.data.remote.dto.payment.PaymentVerifyRequestDto
import com.navassist.android.data.remote.dto.payment.WalletTopupRequestDto
import com.navassist.android.data.remote.dto.payment.WalletTransactionDto
import com.navassist.android.domain.model.TransactionType
import com.navassist.android.domain.model.WalletTransaction
import com.navassist.android.domain.repository.WalletBalanceInfo
import com.navassist.android.domain.repository.WalletRepository
import com.navassist.android.domain.repository.WalletTopupOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletApi: WalletApi,
    private val paymentsApi: PaymentsApi
) : WalletRepository {

    override suspend fun getBalance(): Result<Double> {
        return try {
            val dto = walletApi.getWalletBalance()
            Result.success(dto.balance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFullBalanceInfo(): Result<WalletBalanceInfo> {
        return try {
            val dto = walletApi.getWalletBalance()
            Result.success(
                WalletBalanceInfo(
                    availableBalance = dto.balance,
                    pendingBalance = dto.pendingBalance,
                    cashbackBalance = dto.cashbackBalance,
                    currency = dto.currency,
                    lastUpdated = dto.lastUpdated ?: "Just now"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactions(): Result<List<WalletTransaction>> {
        return getTransactionsFiltered(null, null)
    }

    override suspend fun getTransactionsFiltered(
        filterType: String?,
        searchQuery: String?
    ): Result<List<WalletTransaction>> {
        return try {
            val dtos = walletApi.getTransactions(filterType, searchQuery)
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addFunds(amount: Double): Result<Double> {
        return try {
            val res = initiateTopupOrder(amount)
            res.map { it.amount }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun initiateTopupOrder(amount: Double): Result<WalletTopupOrder> {
        return try {
            val dto = walletApi.topup(WalletTopupRequestDto(amount))
            Result.success(
                WalletTopupOrder(
                    success = dto.success,
                    razorpayOrderId = dto.razorpayOrderId ?: "order_topup_${System.currentTimeMillis()}",
                    razorpayKeyId = dto.razorpayKeyId ?: "rzp_test_mockkey",
                    amount = dto.amount,
                    currency = dto.currency
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyTopupPayment(
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String
    ): Result<Boolean> {
        return try {
            val req = PaymentVerifyRequestDto(
                razorpayOrderId = razorpayOrderId,
                razorpayPaymentId = razorpayPaymentId,
                razorpaySignature = razorpaySignature
            )
            paymentsApi.verifyPayment(req)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun WalletTransactionDto.toDomain(): WalletTransaction {
    return WalletTransaction(
        id = id,
        amount = amount,
        type = if (type.uppercase() == "CREDIT") TransactionType.CREDIT else TransactionType.DEBIT,
        description = description,
        timestamp = timestamp
    )
}
