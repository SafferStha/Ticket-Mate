package com.example.individual_project.testdi

import android.net.Uri
import com.example.individual_project.data.model.User
import com.example.individual_project.domain.repository.UserRepository
import com.example.individual_project.utils.Resource

/** In-memory stand-in for [UserRepository]. */
class FakeUserRepository : UserRepository {

    var profile: User? = User(uid = "fake-uid", name = "Fake User", email = "fake@example.com")
    val favoriteEventIds = mutableListOf<String>()
    var nextUploadImageResult: Resource<String> = Resource.Success("https://example.com/fake-image.jpg")

    override suspend fun getUserProfile(uid: String): Resource<User> =
        profile?.let { Resource.Success(it) } ?: Resource.Error("Profile not found")

    override suspend fun updateUserProfile(user: User): Resource<Unit> {
        profile = user
        return Resource.Success(Unit)
    }

    override suspend fun uploadProfileImage(uid: String, imageUri: Uri): Resource<String> = nextUploadImageResult

    override suspend fun getFavoriteEventIds(uid: String): Resource<List<String>> =
        Resource.Success(favoriteEventIds.toList())

    override suspend fun removeFavorite(uid: String, eventId: String): Resource<Unit> {
        favoriteEventIds.remove(eventId)
        return Resource.Success(Unit)
    }

    fun reset() {
        profile = User(uid = "fake-uid", name = "Fake User", email = "fake@example.com")
        favoriteEventIds.clear()
        nextUploadImageResult = Resource.Success("https://example.com/fake-image.jpg")
    }
}
