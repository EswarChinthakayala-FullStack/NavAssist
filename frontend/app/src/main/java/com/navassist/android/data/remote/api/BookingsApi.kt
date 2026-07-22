package com.navassist.android.data.remote.api

import com.navassist.android.data.remote.dto.booking.*
import retrofit2.http.*

interface BookingsApi {
    @POST("bookings/")
    suspend fun createBooking(@Body request: BookingRequestDto): BookingResponseDto

    @GET("bookings/")
    suspend fun getBookings(): List<BookingResponseDto>

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") id: Int): BookingResponseDto

    @PATCH("bookings/{id}/accept")
    suspend fun acceptBooking(@Path("id") id: Int): BookingResponseDto

    @PATCH("bookings/{id}/reject")
    suspend fun rejectBooking(@Path("id") id: Int): BookingResponseDto

    @PATCH("bookings/{id}/status")
    suspend fun updateBookingStatus(
        @Path("id") id: Int,
        @Body request: BookingStatusUpdateDto
    ): BookingResponseDto

    @POST("bookings/fare-estimate")
    suspend fun calculateFareEstimate(@Body request: BookingRequestDto): FareEstimateDto
}
