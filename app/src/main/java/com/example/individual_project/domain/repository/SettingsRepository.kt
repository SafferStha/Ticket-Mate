package com.example.individual_project.domain.repository

import com.example.individual_project.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * On-device app preferences (theme, notification toggles). Distinct from the user's Firebase
 * profile -- these are local to this install and never leave the device.
 */
interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)

    val notificationsEnabled: Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)

    val bookingUpdatesEnabled: Flow<Boolean>
    suspend fun setBookingUpdatesEnabled(enabled: Boolean)

    val paymentUpdatesEnabled: Flow<Boolean>
    suspend fun setPaymentUpdatesEnabled(enabled: Boolean)

    val eventRemindersEnabled: Flow<Boolean>
    suspend fun setEventRemindersEnabled(enabled: Boolean)

    val promotionalEnabled: Flow<Boolean>
    suspend fun setPromotionalEnabled(enabled: Boolean)
}
