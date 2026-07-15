package com.example.individual_project.testdi

import com.example.individual_project.data.model.User
import com.example.individual_project.domain.repository.AuthRepository
import com.example.individual_project.utils.Resource

/**
 * In-memory stand-in for [AuthRepository]. Every operation is configurable via the `next*`
 * fields so a test can arrange a specific success/failure before driving the screen, and
 * `reset()` restores default (happy-path, logged-out) behavior between tests.
 */
class FakeAuthRepository : AuthRepository {

    var fakeCurrentUserId: String? = null
    var fakeIsLoggedIn: Boolean = false
    var fakeIsEmailVerified: Boolean = true

    var nextLoginResult: Resource<Unit> = Resource.Success(Unit)
    var nextRegisterResult: Resource<Unit> = Resource.Success(Unit)
    var nextSendPasswordResetResult: Resource<Unit> = Resource.Success(Unit)
    var nextSendEmailVerificationResult: Resource<Unit> = Resource.Success(Unit)
    var nextReauthenticateResult: Resource<Unit> = Resource.Success(Unit)
    var nextUpdatePasswordResult: Resource<Unit> = Resource.Success(Unit)
    var nextUserProfileResult: Resource<User> = Resource.Success(User(uid = "fake-uid", name = "Fake User"))

    var lastLoginEmail: String? = null
    var lastRegisterEmail: String? = null

    override val currentUserId: String? get() = fakeCurrentUserId
    override val isLoggedIn: Boolean get() = fakeIsLoggedIn
    override val isEmailVerified: Boolean get() = fakeIsEmailVerified

    override suspend fun login(email: String, password: String): Resource<Unit> {
        lastLoginEmail = email
        if (nextLoginResult is Resource.Success) {
            fakeIsLoggedIn = true
            fakeCurrentUserId = "fake-uid"
        }
        return nextLoginResult
    }

    override suspend fun register(email: String, password: String, name: String, contact: String): Resource<Unit> {
        lastRegisterEmail = email
        if (nextRegisterResult is Resource.Success) {
            fakeIsLoggedIn = true
            fakeCurrentUserId = "fake-uid"
            fakeIsEmailVerified = false
        }
        return nextRegisterResult
    }

    override suspend fun sendPasswordReset(email: String): Resource<Unit> = nextSendPasswordResetResult
    override suspend fun sendEmailVerification(): Resource<Unit> = nextSendEmailVerificationResult
    override suspend fun reauthenticate(currentPassword: String): Resource<Unit> = nextReauthenticateResult
    override suspend fun updatePassword(newPassword: String): Resource<Unit> = nextUpdatePasswordResult
    override suspend fun reloadUser(): Resource<Unit> = Resource.Success(Unit)
    override suspend fun getUserProfile(uid: String): Resource<User> = nextUserProfileResult

    override fun logout(): Resource<Unit> {
        fakeIsLoggedIn = false
        fakeCurrentUserId = null
        return Resource.Success(Unit)
    }

    /** Restores default (logged-out, happy-path) behavior. Call from `@Before` in every test. */
    fun reset() {
        fakeCurrentUserId = null
        fakeIsLoggedIn = false
        fakeIsEmailVerified = true
        nextLoginResult = Resource.Success(Unit)
        nextRegisterResult = Resource.Success(Unit)
        nextSendPasswordResetResult = Resource.Success(Unit)
        nextSendEmailVerificationResult = Resource.Success(Unit)
        nextReauthenticateResult = Resource.Success(Unit)
        nextUpdatePasswordResult = Resource.Success(Unit)
        nextUserProfileResult = Resource.Success(User(uid = "fake-uid", name = "Fake User"))
        lastLoginEmail = null
        lastRegisterEmail = null
    }
}
