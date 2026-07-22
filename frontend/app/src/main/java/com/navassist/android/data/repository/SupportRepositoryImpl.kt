package com.navassist.android.data.repository

import com.navassist.android.domain.model.FaqItem
import com.navassist.android.domain.model.SupportTicket
import com.navassist.android.domain.repository.SupportRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportRepositoryImpl @Inject constructor() : SupportRepository {

    override suspend fun getFaqs(): Result<List<FaqItem>> {
        val faqs = listOf(
            FaqItem(1, "How do I book an assistant?", "Navigate to the home screen and select your pickup/destination.", "Booking"),
            FaqItem(2, "How do I pay?", "NavAssist supports Razorpay online payments, UPI, wallets, and cash.", "Payments"),
            FaqItem(3, "Is SOS active 24/7?", "Yes, SOS alerts immediately broadcast your live location to on-duty support.", "Safety")
        )
        return Result.success(faqs)
    }

    override suspend fun createSupportTicket(subject: String, description: String): Result<SupportTicket> {
        val ticket = SupportTicket(
            id = (1000..9999).random(),
            subject = subject,
            description = description,
            status = "OPEN",
            createdAt = System.currentTimeMillis().toString()
        )
        return Result.success(ticket)
    }

    override suspend fun getSupportTickets(): Result<List<SupportTicket>> {
        return Result.success(emptyList())
    }
}
