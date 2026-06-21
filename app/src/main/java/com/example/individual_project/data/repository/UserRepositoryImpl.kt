package com.example.individual_project.data.repository

import android.net.Uri
import com.example.individual_project.data.model.User
import com.example.individual_project.data.remote.FirebaseUserDataSource
import com.example.individual_project.domain.repository.UserRepository
import com.example.individual_project.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDataSource: FirebaseUserDataSource
) : UserRepository {

    override suspend fun getUserProfile(uid: String): Resource<User>             = userDataSource.getUserProfile(uid)
    override suspend fun updateUserProfile(user: User): Resource<Unit>           = userDataSource.updateUserProfile(user)
    override suspend fun uploadProfileImage(uid: String, imageUri: Uri): Resource<String> = userDataSource.uploadProfileImage(uid, imageUri)
}
