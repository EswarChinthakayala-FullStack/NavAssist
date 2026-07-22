package com.navassist.android.domain.repository

import com.navassist.android.domain.model.FaqItem
import com.navassist.android.domain.model.SupportTicket

interface SupportRepository {
    suspend fun getFaqs(): Result<List<FaqItem>>
    suspend fun createSupportTicket(subject: String, description: String): Result<SupportTicket>
    suspend fun getSupportTickets(): Result<List<SupportTicket>>
}
