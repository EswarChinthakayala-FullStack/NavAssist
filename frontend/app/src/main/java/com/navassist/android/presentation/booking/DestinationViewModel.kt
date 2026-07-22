package com.navassist.android.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.domain.repository.LocationRepository
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(FlowPreview::class)
@HiltViewModel
class DestinationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _searchResults = MutableStateFlow<UiState<List<LocationPoint>>>(UiState.Idle)
    val searchResults: StateFlow<UiState<List<LocationPoint>>> = _searchResults.asStateFlow()

    private val _selectedDestination = MutableStateFlow<LocationPoint?>(null)
    val selectedDestination: StateFlow<LocationPoint?> = _selectedDestination.asStateFlow()

    private val _calculatedDistance = MutableStateFlow(0.0)
    val calculatedDistance: StateFlow<Double> = _calculatedDistance.asStateFlow()

    private val _calculatedEtaMins = MutableStateFlow(0)
    val calculatedEtaMins: StateFlow<Int> = _calculatedEtaMins.asStateFlow()

    private val _primaryRoutePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val primaryRoutePoints: StateFlow<List<LatLng>> = _primaryRoutePoints.asStateFlow()

    private val _altRoutePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val altRoutePoints: StateFlow<List<LatLng>> = _altRoutePoints.asStateFlow()

    private var searchJob: Job? = null
    private var routeJob: Job? = null

    init {
        loadDefaultSuggestions()
        viewModelScope.launch {
            _searchQuery
                .debounce(250)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        loadDefaultSuggestions()
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    fun loadDefaultSuggestions() {
        val defaultSuggestions = listOf(
            LocationPoint(12.9716, 77.5946, "Devanahalli, Bengaluru, Karnataka", "Kempegowda International Airport"),
            LocationPoint(17.3850, 78.4867, "Bengaluru, Karnataka", "MG Road"),
            LocationPoint(12.9279, 77.6271, "Indiranagar, Bengaluru, Karnataka", "Indiranagar 100 Feet Road"),
            LocationPoint(12.9352, 77.6245, "Koramangala, Bengaluru, Karnataka", "Koramangala 4th Block"),
            LocationPoint(12.9784, 77.5701, "Sampangi Rama Nagar, Bengaluru, Karnataka", "Bengaluru City Railway Station")
        )
        _searchResults.value = UiState.Success(defaultSuggestions)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            searchJob?.cancel()
            loadDefaultSuggestions()
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        _searchResults.value = UiState.Loading
        searchJob = viewModelScope.launch {
            val result = locationRepository.searchLocations(query)
            result.onSuccess { points ->
                if (points.isNotEmpty()) {
                    _searchResults.value = UiState.Success(points)
                } else {
                    loadDefaultSuggestions()
                }
            }.onFailure {
                loadDefaultSuggestions()
            }
        }
    }

    fun selectDestination(pickup: LocationPoint?, destination: LocationPoint) {
        _selectedDestination.value = destination
        if (pickup != null) {
            val haversineDist = calculateDistanceKm(pickup.latitude, pickup.longitude, destination.latitude, destination.longitude)
            _calculatedDistance.value = haversineDist
            _calculatedEtaMins.value = Math.max(5, (haversineDist * 2.5).toInt())

            // Fetch OSRM Driving Route
            fetchOsrmRoute(pickup, destination)
        }
    }

    fun reverseGeocodeLocation(lat: Double, lng: Double, pickup: LocationPoint?) {
        viewModelScope.launch {
            val result = locationRepository.reverseGeocode(lat, lng)
            result.onSuccess { point ->
                selectDestination(pickup, point)
            }.onFailure {
                selectDestination(pickup, LocationPoint(lat, lng, "Market Street, Talluru, Prakasam, Andhra Pradesh", "Talluru"))
            }
        }
    }

    private fun fetchOsrmRoute(pickup: LocationPoint, dest: LocationPoint) {
        routeJob?.cancel()
        routeJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val osrmUrl = "https://router.project-osrm.org/route/v1/driving/" +
                        "${pickup.longitude},${pickup.latitude};${dest.longitude},${dest.latitude}" +
                        "?overview=full&geometries=geojson&alternatives=true"

                val connection = (URL(osrmUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 4000
                    readTimeout = 4000
                }

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    val routes = json.optJSONArray("routes")

                    if (routes != null && routes.length() > 0) {
                        // Route 1: Primary
                        val mainRouteObj = routes.getJSONObject(0)
                        val mainDistance = mainRouteObj.optDouble("distance", 0.0) / 1000.0
                        val mainDuration = (mainRouteObj.optDouble("duration", 0.0) / 60.0).toInt()

                        val mainCoords = mainRouteObj.getJSONObject("geometry").getJSONArray("coordinates")
                        val mainPoints = mutableListOf<LatLng>()
                        for (i in 0 until mainCoords.length()) {
                            val coordPair = mainCoords.getJSONArray(i)
                            mainPoints.add(LatLng(coordPair.getDouble(1), coordPair.getDouble(0)))
                        }

                        // Route 2: Alternative (if present)
                        val altPoints = mutableListOf<LatLng>()
                        if (routes.length() > 1) {
                            val altRouteObj = routes.getJSONObject(1)
                            val altCoords = altRouteObj.getJSONObject("geometry").getJSONArray("coordinates")
                            for (i in 0 until altCoords.length()) {
                                val coordPair = altCoords.getJSONArray(i)
                                altPoints.add(LatLng(coordPair.getDouble(1), coordPair.getDouble(0)))
                            }
                        }

                        withContext(Dispatchers.Main) {
                            if (mainDistance > 0) _calculatedDistance.value = mainDistance
                            if (mainDuration > 0) _calculatedEtaMins.value = Math.max(5, mainDuration)
                            _primaryRoutePoints.value = mainPoints
                            _altRoutePoints.value = altPoints
                        }
                        return@launch
                    }
                }
            } catch (e: Exception) {
                // Fallback route interpolation on network failure
            }

            // Fallback interpolation between pickup and destination
            val interpolatedPoints = generateInterpolatedRoute(pickup, dest)
            withContext(Dispatchers.Main) {
                _primaryRoutePoints.value = interpolatedPoints
                _altRoutePoints.value = emptyList()
            }
        }
    }

    private fun generateInterpolatedRoute(pickup: LocationPoint, dest: LocationPoint): List<LatLng> {
        val points = mutableListOf<LatLng>()
        val count = 25
        val latSpan = dest.latitude - pickup.latitude
        val lngSpan = dest.longitude - pickup.longitude

        for (i in 0..count) {
            val fraction = i.toDouble() / count
            // Slight curve offset
            val curveOffset = sin(fraction * Math.PI) * 0.005
            val lat = pickup.latitude + (latSpan * fraction) + curveOffset
            val lng = pickup.longitude + (lngSpan * fraction)
            points.add(LatLng(lat, lng))
        }
        return points
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
