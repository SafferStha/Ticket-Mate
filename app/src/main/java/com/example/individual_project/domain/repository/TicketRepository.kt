package com.example.individual_project.domain.repository

import com.example.individual_project.data.model.Ticket
import com.example.individual_project.utils.Resource

interface TicketRepository {
    suspend fun getUserTickets(userId: String): Resource<List<Ticket>>
    suspend fun getTicketById(ticketId: String): Resource<Ticket>
}
