package com.example.individual_project.data.remote

import com.example.individual_project.data.model.SearchHistoryItem
import com.example.individual_project.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns all Realtime Database operations for search_history/ and trending_keywords/ nodes.
 *
 * Firebase structure:
 *   search_history/{userId}/{pushKey}/  →  { id, query, timestamp }
 *   trending_keywords/{pushKey}         →  "keyword string"
 */
@Singleton
class FirebaseSearchDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {
    private fun historyRef(userId: String) =
        database.getReference("search_history").child(userId)

    private val keywordsRef get() =
        database.getReference("trending_keywords")

    suspend fun saveSearchHistory(userId: String, query: String): Resource<Unit> = try {
        val key = historyRef(userId).push().key
            ?: return Resource.Error("Failed to generate history key")
        val item = mapOf(
            "id"        to key,
            "query"     to query.trim(),
            "timestamp" to System.currentTimeMillis()
        )
        historyRef(userId).child(key).setValue(item).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save search history", e)
    }

    suspend fun getSearchHistory(userId: String): Resource<List<SearchHistoryItem>> = try {
        val snapshot = historyRef(userId)
            .orderByChild("timestamp")
            .limitToLast(10)
            .get().await()
        val items = snapshot.children
            .mapNotNull { it.getValue(SearchHistoryItem::class.java) }
            .sortedByDescending { it.timestamp }
        Resource.Success(items)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to load search history", e)
    }

    suspend fun clearSearchHistory(userId: String): Resource<Unit> = try {
        historyRef(userId).removeValue().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to clear search history", e)
    }

    // Falls back to static defaults when the Firebase node is empty or unreachable.
    suspend fun getTrendingKeywords(): Resource<List<String>> = try {
        val snapshot = keywordsRef.get().await()
        val keywords = snapshot.children.mapNotNull { it.getValue(String::class.java) }
        Resource.Success(keywords.ifEmpty { defaultKeywords })
    } catch (e: Exception) {
        Resource.Success(defaultKeywords)
    }

    companion object {
        val defaultKeywords = listOf(
            "Taylor Swift", "Football", "EDM Festival",
            "Stand-up Comedy", "Rock Concert", "Theater", "Festivals"
        )
    }
}
