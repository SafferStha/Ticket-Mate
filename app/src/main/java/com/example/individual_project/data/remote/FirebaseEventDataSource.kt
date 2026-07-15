package com.example.individual_project.data.remote

import com.example.individual_project.data.model.Event
import com.example.individual_project.utils.FirebaseErrorMapper
import com.example.individual_project.utils.Resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Owns all Realtime Database operations under the "events/" node.
 * All filtering is done server-side where Firebase supports it; otherwise client-side.
 *
 * Firebase rules recommendation for server-side category/featured queries:
 * {
 *   "rules": {
 *     "events": {
 *       ".indexOn": ["category", "featured"]
 *     }
 *   }
 * }
 */
@Singleton
class FirebaseEventDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val eventsRef    get() = database.getReference("events")
    private val favoritesRef get() = database.getReference("favorites")

    private fun favRef(userId: String, eventId: String) =
        favoritesRef.child(userId).child(eventId)

    suspend fun getAllEvents(): Resource<List<Event>> = try {
        val snapshot = eventsRef.get().await()
        val events   = snapshot.children.mapNotNull { it.getValue(Event::class.java) }
        Resource.Success(events)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to fetch events"), e)
    }

    suspend fun getFeaturedEvents(): Resource<List<Event>> = try {
        val snapshot = eventsRef.orderByChild("featured").equalTo(true).get().await()
        val events   = snapshot.children.mapNotNull { it.getValue(Event::class.java) }
        Resource.Success(events)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to fetch featured events"), e)
    }

    // Returns all events ordered by date (Firebase orders by key unless .indexOn is set).
    // Upcoming semantics are enforced client-side since dates are stored as formatted strings.
    suspend fun getUpcomingEvents(): Resource<List<Event>> = try {
        val snapshot = eventsRef.get().await()
        val events   = snapshot.children.mapNotNull { it.getValue(Event::class.java) }
        Resource.Success(events)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to fetch upcoming events"), e)
    }

    suspend fun getEventById(eventId: String): Resource<Event> = try {
        val snapshot = eventsRef.child(eventId).get().await()
        val event    = snapshot.getValue(Event::class.java)
            ?: return Resource.Error("Event not found: $eventId")
        Resource.Success(event)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to fetch event"), e)
    }

    // Client-side full-text search across title, category, venue, city.
    // For production scale, replace with Algolia or a Cloud Function.
    suspend fun searchEvents(query: String): Resource<List<Event>> = try {
        val snapshot = eventsRef.get().await()
        val events   = snapshot.children
            .mapNotNull { it.getValue(Event::class.java) }
            .filter { e ->
                e.title.contains(query, ignoreCase = true)    ||
                e.category.contains(query, ignoreCase = true) ||
                e.venue.contains(query, ignoreCase = true)    ||
                e.city.contains(query, ignoreCase = true)     ||
                e.organizer.contains(query, ignoreCase = true)
            }
        Resource.Success(events)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Search failed"), e)
    }

    // Uses Firebase server-side filter. Requires .indexOn: ["category"] in rules.
    suspend fun getEventsByCategory(category: String): Resource<List<Event>> = try {
        val snapshot = eventsRef.orderByChild("category").equalTo(category).get().await()
        val events   = snapshot.children.mapNotNull { it.getValue(Event::class.java) }
        Resource.Success(events)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to fetch events by category"), e)
    }

    suspend fun createEvent(event: Event): Resource<String> = try {
        val key = eventsRef.push().key
            ?: return Resource.Error("Failed to generate event id")
        val eventWithId = event.copy(id = key)
        eventsRef.child(key).setValue(eventWithId).await()
        Resource.Success(key)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to create event"), e)
    }

    suspend fun updateEvent(event: Event): Resource<Unit> = try {
        eventsRef.child(event.id).setValue(event).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to update event"), e)
    }

    suspend fun deleteEvent(eventId: String): Resource<Unit> = try {
        eventsRef.child(eventId).removeValue().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to delete event"), e)
    }

    // ── Favorites ──────────────────────────────────────────────────────────────
    // Stored as favorites/{uid}/{eventId} = true (absent means not favorited).

    suspend fun isFavorite(eventId: String, userId: String): Resource<Boolean> = try {
        val snapshot = favRef(userId, eventId).get().await()
        Resource.Success(snapshot.exists() && snapshot.getValue(Boolean::class.java) == true)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to check favorite status"), e)
    }

    // Read-modify-write: removes the node if it exists, sets true if it doesn't.
    suspend fun toggleFavorite(eventId: String, userId: String): Resource<Unit> = try {
        val ref      = favRef(userId, eventId)
        val snapshot = ref.get().await()
        if (snapshot.exists()) ref.removeValue().await() else ref.setValue(true).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to toggle favorite"), e)
    }

    // ── Seat management (atomic via Firebase transaction) ──────────────────────
    // Transaction aborts — and returns Error — if available seats < requested quantity.
    suspend fun deductSeats(eventId: String, quantity: Int): Resource<Unit> =
        suspendCancellableCoroutine { cont ->
            eventsRef.child(eventId).child("availableSeats")
                .runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val current = currentData.getValue(Int::class.java) ?: 0
                        if (current < quantity) return Transaction.abort()
                        currentData.value = current - quantity
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error     : DatabaseError?,
                        committed : Boolean,
                        snapshot  : DataSnapshot?
                    ) {
                        when {
                            error != null -> cont.resume(Resource.Error(error.message))
                            !committed    -> cont.resume(Resource.Error("Not enough seats available"))
                            else          -> cont.resume(Resource.Success(Unit))
                        }
                    }
                })
        }

    // ── Discovery ──────────────────────────────────────────────────────────────

    // Trending: most recently added events (Firebase push keys are time-ordered).
    // Reversed so newest items appear first. Replace with bookingCount ordering in v2.
    suspend fun getTrendingEvents(): Resource<List<Event>> = try {
        val snapshot = eventsRef.limitToLast(10).get().await()
        val events   = snapshot.children
            .mapNotNull { it.getValue(Event::class.java) }
            .reversed()
        Resource.Success(events)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to fetch trending events"), e)
    }

    // Recommended: events in categories the user has favorited.
    // Falls back to featured events when the user has no favorites yet.
    suspend fun getRecommendedEvents(userId: String): Resource<List<Event>> {
        return try {
            val favSnapshot = favoritesRef.child(userId).get().await()
            val favEventIds = favSnapshot.children
                .filter { it.getValue(Boolean::class.java) == true }
                .mapNotNull { it.key }
                .toSet()

            if (favEventIds.isEmpty()) return getFeaturedEvents()

            val favCategories = favEventIds.mapNotNull { eventId ->
                eventsRef.child(eventId).child("category").get().await()
                    .getValue(String::class.java)
            }.toSet()

            if (favCategories.isEmpty()) return getFeaturedEvents()

            val allSnapshot = eventsRef.get().await()
            val recommended = allSnapshot.children
                .mapNotNull { it.getValue(Event::class.java) }
                .filter { it.category in favCategories && it.id !in favEventIds }
                .take(10)

            Resource.Success(
                recommended.ifEmpty {
                    allSnapshot.children.mapNotNull { it.getValue(Event::class.java) }.take(5)
                }
            )
        } catch (e: Exception) {
            Resource.Error(FirebaseErrorMapper.map(e, "Failed to fetch recommendations"), e)
        }
    }

    // Server-side city filter. Add .indexOn: ["city"] to Firebase rules for performance.
    suspend fun getEventsByCity(city: String): Resource<List<Event>> = try {
        val snapshot = eventsRef.orderByChild("city").equalTo(city).get().await()
        val events   = snapshot.children.mapNotNull { it.getValue(Event::class.java) }
        Resource.Success(events)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to fetch events by city"), e)
    }

    // Restores seats after cancellation. Also atomic to prevent count drift.
    suspend fun restoreSeats(eventId: String, quantity: Int): Resource<Unit> =
        suspendCancellableCoroutine { cont ->
            eventsRef.child(eventId).child("availableSeats")
                .runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val current = currentData.getValue(Int::class.java) ?: 0
                        currentData.value = current + quantity
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error     : DatabaseError?,
                        committed : Boolean,
                        snapshot  : DataSnapshot?
                    ) {
                        if (error != null) cont.resume(Resource.Error(error.message))
                        else cont.resume(Resource.Success(Unit))
                    }
                })
        }
}
