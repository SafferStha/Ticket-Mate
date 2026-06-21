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

    override suspend fun getEvents(): Resource<List<Event>>             = eventDataSource.getEvents()
    override suspend fun getEventById(eventId: String): Resource<Event> = eventDataSource.getEventById(eventId)
    override suspend fun createEvent(event: Event): Resource<String>    = eventDataSource.createEvent(event)
    override suspend fun updateEvent(event: Event): Resource<Unit>      = eventDataSource.updateEvent(event)
    override suspend fun deleteEvent(eventId: String): Resource<Unit>   = eventDataSource.deleteEvent(eventId)
}
