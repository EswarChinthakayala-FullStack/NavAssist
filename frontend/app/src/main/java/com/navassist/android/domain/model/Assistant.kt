package com.navassist.android.domain.model

data class Assistant(
    val id: String,
    val userId: String,
    val name: String,
    val photoUrl: String? = null,
    val rating: Float = 4.9f,
    val totalTrips: Int = 120,
    val vehicleDetails: String? = null,
    val isAvailable: Boolean = true,
    val currentLocation: LocationPoint? = null
)
