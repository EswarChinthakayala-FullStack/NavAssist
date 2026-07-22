package com.navassist.android.data.remote.dto.booking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookingRequestDto(
    @SerialName("pickup_latitude") val pickupLatitude: Double,
    @SerialName("pickup_longitude") val pickupLongitude: Double,
    @SerialName("pickup_address") val pickupAddress: String,
    @SerialName("destination_latitude") val destinationLatitude: Double,
    @SerialName("destination_longitude") val destinationLongitude: Double,
    @SerialName("destination_address") val destinationAddress: String,
    @SerialName("assistant_id") val assistantId: Int? = null,
    @SerialName("scheduled_time") val scheduledTime: String? = null
)

@Serializable
data class BookingStatusHistoryDto(
    @SerialName("status") val status: String,
    @SerialName("changed_at") val changedAt: String,
    @SerialName("changed_by") val changedBy: Int? = null
)

@Serializable
data class BookingResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("guest_id") val guestId: Int,
    @SerialName("assistant_id") val assistantId: Int? = null,
    @SerialName("status") val status: String,
    @SerialName("pickup_latitude") val pickupLatitude: Double,
    @SerialName("pickup_longitude") val pickupLongitude: Double,
    @SerialName("pickup_address") val pickupAddress: String,
    @SerialName("destination_latitude") val destinationLatitude: Double,
    @SerialName("destination_longitude") val destinationLongitude: Double,
    @SerialName("destination_address") val destinationAddress: String,
    @SerialName("fare_amount") val fareAmount: Double,
    @SerialName("otp_start") val otpStart: String,
    @SerialName("guest_name") val guestName: String? = null,
    @SerialName("guest_phone") val guestPhone: String? = null,
    @SerialName("guest_avatar") val guestAvatar: String? = null,
    @SerialName("assistant_name") val assistantName: String? = null,
    @SerialName("assistant_phone") val assistantPhone: String? = null,
    @SerialName("assistant_avatar") val assistantAvatar: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("distance_km") val distanceKm: Double? = null,
    @SerialName("estimated_duration_min") val estimatedDurationMin: Int? = null,
    @SerialName("status_history") val statusHistory: List<BookingStatusHistoryDto>? = emptyList()
)

@Serializable
data class BookingStatusUpdateDto(
    @SerialName("status") val status: String,
    @SerialName("otp") val otp: String? = null,
    @SerialName("cancellation_reason") val cancellationReason: String? = null
)

@Serializable
data class FareEstimateDto(
    @SerialName("pickup_address") val pickupAddress: String,
    @SerialName("destination_address") val destinationAddress: String,
    @SerialName("distance_km") val distanceKm: Double,
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("base_fare") val baseFare: Double,
    @SerialName("distance_fare") val distanceFare: Double,
    @SerialName("time_fare") val timeFare: Double,
    @SerialName("waiting_charges") val waitingCharges: Double = 0.0,
    @SerialName("booking_fee") val bookingFee: Double = 15.0,
    @SerialName("subtotal") val subtotal: Double,
    @SerialName("surge_multiplier") val surgeMultiplier: Double = 1.0,
    @SerialName("surge_amount") val surgeAmount: Double = 0.0,
    @SerialName("taxes") val taxes: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("total_fare") val totalFare: Double,
    @SerialName("estimated_fare") val estimatedFare: Double
)
