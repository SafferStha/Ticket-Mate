@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.individual_project.ui.viewmodel

import com.example.individual_project.auth.AdminStateManager
import com.example.individual_project.auth.AuthState
import com.example.individual_project.auth.AuthStateManager
import com.example.individual_project.domain.repository.AuthRepository
import com.example.individual_project.rules.MainDispatcherRule
import com.example.individual_project.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mock()
    private val authStateManager: AuthStateManager = mock()
    private val adminStateManager: AdminStateManager = mock()

    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Authenticated)
    private val isAdminFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        doAnswer { authStateFlow.asStateFlow() }.`when`(authStateManager).authState
        doAnswer { isAdminFlow.asStateFlow() }.`when`(adminStateManager).isAdmin
    }

    private fun createViewModel(repository: AuthRepository = authRepository): AuthViewModel =
        AuthViewModel(repository, authStateManager, adminStateManager)

    /**
     * Wraps [authRepository] so login/register really suspend once before delegating. Needed
     * because UnconfinedTestDispatcher runs a guarded call synchronously to completion when the
     * mock returns without ever suspending, which would make the in-flight window the
     * duplicate-submission guard depends on unobservable. A plain `yield()` isn't enough here --
     * under an unconfined dispatcher it only yields to other *already-queued* work and falls
     * straight through when nothing else is queued, so it doesn't reliably suspend. `delay(1)`
     * always parks the coroutine on the test scheduler's virtual clock, which does.
     */
    private fun repositoryThatSuspendsOnce(): AuthRepository = object : AuthRepository by authRepository {
        override suspend fun login(email: String, password: String): Resource<Unit> {
            delay(1)
            return authRepository.login(email, password)
        }
        override suspend fun register(email: String, password: String, name: String, contact: String): Resource<Unit> {
            delay(1)
            return authRepository.register(email, password, name, contact)
        }
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state has no loading, no data, and no error for every operation`() {
        val viewModel = createViewModel()

        assertFalse(viewModel.loginState.value.isLoading)
        assertNull(viewModel.loginState.value.data)
        assertNull(viewModel.loginState.value.error)
        assertFalse(viewModel.registerState.value.isLoading)
        assertEquals(0, viewModel.resetCooldown.value)
        assertEquals(0, viewModel.verifyEmailCooldown.value)
    }

    @Test
    fun `authState and isAdmin mirror the injected state managers`() = runTest {
        val viewModel = createViewModel()

        assertEquals(AuthState.Authenticated, viewModel.authState.value)
        assertFalse(viewModel.isAdmin.value)

        isAdminFlow.value = true
        assertTrue(viewModel.isAdmin.value)
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    fun `login success clears loading and exposes no error`() = runTest {
        doAnswer { Resource.Success(Unit) }.`when`(authRepository).login(eq("user@example.com"), eq("password1"))
        val viewModel = createViewModel()

        viewModel.login("user@example.com", "password1")
        advanceUntilIdle()

        verify(authRepository).login(eq("user@example.com"), eq("password1"))
        assertFalse(viewModel.loginState.value.isLoading)
        assertNull(viewModel.loginState.value.error)
        assertEquals(Unit, viewModel.loginState.value.data)
    }

    @Test
    fun `login failure surfaces the mapped error message and clears loading`() = runTest {
        doAnswer { Resource.Error("Incorrect email or password. Please try again.") }
            .`when`(authRepository).login(any(), any())
        val viewModel = createViewModel()

        viewModel.login("user@example.com", "wrong-password")
        advanceUntilIdle()

        assertFalse(viewModel.loginState.value.isLoading)
        assertEquals("Incorrect email or password. Please try again.", viewModel.loginState.value.error)
        assertNull(viewModel.loginState.value.data)
    }

    @Test
    fun `login guards against a duplicate submission while a request is in flight`() = runTest {
        doAnswer { Resource.Success(Unit) }.`when`(authRepository).login(any(), any())
        val viewModel = createViewModel(repositoryThatSuspendsOnce())

        viewModel.login("user@example.com", "password1")
        assertTrue(viewModel.loginState.value.isLoading)

        viewModel.login("user@example.com", "password1")
        advanceUntilIdle()

        verify(authRepository, times(1)).login(any(), any())
    }

    @Test
    fun `clearLoginState resets loginState to its initial value`() = runTest {
        doAnswer { Resource.Error("boom") }.`when`(authRepository).login(any(), any())
        val viewModel = createViewModel()
        viewModel.login("user@example.com", "password1")
        advanceUntilIdle()

        viewModel.clearLoginState()

        assertNull(viewModel.loginState.value.error)
        assertFalse(viewModel.loginState.value.isLoading)
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    fun `register success clears loading and exposes no error`() = runTest {
        doAnswer { Resource.Success(Unit) }
            .`when`(authRepository).register(any(), any(), any(), any())
        val viewModel = createViewModel()

        viewModel.register("user@example.com", "password1", "Jane Doe", "9800000000")
        advanceUntilIdle()

        verify(authRepository).register(eq("user@example.com"), eq("password1"), eq("Jane Doe"), eq("9800000000"))
        assertFalse(viewModel.registerState.value.isLoading)
        assertEquals(Unit, viewModel.registerState.value.data)
    }

    @Test
    fun `register failure for an existing account surfaces the mapped message`() = runTest {
        doAnswer { Resource.Error("An account with this email already exists. Try signing in instead.") }
            .`when`(authRepository).register(any(), any(), any(), any())
        val viewModel = createViewModel()

        viewModel.register("user@example.com", "password1", "Jane Doe", "")
        advanceUntilIdle()

        assertEquals(
            "An account with this email already exists. Try signing in instead.",
            viewModel.registerState.value.error
        )
        assertFalse(viewModel.registerState.value.isLoading)
    }

    @Test
    fun `register guards against a duplicate submission while a request is in flight`() = runTest {
        doAnswer { Resource.Success(Unit) }.`when`(authRepository).register(any(), any(), any(), any())
        val viewModel = createViewModel(repositoryThatSuspendsOnce())

        viewModel.register("user@example.com", "password1", "Jane Doe", "")
        assertTrue(viewModel.registerState.value.isLoading)
        viewModel.register("user@example.com", "password1", "Jane Doe", "")
        advanceUntilIdle()

        verify(authRepository, times(1)).register(any(), any(), any(), any())
    }

    // ── Forgot password ───────────────────────────────────────────────────────

    @Test
    fun `sendPasswordReset success starts the resend cooldown`() = runTest {
        doAnswer { Resource.Success(Unit) }.`when`(authRepository).sendPasswordReset(eq("user@example.com"))
        val viewModel = createViewModel()

        // Deliberately not advancing time: the cooldown counts down from 60 over real seconds,
        // so draining it with advanceUntilIdle() would race the loop to completion (back to 0)
        // before this assertion ever gets to observe the "just started" window.
        viewModel.sendPasswordReset("user@example.com")

        assertEquals(Unit, viewModel.resetState.value.data)
        assertTrue(viewModel.resetCooldown.value > 0)
    }

    @Test
    fun `sendPasswordReset failure surfaces the mapped error and leaves cooldown untouched`() = runTest {
        doAnswer { Resource.Error("No account found with this email.") }
            .`when`(authRepository).sendPasswordReset(any())
        val viewModel = createViewModel()

        viewModel.sendPasswordReset("missing@example.com")
        advanceUntilIdle()

        assertEquals("No account found with this email.", viewModel.resetState.value.error)
        assertEquals(0, viewModel.resetCooldown.value)
    }

    @Test
    fun `sendPasswordReset is blocked while the cooldown is still counting down`() = runTest {
        doAnswer { Resource.Success(Unit) }.`when`(authRepository).sendPasswordReset(any())
        val viewModel = createViewModel()
        viewModel.sendPasswordReset("user@example.com")

        viewModel.sendPasswordReset("user@example.com")

        verify(authRepository, times(1)).sendPasswordReset(any())
    }

    // ── Change password ───────────────────────────────────────────────────────

    @Test
    fun `changePassword rejects a new password shorter than 6 characters without calling the repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.changePassword("currentPass1", "abc")

        assertEquals("New password must be at least 6 characters", viewModel.changePasswordState.value.error)
        verify(authRepository, times(0)).reauthenticate(any())
    }

    @Test
    fun `changePassword reauthenticates before updating and succeeds when both steps succeed`() = runTest {
        doAnswer { Resource.Success(Unit) }.`when`(authRepository).reauthenticate(eq("currentPass1"))
        doAnswer { Resource.Success(Unit) }.`when`(authRepository).updatePassword(eq("newPass123"))
        val viewModel = createViewModel()

        viewModel.changePassword("currentPass1", "newPass123")
        advanceUntilIdle()

        verify(authRepository).reauthenticate(eq("currentPass1"))
        verify(authRepository).updatePassword(eq("newPass123"))
        assertEquals(Unit, viewModel.changePasswordState.value.data)
    }

    @Test
    fun `changePassword stops at reauthentication failure and never calls updatePassword`() = runTest {
        doAnswer { Resource.Error("Incorrect email or password. Please try again.") }
            .`when`(authRepository).reauthenticate(any())
        val viewModel = createViewModel()

        viewModel.changePassword("wrongPass", "newPass123")
        advanceUntilIdle()

        assertEquals("Incorrect email or password. Please try again.", viewModel.changePasswordState.value.error)
        verify(authRepository, times(0)).updatePassword(any())
    }

    @Test
    fun `clearChangePasswordState resets the state back to initial`() = runTest {
        doAnswer { Resource.Error("boom") }.`when`(authRepository).reauthenticate(any())
        val viewModel = createViewModel()
        viewModel.changePassword("wrongPass", "newPass123")
        advanceUntilIdle()

        viewModel.clearChangePasswordState()

        assertNull(viewModel.changePasswordState.value.error)
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Test
    fun `logout signs out, clears all state, and emits the logout event`() = runTest {
        doAnswer { Resource.Success(Unit) }.`when`(authRepository).login(any(), any())
        val viewModel = createViewModel()
        viewModel.login("user@example.com", "password1")
        advanceUntilIdle()

        var eventReceived = false
        val job = launch {
            viewModel.logoutEvent.collect { eventReceived = true }
        }

        viewModel.logout()
        advanceUntilIdle()

        verify(authRepository).logout()
        assertTrue(eventReceived)
        assertNull(viewModel.loginState.value.data)
        job.cancel()
    }
}
