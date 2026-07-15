package com.example.individual_project.testdi

import com.example.individual_project.data.model.SavedLocation
import com.example.individual_project.domain.repository.SavedLocationRepository
import com.example.individual_project.utils.Resource

/** In-memory stand-in for [SavedLocationRepository]. */
class FakeSavedLocationRepository : SavedLocationRepository {

    val locations = mutableListOf<SavedLocation>()
    private var idCounter = 0

    override suspend fun getSavedLocations(userId: String): Resource<List<SavedLocation>> =
        Resource.Success(locations.filter { it.userId == userId })

    override suspend fun addSavedLocation(location: SavedLocation): Resource<String> {
        val id = "location${++idCounter}"
        locations.add(location.copy(id = id))
        return Resource.Success(id)
    }

    override suspend fun updateSavedLocation(location: SavedLocation): Resource<Unit> {
        val index = locations.indexOfFirst { it.id == location.id }
        if (index == -1) return Resource.Error("Location not found")
        locations[index] = location
        return Resource.Success(Unit)
    }

    override suspend fun deleteSavedLocation(userId: String, locationId: String): Resource<Unit> {
        locations.removeAll { it.id == locationId }
        return Resource.Success(Unit)
    }

    override suspend fun setDefaultLocation(userId: String, locationId: String): Resource<Unit> {
        locations.replaceAll { it.copy(isDefault = it.id == locationId) }
        return Resource.Success(Unit)
    }

    fun reset() {
        locations.clear()
        idCounter = 0
    }
}
