package com.navassist.android.data.remote.api

import com.navassist.android.data.remote.dto.payment.*
import retrofit2.http.*

interface PaymentsApi {
    @POST("payments/create-order")
    suspend fun createOrder(@Body request: PaymentOrderRequestDto): PaymentOrderResponseDto

    @POST("payments/booking/{booking_id}/retry")
    suspend fun retryOrder(@Path("booking_id") bookingId: Int): PaymentOrderResponseDto

    @POST("payments/verify")
    suspend fun verifyPayment(@Body request: PaymentVerifyRequestDto): PaymentOrderResponseDto

    @POST("payments/confirm-cash")
    suspend fun confirmCash(@Body request: PaymentOrderRequestDto): PaymentOrderResponseDto

    @POST("payments/{booking_id}/refund")
    suspend fun refundPayment(@Path("booking_id") bookingId: Int): PaymentOrderResponseDto
}
