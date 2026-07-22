package com.navassist.android.domain.repository

import com.navassist.android.data.local.datastore.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val appSettings: Flow<AppSettings>
    suspend fun setThemeMode(mode: String)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
}
