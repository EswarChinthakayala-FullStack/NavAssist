package com.navassist.android.data.repository

import com.navassist.android.data.remote.api.PaymentsApi
import com.navassist.android.data.remote.dto.payment.PaymentOrderRequestDto
import com.navassist.android.domain.repository.PaymentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentsApi: PaymentsApi
) : PaymentRepository {

    override suspend fun processPayment(bookingId: String, amount: Double): Result<Boolean> {
        return try {
            val numericBookingId = bookingId.toIntOrNull() ?: 1
            val response = paymentsApi.createOrder(PaymentOrderRequestDto(bookingId = numericBookingId))
            Result.success(response.status.equals("COMPLETED", ignoreCase = true) || response.status.equals("SUCCESS", ignoreCase = true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refundPayment(bookingId: Int): Result<Boolean> {
        return try {
            paymentsApi.refundPayment(bookingId)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
