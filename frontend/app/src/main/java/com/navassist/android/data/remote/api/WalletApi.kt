package com.navassist.android.data.remote.api

import com.navassist.android.data.remote.dto.payment.WalletBalanceResponseDto
import com.navassist.android.data.remote.dto.payment.WalletTopupRequestDto
import com.navassist.android.data.remote.dto.payment.WalletTopupResponseDto
import com.navassist.android.data.remote.dto.payment.WalletTransactionDto
import retrofit2.http.*

interface WalletApi {
    @GET("wallet/balance")
    suspend fun getWalletBalance(): WalletBalanceResponseDto

    @POST("wallet/topup")
    suspend fun topup(@Body request: WalletTopupRequestDto): WalletTopupResponseDto

    @GET("wallet/transactions")
    suspend fun getTransactions(
        @Query("filter_type") filterType: String? = null,
        @Query("query") searchQuery: String? = null
    ): List<WalletTransactionDto>

    @GET("wallet/transactions/{transaction_id}")
    suspend fun getTransactionDetail(@Path("transaction_id") transactionId: String): WalletTransactionDto
}
