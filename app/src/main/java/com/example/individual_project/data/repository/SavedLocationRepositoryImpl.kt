package com.example.individual_project.data.repository

import com.example.individual_project.data.model.SavedLocation
import com.example.individual_project.data.remote.FirebaseSavedLocationDataSource
import com.example.individual_project.domain.repository.SavedLocationRepository
import com.example.individual_project.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedLocationRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseSavedLocationDataSource
) : SavedLocationRepository {

    override suspend fun getSavedLocations(userId: String): Resource<List<SavedLocation>> =
        dataSource.getSavedLocations(userId)

    override suspend fun addSavedLocation(location: SavedLocation): Resource<String> =
        dataSource.addSavedLocation(location)

    override suspend fun updateSavedLocation(location: SavedLocation): Resource<Unit> =
        dataSource.updateSavedLocation(location)

    override suspend fun deleteSavedLocation(userId: String, locationId: String): Resource<Unit> =
        dataSource.deleteSavedLocation(userId, locationId)

    override suspend fun setDefaultLocation(userId: String, locationId: String): Resource<Unit> =
        dataSource.setDefaultLocation(userId, locationId)
}
