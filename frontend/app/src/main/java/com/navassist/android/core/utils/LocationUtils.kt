package com.navassist.android.core.utils

import android.location.Location
import kotlin.math.*

object LocationUtils {
    fun calculateDistanceMeters(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLng, endLat, endLng, results)
        return results[0]
    }

    fun calculateDistanceKm(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Double {
        return calculateDistanceMeters(startLat, startLng, endLat, endLng) / 1000.0
    }

    fun calculateBearing(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Float {
        val startLatRad = Math.toRadians(startLat)
        val endLatRad = Math.toRadians(endLat)
        val dLngRad = Math.toRadians(endLng - startLng)

        val y = sin(dLngRad) * cos(endLatRad)
        val x = cos(startLatRad) * sin(endLatRad) - sin(startLatRad) * cos(endLatRad) * cos(dLngRad)
        val bearingRad = atan2(y, x)
        return ((Math.toDegrees(bearingRad) + 360) % 360).toFloat()
    }
}
