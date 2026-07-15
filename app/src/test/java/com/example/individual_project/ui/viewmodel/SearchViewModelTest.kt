@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.individual_project.ui.viewmodel

import com.example.individual_project.data.model.Event
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.domain.repository.SearchRepository
import com.example.individual_project.domain.repository.UserRepository
import com.example.individual_project.rules.MainDispatcherRule
import com.example.individual_project.ui.model.FilterState
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val eventRepository: EventRepository = mock()
    private val searchRepository: SearchRepository = mock()
    private val userRepository: UserRepository = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseUser: FirebaseUser = mock()

    @Before
    fun setUp(): Unit = kotlinx.coroutines.runBlocking {
        doAnswer { "user1" }.`when`(firebaseUser).uid
        doAnswer { firebaseUser }.`when`(firebaseAuth).currentUser
        doAnswer { Resource.Success(emptyList<com.example.individual_project.data.model.SearchHistoryItem>()) }
            .`when`(searchRepository).getSearchHistory(any())
        doAnswer { Resource.Success(emptyList<String>()) }.`when`(searchRepository).getTrendingKeywords()
        doAnswer { Resource.Success(emptyList<String>()) }.`when`(userRepository).getFavoriteEventIds(any())
    }

    private fun createViewModel(): SearchViewModel =
        SearchViewModel(eventRepository, searchRepository, userRepository, firebaseAuth)

    @Test
    fun `initial state is empty with no query and no results`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("", viewModel.query.value)
        assertNull(viewModel.searchResults.value.data)
    }

    @Test
    fun `onQueryChange with a blank query clears results without calling the repository`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("")

        assertNull(viewModel.searchResults.value.data)
        verify(eventRepository, times(0)).searchEvents(any())
    }

    @Test
    fun `onQueryChange debounces and only searches after 300ms of virtual time pass`() = runTest {
        doAnswer { Resource.Success(listOf(Event(id = "e1", title = "Jazz Night"))) }
            .`when`(eventRepository).searchEvents(eq("jazz"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("jazz")
        advanceTimeBy(100)
        verify(eventRepository, times(0)).searchEvents(any())

        advanceTimeBy(250)
        advanceUntilIdle()
        verify(eventRepository, times(1)).searchEvents(eq("jazz"))
        assertEquals(1, viewModel.searchResults.value.data?.size)
    }

    @Test
    fun `a newer query cancels the older query's debounce so stale results never land`() = runTest {
        doAnswer { Resource.Success(listOf(Event(id = "e1", title = "Jazz Night"))) }
            .`when`(eventRepository).searchEvents(eq("jazz"))
        doAnswer { Resource.Success(listOf(Event(id = "e2", title = "Rock Show"))) }
            .`when`(eventRepository).searchEvents(eq("rock"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("jazz")
        advanceTimeBy(150)
        viewModel.onQueryChange("rock")
        advanceUntilIdle()

        verify(eventRepository, times(0)).searchEvents(eq("jazz"))
        verify(eventRepository, times(1)).searchEvents(eq("rock"))
        assertEquals("Rock Show", viewModel.searchResults.value.data?.first()?.title)
    }

    @Test
    fun `search failure surfaces the mapped error message`() = runTest {
        doAnswer { Resource.Error("Search failed") }.`when`(eventRepository).searchEvents(any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.submitSearch("anything")
        advanceUntilIdle()

        assertEquals("Search failed", viewModel.searchResults.value.error)
    }

    @Test
    fun `no-result search returns an empty, non-error result list`() = runTest {
        doAnswer { Resource.Success(emptyList<Event>()) }.`when`(eventRepository).searchEvents(any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.submitSearch("nothing-matches-this")
        advanceUntilIdle()

        assertEquals(emptyList<Event>(), viewModel.searchResults.value.data)
        assertNull(viewModel.searchResults.value.error)
    }

    @Test
    fun `submitSearch skips debounce, saves history, and searches immediately`() = runTest {
        doAnswer { Resource.Success(emptyList<Event>()) }.`when`(eventRepository).searchEvents(eq("concert"))
        doAnswer { Resource.Success(Unit) }.`when`(searchRepository).saveSearchHistory(eq("user1"), eq("concert"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.submitSearch("concert")
        advanceUntilIdle()

        verify(searchRepository).saveSearchHistory(eq("user1"), eq("concert"))
        verify(eventRepository, times(1)).searchEvents(eq("concert"))
    }

    @Test
    fun `applyFilter narrows results by category and re-runs the active query`() = runTest {
        doAnswer {
            Resource.Success(
                listOf(
                    Event(id = "e1", category = "Concerts", price = 100.0),
                    Event(id = "e2", category = "Sports", price = 100.0)
                )
            )
        }.`when`(eventRepository).searchEvents(eq("event"))
        val viewModel = createViewModel()
        advanceUntilIdle()
        // applyFilter only re-runs the *active* query, which it reads from `query`, not from
        // whatever was last passed to submitSearch -- so the query must be set via
        // onQueryChange() first, exactly as the real search bar does on every keystroke.
        viewModel.onQueryChange("event")
        advanceUntilIdle()
        assertEquals(2, viewModel.searchResults.value.data?.size)

        viewModel.applyFilter(FilterState(selectedCategories = setOf("Concerts")))
        advanceUntilIdle()

        assertEquals(1, viewModel.searchResults.value.data?.size)
        assertEquals("Concerts", viewModel.searchResults.value.data?.first()?.category)
    }

    @Test
    fun `clearFilter removes an active filter and re-runs the query unfiltered`() = runTest {
        doAnswer {
            Resource.Success(
                listOf(
                    Event(id = "e1", category = "Concerts", price = 100.0),
                    Event(id = "e2", category = "Sports", price = 100.0)
                )
            )
        }.`when`(eventRepository).searchEvents(eq("event"))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onQueryChange("event")
        advanceUntilIdle()
        viewModel.applyFilter(FilterState(selectedCategories = setOf("Concerts")))
        advanceUntilIdle()

        viewModel.clearFilter()
        advanceUntilIdle()

        assertEquals(2, viewModel.searchResults.value.data?.size)
        assertEquals(FilterState(), viewModel.filterState.value)
    }

    @Test
    fun `clearHistory empties history via the repository`() = runTest {
        doAnswer { Resource.Success(Unit) }.`when`(searchRepository).clearSearchHistory(eq("user1"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.clearHistory()
        advanceUntilIdle()

        verify(searchRepository).clearSearchHistory(eq("user1"))
        assertEquals(emptyList<Any>(), viewModel.searchHistory.value)
    }
}
