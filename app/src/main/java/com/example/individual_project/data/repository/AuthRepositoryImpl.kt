package com.example.individual_project.data.repository

import com.example.individual_project.data.model.User
import com.example.individual_project.data.remote.FirebaseAuthDataSource
import com.example.individual_project.domain.repository.AuthRepository
import com.example.individual_project.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource
) : AuthRepository {

    override val currentUserId: String? get() = authDataSource.currentUserId
    override val isLoggedIn: Boolean    get() = authDataSource.currentUser != null

    override suspend fun login(email: String, password: String): Resource<Unit> =
        when (val r = authDataSource.login(email, password)) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error   -> r
            else                -> Resource.Error("Unexpected state during login")
        }

    override suspend fun register(
        email: String, password: String, name: String, contact: String
    ): Resource<Unit> =
        when (val r = authDataSource.register(email, password, name, contact)) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error   -> r
            else                -> Resource.Error("Unexpected state during registration")
        }

    override suspend fun sendPasswordReset(email: String): Resource<Unit> =
        authDataSource.sendPasswordReset(email)

    override suspend fun getUserProfile(uid: String): Resource<User> =
        authDataSource.getUserProfile(uid)

    override fun logout(): Resource<Unit> = authDataSource.logout()
}
