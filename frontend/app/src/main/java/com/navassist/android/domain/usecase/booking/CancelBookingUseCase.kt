package com.navassist.android.domain.usecase.booking

import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.repository.BookingRepository
import javax.inject.Inject

class CancelBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(bookingId: String): Result<Booking> {
        if (bookingId.isBlank()) {
            return Result.failure(IllegalArgumentException("Booking ID cannot be blank."))
        }
        return bookingRepository.cancelBooking(bookingId)
    }
}
