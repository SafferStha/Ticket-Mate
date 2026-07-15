package com.example.individual_project.data.remote

import com.example.individual_project.data.model.SavedPaymentMethod
import com.example.individual_project.utils.FirebaseErrorMapper
import com.example.individual_project.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase structure:
 *   saved_payment_methods/{userId}/{methodId} -> SavedPaymentMethod
 */
@Singleton
class FirebaseSavedPaymentMethodDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {
    private fun methodsRef(userId: String) =
        database.getReference("saved_payment_methods").child(userId)

    suspend fun getSavedPaymentMethods(userId: String): Resource<List<SavedPaymentMethod>> = try {
        val snapshot = methodsRef(userId).get().await()
        val methods = snapshot.children
            .mapNotNull { it.getValue(SavedPaymentMethod::class.java) }
            .sortedByDescending { it.createdAt }
        Resource.Success(methods)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to load saved payment methods"), e)
    }

    suspend fun addSavedPaymentMethod(method: SavedPaymentMethod): Resource<String> = try {
        val ref = methodsRef(method.userId)
        val key = ref.push().key ?: return Resource.Error("Failed to generate payment method id")

        val existing = ref.get().await()
        val isFirst  = !existing.hasChildren()

        val toSave = method.copy(id = key, createdAt = System.currentTimeMillis(), isDefault = isFirst)
        ref.child(key).setValue(toSave).await()
        Resource.Success(key)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to add payment method"), e)
    }

    suspend fun deleteSavedPaymentMethod(userId: String, methodId: String): Resource<Unit> = try {
        val ref = methodsRef(userId)
        val wasDefault = ref.child(methodId).child("isDefault").get().await()
            .getValue(Boolean::class.java) == true

        ref.child(methodId).removeValue().await()

        if (wasDefault) {
            val remaining = ref.get().await().children.mapNotNull { it.getValue(SavedPaymentMethod::class.java) }
            remaining.minByOrNull { it.createdAt }?.let { next ->
                ref.child(next.id).child("isDefault").setValue(true).await()
            }
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to delete payment method"), e)
    }

    suspend fun setDefaultPaymentMethod(userId: String, methodId: String): Resource<Unit> = try {
        val ref = methodsRef(userId)
        val all = ref.get().await().children.mapNotNull { it.getValue(SavedPaymentMethod::class.java) }

        val updates = buildMap {
            all.forEach { m -> put("${m.id}/isDefault", m.id == methodId) }
        }
        ref.updateChildren(updates).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to set default payment method"), e)
    }
}
