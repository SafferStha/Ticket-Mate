@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.individual_project.data.model.Event
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.domain.repository.UserRepository
import com.example.individual_project.rules.MainDispatcherRule
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class EventDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val eventRepository: EventRepository = mock()
    private val userRepository: UserRepository = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseUser: FirebaseUser = mock()

    private val event = Event(
        id = "event1",
        title = "Kathmandu Music Festival",
        category = "Concerts",
        availableSeats = 100
    )

    private fun stubLoggedInUser(uid: String = "user1") {
        doAnswer { uid }.`when`(firebaseUser).uid
        doAnswer { firebaseUser }.`when`(firebaseAuth).currentUser
    }

    private fun createViewModel(eventId: String = "event1"): EventDetailViewModel = EventDetailViewModel(
        eventRepository = eventRepository,
        userRepository  = userRepository,
        firebaseAuth    = firebaseAuth,
        savedStateHandle = SavedStateHandle(mapOf("eventId" to eventId))
    )

    @Test
    fun `event found loads event details and related events by category`() = runTest {
        doAnswer { Resource.Success(event) }.`when`(eventRepository).fetchEventDetails(eq("event1"))
        doAnswer { Resource.Success(listOf(event, Event(id = "event2", category = "Concerts"))) }
            .`when`(eventRepository).filterByCategory(eq("Concerts"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(event, viewModel.eventState.value.data)
        assertEquals(listOf(Event(id = "event2", category = "Concerts")), viewModel.relatedEventsState.value.data)
    }

    @Test
    fun `missing event surfaces the repository error and skips related events`() = runTest {
        doAnswer { Resource.Error("Event not found") }.`when`(eventRepository).fetchEventDetails(eq("event1"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Event not found", viewModel.eventState.value.error)
        assertNull(viewModel.eventState.value.data)
        verify(eventRepository, times(0)).filterByCategory(any())
    }

    @Test
    fun `invalid blank event id skips loading entirely`() = runTest {
        val viewModel = createViewModel(eventId = "")
        advanceUntilIdle()

        assertFalse(viewModel.eventState.value.isLoading)
        assertNull(viewModel.eventState.value.data)
        verify(eventRepository, times(0)).fetchEventDetails(any())
    }

    @Test
    fun `booking availability is reflected via the loaded event's available seats`() = runTest {
        doAnswer { Resource.Success(event.copy(availableSeats = 5)) }.`when`(eventRepository).fetchEventDetails(any())
        doAnswer { Resource.Success(emptyList<Event>()) }.`when`(eventRepository).filterByCategory(any())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue((viewModel.eventState.value.data?.availableSeats ?: 0) > 0)
    }

    @Test
    fun `sold out event is reflected as zero available seats`() = runTest {
        doAnswer { Resource.Success(event.copy(availableSeats = 0)) }.`when`(eventRepository).fetchEventDetails(any())
        doAnswer { Resource.Success(emptyList<Event>()) }.`when`(eventRepository).filterByCategory(any())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(0, viewModel.eventState.value.data?.availableSeats)
    }

    @Test
    fun `checkFavoriteStatus runs on init for a logged-in user and sets isFavorite`() = runTest {
        stubLoggedInUser()
        doAnswer { Resource.Success(event) }.`when`(eventRepository).fetchEventDetails(any())
        doAnswer { Resource.Success(emptyList<Event>()) }.`when`(eventRepository).filterByCategory(any())
        doAnswer { Resource.Success(true) }.`when`(eventRepository).isFavorite(eq("event1"), eq("user1"))
        doAnswer { Resource.Success(emptyList<String>()) }.`when`(userRepository).getFavoriteEventIds(any())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.isFavorite.value)
    }

    @Test
    fun `toggleFavorite optimistically flips state and keeps it on success`() = runTest {
        stubLoggedInUser()
        doAnswer { Resource.Success(event) }.`when`(eventRepository).fetchEventDetails(any())
        doAnswer { Resource.Success(emptyList<Event>()) }.`when`(eventRepository).filterByCategory(any())
        doAnswer { Resource.Success(false) }.`when`(eventRepository).isFavorite(any(), any())
        doAnswer { Resource.Success(emptyList<String>()) }.`when`(userRepository).getFavoriteEventIds(any())
        doAnswer { Resource.Success(Unit) }.`when`(eventRepository).toggleFavorite(eq("event1"), eq("user1"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleFavorite()
        advanceUntilIdle()

        assertTrue(viewModel.isFavorite.value)
        assertFalse(viewModel.favoriteLoading.value)
    }

    @Test
    fun `toggleFavorite reverts the optimistic flip when the repository call fails`() = runTest {
        stubLoggedInUser()
        doAnswer { Resource.Success(event) }.`when`(eventRepository).fetchEventDetails(any())
        doAnswer { Resource.Success(emptyList<Event>()) }.`when`(eventRepository).filterByCategory(any())
        doAnswer { Resource.Success(false) }.`when`(eventRepository).isFavorite(any(), any())
        doAnswer { Resource.Success(emptyList<String>()) }.`when`(userRepository).getFavoriteEventIds(any())
        doAnswer { Resource.Error("Network error") }.`when`(eventRepository).toggleFavorite(any(), any())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleFavorite()
        advanceUntilIdle()

        assertFalse(viewModel.isFavorite.value)
    }

    @Test
    fun `toggleFavorite is a no-op when no user is logged in`() = runTest {
        doAnswer { null }.`when`(firebaseAuth).currentUser
        doAnswer { Resource.Success(event) }.`when`(eventRepository).fetchEventDetails(any())
        doAnswer { Resource.Success(emptyList<Event>()) }.`when`(eventRepository).filterByCategory(any())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleFavorite()
        advanceUntilIdle()

        verify(eventRepository, times(0)).toggleFavorite(any(), any())
    }
}
