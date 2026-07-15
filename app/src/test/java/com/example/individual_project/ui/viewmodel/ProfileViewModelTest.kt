@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.individual_project.ui.viewmodel

import android.net.Uri
import com.example.individual_project.data.model.Booking
import com.example.individual_project.data.model.BookingStatus
import com.example.individual_project.data.model.Payment
import com.example.individual_project.data.model.PaymentStatus
import com.example.individual_project.data.model.Ticket
import com.example.individual_project.data.model.User
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.domain.repository.PaymentRepository
import com.example.individual_project.domain.repository.TicketRepository
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository: UserRepository = mock()
    private val ticketRepository: TicketRepository = mock()
    private val bookingRepository: BookingRepository = mock()
    private val paymentRepository: PaymentRepository = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseUser: FirebaseUser = mock()

    private val user = User(uid = "user1", name = "Jane Doe", email = "jane@example.com")

    @Before
    fun setUp(): Unit = kotlinx.coroutines.runBlocking {
        doAnswer { "user1" }.`when`(firebaseUser).uid
        doAnswer { firebaseUser }.`when`(firebaseAuth).currentUser
        doAnswer { Resource.Success(user) }.`when`(userRepository).getUserProfile(eq("user1"))
        doAnswer { Resource.Success(emptyList<Ticket>()) }.`when`(ticketRepository).getUserTickets(any())
        doAnswer { Resource.Success(emptyList<String>()) }.`when`(userRepository).getFavoriteEventIds(any())
        doAnswer { Resource.Success(emptyList<Booking>()) }.`when`(bookingRepository).getUserBookings(any())
        doAnswer { Resource.Success(emptyList<Payment>()) }.`when`(paymentRepository).fetchPaymentsByUser(any())
    }

    private fun createViewModel(): ProfileViewModel =
        ProfileViewModel(userRepository, ticketRepository, bookingRepository, paymentRepository, firebaseAuth)

    @Test
    fun `profile loads successfully with user and zeroed stats when nothing exists yet`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(user, viewModel.profileState.value.user)
        assertEquals(0, viewModel.profileState.value.ticketCount)
        assertEquals(0.0, viewModel.profileState.value.totalSpent, 0.001)
        assertFalse(viewModel.profileState.value.isLoading)
    }

    @Test
    fun `missing profile surfaces the repository error`() = runTest {
        doAnswer { Resource.Error("Profile not found") }.`when`(userRepository).getUserProfile(any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertNull(viewModel.profileState.value.user)
        assertEquals("Profile not found", viewModel.profileState.value.error)
    }

    @Test
    fun `statistics are computed from tickets, bookings, and successful payments`() = runTest {
        doAnswer {
            Resource.Success(
                listOf(
                    Ticket(id = "t1", ticketStatus = "ACTIVE"),
                    Ticket(id = "t2", ticketStatus = "USED")
                )
            )
        }.`when`(ticketRepository).getUserTickets(any())
        doAnswer {
            Resource.Success(
                listOf(
                    Booking(id = "b1", bookingStatus = BookingStatus.COMPLETED.name),
                    Booking(id = "b2", bookingStatus = BookingStatus.CANCELLED.name),
                    Booking(id = "b3", bookingStatus = BookingStatus.CONFIRMED.name)
                )
            )
        }.`when`(bookingRepository).getUserBookings(any())
        doAnswer {
            Resource.Success(
                listOf(
                    Payment(id = "p1", totalAmount = 500.0, paymentStatus = PaymentStatus.SUCCESS.name),
                    Payment(id = "p2", totalAmount = 300.0, paymentStatus = PaymentStatus.FAILED.name)
                )
            )
        }.`when`(paymentRepository).fetchPaymentsByUser(any())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.profileState.value.ticketCount)
        assertEquals(3, viewModel.profileState.value.totalBookings)
        assertEquals(1, viewModel.profileState.value.completedEvents)
        assertEquals(1, viewModel.profileState.value.cancelledBookings)
        assertEquals(500.0, viewModel.profileState.value.totalSpent, 0.001)
    }

    @Test
    fun `edit profile loads current name, email, and image from the user profile`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Jane Doe", viewModel.editState.value.name)
        assertEquals("jane@example.com", viewModel.editState.value.email)
    }

    @Test
    fun `onNameChange updates the edit form field only`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onNameChange("New Name")

        assertEquals("New Name", viewModel.editState.value.name)
    }

    @Test
    fun `saveProfile rejects a blank name without calling the repository`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onNameChange("   ")

        viewModel.saveProfile()

        assertEquals("Name cannot be empty", viewModel.editState.value.error)
        verify(userRepository, times(0)).updateUserProfile(any())
    }

    @Test
    fun `saveProfile success updates the profile and refreshes header stats`() = runTest {
        doAnswer { Resource.Success(Unit) }.`when`(userRepository).updateUserProfile(any())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onNameChange("Updated Name")

        viewModel.saveProfile()
        advanceUntilIdle()

        verify(userRepository).updateUserProfile(any())
        assertTrue(viewModel.editState.value.saveSuccess)
        // init{} calls getUserProfile twice (loadProfile + loadEditProfile); a successful save
        // triggers loadProfile() again to refresh header stats, for 3 calls total.
        verify(userRepository, times(3)).getUserProfile(any())
    }

    @Test
    fun `saveProfile failure surfaces the mapped error and clears isSaving`() = runTest {
        doAnswer { Resource.Error("Update failed") }.`when`(userRepository).updateUserProfile(any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals("Update failed", viewModel.editState.value.error)
        assertFalse(viewModel.editState.value.isSaving)
    }

    @Test
    fun `uploadImageAndSave uploads then saves the profile with the returned url`() = runTest {
        val uri: Uri = mock()
        doAnswer { Resource.Success("https://example.com/image.jpg") }
            .`when`(userRepository).uploadProfileImage(eq("user1"), eq(uri))
        doAnswer { Resource.Success(Unit) }.`when`(userRepository).updateUserProfile(any())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uploadImageAndSave(uri)
        advanceUntilIdle()

        assertEquals("https://example.com/image.jpg", viewModel.editState.value.profileImage)
        verify(userRepository).updateUserProfile(any())
    }

    @Test
    fun `clearEditError and clearEditSuccess reset only their own flag`() = runTest {
        doAnswer { Resource.Error("boom") }.`when`(userRepository).updateUserProfile(any())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.saveProfile()
        advanceUntilIdle()

        viewModel.clearEditError()

        assertNull(viewModel.editState.value.error)
    }
}
