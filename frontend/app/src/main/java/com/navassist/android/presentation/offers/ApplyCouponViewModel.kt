package com.navassist.android.presentation.offers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CouponResult(
    val code: String,
    val discountAmount: Double,
    val isApplied: Boolean
)

@HiltViewModel
class ApplyCouponViewModel @Inject constructor() : ViewModel() {

    private val _applyState = MutableStateFlow<UiState<CouponResult>>(UiState.Idle)
    val applyState: StateFlow<UiState<CouponResult>> = _applyState.asStateFlow()

    fun validateAndApplyCoupon(code: String) {
        if (code.isBlank()) {
            _applyState.value = UiState.Error("Please enter a valid promo code")
            return
        }

        _applyState.value = UiState.Loading
        viewModelScope.launch {
            val upperCode = code.uppercase().trim()
            if (upperCode == "WELCOME50") {
                _applyState.value = UiState.Success(CouponResult(upperCode, 50.0, true))
            } else if (upperCode == "SUPER100") {
                _applyState.value = UiState.Success(CouponResult(upperCode, 100.0, true))
            } else {
                _applyState.value = UiState.Error("This coupon code is invalid or expired")
            }
        }
    }
}
