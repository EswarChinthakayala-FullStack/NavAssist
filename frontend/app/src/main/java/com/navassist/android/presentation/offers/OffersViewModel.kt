package com.navassist.android.presentation.offers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.Coupon
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OffersViewModel @Inject constructor() : ViewModel() {

    private val _couponsState = MutableStateFlow<UiState<List<Coupon>>>(UiState.Loading)
    val couponsState: StateFlow<UiState<List<Coupon>>> = _couponsState.asStateFlow()

    init {
        loadAvailableCoupons()
    }

    fun loadAvailableCoupons() {
        _couponsState.value = UiState.Loading
        viewModelScope.launch {
            val list = listOf(
                Coupon("1", "WELCOME50", "Welcome Discount", "Get ₹50 off on your first booking with NavAssist.", "Flat ₹50 OFF", "BEST OFFER", 0.0, true),
                Coupon("2", "SUPER100", "Super Saver Offer", "Save ₹100 on bookings above ₹400.", "Flat ₹100 OFF", "POPULAR", 400.0, true),
                Coupon("3", "PREMIUM200", "Executive Travel Perk", "Exclusive ₹200 discount for high-tier trips.", "Flat ₹200 OFF", "EXCLUSIVE", 600.0, false)
            )
            _couponsState.value = UiState.Success(list)
        }
    }
}
