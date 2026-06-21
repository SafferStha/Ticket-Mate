package com.example.individual_project.data.repository

import com.example.individual_project.data.model.Event
import com.example.individual_project.data.remote.FirebaseEventDataSource
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDataSource: FirebaseEventDataSource
) : EventRepository {

    override suspend fun fetchEvents(): Resource<List<Event>>                  = eventDataSource.getAllEvents()
    override suspend fun fetchFeaturedEvents(): Resource<List<Event>>          = eventDataSource.getFeaturedEvents()
    override suspend fun fetchUpcomingEvents(): Resource<List<Event>>          = eventDataSource.getUpcomingEvents()
    override suspend fun fetchEventDetails(id: String): Resource<Event>        = eventDataSource.getEventById(id)
    override suspend fun searchEvents(query: String): Resource<List<Event>>    = eventDataSource.searchEvents(query)
    override suspend fun filterByCategory(category: String): Resource<List<Event>> = eventDataSource.getEventsByCategory(category)
    override suspend fun createEvent(event: Event): Resource<String>           = eventDataSource.createEvent(event)
    override suspend fun updateEvent(event: Event): Resource<Unit>             = eventDataSource.updateEvent(event)
    override suspend fun deleteEvent(eventId: String): Resource<Unit>          = eventDataSource.deleteEvent(eventId)
}
