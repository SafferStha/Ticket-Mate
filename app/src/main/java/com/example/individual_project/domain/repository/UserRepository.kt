package com.example.individual_project.domain.repository

import android.net.Uri
import com.example.individual_project.data.model.User
import com.example.individual_project.utils.Resource

interface UserRepository {
    suspend fun getUserProfile(uid: String): Resource<User>
    suspend fun updateUserProfile(user: User): Resource<Unit>
    suspend fun uploadProfileImage(uid: String, imageUri: Uri): Resource<String>

    // ── Favorites ──────────────────────────────────────────────────────────────
    suspend fun getFavoriteEventIds(uid: String): Resource<List<String>>
    suspend fun removeFavorite(uid: String, eventId: String): Resource<Unit>
}
