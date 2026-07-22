package com.navassist.android.domain.model

data class SupportTicket(
    val id: Int,
    val subject: String,
    val description: String,
    val status: String,
    val createdAt: String
)

data class FaqItem(
    val id: Int,
    val question: String,
    val answer: String,
    val category: String
)
