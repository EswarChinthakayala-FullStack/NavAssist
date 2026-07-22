package com.navassist.android.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.FareEstimate
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PriceEstimateViewModel @Inject constructor() : ViewModel() {

    private val _estimateState = MutableStateFlow<UiState<FareEstimate>>(UiState.Loading)
    val estimateState: StateFlow<UiState<FareEstimate>> = _estimateState.asStateFlow()

    fun loadFareEstimate(pickupLat: Double, pickupLng: Double, destLat: Double, destLng: Double) {
        _estimateState.value = UiState.Loading
        viewModelScope.launch {
            val mockEstimate = FareEstimate(
                baseFare = 180.0,
                distanceCharge = 40.0,
                timeCharge = 20.0,
                platformFee = 15.0,
                taxes = 10.0,
                surgeMultiplier = 1.0,
                couponDiscount = 0.0,
                totalFare = 265.0
            )
            _estimateState.value = UiState.Success(mockEstimate)
        }
    }
}
