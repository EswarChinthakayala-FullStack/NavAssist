package com.navassist.android.domain.model

data class SavedLocation(
    val id: Int,
    val label: String,
    val customLabel: String? = null,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val placeId: String? = null
)
