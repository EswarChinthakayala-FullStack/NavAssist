package com.navassist.android.domain.usecase.booking

import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.domain.repository.BookingRepository
import javax.inject.Inject

class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(
        pickup: LocationPoint,
        destination: LocationPoint,
        fare: Double
    ): Result<Booking> {
        if (fare <= 0) {
            return Result.failure(IllegalArgumentException("Fare amount must be greater than zero."))
        }
        return bookingRepository.createBooking(pickup, destination, fare)
    }
}
