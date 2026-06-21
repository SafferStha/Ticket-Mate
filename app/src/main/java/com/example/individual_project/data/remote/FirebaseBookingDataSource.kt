package com.example.individual_project.data.remote

import com.example.individual_project.data.model.Booking
import com.example.individual_project.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns all Realtime Database operations under the "bookings/" node.
 * Queries bookings by userId using Firebase's orderByChild index.
 * Make sure you add .indexOn: ["userId"] to your Firebase Realtime DB rules.
 */
@Singleton
class FirebaseBookingDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val bookingsRef get() = database.getReference("bookings")

    suspend fun createBooking(booking: Booking): Resource<String> = try {
        val key = bookingsRef.push().key
            ?: return Resource.Error("Failed to generate booking id")
        val bookingWithId = booking.copy(id = key)
        bookingsRef.child(key).setValue(bookingWithId).await()
        Resource.Success(key)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to create booking", e)
    }

    suspend fun getUserBookings(userId: String): Resource<List<Booking>> = try {
        val snapshot = bookingsRef.orderByChild("userId").equalTo(userId).get().await()
        val bookings = snapshot.children.mapNotNull { it.getValue(Booking::class.java) }
        Resource.Success(bookings)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch bookings", e)
    }

    suspend fun getBookingById(bookingId: String): Resource<Booking> = try {
        val snapshot = bookingsRef.child(bookingId).get().await()
        val booking  = snapshot.getValue(Booking::class.java)
            ?: return Resource.Error("Booking not found: $bookingId")
        Resource.Success(booking)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch booking", e)
    }

    suspend fun cancelBooking(bookingId: String): Resource<Unit> = try {
        bookingsRef.child(bookingId).child("bookingStatus").setValue("CANCELLED").await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to cancel booking", e)
    }
}
