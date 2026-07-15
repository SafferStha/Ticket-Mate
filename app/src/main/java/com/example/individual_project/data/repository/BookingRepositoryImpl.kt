package com.example.individual_project.data.repository

import com.example.individual_project.data.model.Booking
import com.example.individual_project.data.model.BookingStatus
import com.example.individual_project.data.remote.FirebaseBookingDataSource
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.domain.repository.TicketRepository
import com.example.individual_project.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val bookingDataSource : FirebaseBookingDataSource,
    private val eventRepository   : EventRepository,
    private val ticketRepository  : TicketRepository
) : BookingRepository {

    override suspend fun bookTicket(booking: Booking): Resource<String> {
        // ── Validate quantity ──────────────────────────────────────────────────
        if (booking.quantity <= 0) {
            return Resource.Error("Quantity must be at least 1")
        }

        // ── Atomic seat deduction (validates + deducts in one Firebase TX) ─────
        val seatResult = eventRepository.deductSeats(booking.eventId, booking.quantity)
        if (seatResult is Resource.Error) return Resource.Error(seatResult.message)

        // ── Persist the booking as PENDING until payment is confirmed ─────────
        val bookingResult = bookingDataSource.createBooking(
            booking.copy(
                bookingStatus = BookingStatus.PENDING.name,
                bookingDate   = System.currentTimeMillis()
            )
        )

        // ── Compensate: restore seats if the write failed ──────────────────────
        if (bookingResult is Resource.Error) {
            eventRepository.restoreSeats(booking.eventId, booking.quantity)
            return Resource.Error(bookingResult.message)
        }

        return bookingResult
    }

    override suspend fun getUserBookings(userId: String): Resource<List<Booking>> =
        bookingDataSource.getUserBookings(userId)

    override suspend fun getBookingById(bookingId: String): Resource<Booking> =
        bookingDataSource.getBookingById(bookingId)

    override suspend fun updateBookingStatus(bookingId: String, status: String): Resource<Unit> =
        bookingDataSource.updateBookingStatus(bookingId, status)

    override suspend fun cancelBooking(bookingId: String): Resource<Unit> {
        // ── Fetch booking to get eventId + quantity for seat restoration ───────
        val bookingResult = bookingDataSource.getBookingById(bookingId)
        if (bookingResult is Resource.Error) return Resource.Error(bookingResult.message)
        val booking = (bookingResult as Resource.Success).data

        if (booking.bookingStatus == BookingStatus.CANCELLED.name) {
            return Resource.Error("Booking is already cancelled")
        }

        // ── Update status first ────────────────────────────────────────────────
        val cancelResult = bookingDataSource.cancelBooking(bookingId)
        if (cancelResult is Resource.Error) return cancelResult

        // ── Cancel corresponding ticket if exists ──────────────────────────────
        val ticketResult = ticketRepository.getTicketByBookingId(bookingId)
        if (ticketResult is Resource.Success && ticketResult.data != null) {
            ticketRepository.updateTicketStatus(ticketResult.data.id, "CANCELLED")
        }

        // ── Restore seats (best-effort; non-fatal if it fails) ────────────────
        eventRepository.restoreSeats(booking.eventId, booking.quantity)

        return Resource.Success(Unit)
    }
}
