package com.navassist.android.domain.usecase.payment

import com.navassist.android.domain.repository.PaymentRepository
import javax.inject.Inject

class ProcessPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(bookingId: String, amount: Double): Result<Boolean> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Payment amount must be greater than zero."))
        }
        return paymentRepository.processPayment(bookingId, amount)
    }
}
