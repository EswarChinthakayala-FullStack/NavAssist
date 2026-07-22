package com.navassist.android.domain.usecase.booking

import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.repository.BookingRepository
import javax.inject.Inject

class GetBookingsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(): Result<List<Booking>> {
        return bookingRepository.getBookings()
    }
}
