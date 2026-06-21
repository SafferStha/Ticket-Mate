package com.example.individual_project.data.repository

import com.example.individual_project.data.model.Booking
import com.example.individual_project.data.remote.FirebaseBookingDataSource
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val bookingDataSource: FirebaseBookingDataSource
) : BookingRepository {

    override suspend fun bookTicket(booking: Booking): Resource<String>         = bookingDataSource.bookTicket(booking)
    override suspend fun getUserBookings(userId: String): Resource<List<Booking>> = bookingDataSource.getUserBookings(userId)
    override suspend fun cancelBooking(bookingId: String): Resource<Unit>       = bookingDataSource.cancelBooking(bookingId)
}
