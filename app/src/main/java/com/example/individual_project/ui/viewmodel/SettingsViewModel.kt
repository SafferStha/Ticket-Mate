package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individual_project.data.model.ThemeMode
import com.example.individual_project.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val notificationsEnabled: StateFlow<Boolean> = settingsRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val bookingUpdatesEnabled: StateFlow<Boolean> = settingsRepository.bookingUpdatesEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val paymentUpdatesEnabled: StateFlow<Boolean> = settingsRepository.paymentUpdatesEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val eventRemindersEnabled: StateFlow<Boolean> = settingsRepository.eventRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val promotionalEnabled: StateFlow<Boolean> = settingsRepository.promotionalEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNotificationsEnabled(enabled) }
    }

    fun setBookingUpdatesEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setBookingUpdatesEnabled(enabled) }
    }

    fun setPaymentUpdatesEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setPaymentUpdatesEnabled(enabled) }
    }

    fun setEventRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setEventRemindersEnabled(enabled) }
    }

    fun setPromotionalEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setPromotionalEnabled(enabled) }
    }
}
