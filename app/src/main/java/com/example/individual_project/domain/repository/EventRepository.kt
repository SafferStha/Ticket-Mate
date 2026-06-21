package com.example.individual_project.domain.repository

import com.example.individual_project.data.model.Event
import com.example.individual_project.utils.Resource

interface EventRepository {
    // ── Queries ────────────────────────────────────────────────────────────────
    suspend fun fetchEvents(): Resource<List<Event>>
    suspend fun fetchFeaturedEvents(): Resource<List<Event>>
    suspend fun fetchUpcomingEvents(): Resource<List<Event>>
    suspend fun fetchEventDetails(id: String): Resource<Event>
    suspend fun searchEvents(query: String): Resource<List<Event>>
    suspend fun filterByCategory(category: String): Resource<List<Event>>

    // ── CRUD (admin / organiser flows) ─────────────────────────────────────────
    suspend fun createEvent(event: Event): Resource<String>
    suspend fun updateEvent(event: Event): Resource<Unit>
    suspend fun deleteEvent(eventId: String): Resource<Unit>
}
