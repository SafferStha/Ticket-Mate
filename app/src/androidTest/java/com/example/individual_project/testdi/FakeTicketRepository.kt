package com.example.individual_project.testdi

import com.example.individual_project.data.model.Ticket
import com.example.individual_project.domain.repository.TicketRepository
import com.example.individual_project.utils.Resource

/** In-memory stand-in for [TicketRepository]. */
class FakeTicketRepository : TicketRepository {

    val tickets = mutableListOf<Ticket>()

    override suspend fun getUserTickets(userId: String): Resource<List<Ticket>> =
        Resource.Success(tickets.filter { it.userId == userId })

    override suspend fun getTicketById(ticketId: String): Resource<Ticket> =
        tickets.find { it.id == ticketId }?.let { Resource.Success(it) } ?: Resource.Error("Ticket not found")

    override suspend fun getTicketByBookingId(bookingId: String): Resource<Ticket?> =
        Resource.Success(tickets.find { it.bookingId == bookingId })

    override suspend fun updateTicketStatus(ticketId: String, status: String): Resource<Unit> {
        val index = tickets.indexOfFirst { it.id == ticketId }
        if (index == -1) return Resource.Error("Ticket not found")
        tickets[index] = tickets[index].copy(ticketStatus = status)
        return Resource.Success(Unit)
    }

    fun reset() = tickets.clear()
}
