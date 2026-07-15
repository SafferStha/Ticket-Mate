@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.individual_project.rules.MainDispatcherRule
import com.example.individual_project.data.model.Event
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.utils.Resource
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

class EventViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: EventRepository = mock()

    private val event = Event(
        id = "event1",
        title = "Kathmandu Music Festival",
        category = "Concerts"
    )

    private fun createViewModel(eventId: String = "event1"): EventViewModel = EventViewModel(
        eventRepository = repository,
        savedStateHandle = SavedStateHandle(mapOf("eventId" to eventId))
    )

    @Test
    fun loadEvent_success_test() = runTest {
        doAnswer { Resource.Success(event) }
            .`when`(repository)
            .fetchEventDetails(eq("event1"))
        doAnswer { Resource.Success(listOf(event, Event(id = "event2", category = "Concerts"))) }
            .`when`(repository)
            .filterByCategory(eq("Concerts"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        verify(repository).fetchEventDetails(eq("event1"))
        verify(repository).filterByCategory(eq("Concerts"))
        assertEquals(event, viewModel.eventState.value.data)
        assertEquals(listOf(Event(id = "event2", category = "Concerts")), viewModel.relatedEventsState.value.data)
        assertTrue(viewModel.eventState.value.error == null)
    }

    @Test
    fun loadEvent_failure_test() = runTest {
        doAnswer { Resource.Error("Event not found") }
            .`when`(repository)
            .fetchEventDetails(eq("event1"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        verify(repository).fetchEventDetails(eq("event1"))
        assertEquals("Event not found", viewModel.eventState.value.error)
        assertTrue(viewModel.eventState.value.data == null)
    }
}
