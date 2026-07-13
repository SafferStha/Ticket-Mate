package com.example.individual_project.data.remote

import android.net.Uri
import com.example.individual_project.data.model.User
import com.example.individual_project.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUserDataSource @Inject constructor(
    private val database : FirebaseDatabase,
    private val storage  : FirebaseStorage
) {
    private fun userRef(uid: String) = database.getReference("users").child(uid)

    suspend fun getUserProfile(uid: String): Resource<User> = try {
        val snapshot = userRef(uid).get().await()
        val user = snapshot.getValue(User::class.java)
            ?: return Resource.Error("User profile not found")
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch user profile", e)
    }

    suspend fun updateUserProfile(user: User): Resource<Unit> = try {
        val updates = mapOf(
            "name"         to user.name,
            "contact"      to user.contact,
            "profileImage" to user.profileImage
        )
        userRef(user.uid).updateChildren(updates).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to update profile", e)
    }

    suspend fun uploadProfileImage(uid: String, imageUri: Uri): Resource<String> = try {
        val ref = storage.reference.child("profile_images/$uid.jpg")
        ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        Resource.Success(downloadUrl)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to upload profile image", e)
    }

    // ── Favorites ─────────────────────────────────────────────────────────────────
    // Firebase path: favorites/{uid}/{eventId} = true

    suspend fun getFavoriteEventIds(uid: String): Resource<List<String>> = try {
        val snapshot = database.getReference("favorites").child(uid).get().await()
        val ids = snapshot.children.mapNotNull { it.key }
        Resource.Success(ids)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch favorites", e)
    }

    suspend fun removeFavorite(uid: String, eventId: String): Resource<Unit> = try {
        database.getReference("favorites").child(uid).child(eventId).removeValue().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to remove favorite", e)
    }
}
