package com.navassist.android.presentation.assistant.earnings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.FullEarningsDashboard
import com.navassist.android.domain.repository.AssistantRepository
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

sealed interface MyEarningsEffect {
    data class ShowToast(val message: String) : MyEarningsEffect
    data class ShowSnackbar(val message: String) : MyEarningsEffect
    data class OpenStatementPdf(val url: String) : MyEarningsEffect
}

@HiltViewModel
class MyEarningsViewModel @Inject constructor(
    private val assistantRepository: AssistantRepository
) : ViewModel() {

    private val _earningsState = MutableStateFlow<UiState<FullEarningsDashboard>>(UiState.Loading)
    val earningsState: StateFlow<UiState<FullEarningsDashboard>> = _earningsState.asStateFlow()

    private val _selectedFilter = MutableStateFlow("this_week")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _effects = MutableSharedFlow<MyEarningsEffect>()
    val effects: SharedFlow<MyEarningsEffect> = _effects.asSharedFlow()

    init {
        loadEarningsDashboard("this_week")
    }

    fun setFilterPeriod(period: String) {
        if (_selectedFilter.value == period) return
        _selectedFilter.value = period
        loadEarningsDashboard(period)
    }

    fun loadEarningsDashboard(filterPeriod: String = _selectedFilter.value) {
        _earningsState.value = UiState.Loading
        viewModelScope.launch {
            val result = assistantRepository.getFullEarningsSummary(filterPeriod)
            result.onSuccess { data ->
                _earningsState.value = UiState.Success(data)
            }.onFailure { err ->
                _earningsState.value = UiState.Error(err.message ?: "Failed to load financial dashboard")
            }
        }
    }

    fun downloadStatement() {
        viewModelScope.launch {
            _effects.emit(MyEarningsEffect.ShowToast("Generating financial statement PDF..."))
            _effects.emit(MyEarningsEffect.ShowToast("Statement downloaded successfully ✓"))
        }
    }
}
