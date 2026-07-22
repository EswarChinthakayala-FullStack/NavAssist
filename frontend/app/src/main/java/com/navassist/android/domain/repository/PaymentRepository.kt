package com.navassist.android.domain.repository

interface PaymentRepository {
    suspend fun processPayment(bookingId: String, amount: Double): Result<Boolean>
    suspend fun refundPayment(bookingId: Int): Result<Boolean>
}
