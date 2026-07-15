package com.example.individual_project.data.remote

import com.example.individual_project.data.model.Ticket
import com.example.individual_project.utils.FirebaseErrorMapper
import com.example.individual_project.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns all Realtime Database operations under the "tickets/" node.
 * Tickets are write-once on payment success; never mutated by this layer.
 */
@Singleton
class FirebaseTicketDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val ticketsRef get() = database.getReference("tickets")

    suspend fun createTicket(ticket: Ticket): Resource<String> = try {
        val key    = ticketsRef.push().key
            ?: return Resource.Error("Failed to generate ticket id")
        val withId = ticket.copy(id = key)
        ticketsRef.child(key).setValue(withId).await()
        Resource.Success(key)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to create ticket"), e)
    }

    suspend fun getTicketsByUserId(userId: String): Resource<List<Ticket>> = try {
        val snapshot = ticketsRef.orderByChild("userId").equalTo(userId).get().await()
        val tickets  = snapshot.children.mapNotNull { it.getValue(Ticket::class.java) }
        Resource.Success(tickets)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to fetch tickets"), e)
    }

    suspend fun getTicketByBookingId(bookingId: String): Resource<Ticket?> = try {
        val snapshot = ticketsRef.orderByChild("bookingId").equalTo(bookingId).get().await()
        val ticket   = snapshot.children.firstOrNull()?.getValue(Ticket::class.java)
        Resource.Success(ticket)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to fetch ticket"), e)
    }

    suspend fun getTicketById(ticketId: String): Resource<Ticket> = try {
        val snapshot = ticketsRef.child(ticketId).get().await()
        val ticket   = snapshot.getValue(Ticket::class.java)
            ?: return Resource.Error("Ticket not found: $ticketId")
        Resource.Success(ticket)
    } catch (e: Exception) {
        Resource.Error(FirebaseErrorMapper.map(e, "Failed to fetch ticket"), e)
    }
}
