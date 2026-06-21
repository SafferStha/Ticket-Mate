package com.example.individual_project.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthState {
    object Loading          : AuthState()
    object Authenticated    : AuthState()
    object Unauthenticated  : AuthState()
    object EmailNotVerified : AuthState()
}

/**
 * Single source of truth for authentication state.
 * Listens to Firebase AuthStateListener and exposes a reactive StateFlow.
 */
@Singleton
class AuthStateManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        _authState.value = when {
            user == null             -> AuthState.Unauthenticated
            !user.isEmailVerified    -> AuthState.EmailNotVerified
            else                     -> AuthState.Authenticated
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    /**
     * Reload the current user from Firebase and refresh auth state.
     * Call after the user taps "I've Verified My Email" on VerifyEmailScreen.
     */
    suspend fun refreshVerificationStatus() {
        try {
            firebaseAuth.currentUser?.reload()?.await()
        } catch (_: Exception) { /* network error — authState stays as-is */ }

        val user = firebaseAuth.currentUser
        _authState.value = when {
            user == null             -> AuthState.Unauthenticated
            !user.isEmailVerified    -> AuthState.EmailNotVerified
            else                     -> AuthState.Authenticated
        }
    }
}
