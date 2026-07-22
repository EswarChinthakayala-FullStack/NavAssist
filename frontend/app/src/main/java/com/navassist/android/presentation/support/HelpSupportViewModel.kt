package com.navassist.android.presentation.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.support.adapter.FaqItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpSupportViewModel @Inject constructor() : ViewModel() {

    private val _faqState = MutableStateFlow<UiState<List<FaqItem>>>(UiState.Loading)
    val faqState: StateFlow<UiState<List<FaqItem>>> = _faqState.asStateFlow()

    private val faqsList = listOf(
        FaqItem("1", "How do I verify my travel assistant upon arrival?", "Your assigned travel assistant will ask for the 4-digit OTP generated on your screen before starting the journey. Never share the OTP until you confirm their identity badge.", "Safety"),
        FaqItem("2", "What payment methods are supported for booking?", "NavAssist accepts Credit/Debit Cards, UPI, Net Banking, and NavAssist Wallet balances.", "Payments"),
        FaqItem("3", "How do I trigger Emergency SOS during an active journey?", "Swipe or press the red SOS floating button on the Live Navigation screen to instantly dispatch your live location to safety agents and emergency contacts.", "Emergency")
    )

    fun loadFaqs() {
        _faqState.value = UiState.Loading
        viewModelScope.launch {
            _faqState.value = UiState.Success(faqsList)
        }
    }
}
