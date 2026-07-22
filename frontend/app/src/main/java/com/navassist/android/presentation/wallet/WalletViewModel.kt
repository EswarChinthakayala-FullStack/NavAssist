package com.navassist.android.presentation.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.WalletTransaction
import com.navassist.android.domain.repository.WalletBalanceInfo
import com.navassist.android.domain.repository.WalletRepository
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WalletEffect {
    data class ShowToast(val message: String) : WalletEffect
    data class ShowSnackbar(val message: String) : WalletEffect
    data class LaunchRazorpayCheckout(
        val orderId: String,
        val razorpayKeyId: String,
        val amount: Double
    ) : WalletEffect
}

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _balanceState = MutableStateFlow<UiState<WalletBalanceInfo>>(UiState.Loading)
    val balanceState: StateFlow<UiState<WalletBalanceInfo>> = _balanceState.asStateFlow()

    private val _transactionsState = MutableStateFlow<UiState<List<WalletTransaction>>>(UiState.Loading)
    val transactionsState: StateFlow<UiState<List<WalletTransaction>>> = _transactionsState.asStateFlow()

    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _effects = MutableSharedFlow<WalletEffect>()
    val effects: SharedFlow<WalletEffect> = _effects.asSharedFlow()

    init {
        loadWalletData()
    }

    fun loadWalletData() {
        _balanceState.value = UiState.Loading
        _transactionsState.value = UiState.Loading

        viewModelScope.launch {
            val balanceRes = walletRepository.getFullBalanceInfo()
            balanceRes.onSuccess { info ->
                _balanceState.value = UiState.Success(info)
            }.onFailure { err ->
                _balanceState.value = UiState.Error(err.message ?: "Failed to load wallet balance")
            }

            loadTransactions(_selectedFilter.value)
        }
    }

    fun setFilter(filterType: String) {
        _selectedFilter.value = filterType
        loadTransactions(filterType)
    }

    fun loadTransactions(filterType: String = _selectedFilter.value, searchQuery: String? = null) {
        viewModelScope.launch {
            val transRes = walletRepository.getTransactionsFiltered(
                filterType = if (filterType == "all") null else filterType,
                searchQuery = searchQuery
            )
            transRes.onSuccess { list ->
                _transactionsState.value = UiState.Success(list)
            }.onFailure { err ->
                _transactionsState.value = UiState.Error(err.message ?: "Failed to load transactions")
            }
        }
    }

    fun initiateTopUp(amount: Double) {
        viewModelScope.launch {
            _effects.emit(WalletEffect.ShowToast("Creating Razorpay Payment Order..."))
            val orderRes = walletRepository.initiateTopupOrder(amount)
            orderRes.onSuccess { order ->
                _effects.emit(
                    WalletEffect.LaunchRazorpayCheckout(
                        orderId = order.razorpayOrderId ?: "order_topup_${System.currentTimeMillis()}",
                        razorpayKeyId = order.razorpayKeyId ?: "rzp_test_mockkey",
                        amount = amount
                    )
                )
            }.onFailure { err ->
                _effects.emit(WalletEffect.ShowSnackbar("Failed to create top-up order: ${err.message}"))
            }
        }
    }

    fun onRazorpayPaymentSuccess(razorpayOrderId: String, razorpayPaymentId: String, signature: String) {
        viewModelScope.launch {
            _effects.emit(WalletEffect.ShowToast("Verifying payment with backend..."))
            val verifyRes = walletRepository.verifyTopupPayment(razorpayOrderId, razorpayPaymentId, signature)
            verifyRes.onSuccess {
                _effects.emit(WalletEffect.ShowToast("Top-Up Successful! Funds added to wallet ✓"))
                loadWalletData()
            }.onFailure { err ->
                _effects.emit(WalletEffect.ShowSnackbar("Payment verification issue: ${err.message}"))
                loadWalletData()
            }
        }
    }

    fun onRazorpayPaymentError(code: Int, description: String?) {
        viewModelScope.launch {
            _effects.emit(WalletEffect.ShowSnackbar("Payment failed: ${description ?: "Cancelled by user"}"))
        }
    }
}
