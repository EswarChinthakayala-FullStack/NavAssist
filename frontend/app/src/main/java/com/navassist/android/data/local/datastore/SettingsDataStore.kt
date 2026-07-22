package com.navassist.android.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "navassist_settings")

data class AppSettings(
    val themeMode: String = "SYSTEM",
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val lastLatitude: Double? = null,
    val lastLongitude: Double? = null
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val appSettings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            themeMode = preferences[PreferenceKeys.THEME_MODE] ?: "SYSTEM",
            language = preferences[PreferenceKeys.LANGUAGE] ?: "en",
            notificationsEnabled = preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true,
            onboardingCompleted = preferences[PreferenceKeys.ONBOARDING_COMPLETED] ?: false,
            lastLatitude = preferences[PreferenceKeys.LAST_LATITUDE],
            lastLongitude = preferences[PreferenceKeys.LAST_LONGITUDE]
        )
    }

    suspend fun setThemeMode(mode: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = mode
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferenceKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun saveLastKnownLocation(latitude: Double, longitude: Double) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferenceKeys.LAST_LATITUDE] = latitude
            preferences[PreferenceKeys.LAST_LONGITUDE] = longitude
        }
    }
}
