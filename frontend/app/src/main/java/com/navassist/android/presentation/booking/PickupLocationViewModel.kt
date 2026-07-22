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

@OptIn(FlowPreview::class)
@HiltViewModel
class PickupLocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _searchResults = MutableStateFlow<UiState<List<LocationPoint>>>(UiState.Idle)
    val searchResults: StateFlow<UiState<List<LocationPoint>>> = _searchResults.asStateFlow()

    private val _selectedPickup = MutableStateFlow<LocationPoint?>(null)
    val selectedPickup: StateFlow<LocationPoint?> = _selectedPickup.asStateFlow()

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
                _searchResults.value = UiState.Error(error.message ?: "Failed to find locations")
            }
        }
    }

    fun selectPickupLocation(point: LocationPoint) {
        _selectedPickup.value = point
    }

    fun reverseGeocodeLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            val result = locationRepository.reverseGeocode(lat, lng)
            result.onSuccess { point ->
                _selectedPickup.value = point
            }.onFailure {
                _selectedPickup.value = LocationPoint(lat, lng, "Selected Location ($lat, $lng)")
            }
        }
    }
}
