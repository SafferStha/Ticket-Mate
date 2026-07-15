@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.individual_project.rules.MainDispatcherRule
import com.example.individual_project.data.model.Booking
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.domain.repository.PaymentRepository
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

class PaymentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val paymentRepository: PaymentRepository = mock()
    private val bookingRepository: BookingRepository = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseUser: FirebaseUser = mock()

    private val booking = Booking(
        id = "booking1",
        userId = "user1",
        eventId = "event1",
        eventTitle = "Test Concert",
        venue = "Test Arena",
        date = "2026-08-20",
        quantity = 2,
        pricePerTicket = 100.0,
        totalPrice = 200.0
    )

    @Before
    fun setUp() {
        doAnswer { "user1" }.`when`(firebaseUser).uid
        doAnswer { firebaseUser }.`when`(firebaseAuth).currentUser
    }

    private fun createViewModel(
        bookingId: String = "booking1",
        repository: PaymentRepository = paymentRepository
    ): PaymentViewModel = PaymentViewModel(
        paymentRepository = repository,
        bookingRepository = bookingRepository,
        firebaseAuth = firebaseAuth,
        savedStateHandle = SavedStateHandle(mapOf("bookingId" to bookingId))
    )

    @Test
    fun loadBooking_success_test() = runTest {
        doAnswer { Resource.Success(booking) }
            .`when`(bookingRepository)
            .getBookingById(eq("booking1"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        verify(bookingRepository).getBookingById(eq("booking1"))
        assertEquals(booking, viewModel.uiState.value.booking)
        assertEquals(200.0, viewModel.uiState.value.breakdown?.subtotal)
        assertTrue(viewModel.uiState.value.bookingError == null)
    }

    @Test
    fun loadBooking_failure_test() = runTest {
        doAnswer { Resource.Error("Booking not found") }
            .`when`(bookingRepository)
            .getBookingById(eq("booking1"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        verify(bookingRepository).getBookingById(eq("booking1"))
        assertEquals("Booking not found", viewModel.uiState.value.bookingError)
        assertTrue(viewModel.uiState.value.booking == null)
    }

    @Test
    fun processPayment_success_test() = runTest {
        doAnswer { "user1" }.`when`(firebaseUser).uid
        doAnswer { firebaseUser }.`when`(firebaseAuth).currentUser
        doAnswer { Resource.Success(booking) }
            .`when`(bookingRepository)
            .getBookingById(eq("booking1"))
        doAnswer { Resource.Success("payment1") }
            .`when`(paymentRepository)
            .processPayment(
                bookingId = eq("booking1"),
                userId = eq("user1"),
                subtotal = any(),
                tax = any(),
                serviceFee = any(),
                discount = any(),
                totalAmount = any(),
                paymentMethod = eq(PaymentMethod.CASH.key)
            )

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.processPayment()
        advanceUntilIdle()

        verify(paymentRepository).processPayment(
            bookingId = eq("booking1"),
            userId = eq("user1"),
            subtotal = any(),
            tax = any(),
            serviceFee = any(),
            discount = any(),
            totalAmount = any(),
            paymentMethod = eq(PaymentMethod.CASH.key)
        )
        assertEquals("payment1", viewModel.uiState.value.paymentId)
        assertTrue(!viewModel.uiState.value.paymentFailed)
    }

    @Test
    fun processPayment_failure_test() = runTest {
        doAnswer { "user1" }.`when`(firebaseUser).uid
        doAnswer { firebaseUser }.`when`(firebaseAuth).currentUser
        doAnswer { Resource.Success(booking) }
            .`when`(bookingRepository)
            .getBookingById(eq("booking1"))
        doAnswer { Resource.Error("Payment declined") }
            .`when`(paymentRepository)
            .processPayment(
                bookingId = eq("booking1"),
                userId = eq("user1"),
                subtotal = any(),
                tax = any(),
                serviceFee = any(),
                discount = any(),
                totalAmount = any(),
                paymentMethod = eq(PaymentMethod.CASH.key)
            )

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.processPayment()
        advanceUntilIdle()

        verify(paymentRepository).processPayment(
            bookingId = eq("booking1"),
            userId = eq("user1"),
            subtotal = any(),
            tax = any(),
            serviceFee = any(),
            discount = any(),
            totalAmount = any(),
            paymentMethod = eq(PaymentMethod.CASH.key)
        )
        assertEquals("Payment declined", viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.paymentFailed)
    }

    @Test
    fun `selectPaymentMethod updates the selected method without touching other state`() = runTest {
        doAnswer { Resource.Success(booking) }.`when`(bookingRepository).getBookingById(any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectPaymentMethod(PaymentMethod.ESEWA)

        assertEquals(PaymentMethod.ESEWA.key, viewModel.uiState.value.selectedMethod)
        assertEquals(booking, viewModel.uiState.value.booking)
    }

    @Test
    fun `processPayment without a logged-in user surfaces an error and never calls the repository`() = runTest {
        doAnswer { null }.`when`(firebaseAuth).currentUser
        doAnswer { Resource.Success(booking) }.`when`(bookingRepository).getBookingById(any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processPayment()
        advanceUntilIdle()

        assertEquals("You must be logged in to pay", viewModel.uiState.value.error)
        verify(paymentRepository, times(0)).processPayment(
            any(), any(), any(), any(), any(), any(), any(), any()
        )
    }

    @Test
    fun `processPayment guards against a duplicate submission while a request is in flight`() = runTest {
        doAnswer { Resource.Success(booking) }.`when`(bookingRepository).getBookingById(any())
        doAnswer { Resource.Success("payment1") }.`when`(paymentRepository).processPayment(
            any(), any(), any(), any(), any(), any(), any(), any()
        )
        val suspendingRepository = object : PaymentRepository by paymentRepository {
            override suspend fun processPayment(
                bookingId: String, userId: String, subtotal: Double, tax: Double,
                serviceFee: Double, discount: Double, totalAmount: Double, paymentMethod: String
            ): Resource<String> {
                delay(1)
                return paymentRepository.processPayment(
                    bookingId, userId, subtotal, tax, serviceFee, discount, totalAmount, paymentMethod
                )
            }
        }
        val viewModel = createViewModel(repository = suspendingRepository)
        advanceUntilIdle()

        viewModel.processPayment()
        assertTrue(viewModel.uiState.value.isProcessing)
        viewModel.processPayment()
        advanceUntilIdle()

        verify(paymentRepository, times(1)).processPayment(
            any(), any(), any(), any(), any(), any(), any(), any()
        )
    }

    @Test
    fun `a declined payment never leaks raw booking or card details into the error state`() = runTest {
        doAnswer { Resource.Success(booking) }.`when`(bookingRepository).getBookingById(any())
        doAnswer { Resource.Error("Payment declined") }.`when`(paymentRepository).processPayment(
            any(), any(), any(), any(), any(), any(), any(), any()
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processPayment()
        advanceUntilIdle()

        val error = viewModel.uiState.value.error.orEmpty()
        assertFalse(error.contains(booking.id))
        assertFalse(error.contains("user1"))
    }

    @Test
    fun `resetPaymentResult clears paymentId, paymentFailed, and error so the result is consumed only once`() = runTest {
        doAnswer { Resource.Success(booking) }.`when`(bookingRepository).getBookingById(any())
        doAnswer { Resource.Success("payment1") }.`when`(paymentRepository).processPayment(
            any(), any(), any(), any(), any(), any(), any(), any()
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.processPayment()
        advanceUntilIdle()
        assertEquals("payment1", viewModel.uiState.value.paymentId)

        viewModel.resetPaymentResult()

        assertNull(viewModel.uiState.value.paymentId)
        assertFalse(viewModel.uiState.value.paymentFailed)
        assertNull(viewModel.uiState.value.error)
    }
}
