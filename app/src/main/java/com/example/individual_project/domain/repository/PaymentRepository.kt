package com.example.individual_project.domain.repository

import com.example.individual_project.data.model.Payment
import com.example.individual_project.utils.Resource

interface PaymentRepository {

    /**
     * Processes a simulated payment for the given booking.
     * On success: payment is persisted as SUCCESS, booking is set to CONFIRMED,
     * and a ticket entry is generated.
     *
     * @return [Resource.Success] carrying the generated paymentId, or [Resource.Error].
     */
    suspend fun processPayment(
        bookingId    : String,
        userId       : String,
        subtotal     : Double,
        tax          : Double,
        serviceFee   : Double,
        discount     : Double,
        totalAmount  : Double,
        paymentMethod: String
    ): Resource<String>

    suspend fun fetchPayment(paymentId: String): Resource<Payment>

    /** Marks payment REFUNDED and booking CANCELLED. */
    suspend fun refundPayment(paymentId: String): Resource<Unit>
}
