package com.navassist.android.data.repository

import com.navassist.android.data.local.datastore.AppSettings
import com.navassist.android.data.local.datastore.SettingsDataStore
import com.navassist.android.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override val appSettings: Flow<AppSettings> = settingsDataStore.appSettings

    override suspend fun setThemeMode(mode: String) {
        settingsDataStore.setThemeMode(mode)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        settingsDataStore.setNotificationsEnabled(enabled)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        settingsDataStore.setOnboardingCompleted(completed)
    }
}
