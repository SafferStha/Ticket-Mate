@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.individual_project.auth.AdminStateManager
import com.example.individual_project.data.model.Event
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.rules.MainDispatcherRule
import com.example.individual_project.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class AdminEventViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val eventRepository: EventRepository = mock()
    private val adminStateManager: AdminStateManager = mock()
    private val isAdminFlow = MutableStateFlow(true)

    private val event = Event(
        id = "event1", title = "Kathmandu Music Festival", venue = "Tundikhel",
        city = "Kathmandu", date = "2026-09-15", price = 500.0, availableSeats = 100
    )

    @Before
    fun setUp(): Unit = kotlinx.coroutines.runBlocking {
        doAnswer { isAdminFlow.asStateFlow() }.`when`(adminStateManager).isAdmin
        doAnswer { Resource.Success(listOf(event)) }.`when`(eventRepository).fetchEvents()
    }

    private fun createViewModel(eventId: String? = null): AdminEventViewModel = AdminEventViewModel(
        eventRepository, adminStateManager,
        SavedStateHandle(if (eventId != null) mapOf("eventId" to eventId) else emptyMap())
    )

    @Test
    fun `admin event list loads on init`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(listOf(event), viewModel.dashboardState.value.events)
    }

    @Test
    fun `loadEvents failure surfaces the mapped error`() = runTest {
        doAnswer { Resource.Error("Failed to load events") }.`when`(eventRepository).fetchEvents()
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Failed to load events", viewModel.dashboardState.value.error)
    }

    @Test
    fun `create event validates required fields before calling the repository`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.submitForm()

        assertEquals("Title, venue, city, and date are required.", viewModel.formState.value.error)
        verify(eventRepository, times(0)).createEvent(any())
    }

    @Test
    fun `create event rejects a negative price`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onFieldChange {
            it.copy(title = "New Event", venue = "V", city = "C", date = "2026-01-01", price = "-5")
        }

        viewModel.submitForm()

        assertEquals("Price must be a non-negative number.", viewModel.formState.value.error)
        verify(eventRepository, times(0)).createEvent(any())
    }

    @Test
    fun `create event rejects a negative seat count`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onFieldChange {
            it.copy(title = "New Event", venue = "V", city = "C", date = "2026-01-01", price = "10", availableSeats = "-1")
        }

        viewModel.submitForm()

        assertEquals("Available seats must be a non-negative whole number.", viewModel.formState.value.error)
        verify(eventRepository, times(0)).createEvent(any())
    }

    @Test
    fun `create event success persists a new event via the repository`() = runTest {
        doAnswer { Resource.Success("event2") }.`when`(eventRepository).createEvent(any())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onFieldChange {
            it.copy(title = "New Event", venue = "Venue", city = "City", date = "2026-01-01", price = "100", availableSeats = "50")
        }

        viewModel.submitForm()
        advanceUntilIdle()

        verify(eventRepository).createEvent(any())
        assertTrue(viewModel.formState.value.saveSuccess)
    }

    @Test
    fun `edit event loads the existing event into the form and updates instead of creates`() = runTest {
        doAnswer { Resource.Success(event) }.`when`(eventRepository).fetchEventDetails(eq("event1"))
        doAnswer { Resource.Success(Unit) }.`when`(eventRepository).updateEvent(any())
        val viewModel = createViewModel(eventId = "event1")
        advanceUntilIdle()

        assertEquals("Kathmandu Music Festival", viewModel.formState.value.title)

        viewModel.submitForm()
        advanceUntilIdle()

        verify(eventRepository).updateEvent(any())
        verify(eventRepository, times(0)).createEvent(any())
    }

    @Test
    fun `delete event calls the repository and reloads the list when admin`() = runTest {
        doAnswer { Resource.Success(Unit) }.`when`(eventRepository).deleteEvent(eq("event1"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteEvent("event1")
        advanceUntilIdle()

        verify(eventRepository).deleteEvent(eq("event1"))
        verify(eventRepository, times(2)).fetchEvents()
    }

    @Test
    fun `delete event failure surfaces the mapped error`() = runTest {
        doAnswer { Resource.Error("Delete failed") }.`when`(eventRepository).deleteEvent(any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteEvent("event1")
        advanceUntilIdle()

        assertEquals("Delete failed", viewModel.dashboardState.value.error)
    }

    @Test
    fun `non-admin access is rejected for delete, submit, and seed operations`() = runTest {
        isAdminFlow.value = false
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onFieldChange {
            it.copy(title = "New Event", venue = "V", city = "C", date = "2026-01-01")
        }

        viewModel.deleteEvent("event1")
        viewModel.submitForm()
        viewModel.seedDatabase()
        advanceUntilIdle()

        verify(eventRepository, times(0)).deleteEvent(any())
        verify(eventRepository, times(0)).createEvent(any())
        assertEquals("Admin privileges required.", viewModel.formState.value.error)
        assertEquals("Admin privileges required to seed data.", viewModel.dashboardState.value.error)
    }

    @Test
    fun `onFieldChange clears any previous form error`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.submitForm() // populates a validation error
        assertEquals("Title, venue, city, and date are required.", viewModel.formState.value.error)

        viewModel.onFieldChange { it.copy(title = "New title") }

        assertNull(viewModel.formState.value.error)
        assertEquals("New title", viewModel.formState.value.title)
    }
}
