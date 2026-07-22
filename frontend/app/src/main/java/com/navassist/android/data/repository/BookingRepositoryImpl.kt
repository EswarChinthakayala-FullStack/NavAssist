package com.navassist.android.data.repository

import com.navassist.android.data.remote.api.BookingsApi
import com.navassist.android.data.remote.dto.booking.BookingRequestDto
import com.navassist.android.data.remote.dto.booking.BookingResponseDto
import com.navassist.android.data.remote.dto.booking.BookingStatusUpdateDto
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.BookingStatus
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.domain.repository.BookingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val bookingsApi: BookingsApi
) : BookingRepository {

    override suspend fun createBooking(
        pickup: LocationPoint,
        destination: LocationPoint,
        fare: Double
    ): Result<Booking> {
        return try {
            val request = BookingRequestDto(
                pickupLatitude = pickup.latitude,
                pickupLongitude = pickup.longitude,
                pickupAddress = pickup.addressName ?: "Current Pickup Location",
                destinationLatitude = destination.latitude,
                destinationLongitude = destination.longitude,
                destinationAddress = destination.addressName ?: "Destination Address"
            )
            val dto = bookingsApi.createBooking(request)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBookings(): Result<List<Booking>> {
        return try {
            val dtos = bookingsApi.getBookings()
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBookingById(id: String): Result<Booking> {
        return try {
            val numericId = id.toIntOrNull() ?: 1
            val dto = bookingsApi.getBookingById(numericId)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelBooking(id: String): Result<Booking> {
        return try {
            val numericId = id.toIntOrNull() ?: 1
            val dto = bookingsApi.updateBookingStatus(
                id = numericId,
                request = BookingStatusUpdateDto(status = "CANCELLED", cancellationReason = "User cancelled from app")
            )
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun BookingResponseDto.toDomain(): Booking {
    return Booking(
        id = id.toString(),
        guestId = guestId.toString(),
        assistantId = assistantId?.toString(),
        assistantName = assistantName,
        assistantPhoto = assistantAvatar,
        assistantPhone = assistantPhone,
        pickupLocation = LocationPoint(pickupLatitude, pickupLongitude, pickupAddress),
        destinationLocation = LocationPoint(destinationLatitude, destinationLongitude, destinationAddress),
        status = parseStatus(status),
        fare = fareAmount,
        createdAt = createdAt
    )
}

private fun parseStatus(statusStr: String): BookingStatus {
    return when (statusStr.uppercase()) {
        "ACCEPTED" -> BookingStatus.ACCEPTED
        "ONGOING" -> BookingStatus.ONGOING
        "COMPLETED" -> BookingStatus.COMPLETED
        "CANCELLED" -> BookingStatus.CANCELLED
        else -> BookingStatus.PENDING
    }
}
