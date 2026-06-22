package com.example.individual_project.data.repository

import com.example.individual_project.data.model.SearchHistoryItem
import com.example.individual_project.data.remote.FirebaseSearchDataSource
import com.example.individual_project.domain.repository.SearchRepository
import com.example.individual_project.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseSearchDataSource
) : SearchRepository {

    override suspend fun saveSearchHistory(userId: String, query: String): Resource<Unit> =
        dataSource.saveSearchHistory(userId, query)

    override suspend fun getSearchHistory(userId: String): Resource<List<SearchHistoryItem>> =
        dataSource.getSearchHistory(userId)

    override suspend fun clearSearchHistory(userId: String): Resource<Unit> =
        dataSource.clearSearchHistory(userId)

    override suspend fun getTrendingKeywords(): Resource<List<String>> =
        dataSource.getTrendingKeywords()
}
