package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individual_project.auth.AuthStateManager
import com.example.individual_project.domain.repository.AuthRepository
import com.example.individual_project.ui.model.UiState
import com.example.individual_project.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository  : AuthRepository,
    val          authStateManager: AuthStateManager
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

    // ── One-shot logout navigation event ─────────────────────────────────────
    private val _logoutEvent = Channel<Unit>(Channel.BUFFERED)
    val logoutEvent = _logoutEvent.receiveAsFlow()

    // ── Auth state (single source of truth) ──────────────────────────────────
    val authState = authStateManager.authState

    // ── Derived properties ────────────────────────────────────────────────────
    val isLoggedIn      : Boolean get() = authRepository.isLoggedIn
    val currentUserId   : String? get() = authRepository.currentUserId
    val isEmailVerified : Boolean get() = authRepository.isEmailVerified

    // ── Operations ────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
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
        viewModelScope.launch {
            _resetState.value = UiState(isLoading = true)
            _resetState.value = when (val r = authRepository.sendPasswordReset(email)) {
                is Resource.Success -> UiState(data = Unit)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error sending reset email")
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _verifyEmailState.value = UiState(isLoading = true)
            _verifyEmailState.value = when (val r = authRepository.sendEmailVerification()) {
                is Resource.Success -> UiState(data = Unit)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Failed to resend verification email")
            }
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
        _loginState.value       = UiState()
        _registerState.value    = UiState()
        _resetState.value       = UiState()
        _verifyEmailState.value = UiState()
    }
}
