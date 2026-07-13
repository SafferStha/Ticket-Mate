package com.example.individual_project.domain.repository

import com.example.individual_project.data.model.Booking
import com.example.individual_project.utils.Resource

interface BookingRepository {
    suspend fun bookTicket(booking: Booking): Resource<String>
    suspend fun getUserBookings(userId: String): Resource<List<Booking>>
    suspend fun getBookingById(bookingId: String): Resource<Booking>
    suspend fun cancelBooking(bookingId: String): Resource<Unit>
    suspend fun updateBookingStatus(bookingId: String, status: String): Resource<Unit>
}
