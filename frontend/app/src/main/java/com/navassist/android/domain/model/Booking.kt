package com.navassist.android.domain.model

data class Booking(
    val id: String,
    val guestId: String,
    val assistantId: String? = null,
    val assistantName: String? = null,
    val assistantPhoto: String? = null,
    val assistantPhone: String? = null,
    val guestName: String? = "Passenger",
    val guestPhoto: String? = null,
    val guestPhone: String? = null,
    val pickupLocation: LocationPoint,
    val destinationLocation: LocationPoint,
    val status: BookingStatus,
    val fare: Double,
    val currency: String = "INR",
    val estimatedMinutes: Int = 15,
    val createdAt: String = "",
    val completedAt: String? = null
)

val Booking.estimatedFare: Double
    get() = fare

enum class BookingStatus {
    PENDING,
    ACCEPTED,
    ONGOING,
    COMPLETED,
    CANCELLED
}
