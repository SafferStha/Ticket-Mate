package com.example.individual_project.testdi

import com.example.individual_project.data.model.ThemeMode
import com.example.individual_project.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory stand-in for [SettingsRepository]. Backed by StateFlows instead of DataStore. */
class FakeSettingsRepository : SettingsRepository {

    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
    private val notificationsEnabledFlow = MutableStateFlow(true)
    private val bookingUpdatesEnabledFlow = MutableStateFlow(true)
    private val paymentUpdatesEnabledFlow = MutableStateFlow(true)
    private val eventRemindersEnabledFlow = MutableStateFlow(true)
    private val promotionalEnabledFlow = MutableStateFlow(false)

    override val themeMode = themeModeFlow
    override suspend fun setThemeMode(mode: ThemeMode) { themeModeFlow.value = mode }

    override val notificationsEnabled = notificationsEnabledFlow
    override suspend fun setNotificationsEnabled(enabled: Boolean) { notificationsEnabledFlow.value = enabled }

    override val bookingUpdatesEnabled = bookingUpdatesEnabledFlow
    override suspend fun setBookingUpdatesEnabled(enabled: Boolean) { bookingUpdatesEnabledFlow.value = enabled }

    override val paymentUpdatesEnabled = paymentUpdatesEnabledFlow
    override suspend fun setPaymentUpdatesEnabled(enabled: Boolean) { paymentUpdatesEnabledFlow.value = enabled }

    override val eventRemindersEnabled = eventRemindersEnabledFlow
    override suspend fun setEventRemindersEnabled(enabled: Boolean) { eventRemindersEnabledFlow.value = enabled }

    override val promotionalEnabled = promotionalEnabledFlow
    override suspend fun setPromotionalEnabled(enabled: Boolean) { promotionalEnabledFlow.value = enabled }

    fun reset() {
        themeModeFlow.value = ThemeMode.SYSTEM
        notificationsEnabledFlow.value = true
        bookingUpdatesEnabledFlow.value = true
        paymentUpdatesEnabledFlow.value = true
        eventRemindersEnabledFlow.value = true
        promotionalEnabledFlow.value = false
    }
}
