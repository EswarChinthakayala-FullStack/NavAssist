package com.navassist.android.domain.model

data class WalletTransaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val description: String,
    val timestamp: String,
    val status: String = "SUCCESS"
)

enum class TransactionType {
    CREDIT,
    DEBIT
}
