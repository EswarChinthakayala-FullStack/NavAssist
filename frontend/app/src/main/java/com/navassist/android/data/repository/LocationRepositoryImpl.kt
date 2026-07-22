package com.navassist.android.data.repository

import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.domain.repository.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor() : LocationRepository {

    override suspend fun searchLocations(query: String): Result<List<LocationPoint>> {
        val results = listOf(
            LocationPoint(17.3850, 78.4867, "Hyderabad, Telangana, India"),
            LocationPoint(12.9716, 77.5946, "Bengaluru, Karnataka, India"),
            LocationPoint(19.0760, 72.8777, "Mumbai, Maharashtra, India")
        ).filter { it.addressName?.contains(query, ignoreCase = true) == true }
        return Result.success(results)
    }

    override suspend fun geocodeAddress(address: String): Result<LocationPoint> {
        val point = LocationPoint(17.3850, 78.4867, address)
        return Result.success(point)
    }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<LocationPoint> {
        val point = LocationPoint(latitude, longitude, "Lat: $latitude, Lng: $longitude")
        return Result.success(point)
    }
}
