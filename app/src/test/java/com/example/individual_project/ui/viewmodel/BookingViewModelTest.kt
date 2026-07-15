@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.individual_project.data.model.Event
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.notifications.EventReminderScheduler
import com.example.individual_project.rules.MainDispatcherRule
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

class BookingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val eventRepository: EventRepository = mock()
    private val bookingRepository: BookingRepository = mock()
    private val reminderScheduler: EventReminderScheduler = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseUser: FirebaseUser = mock()

    private val event = Event(
        id = "event1",
        title = "Kathmandu Music Festival",
        venue = "Tundikhel",
        city = "Kathmandu",
        date = "2026-09-15",
        time = "18:00",
        price = 500.0,
        availableSeats = 100
    )

    @Before
    fun setUp(): Unit = kotlinx.coroutines.runBlocking {
        doAnswer { "user1" }.`when`(firebaseUser).uid
        doAnswer { firebaseUser }.`when`(firebaseAuth).currentUser
        doAnswer { Resource.Success(event) }.`when`(eventRepository).fetchEventDetails(eq("event1"))
    }

    private fun createViewModel(eventId: String = "event1"): BookingViewModel = BookingViewModel(
        eventRepository   = eventRepository,
        bookingRepository = bookingRepository,
        reminderScheduler = reminderScheduler,
        firebaseAuth      = firebaseAuth,
        savedStateHandle  = SavedStateHandle(mapOf("eventId" to eventId))
    )

    @Test
    fun `initial ticket quantity is 1 and total price reflects the loaded event`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.quantity)
        assertEquals(500.0, viewModel.uiState.value.totalPrice, 0.001)
    }

    @Test
    fun `increaseQuantity raises quantity and recalculates total price`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.increaseQuantity()

        assertEquals(2, viewModel.uiState.value.quantity)
        assertEquals(1000.0, viewModel.uiState.value.totalPrice, 0.001)
    }

    @Test
    fun `decreaseQuantity never goes below the minimum of 1`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.decreaseQuantity()

        assertEquals(1, viewModel.uiState.value.quantity)
        assertEquals(500.0, viewModel.uiState.value.totalPrice, 0.001)
    }

    @Test
    fun `decreaseQuantity lowers quantity above the minimum boundary`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.increaseQuantity()
        viewModel.increaseQuantity()

        viewModel.decreaseQuantity()

        assertEquals(2, viewModel.uiState.value.quantity)
    }

    @Test
    fun `confirmBooking without a logged-in user surfaces an error and never calls the repository`() = runTest {
        doAnswer { null }.`when`(firebaseAuth).currentUser
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.confirmBooking()
        advanceUntilIdle()

        assertEquals("You must be logged in to book tickets", viewModel.uiState.value.bookingError)
        verify(bookingRepository, times(0)).bookTicket(any())
    }

    @Test
    fun `confirmBooking success stores the booking id and schedules a reminder`() = runTest {
        doAnswer { Resource.Success("booking1") }.`when`(bookingRepository).bookTicket(any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.confirmBooking()
        advanceUntilIdle()

        assertEquals("booking1", viewModel.uiState.value.bookingId)
        assertNull(viewModel.uiState.value.bookingError)
        verify(reminderScheduler).scheduleReminder(
            bookingId = eq("booking1"),
            eventId   = eq("event1"),
            eventTitle = any(),
            venue     = any(),
            eventEpochMillis = any()
        )
    }

    @Test
    fun `confirmBooking failure surfaces the mapped error and leaves bookingId unset`() = runTest {
        doAnswer { Resource.Error("Not enough seats available") }.`when`(bookingRepository).bookTicket(any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.confirmBooking()
        advanceUntilIdle()

        assertEquals("Not enough seats available", viewModel.uiState.value.bookingError)
        assertNull(viewModel.uiState.value.bookingId)
    }

    @Test
    fun `confirmBooking guards against a duplicate submission while a request is in flight`() = runTest {
        doAnswer { Resource.Success("booking1") }.`when`(bookingRepository).bookTicket(any())
        val suspendingBookingRepository = object : BookingRepository by bookingRepository {
            override suspend fun bookTicket(booking: com.example.individual_project.data.model.Booking): Resource<String> {
                delay(1)
                return bookingRepository.bookTicket(booking)
            }
        }
        val viewModel = BookingViewModel(
            eventRepository, suspendingBookingRepository, reminderScheduler, firebaseAuth,
            SavedStateHandle(mapOf("eventId" to "event1"))
        )
        advanceUntilIdle()

        viewModel.confirmBooking()
        assertTrue(viewModel.uiState.value.isBooking)
        viewModel.confirmBooking()
        advanceUntilIdle()

        verify(bookingRepository, times(1)).bookTicket(any())
    }

    @Test
    fun `resetBookingResult clears both bookingId and bookingError`() = runTest {
        doAnswer { Resource.Success("booking1") }.`when`(bookingRepository).bookTicket(any())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.confirmBooking()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.bookingId)

        viewModel.resetBookingResult()

        assertNull(viewModel.uiState.value.bookingId)
        assertNull(viewModel.uiState.value.bookingError)
    }

    @Test
    fun `clearBookingError only clears the error, leaving other state untouched`() = runTest {
        doAnswer { Resource.Error("boom") }.`when`(bookingRepository).bookTicket(any())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.confirmBooking()
        advanceUntilIdle()

        viewModel.clearBookingError()

        assertNull(viewModel.uiState.value.bookingError)
        assertEquals(1, viewModel.uiState.value.quantity)
    }
}
