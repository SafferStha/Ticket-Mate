package com.example.individual_project.testdi

import com.example.individual_project.data.model.Event
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.utils.Resource

/** In-memory stand-in for [EventRepository], seeded with a couple of sample events by default. */
class FakeEventRepository : EventRepository {

    val events = mutableListOf(
        Event(
            id = "event1", title = "Kathmandu Music Festival", category = "Concerts",
            venue = "Tundikhel", city = "Kathmandu", date = "2026-09-15", time = "18:00",
            price = 500.0, availableSeats = 100, featured = true
        ),
        Event(
            id = "event2", title = "Sold Out Comedy Night", category = "Comedy",
            venue = "City Hall", city = "Kathmandu", date = "2026-09-20", time = "20:00",
            price = 300.0, availableSeats = 0
        )
    )

    private val favorites = mutableSetOf<String>()

    override suspend fun fetchEvents(): Resource<List<Event>> = Resource.Success(events.toList())
    override suspend fun fetchFeaturedEvents(): Resource<List<Event>> = Resource.Success(events.filter { it.featured })
    override suspend fun fetchUpcomingEvents(): Resource<List<Event>> = Resource.Success(events.toList())

    override suspend fun fetchEventDetails(id: String): Resource<Event> =
        events.find { it.id == id }?.let { Resource.Success(it) } ?: Resource.Error("Event not found")

    override suspend fun searchEvents(query: String): Resource<List<Event>> =
        Resource.Success(events.filter { it.title.contains(query, ignoreCase = true) })

    override suspend fun filterByCategory(category: String): Resource<List<Event>> =
        Resource.Success(events.filter { it.category.equals(category, ignoreCase = true) })

    override suspend fun getTrendingEvents(): Resource<List<Event>> = Resource.Success(events.toList())
    override suspend fun getRecommendedEvents(userId: String): Resource<List<Event>> = Resource.Success(emptyList())
    override suspend fun getEventsByCity(city: String): Resource<List<Event>> =
        Resource.Success(events.filter { it.city.equals(city, ignoreCase = true) })

    override suspend fun createEvent(event: Event): Resource<String> {
        val id = event.id.ifBlank { "event${events.size + 1}" }
        events.add(event.copy(id = id))
        return Resource.Success(id)
    }

    override suspend fun updateEvent(event: Event): Resource<Unit> {
        val index = events.indexOfFirst { it.id == event.id }
        if (index == -1) return Resource.Error("Event not found")
        events[index] = event
        return Resource.Success(Unit)
    }

    override suspend fun deleteEvent(eventId: String): Resource<Unit> {
        events.removeAll { it.id == eventId }
        return Resource.Success(Unit)
    }

    override suspend fun toggleFavorite(eventId: String, userId: String): Resource<Unit> {
        if (!favorites.add(eventId)) favorites.remove(eventId)
        return Resource.Success(Unit)
    }

    override suspend fun isFavorite(eventId: String, userId: String): Resource<Boolean> =
        Resource.Success(eventId in favorites)

    override suspend fun deductSeats(eventId: String, quantity: Int): Resource<Unit> = Resource.Success(Unit)
    override suspend fun restoreSeats(eventId: String, quantity: Int): Resource<Unit> = Resource.Success(Unit)

    fun reset() {
        events.clear()
        events.addAll(
            listOf(
                Event(
                    id = "event1", title = "Kathmandu Music Festival", category = "Concerts",
                    venue = "Tundikhel", city = "Kathmandu", date = "2026-09-15", time = "18:00",
                    price = 500.0, availableSeats = 100, featured = true
                ),
                Event(
                    id = "event2", title = "Sold Out Comedy Night", category = "Comedy",
                    venue = "City Hall", city = "Kathmandu", date = "2026-09-20", time = "20:00",
                    price = 300.0, availableSeats = 0
                )
            )
        )
        favorites.clear()
    }
}
