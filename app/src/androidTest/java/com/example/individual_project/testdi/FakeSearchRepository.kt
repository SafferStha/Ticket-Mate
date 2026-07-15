package com.example.individual_project.testdi

import com.example.individual_project.data.model.SearchHistoryItem
import com.example.individual_project.domain.repository.SearchRepository
import com.example.individual_project.utils.Resource

/** In-memory stand-in for [SearchRepository]. */
class FakeSearchRepository : SearchRepository {

    val history = mutableListOf<SearchHistoryItem>()
    var trendingKeywords = listOf("Concerts", "Comedy", "Sports")
    private var idCounter = 0

    override suspend fun saveSearchHistory(userId: String, query: String): Resource<Unit> {
        history.add(0, SearchHistoryItem(id = "history${++idCounter}", query = query))
        return Resource.Success(Unit)
    }

    override suspend fun getSearchHistory(userId: String): Resource<List<SearchHistoryItem>> =
        Resource.Success(history.toList())

    override suspend fun clearSearchHistory(userId: String): Resource<Unit> {
        history.clear()
        return Resource.Success(Unit)
    }

    override suspend fun getTrendingKeywords(): Resource<List<String>> = Resource.Success(trendingKeywords)

    fun reset() {
        history.clear()
        idCounter = 0
        trendingKeywords = listOf("Concerts", "Comedy", "Sports")
    }
}
