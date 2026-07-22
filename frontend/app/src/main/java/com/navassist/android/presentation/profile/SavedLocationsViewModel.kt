package com.navassist.android.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.profile.adapter.SavedLocationItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedLocationsViewModel @Inject constructor() : ViewModel() {

    private val _locationsState = MutableStateFlow<UiState<List<SavedLocationItem>>>(UiState.Loading)
    val locationsState: StateFlow<UiState<List<SavedLocationItem>>> = _locationsState.asStateFlow()

    private val locationsList = mutableListOf(
        SavedLocationItem("1", "Home", "123 MG Road, Sector 4, Central District", "Near Metro Station", "🏠", 17.385, 78.486),
        SavedLocationItem("2", "Work", "Tech Park Tower B, 5th Floor", "Opposite City Mall", "🏢", 17.440, 78.380),
        SavedLocationItem("3", "Airport", "International Airport, Terminal 2 Entrance", "Departure Gate #3", "✈", 17.240, 78.420)
    )

    fun loadSavedLocations() {
        _locationsState.value = UiState.Loading
        viewModelScope.launch {
            _locationsState.value = UiState.Success(locationsList.toList())
        }
    }

    fun addLocation(label: String, address: String, landmark: String, iconSymbol: String) {
        val newLoc = SavedLocationItem(
            id = System.currentTimeMillis().toString(),
            label = label,
            address = address,
            landmark = landmark,
            iconSymbol = iconSymbol
        )
        locationsList.add(newLoc)
        _locationsState.value = UiState.Success(locationsList.toList())
    }
}
