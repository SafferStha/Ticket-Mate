package com.example.individual_project.data.repository

import com.example.individual_project.data.model.SavedPaymentMethod
import com.example.individual_project.data.remote.FirebaseSavedPaymentMethodDataSource
import com.example.individual_project.domain.repository.SavedPaymentMethodRepository
import com.example.individual_project.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedPaymentMethodRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseSavedPaymentMethodDataSource
) : SavedPaymentMethodRepository {

    override suspend fun getSavedPaymentMethods(userId: String): Resource<List<SavedPaymentMethod>> =
        dataSource.getSavedPaymentMethods(userId)

    override suspend fun addSavedPaymentMethod(method: SavedPaymentMethod): Resource<String> =
        dataSource.addSavedPaymentMethod(method)

    override suspend fun deleteSavedPaymentMethod(userId: String, methodId: String): Resource<Unit> =
        dataSource.deleteSavedPaymentMethod(userId, methodId)

    override suspend fun setDefaultPaymentMethod(userId: String, methodId: String): Resource<Unit> =
        dataSource.setDefaultPaymentMethod(userId, methodId)
}
