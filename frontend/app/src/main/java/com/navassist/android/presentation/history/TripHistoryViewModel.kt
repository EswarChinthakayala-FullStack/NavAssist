package com.navassist.android.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.history.adapter.TripHistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripHistoryViewModel @Inject constructor() : ViewModel() {

    private val _historyState = MutableStateFlow<UiState<List<TripHistoryItem>>>(UiState.Loading)
    val historyState: StateFlow<UiState<List<TripHistoryItem>>> = _historyState.asStateFlow()

    private val tripsList = listOf(
        TripHistoryItem("1", "Booking #BK_10293", "Central Station, Entrance #1", "Terminal 2 Entrance", "Vikram Sharma", "18.5 km", "$48.50", "July 20, 2026", "COMPLETED"),
        TripHistoryItem("2", "Booking #BK_10188", "Airport Express Plaza", "Hilton Grand Hotel", "Ananya Roy", "12.2 km", "$32.00", "July 18, 2026", "COMPLETED"),
        TripHistoryItem("3", "Booking #BK_10045", "City Mall Gate 4", "Tech Park Tower B", "Rahul Verma", "8.0 km", "$22.40", "July 12, 2026", "COMPLETED")
    )

    fun loadTripHistory() {
        _historyState.value = UiState.Loading
        viewModelScope.launch {
            _historyState.value = UiState.Success(tripsList)
        }
    }
}
