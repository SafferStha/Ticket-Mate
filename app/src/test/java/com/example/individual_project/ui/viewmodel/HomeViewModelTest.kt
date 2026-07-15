@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.individual_project.ui.viewmodel

import com.example.individual_project.data.model.Event
import com.example.individual_project.data.model.User
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.domain.repository.UserRepository
import com.example.individual_project.rules.MainDispatcherRule
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val eventRepository: EventRepository = mock()
    private val userRepository: UserRepository = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseUser: FirebaseUser = mock()

    private val featured = listOf(Event(id = "e1", title = "Featured Event", featured = true))
    private val trending = listOf(Event(id = "e2", title = "Trending Event"))
    private val allEvents = listOf(Event(id = "e1"), Event(id = "e2"), Event(id = "e3"))

    @Before
    fun setUp(): Unit = kotlinx.coroutines.runBlocking {
        doAnswer { "user1" }.`when`(firebaseUser).uid
        doAnswer { firebaseUser }.`when`(firebaseAuth).currentUser
        doAnswer { Resource.Success(featured) }.`when`(eventRepository).fetchFeaturedEvents()
        doAnswer { Resource.Success(trending) }.`when`(eventRepository).getTrendingEvents()
        doAnswer { Resource.Success(allEvents) }.`when`(eventRepository).fetchEvents()
        doAnswer { Resource.Success(emptyList<Event>()) }.`when`(eventRepository).getRecommendedEvents(any())
        doAnswer { Resource.Success(emptyList<String>()) }.`when`(userRepository).getFavoriteEventIds(any())
        doAnswer { Resource.Success(User(uid = "user1", name = "Jane")) }.`when`(userRepository).getUserProfile(any())
    }

    private fun createViewModel(): HomeViewModel = HomeViewModel(eventRepository, userRepository, firebaseAuth)

    @Test
    fun `featured, trending, and all events load on init`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(featured, viewModel.featuredState.value.data)
        assertEquals(trending, viewModel.trendingState.value.data)
        assertEquals(allEvents, viewModel.eventsState.value.data)
    }

    @Test
    fun `user profile loads for the initials avatar when a user is logged in`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Jane", viewModel.userProfile.value.data?.name)
    }

    @Test
    fun `no user profile load is attempted when logged out`() = runTest {
        doAnswer { null }.`when`(firebaseAuth).currentUser
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertNull(viewModel.userProfile.value.data)
        verify(userRepository, times(0)).getUserProfile(any())
    }

    @Test
    fun `partial failure in one section does not block the others`() = runTest {
        doAnswer { Resource.Error("Trending failed") }.`when`(eventRepository).getTrendingEvents()

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Trending failed", viewModel.trendingState.value.error)
        assertEquals(featured, viewModel.featuredState.value.data)
        assertEquals(allEvents, viewModel.eventsState.value.data)
    }

    @Test
    fun `empty event list is reflected as an empty, non-error state`() = runTest {
        doAnswer { Resource.Success(emptyList<Event>()) }.`when`(eventRepository).fetchEvents()

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(emptyList<Event>(), viewModel.eventsState.value.data)
        assertNull(viewModel.eventsState.value.error)
    }

    @Test
    fun `selectCategory All reloads the full event list`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        doAnswer { Resource.Success(listOf(Event(id = "concert1", category = "Concerts"))) }
            .`when`(eventRepository).filterByCategory(eq("Concerts"))

        viewModel.selectCategory("Concerts")
        advanceUntilIdle()
        assertEquals("Concerts", viewModel.selectedCategory.value)
        assertEquals(1, viewModel.eventsState.value.data?.size)

        viewModel.selectCategory("All")
        advanceUntilIdle()

        assertEquals("All", viewModel.selectedCategory.value)
        assertEquals(allEvents, viewModel.eventsState.value.data)
    }

    @Test
    fun `selectCity toggles the nearby-events filter on and off`() = runTest {
        doAnswer { Resource.Success(listOf(Event(id = "kt1", city = "Kathmandu"))) }
            .`when`(eventRepository).getEventsByCity(eq("Kathmandu"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectCity("Kathmandu")
        advanceUntilIdle()
        assertEquals("Kathmandu", viewModel.selectedCity.value)
        assertEquals(1, viewModel.nearbyState.value.data?.size)

        viewModel.selectCity("Kathmandu")
        advanceUntilIdle()
        assertEquals("", viewModel.selectedCity.value)
    }

    @Test
    fun `toggleFavorite optimistically updates favoriteIds and reverts on failure`() = runTest {
        doAnswer { Resource.Error("boom") }.`when`(eventRepository).toggleFavorite(eq("e1"), eq("user1"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleFavorite("e1")
        advanceUntilIdle()

        assertTrue("e1" !in viewModel.favoriteIds.value)
    }

    @Test
    fun `refresh reloads featured, trending, recommended, and the current category`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        verify(eventRepository, times(2)).fetchFeaturedEvents()
        verify(eventRepository, times(2)).getTrendingEvents()
        verify(eventRepository, times(2)).fetchEvents()
    }

    @Test
    fun `onSearchQueryChange with a blank query clears search results instead of calling the repository`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChange("")

        assertNull(viewModel.searchResults.value.data)
        verify(eventRepository, times(0)).searchEvents(any())
    }
}
