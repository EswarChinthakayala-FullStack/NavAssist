package com.navassist.android.data.remote.dto.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentOrderRequestDto(
    @SerialName("booking_id") val bookingId: Int,
    @SerialName("payment_method") val paymentMethod: String = "online"
)

@Serializable
data class PaymentVerifyRequestDto(
    @SerialName("razorpay_order_id") val razorpayOrderId: String,
    @SerialName("razorpay_payment_id") val razorpayPaymentId: String,
    @SerialName("razorpay_signature") val razorpaySignature: String
)

@Serializable
data class PaymentOrderResponseDto(
    @SerialName("id") val id: Int = 0,
    @SerialName("booking_id") val bookingId: Int = 0,
    @SerialName("gateway_order_id") val gatewayOrderId: String? = null,
    @SerialName("gateway_payment_id") val gatewayPaymentId: String? = null,
    @SerialName("razorpay_order_id") val razorpayOrderId: String? = null,
    @SerialName("razorpay_payment_id") val razorpayPaymentId: String? = null,
    @SerialName("razorpay_key_id") val razorpayKeyId: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("status") val status: String = "SUCCESS",
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("currency") val currency: String? = "INR",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

@Serializable
data class WalletBalanceResponseDto(
    @SerialName("balance") val balance: Double = 0.0,
    @SerialName("pending_balance") val pendingBalance: Double = 0.0,
    @SerialName("cashback_balance") val cashbackBalance: Double = 0.0,
    @SerialName("currency") val currency: String = "INR",
    @SerialName("last_updated") val lastUpdated: String? = null
)

@Serializable
data class WalletDto(
    @SerialName("user_id") val userId: Int = 0,
    @SerialName("balance") val balance: Double = 0.0,
    @SerialName("currency") val currency: String = "INR"
)

@Serializable
data class WalletTopupRequestDto(
    @SerialName("amount") val amount: Double
)

@Serializable
data class WalletTopupResponseDto(
    @SerialName("success") val success: Boolean = true,
    @SerialName("razorpay_order_id") val razorpayOrderId: String? = null,
    @SerialName("razorpay_key_id") val razorpayKeyId: String? = null,
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("currency") val currency: String = "INR",
    @SerialName("new_balance") val newBalance: Double = 0.0
)

@Serializable
data class WalletTransactionDto(
    @SerialName("id") val id: String = "",
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("type") val type: String = "CREDIT",
    @SerialName("description") val description: String = "",
    @SerialName("booking_id") val bookingId: String? = null,
    @SerialName("timestamp") val timestamp: String = "",
    @SerialName("status") val status: String = "SUCCESS",
    @SerialName("balance_after") val balanceAfter: Double = 0.0
)
