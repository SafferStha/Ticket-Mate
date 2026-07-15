package com.example.individual_project.domain.repository

import com.example.individual_project.data.model.User
import com.example.individual_project.utils.Resource

interface AuthRepository {
    val currentUserId   : String?
    val isLoggedIn      : Boolean
    val isEmailVerified : Boolean

    suspend fun login(email: String, password: String): Resource<Unit>
    suspend fun register(email: String, password: String, name: String, contact: String): Resource<Unit>
    suspend fun sendPasswordReset(email: String): Resource<Unit>
    suspend fun sendEmailVerification(): Resource<Unit>
    suspend fun reauthenticate(currentPassword: String): Resource<Unit>
    suspend fun updatePassword(newPassword: String): Resource<Unit>
    suspend fun reloadUser(): Resource<Unit>
    suspend fun getUserProfile(uid: String): Resource<User>
    fun logout(): Resource<Unit>
}
