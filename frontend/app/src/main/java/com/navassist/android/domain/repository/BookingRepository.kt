package com.navassist.android.domain.repository

import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.LocationPoint

interface BookingRepository {
    suspend fun createBooking(pickup: LocationPoint, destination: LocationPoint, fare: Double): Result<Booking>
    suspend fun getBookings(): Result<List<Booking>>
    suspend fun getBookingById(id: String): Result<Booking>
    suspend fun cancelBooking(id: String): Result<Booking>
}
