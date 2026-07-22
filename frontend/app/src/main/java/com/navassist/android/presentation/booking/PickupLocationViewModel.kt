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

    fun selectPickupLocation(point: LocationPoint) {
        _selectedPickup.value = point
    }

    fun reverseGeocodeLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            val result = locationRepository.reverseGeocode(lat, lng)
            result.onSuccess { point ->
                _selectedPickup.value = point
            }.onFailure {
                _selectedPickup.value = LocationPoint(lat, lng, "Market Street, Talluru, Prakasam, Andhra Pradesh", "Talluru")
            }
        }
    }
}
