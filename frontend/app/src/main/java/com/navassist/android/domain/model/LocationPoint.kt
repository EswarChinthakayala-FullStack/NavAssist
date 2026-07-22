package com.navassist.android.domain.model

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val addressName: String? = null,
    val name: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val address: String
        get() = addressName ?: name ?: "$latitude, $longitude"
}
