package com.example.individual_project.testdi

import com.example.individual_project.data.model.SavedPaymentMethod
import com.example.individual_project.domain.repository.SavedPaymentMethodRepository
import com.example.individual_project.utils.Resource

/** In-memory stand-in for [SavedPaymentMethodRepository]. */
class FakeSavedPaymentMethodRepository : SavedPaymentMethodRepository {

    val methods = mutableListOf<SavedPaymentMethod>()
    private var idCounter = 0

    override suspend fun getSavedPaymentMethods(userId: String): Resource<List<SavedPaymentMethod>> =
        Resource.Success(methods.filter { it.userId == userId })

    override suspend fun addSavedPaymentMethod(method: SavedPaymentMethod): Resource<String> {
        val id = "method${++idCounter}"
        methods.add(method.copy(id = id))
        return Resource.Success(id)
    }

    override suspend fun deleteSavedPaymentMethod(userId: String, methodId: String): Resource<Unit> {
        methods.removeAll { it.id == methodId }
        return Resource.Success(Unit)
    }

    override suspend fun setDefaultPaymentMethod(userId: String, methodId: String): Resource<Unit> {
        methods.replaceAll { it.copy(isDefault = it.id == methodId) }
        return Resource.Success(Unit)
    }

    fun reset() {
        methods.clear()
        idCounter = 0
    }
}
