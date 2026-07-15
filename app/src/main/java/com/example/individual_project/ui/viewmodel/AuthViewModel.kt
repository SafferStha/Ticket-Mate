package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individual_project.auth.AdminStateManager
import com.example.individual_project.auth.AuthStateManager
import com.example.individual_project.domain.repository.AuthRepository
import com.example.individual_project.ui.model.UiState
import com.example.individual_project.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RESEND_COOLDOWN_SECONDS = 60

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository   : AuthRepository,
    val          authStateManager: AuthStateManager,
    private val adminStateManager: AdminStateManager
) : ViewModel() {

    // ── Per-operation UI states ───────────────────────────────────────────────

    private val _loginState = MutableStateFlow(UiState<Unit>())
    val loginState: StateFlow<UiState<Unit>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(UiState<Unit>())
    val registerState: StateFlow<UiState<Unit>> = _registerState.asStateFlow()

    private val _resetState = MutableStateFlow(UiState<Unit>())
    val resetState: StateFlow<UiState<Unit>> = _resetState.asStateFlow()

    private val _verifyEmailState = MutableStateFlow(UiState<Unit>())
    val verifyEmailState: StateFlow<UiState<Unit>> = _verifyEmailState.asStateFlow()

    private val _changePasswordState = MutableStateFlow(UiState<Unit>())
    val changePasswordState: StateFlow<UiState<Unit>> = _changePasswordState.asStateFlow()

    // ── Resend cooldowns (seconds remaining; 0 = ready) ──────────────────────
    private val _resetCooldown = MutableStateFlow(0)
    val resetCooldown: StateFlow<Int> = _resetCooldown.asStateFlow()
    private var resetCooldownJob: Job? = null

    private val _verifyEmailCooldown = MutableStateFlow(0)
    val verifyEmailCooldown: StateFlow<Int> = _verifyEmailCooldown.asStateFlow()
    private var verifyEmailCooldownJob: Job? = null

    // ── One-shot logout navigation event ─────────────────────────────────────
    private val _logoutEvent = Channel<Unit>(Channel.BUFFERED)
    val logoutEvent = _logoutEvent.receiveAsFlow()

    // ── Auth state (single source of truth) ──────────────────────────────────
    val authState = authStateManager.authState
    val isAdmin   = adminStateManager.isAdmin

    // ── Derived properties ────────────────────────────────────────────────────
    val isLoggedIn      : Boolean get() = authRepository.isLoggedIn
    val currentUserId   : String? get() = authRepository.currentUserId
    val isEmailVerified : Boolean get() = authRepository.isEmailVerified

    // ── Operations ────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        if (_loginState.value.isLoading) return
        viewModelScope.launch {
            _loginState.value = UiState(isLoading = true)
            _loginState.value = when (val r = authRepository.login(email, password)) {
                is Resource.Success -> UiState(data = Unit)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error during login")
            }
        }
    }

    fun register(email: String, password: String, name: String, contact: String) {
        if (_registerState.value.isLoading) return
        viewModelScope.launch {
            _registerState.value = UiState(isLoading = true)
            _registerState.value = when (val r = authRepository.register(email, password, name, contact)) {
                is Resource.Success -> UiState(data = Unit)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error during registration")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (_resetState.value.isLoading || _resetCooldown.value > 0) return
        viewModelScope.launch {
            _resetState.value = UiState(isLoading = true)
            _resetState.value = when (val r = authRepository.sendPasswordReset(email)) {
                is Resource.Success -> {
                    resetCooldownJob = startCooldown(_resetCooldown)
                    UiState(data = Unit)
                }
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error sending reset email")
            }
        }
    }

    fun resendVerificationEmail() {
        if (_verifyEmailState.value.isLoading || _verifyEmailCooldown.value > 0) return
        viewModelScope.launch {
            _verifyEmailState.value = UiState(isLoading = true)
            _verifyEmailState.value = when (val r = authRepository.sendEmailVerification()) {
                is Resource.Success -> {
                    verifyEmailCooldownJob = startCooldown(_verifyEmailCooldown)
                    UiState(data = Unit)
                }
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Failed to resend verification email")
            }
        }
    }

    /**
     * Change password while logged in (distinct from the "forgot password" reset-email flow).
     * Firebase requires a fresh sign-in before a sensitive op like updatePassword(), so this
     * reauthenticates with the current password first.
     */
    fun changePassword(currentPassword: String, newPassword: String) {
        if (_changePasswordState.value.isLoading) return
        if (newPassword.length < 6) {
            _changePasswordState.value = UiState(error = "New password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _changePasswordState.value = UiState(isLoading = true)

            val reauthResult = authRepository.reauthenticate(currentPassword)
            if (reauthResult is Resource.Error) {
                _changePasswordState.value = UiState(error = reauthResult.message)
                return@launch
            }

            _changePasswordState.value = when (val r = authRepository.updatePassword(newPassword)) {
                is Resource.Success -> UiState(data = Unit)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error updating password")
            }
        }
    }

    fun clearChangePasswordState() { _changePasswordState.value = UiState() }

    /** Counts a cooldown flow down from [RESEND_COOLDOWN_SECONDS] to 0, one tick per second. */
    private fun startCooldown(cooldown: MutableStateFlow<Int>): Job =
        viewModelScope.launch {
            for (remaining in RESEND_COOLDOWN_SECONDS downTo 0) {
                cooldown.value = remaining
                delay(1000)
            }
        }

    /** Reload the Firebase user and push the new AuthState through AuthStateManager. */
    fun refreshVerificationStatus() {
        viewModelScope.launch {
            authStateManager.refreshVerificationStatus()
        }
    }

    /**
     * Clean logout:
     *  1. Signs out from Firebase (clears cached credentials)
     *  2. Resets all UI states
     *  3. Emits a one-shot event so the UI can navigate to Login with a cleared back stack
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            clearAllState()
            _logoutEvent.send(Unit)
        }
    }

    // ── State clearers ────────────────────────────────────────────────────────

    fun clearLoginState()       { _loginState.value       = UiState() }
    fun clearRegisterState()    { _registerState.value    = UiState() }
    fun clearResetState()       { _resetState.value       = UiState() }
    fun clearVerifyEmailState() { _verifyEmailState.value = UiState() }

    private fun clearAllState() {
        _loginState.value          = UiState()
        _registerState.value       = UiState()
        _resetState.value          = UiState()
        _verifyEmailState.value    = UiState()
        _changePasswordState.value = UiState()
        resetCooldownJob?.cancel()
        verifyEmailCooldownJob?.cancel()
        _resetCooldown.value       = 0
        _verifyEmailCooldown.value = 0
    }
}
