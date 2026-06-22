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

    // ── Discovery ──────────────────────────────────────────────────────────────
    suspend fun getTrendingEvents(): Resource<List<Event>>
    suspend fun getRecommendedEvents(userId: String): Resource<List<Event>>
    suspend fun getEventsByCity(city: String): Resource<List<Event>>

    // ── CRUD (admin / organiser flows) ─────────────────────────────────────────
    suspend fun createEvent(event: Event): Resource<String>
    suspend fun updateEvent(event: Event): Resource<Unit>
    suspend fun deleteEvent(eventId: String): Resource<Unit>

    // ── Favorites (per-user, stored at favorites/{uid}/{eventId}) ──────────────
    suspend fun toggleFavorite(eventId: String, userId: String): Resource<Unit>
    suspend fun isFavorite(eventId: String, userId: String): Resource<Boolean>

    // ── Seat management (atomic transactions) ──────────────────────────────────
    suspend fun deductSeats(eventId: String, quantity: Int): Resource<Unit>
    suspend fun restoreSeats(eventId: String, quantity: Int): Resource<Unit>
}
