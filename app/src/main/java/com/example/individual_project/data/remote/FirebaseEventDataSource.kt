package com.example.individual_project.data.remote

import com.example.individual_project.data.model.Event
import com.example.individual_project.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

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
    private val eventsRef get() = database.getReference("events")

    suspend fun getAllEvents(): Resource<List<Event>> = try {
        val snapshot = eventsRef.get().await()
        val events   = snapshot.children.mapNotNull { it.getValue(Event::class.java) }
        Resource.Success(events)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch events", e)
    }

    suspend fun getFeaturedEvents(): Resource<List<Event>> = try {
        val snapshot = eventsRef.orderByChild("featured").equalTo(true).get().await()
        val events   = snapshot.children.mapNotNull { it.getValue(Event::class.java) }
        Resource.Success(events)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch featured events", e)
    }

    // Returns all events ordered by date (Firebase orders by key unless .indexOn is set).
    // Upcoming semantics are enforced client-side since dates are stored as formatted strings.
    suspend fun getUpcomingEvents(): Resource<List<Event>> = try {
        val snapshot = eventsRef.get().await()
        val events   = snapshot.children.mapNotNull { it.getValue(Event::class.java) }
        Resource.Success(events)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch upcoming events", e)
    }

    suspend fun getEventById(eventId: String): Resource<Event> = try {
        val snapshot = eventsRef.child(eventId).get().await()
        val event    = snapshot.getValue(Event::class.java)
            ?: return Resource.Error("Event not found: $eventId")
        Resource.Success(event)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch event", e)
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
        Resource.Error(e.message ?: "Search failed", e)
    }

    // Uses Firebase server-side filter. Requires .indexOn: ["category"] in rules.
    suspend fun getEventsByCategory(category: String): Resource<List<Event>> = try {
        val snapshot = eventsRef.orderByChild("category").equalTo(category).get().await()
        val events   = snapshot.children.mapNotNull { it.getValue(Event::class.java) }
        Resource.Success(events)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch events by category", e)
    }

    suspend fun createEvent(event: Event): Resource<String> = try {
        val key = eventsRef.push().key
            ?: return Resource.Error("Failed to generate event id")
        val eventWithId = event.copy(id = key)
        eventsRef.child(key).setValue(eventWithId).await()
        Resource.Success(key)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to create event", e)
    }

    suspend fun updateEvent(event: Event): Resource<Unit> = try {
        eventsRef.child(event.id).setValue(event).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to update event", e)
    }

    suspend fun deleteEvent(eventId: String): Resource<Unit> = try {
        eventsRef.child(eventId).removeValue().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to delete event", e)
    }
}
