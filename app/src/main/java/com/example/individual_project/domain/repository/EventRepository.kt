package com.example.individual_project.domain.repository

import com.example.individual_project.data.model.Event
import com.example.individual_project.utils.Resource

interface EventRepository {
    suspend fun getEvents(): Resource<List<Event>>
    suspend fun getEventById(eventId: String): Resource<Event>
    suspend fun createEvent(event: Event): Resource<String>
    suspend fun updateEvent(event: Event): Resource<Unit>
    suspend fun deleteEvent(eventId: String): Resource<Unit>
}
