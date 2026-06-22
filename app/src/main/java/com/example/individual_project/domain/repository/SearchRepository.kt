package com.example.individual_project.domain.repository

import com.example.individual_project.data.model.SearchHistoryItem
import com.example.individual_project.utils.Resource

interface SearchRepository {
    suspend fun saveSearchHistory(userId: String, query: String): Resource<Unit>
    suspend fun getSearchHistory(userId: String): Resource<List<SearchHistoryItem>>
    suspend fun clearSearchHistory(userId: String): Resource<Unit>
    suspend fun getTrendingKeywords(): Resource<List<String>>
}
