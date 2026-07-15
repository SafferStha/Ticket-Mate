@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.individual_project.MainDispatcherRule
import com.example.individual_project.data.model.Booking
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.domain.repository.PaymentRepository
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Verifies the guard-at-source pattern introduced across this codebase (Phase 3): a mutating
 * ViewModel action must reject a re-entrant call while the first one is still in flight, not
 * rely solely on the UI disabling its button in time.
 */
class PaymentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val paymentRepository = mockk<PaymentRepository>()
    private val bookingRepository = mockk<BookingRepository>()
    private val firebaseAuth      = mockk<FirebaseAuth>()

    private val booking = Booking(
        id             = "booking1",
        userId         = "user1",
        eventId        = "event1",
        eventTitle     = "Test Concert",
        venue          = "Test Arena",
        date           = "2026-08-20",
        quantity       = 2,
        pricePerTicket = 100.0,
        totalPrice     = 200.0
    )

    private fun createViewModel(): PaymentViewModel {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns "user1"
        every { firebaseAuth.currentUser } returns user

        coEvery { bookingRepository.getBookingById("booking1") } returns Resource.Success(booking)

        return PaymentViewModel(
            paymentRepository = paymentRepository,
            bookingRepository = bookingRepository,
            firebaseAuth      = firebaseAuth,
            savedStateHandle  = SavedStateHandle(mapOf("bookingId" to "booking1"))
        )
    }

    @Test
    fun `a second processPayment call while the first is in flight is ignored`() = runTest {
        coEvery {
            paymentRepository.processPayment(
                bookingId     = "booking1",
                userId        = "user1",
                subtotal      = any(),
                tax           = any(),
                serviceFee    = any(),
                discount      = any(),
                totalAmount   = any(),
                paymentMethod = any()
            )
        } coAnswers {
            delay(1000) // simulates network latency, giving a second call a window to race in
            Resource.Success("payment1")
        }

        val viewModel = createViewModel()
        advanceUntilIdle() // let loadBooking() (called from init) finish

        viewModel.processPayment()
        viewModel.processPayment() // should be a no-op: the first call hasn't resolved yet
        advanceUntilIdle()

        coVerify(exactly = 1) {
            paymentRepository.processPayment(
                bookingId     = "booking1",
                userId        = "user1",
                subtotal      = any(),
                tax           = any(),
                serviceFee    = any(),
                discount      = any(),
                totalAmount   = any(),
                paymentMethod = any()
            )
        }
        assertEquals("payment1", viewModel.uiState.value.paymentId)
    }

    @Test
    fun `processPayment is allowed again once the previous call has completed`() = runTest {
        coEvery {
            paymentRepository.processPayment(
                bookingId = "booking1", userId = "user1", subtotal = any(), tax = any(),
                serviceFee = any(), discount = any(), totalAmount = any(), paymentMethod = any()
            )
        } returns Resource.Success("payment1")

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processPayment()
        advanceUntilIdle()
        viewModel.resetPaymentResult()
        viewModel.processPayment()
        advanceUntilIdle()

        coVerify(exactly = 2) {
            paymentRepository.processPayment(
                bookingId = "booking1", userId = "user1", subtotal = any(), tax = any(),
                serviceFee = any(), discount = any(), totalAmount = any(), paymentMethod = any()
            )
        }
    }
}
