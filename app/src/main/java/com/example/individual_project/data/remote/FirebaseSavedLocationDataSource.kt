package com.example.individual_project.data.remote

import com.example.individual_project.data.model.SavedLocation
import com.example.individual_project.utils.FirebaseErrorMapper
import com.example.individual_project.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase structure:
 *   saved_locations/{userId}/{locationId} -> SavedLocation
 */
@Singleton
class FirebaseSavedLocationDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {
    private fun locationsRef(userId: String) =
        database.getReference("saved_locations").child(userId)

    suspend fun getSavedLocations(userId: String): Resource<List<SavedLocation>> = try {
        val snapshot = locationsRef(userId).get().await()
        val locations = snapshot.children
            .mapNotNull { it.getValue(SavedLocation::class.java) }
            .sortedByDescending { it.createdAt }
        Resource.Success(locations)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to load saved locations"), e)
    }

    suspend fun addSavedLocation(location: SavedLocation): Resource<String> = try {
        val ref = locationsRef(location.userId)
        val key = ref.push().key ?: return Resource.Error("Failed to generate location id")
        val now = System.currentTimeMillis()

        // A user's very first saved location becomes their default automatically.
        val existing = ref.get().await()
        val isFirst  = !existing.hasChildren()

        val toSave = location.copy(id = key, createdAt = now, updatedAt = now, isDefault = isFirst)
        ref.child(key).setValue(toSave).await()
        Resource.Success(key)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to add saved location"), e)
    }

    suspend fun updateSavedLocation(location: SavedLocation): Resource<Unit> = try {
        val updates = mapOf(
            "label"     to location.label,
            "address"   to location.address,
            "city"      to location.city,
            "updatedAt" to System.currentTimeMillis()
        )
        locationsRef(location.userId).child(location.id).updateChildren(updates).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to update saved location"), e)
    }

    suspend fun deleteSavedLocation(userId: String, locationId: String): Resource<Unit> = try {
        val ref = locationsRef(userId)
        val wasDefault = ref.child(locationId).child("isDefault").get().await()
            .getValue(Boolean::class.java) == true

        ref.child(locationId).removeValue().await()

        // If the deleted location was the default, promote the next remaining one (if any)
        // so the user always has at most one, unambiguous default.
        if (wasDefault) {
            val remaining = ref.get().await().children.mapNotNull { it.getValue(SavedLocation::class.java) }
            remaining.minByOrNull { it.createdAt }?.let { next ->
                ref.child(next.id).child("isDefault").setValue(true).await()
            }
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to delete saved location"), e)
    }

    suspend fun setDefaultLocation(userId: String, locationId: String): Resource<Unit> = try {
        val ref = locationsRef(userId)
        val all = ref.get().await().children.mapNotNull { it.getValue(SavedLocation::class.java) }

        // Single multi-path update: RTDB applies all paths in one write, so there's never a
        // moment where two locations are simultaneously marked default.
        val updates = buildMap {
            all.forEach { loc -> put("${loc.id}/isDefault", loc.id == locationId) }
            put("$locationId/updatedAt", System.currentTimeMillis())
        }
        ref.updateChildren(updates).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to set default location"), e)
    }
}
