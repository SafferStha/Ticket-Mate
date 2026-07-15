@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.individual_project.ui.viewmodel

import com.example.individual_project.rules.MainDispatcherRule
import com.example.individual_project.data.model.ThemeMode
import com.example.individual_project.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: SettingsRepository = mock()

    private fun createViewModel(): SettingsViewModel {
        doAnswer { flowOf(ThemeMode.SYSTEM) }.`when`(repository).themeMode
        doAnswer { flowOf(true) }.`when`(repository).notificationsEnabled
        doAnswer { flowOf(true) }.`when`(repository).bookingUpdatesEnabled
        doAnswer { flowOf(true) }.`when`(repository).paymentUpdatesEnabled
        doAnswer { flowOf(true) }.`when`(repository).eventRemindersEnabled
        doAnswer { flowOf(false) }.`when`(repository).promotionalEnabled
        return SettingsViewModel(repository)
    }

    @Test
    fun setThemeMode_success_test() = runTest {
        doAnswer { Unit }.`when`(repository).setThemeMode(eq(ThemeMode.DARK))
        val viewModel = createViewModel()

        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        verify(repository).setThemeMode(eq(ThemeMode.DARK))
        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.value)
    }

    @Test
    fun setNotificationsEnabled_success_test() = runTest {
        doAnswer { Unit }.`when`(repository).setNotificationsEnabled(eq(false))
        val viewModel = createViewModel()

        viewModel.setNotificationsEnabled(false)
        advanceUntilIdle()

        verify(repository).setNotificationsEnabled(eq(false))
        assertTrue(viewModel.notificationsEnabled.value)
    }

    @Test
    fun setBookingUpdatesEnabled_success_test() = runTest {
        doAnswer { Unit }.`when`(repository).setBookingUpdatesEnabled(eq(false))
        val viewModel = createViewModel()

        viewModel.setBookingUpdatesEnabled(false)
        advanceUntilIdle()

        verify(repository).setBookingUpdatesEnabled(eq(false))
        assertTrue(viewModel.bookingUpdatesEnabled.value)
    }

    @Test
    fun setPaymentUpdatesEnabled_success_test() = runTest {
        doAnswer { Unit }.`when`(repository).setPaymentUpdatesEnabled(eq(false))
        val viewModel = createViewModel()

        viewModel.setPaymentUpdatesEnabled(false)
        advanceUntilIdle()

        verify(repository).setPaymentUpdatesEnabled(eq(false))
        assertTrue(viewModel.paymentUpdatesEnabled.value)
    }

    @Test
    fun setEventRemindersEnabled_success_test() = runTest {
        doAnswer { Unit }.`when`(repository).setEventRemindersEnabled(eq(false))
        val viewModel = createViewModel()

        viewModel.setEventRemindersEnabled(false)
        advanceUntilIdle()

        verify(repository).setEventRemindersEnabled(eq(false))
        assertTrue(viewModel.eventRemindersEnabled.value)
    }

    @Test
    fun setPromotionalEnabled_success_test() = runTest {
        doAnswer { Unit }.`when`(repository).setPromotionalEnabled(eq(true))
        val viewModel = createViewModel()

        viewModel.setPromotionalEnabled(true)
        advanceUntilIdle()

        verify(repository).setPromotionalEnabled(eq(true))
        assertTrue(!viewModel.promotionalEnabled.value)
    }
}
