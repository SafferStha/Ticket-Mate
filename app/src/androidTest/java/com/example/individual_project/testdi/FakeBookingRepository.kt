package com.example.individual_project.testdi

import com.example.individual_project.data.model.Booking
import com.example.individual_project.data.model.BookingStatus
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.utils.Resource

/** In-memory stand-in for [BookingRepository]. */
class FakeBookingRepository : BookingRepository {

    val bookings = mutableListOf<Booking>()
    var nextBookTicketResult: Resource<String>? = null
    private var idCounter = 0

    override suspend fun bookTicket(booking: Booking): Resource<String> {
        nextBookTicketResult?.let { return it }
        val id = "booking${++idCounter}"
        bookings.add(booking.copy(id = id))
        return Resource.Success(id)
    }

    override suspend fun getUserBookings(userId: String): Resource<List<Booking>> =
        Resource.Success(bookings.filter { it.userId == userId })

    override suspend fun getBookingById(bookingId: String): Resource<Booking> =
        bookings.find { it.id == bookingId }?.let { Resource.Success(it) } ?: Resource.Error("Booking not found")

    override suspend fun cancelBooking(bookingId: String): Resource<Unit> {
        val index = bookings.indexOfFirst { it.id == bookingId }
        if (index == -1) return Resource.Error("Booking not found")
        bookings[index] = bookings[index].copy(bookingStatus = BookingStatus.CANCELLED.name)
        return Resource.Success(Unit)
    }

    override suspend fun updateBookingStatus(bookingId: String, status: String): Resource<Unit> {
        val index = bookings.indexOfFirst { it.id == bookingId }
        if (index == -1) return Resource.Error("Booking not found")
        bookings[index] = bookings[index].copy(bookingStatus = status)
        return Resource.Success(Unit)
    }

    fun reset() {
        bookings.clear()
        nextBookTicketResult = null
        idCounter = 0
    }
}
