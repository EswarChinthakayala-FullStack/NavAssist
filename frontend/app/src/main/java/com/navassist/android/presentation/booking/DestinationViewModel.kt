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
import kotlinx.coroutines.flow.filter
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
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.length < 2) {
            searchJob?.cancel()
            _searchResults.value = UiState.Idle
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        _searchResults.value = UiState.Loading
        searchJob = viewModelScope.launch {
            val result = locationRepository.searchLocations(query)
            result.onSuccess { points ->
                _searchResults.value = UiState.Success(points)
            }.onFailure { error ->
                _searchResults.value = UiState.Error(error.message ?: "Failed to find destinations")
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
