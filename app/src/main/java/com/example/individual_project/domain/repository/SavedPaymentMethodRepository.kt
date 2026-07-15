package com.example.individual_project.domain.repository

import com.example.individual_project.data.model.SavedPaymentMethod
import com.example.individual_project.utils.Resource

interface SavedPaymentMethodRepository {
    suspend fun getSavedPaymentMethods(userId: String): Resource<List<SavedPaymentMethod>>
    suspend fun addSavedPaymentMethod(method: SavedPaymentMethod): Resource<String>
    suspend fun deleteSavedPaymentMethod(userId: String, methodId: String): Resource<Unit>
    suspend fun setDefaultPaymentMethod(userId: String, methodId: String): Resource<Unit>
}
