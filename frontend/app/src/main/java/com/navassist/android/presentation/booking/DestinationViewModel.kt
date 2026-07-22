package com.navassist.android.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.domain.repository.LocationRepository
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
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

    private var searchJob: Job? = null

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
            val dist = calculateDistanceKm(pickup.latitude, pickup.longitude, destination.latitude, destination.longitude)
            _calculatedDistance.value = dist
            _calculatedEtaMins.value = Math.max(5, (dist * 2.5).toInt())
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
