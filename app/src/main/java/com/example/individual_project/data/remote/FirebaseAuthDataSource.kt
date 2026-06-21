package com.example.individual_project.data.remote

import com.example.individual_project.data.model.User
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns all Firebase Authentication operations.
 * Also writes the user profile record to Realtime Database on first registration.
 * Passwords are NEVER stored — Firebase Auth handles credential storage.
 */
@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth     : FirebaseAuth,
    private val database : FirebaseDatabase
) {
    val currentUser: FirebaseUser? get() = auth.currentUser
    val currentUserId: String?     get() = auth.currentUser?.uid

    suspend fun login(email: String, password: String): Resource<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user   = result.user ?: return Resource.Error("Login succeeded but user is null")
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Login failed", e)
    }

    suspend fun register(
        email    : String,
        password : String,
        name     : String,
        contact  : String
    ): Resource<FirebaseUser> = try {
        val authResult   = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: return Resource.Error("Registration succeeded but user is null")

        // Persist profile — no password written to DB
        val userMap = mapOf(
            "uid"          to firebaseUser.uid,
            "name"         to name,
            "email"        to email,
            "contact"      to contact,
            "profileImage" to ""
        )
        database.getReference("users").child(firebaseUser.uid).setValue(userMap).await()
        Resource.Success(firebaseUser)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Registration failed", e)
    }

    suspend fun sendPasswordReset(email: String): Resource<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to send reset email", e)
    }

    suspend fun getUserProfile(uid: String): Resource<User> = try {
        val snapshot = database.getReference("users").child(uid).get().await()
        val user     = snapshot.getValue(User::class.java)
            ?: return Resource.Error("User profile not found")
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch user profile", e)
    }

    fun logout(): Resource<Unit> = try {
        auth.signOut()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Logout failed", e)
    }
}
