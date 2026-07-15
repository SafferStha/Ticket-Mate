package com.example.individual_project.data.repository

import com.example.individual_project.data.model.Ticket
import com.example.individual_project.data.remote.FirebaseTicketDataSource
import com.example.individual_project.domain.repository.TicketRepository
import com.example.individual_project.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketRepositoryImpl @Inject constructor(
    private val ticketDataSource: FirebaseTicketDataSource
) : TicketRepository {

    override suspend fun getUserTickets(userId: String): Resource<List<Ticket>> =
        ticketDataSource.getTicketsByUserId(userId)

    override suspend fun getTicketById(ticketId: String): Resource<Ticket> =
        ticketDataSource.getTicketById(ticketId)

    override suspend fun getTicketByBookingId(bookingId: String): Resource<Ticket?> =
        ticketDataSource.getTicketByBookingId(bookingId)

    override suspend fun updateTicketStatus(ticketId: String, status: String): Resource<Unit> =
        ticketDataSource.updateTicketStatus(ticketId, status)
}
