package com.example.individual_project.data.remote

import com.example.individual_project.data.model.Event
import com.example.individual_project.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns all Realtime Database operations under the "events/" node.
 * No auth logic here.
 */
@Singleton
class FirebaseEventDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val eventsRef get() = database.getReference("events")

    suspend fun getEvents(): Resource<List<Event>> = try {
        val snapshot = eventsRef.get().await()
        val events   = snapshot.children.mapNotNull { it.getValue(Event::class.java) }
        Resource.Success(events)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch events", e)
    }

    suspend fun getEventById(eventId: String): Resource<Event> = try {
        val snapshot = eventsRef.child(eventId).get().await()
        val event    = snapshot.getValue(Event::class.java)
            ?: return Resource.Error("Event not found: $eventId")
        Resource.Success(event)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch event", e)
    }

    suspend fun createEvent(event: Event): Resource<String> = try {
        val key = eventsRef.push().key
            ?: return Resource.Error("Failed to generate event id")
        val eventWithId = event.copy(eventId = key)
        eventsRef.child(key).setValue(eventWithId).await()
        Resource.Success(key)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to create event", e)
    }

    suspend fun updateEvent(event: Event): Resource<Unit> = try {
        eventsRef.child(event.eventId).setValue(event).await()
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
