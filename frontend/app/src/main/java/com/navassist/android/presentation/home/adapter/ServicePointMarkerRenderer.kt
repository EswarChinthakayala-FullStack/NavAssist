package com.navassist.android.presentation.home.adapter

import android.content.Context
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.annotations.MarkerOptions

class ServicePointMarkerRenderer(private val context: Context) {

    fun renderServicePoints(map: MapLibreMap, points: List<Pair<LatLng, String>>) {
        points.forEach { (latLng, title) ->
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(title)
            )
        }
    }
}
