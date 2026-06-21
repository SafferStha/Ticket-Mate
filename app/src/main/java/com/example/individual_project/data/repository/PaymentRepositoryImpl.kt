package com.example.individual_project.data.repository

import com.example.individual_project.data.model.BookingStatus
import com.example.individual_project.data.model.Payment
import com.example.individual_project.data.model.PaymentStatus
import com.example.individual_project.data.model.Ticket
import com.example.individual_project.data.remote.FirebaseBookingDataSource
import com.example.individual_project.data.remote.FirebasePaymentDataSource
import com.example.individual_project.data.remote.FirebaseTicketDataSource
import com.example.individual_project.domain.repository.PaymentRepository
import com.example.individual_project.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentDataSource : FirebasePaymentDataSource,
    private val bookingDataSource : FirebaseBookingDataSource,
    private val ticketDataSource  : FirebaseTicketDataSource
) : PaymentRepository {

    override suspend fun processPayment(
        bookingId    : String,
        userId       : String,
        subtotal     : Double,
        tax          : Double,
        serviceFee   : Double,
        discount     : Double,
        totalAmount  : Double,
        paymentMethod: String
    ): Resource<String> {
        if (totalAmount <= 0.0) return Resource.Error("Invalid payment amount")

        // ── 1. Create PENDING payment record ─────────────────────────────────
        val pendingPayment = Payment(
            bookingId       = bookingId,
            userId          = userId,
            subtotal        = subtotal,
            tax             = tax,
            serviceFee      = serviceFee,
            discount        = discount,
            totalAmount     = totalAmount,
            paymentMethod   = paymentMethod,
            paymentStatus   = PaymentStatus.PENDING.name,
            transactionDate = System.currentTimeMillis()
        )
        val createResult = paymentDataSource.createPayment(pendingPayment)
        if (createResult is Resource.Error) return createResult
        val paymentId = (createResult as Resource.Success).data

        // ── 2. Simulate payment processing (v1: always succeeds) ─────────────
        val statusResult = paymentDataSource.updatePaymentStatus(paymentId, PaymentStatus.SUCCESS)
        if (statusResult is Resource.Error) {
            paymentDataSource.updatePaymentStatus(paymentId, PaymentStatus.FAILED)
            return Resource.Error("Payment processing failed. Please retry.")
        }

        // ── 3. Confirm the booking ────────────────────────────────────────────
        bookingDataSource.updateBookingStatus(bookingId, BookingStatus.CONFIRMED.name)

        // ── 4. Generate ticket (critical: only on success) ────────────────────
        val bookingResult = bookingDataSource.getBookingById(bookingId)
        if (bookingResult is Resource.Success) {
            val b = bookingResult.data
            ticketDataSource.createTicket(
                Ticket(
                    bookingId    = bookingId,
                    paymentId    = paymentId,
                    userId       = userId,
                    eventId      = b.eventId,
                    eventTitle   = b.eventTitle,
                    venue        = b.venue,
                    date         = b.date,
                    quantity     = b.quantity,
                    totalPrice   = totalAmount,
                    ticketStatus = "ACTIVE",
                    generatedAt  = System.currentTimeMillis()
                )
            )
        }

        return Resource.Success(paymentId)
    }

    override suspend fun fetchPayment(paymentId: String): Resource<Payment> =
        paymentDataSource.getPaymentById(paymentId)

    override suspend fun refundPayment(paymentId: String): Resource<Unit> {
        val refundResult = paymentDataSource.refundPayment(paymentId)
        if (refundResult is Resource.Error) return refundResult

        val paymentResult = paymentDataSource.getPaymentById(paymentId)
        if (paymentResult is Resource.Success) {
            bookingDataSource.updateBookingStatus(
                paymentResult.data.bookingId,
                BookingStatus.CANCELLED.name
            )
        }

        return Resource.Success(Unit)
    }
}
