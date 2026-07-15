package com.example.individual_project.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.individual_project.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Not private: EventReminderWorker is instantiated directly by WorkManager, outside Hilt's
// graph, and reads these same keys off the shared appPreferencesDataStore instance directly.
internal object AppPreferenceKeys {
    val THEME_MODE            = stringPreferencesKey("theme_mode")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val BOOKING_UPDATES       = booleanPreferencesKey("booking_updates_enabled")
    val PAYMENT_UPDATES       = booleanPreferencesKey("payment_updates_enabled")
    val EVENT_REMINDERS       = booleanPreferencesKey("event_reminders_enabled")
    val PROMOTIONAL           = booleanPreferencesKey("promotional_enabled")
}

@Singleton
class AppPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[AppPreferenceKeys.THEME_MODE]?.let { raw ->
            runCatching { ThemeMode.valueOf(raw) }.getOrNull()
        } ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[AppPreferenceKeys.THEME_MODE] = mode.name }
    }

    val notificationsEnabled: Flow<Boolean> = boolFlow(AppPreferenceKeys.NOTIFICATIONS_ENABLED, default = true)
    suspend fun setNotificationsEnabled(enabled: Boolean) = setBool(AppPreferenceKeys.NOTIFICATIONS_ENABLED, enabled)

    val bookingUpdatesEnabled: Flow<Boolean> = boolFlow(AppPreferenceKeys.BOOKING_UPDATES, default = true)
    suspend fun setBookingUpdatesEnabled(enabled: Boolean) = setBool(AppPreferenceKeys.BOOKING_UPDATES, enabled)

    val paymentUpdatesEnabled: Flow<Boolean> = boolFlow(AppPreferenceKeys.PAYMENT_UPDATES, default = true)
    suspend fun setPaymentUpdatesEnabled(enabled: Boolean) = setBool(AppPreferenceKeys.PAYMENT_UPDATES, enabled)

    val eventRemindersEnabled: Flow<Boolean> = boolFlow(AppPreferenceKeys.EVENT_REMINDERS, default = true)
    suspend fun setEventRemindersEnabled(enabled: Boolean) = setBool(AppPreferenceKeys.EVENT_REMINDERS, enabled)

    val promotionalEnabled: Flow<Boolean> = boolFlow(AppPreferenceKeys.PROMOTIONAL, default = false)
    suspend fun setPromotionalEnabled(enabled: Boolean) = setBool(AppPreferenceKeys.PROMOTIONAL, enabled)

    private fun boolFlow(key: Preferences.Key<Boolean>, default: Boolean): Flow<Boolean> =
        dataStore.data.map { it[key] ?: default }

    private suspend fun setBool(key: Preferences.Key<Boolean>, value: Boolean) {
        dataStore.edit { it[key] = value }
    }
}
