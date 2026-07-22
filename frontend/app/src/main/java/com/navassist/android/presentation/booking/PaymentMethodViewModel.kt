package com.navassist.android.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.PaymentMethod
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentMethodViewModel @Inject constructor() : ViewModel() {

    private val _walletBalanceState = MutableStateFlow<UiState<Double>>(UiState.Loading)
    val walletBalanceState: StateFlow<UiState<Double>> = _walletBalanceState.asStateFlow()

    private val _paymentState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val paymentState: StateFlow<UiState<String>> = _paymentState.asStateFlow()

    init {
        loadWalletBalance()
    }

    fun loadWalletBalance() {
        _walletBalanceState.value = UiState.Loading
        viewModelScope.launch {
            _walletBalanceState.value = UiState.Success(1245.0)
        }
    }

    fun processPayment(selectedMethodId: String) {
        _paymentState.value = UiState.Loading
        viewModelScope.launch {
            _paymentState.value = UiState.Success("order_xyz123")
        }
    }
}
