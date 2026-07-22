package com.navassist.android.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.settings.adapter.SettingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _settingsState = MutableStateFlow<UiState<List<SettingItem>>>(UiState.Loading)
    val settingsState: StateFlow<UiState<List<SettingItem>>> = _settingsState.asStateFlow()

    private val settingsList = mutableListOf(
        SettingItem("1", "Booking Notifications", "Receive real-time push alerts for assistant arrivals", "🔔", true),
        SettingItem("2", "Live Location Sharing", "Allow trusted contacts to view active trip progress", "📍", true),
        SettingItem("3", "Dark Theme Mode", "Enable NavAssist monochrome dark appearance", "🌙", true),
        SettingItem("4", "Biometric Lock", "Require Fingerprint/Face ID to access payments", "🔒", false),
        SettingItem("5", "Promotions & Offers", "Receive special discounts and reward updates", "🎁", false),
        SettingItem("6", "Log Out", "Securely sign out of your NavAssist account", "🚪", false, false)
    )

    fun loadSettings() {
        _settingsState.value = UiState.Success(settingsList.toList())
    }

    fun toggleSetting(item: SettingItem, isChecked: Boolean) {
        val index = settingsList.indexOfFirst { it.id == item.id }
        if (index != -1) {
            settingsList[index] = settingsList[index].copy(isChecked = isChecked)
            _settingsState.value = UiState.Success(settingsList.toList())
        }
    }
}
