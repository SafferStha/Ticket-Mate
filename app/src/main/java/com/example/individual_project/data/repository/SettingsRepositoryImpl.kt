package com.example.individual_project.data.repository

import com.example.individual_project.data.local.AppPreferencesDataSource
import com.example.individual_project.data.model.ThemeMode
import com.example.individual_project.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferences: AppPreferencesDataSource
) : SettingsRepository {

    override val themeMode: Flow<ThemeMode> = preferences.themeMode
    override suspend fun setThemeMode(mode: ThemeMode) = preferences.setThemeMode(mode)

    override val notificationsEnabled: Flow<Boolean> = preferences.notificationsEnabled
    override suspend fun setNotificationsEnabled(enabled: Boolean) = preferences.setNotificationsEnabled(enabled)

    override val bookingUpdatesEnabled: Flow<Boolean> = preferences.bookingUpdatesEnabled
    override suspend fun setBookingUpdatesEnabled(enabled: Boolean) = preferences.setBookingUpdatesEnabled(enabled)

    override val paymentUpdatesEnabled: Flow<Boolean> = preferences.paymentUpdatesEnabled
    override suspend fun setPaymentUpdatesEnabled(enabled: Boolean) = preferences.setPaymentUpdatesEnabled(enabled)

    override val eventRemindersEnabled: Flow<Boolean> = preferences.eventRemindersEnabled
    override suspend fun setEventRemindersEnabled(enabled: Boolean) = preferences.setEventRemindersEnabled(enabled)

    override val promotionalEnabled: Flow<Boolean> = preferences.promotionalEnabled
    override suspend fun setPromotionalEnabled(enabled: Boolean) = preferences.setPromotionalEnabled(enabled)
}
