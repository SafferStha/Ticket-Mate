package com.example.individual_project.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for whether the current user has the `admin` Firebase Auth custom
 * claim (see functions/src/index.ts:setAdminClaim -- the only place that claim is ever set).
 *
 * There is deliberately no client-writable "role" field anywhere in this app: a custom claim
 * lives on the ID token itself, signed by Firebase, and cannot be forged or self-granted by
 * modifying data the client can write. database.rules.json enforces the same claim
 * server-side for any admin-only write, so this StateFlow is a UI convenience only -- it
 * gates navigation and button visibility, not actual authorization.
 */
@Singleton
class AdminStateManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        if (auth.currentUser == null) {
            _isAdmin.value = false
        } else {
            refresh()
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    /**
     * Re-reads the admin claim from a fresh ID token.
     * @param forceTokenRefresh pass true right after granting/revoking admin (via
     *   setAdminClaim) -- Firebase caches ID tokens for up to an hour otherwise, so a claim
     *   change made this moment wouldn't be visible on this device without forcing a refresh.
     */
    fun refresh(forceTokenRefresh: Boolean = false) {
        val user = firebaseAuth.currentUser ?: run {
            _isAdmin.value = false
            return
        }
        scope.launch {
            try {
                val result = user.getIdToken(forceTokenRefresh).await()
                _isAdmin.value = result.claims["admin"] == true
            } catch (_: Exception) {
                // Network error -- leave the last-known value in place rather than flipping
                // a legitimate admin to "not admin" on a transient failure.
            }
        }
    }
}
