package com.navassist.android.data.repository

import com.navassist.android.data.remote.api.AssistantsApi
import com.navassist.android.data.remote.api.BookingsApi
import com.navassist.android.data.remote.dto.assistant.AssistantLocationDto
import com.navassist.android.data.remote.dto.assistant.AssistantNearbyDto
import com.navassist.android.data.remote.dto.assistant.AssistantStatusToggleDto
import com.navassist.android.data.remote.dto.booking.BookingResponseDto
import com.navassist.android.data.remote.dto.user.AssistantProfileDto
import com.navassist.android.domain.model.Assistant
import com.navassist.android.domain.model.AssistantDashboardStats
import com.navassist.android.domain.model.AssistantProfileData
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.BookingStatus
import com.navassist.android.domain.model.FullEarningsDashboard
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.domain.model.TodayEarnings
import com.navassist.android.domain.model.TripEarningsItem
import com.navassist.android.domain.repository.AssistantRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantRepositoryImpl @Inject constructor(
    private val assistantsApi: AssistantsApi,
    private val bookingsApi: BookingsApi
) : AssistantRepository {

    override suspend fun getNearbyAssistants(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<Assistant>> {
        return try {
            val dtos = assistantsApi.getNearbyAssistants(latitude, longitude, radiusKm)
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAssistantProfile(assistantId: Int): Result<Assistant> {
        return try {
            val dto = assistantsApi.getAssistantProfile(assistantId)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleOnlineStatus(isOnline: Boolean): Result<Boolean> {
        return try {
            val dto = assistantsApi.toggleOnlineStatus(AssistantStatusToggleDto(online = isOnline))
            Result.success(dto.isOnline)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLocation(
        latitude: Double,
        longitude: Double,
        heading: Double,
        speed: Double
    ): Result<Unit> {
        return try {
            assistantsApi.updateLocation(
                AssistantLocationDto(
                    assistantId = 0,
                    latitude = latitude,
                    longitude = longitude,
                    heading = heading,
                    speed = speed
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyAssistantProfile(): Result<AssistantProfileData> {
        return try {
            val dto = assistantsApi.getMyProfile()
            Result.success(
                AssistantProfileData(
                    id = dto.id.toString(),
                    userId = dto.userId.toString(),
                    name = dto.name ?: "NavAssist Guide",
                    photoUrl = dto.profilePhotoUrl,
                    rating = dto.avgRating.toFloat(),
                    totalTrips = dto.totalTrips,
                    trustScore = dto.trustScore.toFloat(),
                    verificationStatus = dto.verificationStatus,
                    profileCompletionPct = 100,
                    isOnline = dto.isOnline,
                    bio = dto.bio,
                    experienceYears = dto.experienceYears,
                    serviceRadiusKm = dto.serviceRadiusKm
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMyAssistantProfile(
        bio: String?,
        experienceYears: Int?,
        serviceRadiusKm: Double?
    ): Result<AssistantProfileData> {
        return try {
            val dto = assistantsApi.updateMyAssistantProfile(
                com.navassist.android.data.remote.dto.assistant.UpdateAssistantProfileRequestDto(
                    bio = bio,
                    experienceYears = experienceYears,
                    serviceRadiusKm = serviceRadiusKm
                )
            )
            Result.success(
                AssistantProfileData(
                    id = dto.id.toString(),
                    userId = dto.userId.toString(),
                    name = dto.name ?: "NavAssist Guide",
                    photoUrl = dto.profilePhotoUrl,
                    rating = dto.avgRating.toFloat(),
                    totalTrips = dto.totalTrips,
                    trustScore = dto.trustScore.toFloat(),
                    verificationStatus = dto.verificationStatus,
                    profileCompletionPct = 100,
                    isOnline = dto.isOnline,
                    bio = dto.bio,
                    experienceYears = dto.experienceYears,
                    serviceRadiusKm = dto.serviceRadiusKm
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDashboardStats(): Result<AssistantDashboardStats> {
        return try {
            val dto = assistantsApi.getDashboardStats()
            Result.success(
                AssistantDashboardStats(
                    todayTrips = dto.todayTrips,
                    todayEarnings = dto.todayEarnings,
                    rating = dto.rating,
                    acceptanceRate = dto.acceptanceRate,
                    completionRate = dto.completionRate,
                    onlineTimeHours = dto.onlineTimeHours
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTodayEarnings(): Result<TodayEarnings> {
        return try {
            val dto = assistantsApi.getTodayEarnings()
            Result.success(
                TodayEarnings(
                    todayEarningsInr = dto.todayEarningsInr,
                    completedTripsToday = dto.completedTripsToday,
                    averageFareInr = dto.averageFareInr,
                    weeklyProgressPct = dto.weeklyProgressPct
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFullEarningsSummary(filterPeriod: String): Result<FullEarningsDashboard> {
        return try {
            val dto = assistantsApi.getFullEarningsSummary(filterPeriod)
            Result.success(
                FullEarningsDashboard(
                    walletBalanceInr = dto.walletBalanceInr,
                    lifetimeEarningsInr = dto.lifetimeEarningsInr,
                    todayEarningsInr = dto.todayEarningsInr,
                    weeklyEarningsInr = dto.weeklyEarningsInr,
                    monthlyEarningsInr = dto.monthlyEarningsInr,
                    nextPayoutAmountInr = dto.nextPayoutAmountInr,
                    nextPayoutDate = dto.nextPayoutDate,
                    payoutStatus = dto.payoutStatus,
                    incentivesEarnedInr = dto.incentivesEarnedInr,
                    bonusesEarnedInr = dto.bonusesEarnedInr,
                    completedTrips = dto.completedTrips,
                    hasPayoutAccount = dto.hasPayoutAccount,
                    earningsHistory = dto.earningsHistory.map {
                        com.navassist.android.domain.model.TripEarningsItem(
                            bookingId = it.bookingId.toString(),
                            guestName = it.guestName,
                            date = it.date,
                            pickup = it.pickup,
                            destination = it.destination,
                            fareAmount = it.fareAmount,
                            netEarnings = it.netEarnings,
                            paymentMethod = it.paymentMethod,
                            status = it.status
                        )
                    }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getIncomingBookings(): Result<List<Booking>> {
        return try {
            val dtos = assistantsApi.getIncomingBookings()
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptBooking(bookingId: Int): Result<Booking> {
        return try {
            val dto = bookingsApi.acceptBooking(bookingId)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectBooking(bookingId: Int): Result<Booking> {
        return try {
            val dto = bookingsApi.rejectBooking(bookingId)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun AssistantNearbyDto.toDomain(): Assistant {
    return Assistant(
        id = id.toString(),
        userId = id.toString(),
        name = name,
        rating = rating.toFloat(),
        isAvailable = isOnline,
        currentLocation = LocationPoint(latitude, longitude)
    )
}

private fun AssistantProfileDto.toDomain(): Assistant {
    return Assistant(
        id = userId.toString(),
        userId = userId.toString(),
        name = name,
        photoUrl = profilePictureUrl,
        rating = 5.0f,
        isAvailable = onlineStatus.equals("ONLINE", ignoreCase = true),
        currentLocation = if (currentLatitude != null && currentLongitude != null) {
            LocationPoint(currentLatitude, currentLongitude)
        } else null
    )
}

private fun BookingResponseDto.toDomain(): Booking {
    return Booking(
        id = id.toString(),
        guestId = guestId.toString(),
        assistantId = assistantId?.toString(),
        guestName = guestName ?: "Passenger",
        guestPhoto = guestAvatar,
        guestPhone = guestPhone,
        pickupLocation = LocationPoint(pickupLatitude, pickupLongitude, pickupAddress),
        destinationLocation = LocationPoint(destinationLatitude, destinationLongitude, destinationAddress),
        status = parseBookingStatus(status),
        fare = fareAmount,
        currency = "INR",
        createdAt = createdAt
    )
}

private fun parseBookingStatus(statusStr: String): BookingStatus {
    return when (statusStr.uppercase()) {
        "PENDING", "SEARCHING" -> BookingStatus.PENDING
        "ASSIGNED", "ACCEPTED" -> BookingStatus.ACCEPTED
        "ENROUTE", "ARRIVED", "STARTED", "PICKED_UP", "ONGOING" -> BookingStatus.ONGOING
        "COMPLETED" -> BookingStatus.COMPLETED
        "CANCELLED" -> BookingStatus.CANCELLED
        else -> BookingStatus.PENDING
    }
}
