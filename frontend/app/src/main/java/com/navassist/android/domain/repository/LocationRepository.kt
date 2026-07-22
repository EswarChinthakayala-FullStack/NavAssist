package com.navassist.android.domain.repository

import com.navassist.android.domain.model.LocationPoint

interface LocationRepository {
    suspend fun searchLocations(query: String): Result<List<LocationPoint>>
    suspend fun geocodeAddress(address: String): Result<LocationPoint>
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<LocationPoint>
}
