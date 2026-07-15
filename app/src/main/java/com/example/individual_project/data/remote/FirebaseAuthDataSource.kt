package com.example.individual_project.data.remote

import com.example.individual_project.data.model.User
import com.example.individual_project.utils.AuthErrorMapper
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns all Firebase Authentication operations.
 * All errors are mapped through AuthErrorMapper — raw Firebase messages never reach the UI.
 * Passwords are NEVER stored in the database; Firebase Auth handles credential storage.
 */
@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth     : FirebaseAuth,
    private val database : FirebaseDatabase
) {
    val currentUser     : FirebaseUser? get() = auth.currentUser
    val currentUserId   : String?       get() = auth.currentUser?.uid
    val isEmailVerified : Boolean       get() = auth.currentUser?.isEmailVerified == true

    suspend fun login(email: String, password: String): Resource<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user   = result.user ?: return Resource.Error("Login succeeded but user is null")
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(AuthErrorMapper.map(e), e)
    }

    suspend fun register(
        email   : String,
        password: String,
        name    : String,
        contact : String
    ): Resource<FirebaseUser> = try {
        val authResult   = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: return Resource.Error("Registration succeeded but user is null")

        val userMap = mapOf(
            "uid"          to firebaseUser.uid,
            "name"         to name,
            "email"        to email,
            "contact"      to contact,
            "createdAt"    to System.currentTimeMillis(),
            "profileImage" to ""
        )
        database.getReference("users").child(firebaseUser.uid).setValue(userMap).await()

        // Send verification email immediately — user must verify before accessing the app
        firebaseUser.sendEmailVerification().await()

        Resource.Success(firebaseUser)
    } catch (e: Exception) {
        Resource.Error(AuthErrorMapper.map(e), e)
    }

    suspend fun sendEmailVerification(): Resource<Unit> = try {
        val user = auth.currentUser ?: return Resource.Error("No authenticated user found.")
        user.sendEmailVerification().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(AuthErrorMapper.map(e), e)
    }

    suspend fun reloadUser(): Resource<Unit> = try {
        val user = auth.currentUser ?: return Resource.Error("No authenticated user found.")
        user.reload().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(AuthErrorMapper.map(e), e)
    }

    suspend fun sendPasswordReset(email: String): Resource<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(AuthErrorMapper.map(e), e)
    }

    suspend fun getUserProfile(uid: String): Resource<User> = try {
        val snapshot = database.getReference("users").child(uid).get().await()
        val user     = snapshot.getValue(User::class.java)
            ?: return Resource.Error("User profile not found.")
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(AuthErrorMapper.map(e), e)
    }

    /** Re-proves the user's identity with their current password. Required by Firebase
     *  before a sensitive operation like updatePassword() on a session that isn't fresh. */
    suspend fun reauthenticate(currentPassword: String): Resource<Unit> = try {
        val user  = auth.currentUser ?: return Resource.Error("No authenticated user found.")
        val email = user.email ?: return Resource.Error("No email associated with this account.")
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).await()
        Resource.Success(Unit)
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Resource.Error("Current password is incorrect.", e)
    } catch (e: Exception) {
        Resource.Error(AuthErrorMapper.map(e), e)
    }

    suspend fun updatePassword(newPassword: String): Resource<Unit> = try {
        val user = auth.currentUser ?: return Resource.Error("No authenticated user found.")
        user.updatePassword(newPassword).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(AuthErrorMapper.map(e), e)
    }

    fun logout(): Resource<Unit> = try {
        auth.signOut()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(AuthErrorMapper.map(e), e)
    }
}
