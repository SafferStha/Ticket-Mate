package com.example.individual_project.domain.repository

import com.example.individual_project.data.model.SavedLocation
import com.example.individual_project.utils.Resource

interface SavedLocationRepository {
    suspend fun getSavedLocations(userId: String): Resource<List<SavedLocation>>
    suspend fun addSavedLocation(location: SavedLocation): Resource<String>
    suspend fun updateSavedLocation(location: SavedLocation): Resource<Unit>
    suspend fun deleteSavedLocation(userId: String, locationId: String): Resource<Unit>

    /** Marks [locationId] as the user's default and unmarks any previous default. */
    suspend fun setDefaultLocation(userId: String, locationId: String): Resource<Unit>
}
