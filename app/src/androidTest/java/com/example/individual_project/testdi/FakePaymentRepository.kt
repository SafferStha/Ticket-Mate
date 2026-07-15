package com.example.individual_project.testdi

import com.example.individual_project.data.model.Payment
import com.example.individual_project.data.model.PaymentStatus
import com.example.individual_project.domain.repository.PaymentRepository
import com.example.individual_project.utils.Resource

/** In-memory stand-in for [PaymentRepository]. Always simulates a charge, same as production. */
class FakePaymentRepository : PaymentRepository {

    val payments = mutableListOf<Payment>()
    var nextProcessPaymentResult: Resource<String>? = null
    private var idCounter = 0

    override suspend fun processPayment(
        bookingId: String, userId: String, subtotal: Double, tax: Double,
        serviceFee: Double, discount: Double, totalAmount: Double, paymentMethod: String
    ): Resource<String> {
        nextProcessPaymentResult?.let { return it }
        val id = "payment${++idCounter}"
        payments.add(
            Payment(
                id = id, bookingId = bookingId, userId = userId, subtotal = subtotal, tax = tax,
                serviceFee = serviceFee, discount = discount, totalAmount = totalAmount,
                paymentMethod = paymentMethod, paymentStatus = PaymentStatus.SUCCESS.name
            )
        )
        return Resource.Success(id)
    }

    override suspend fun fetchPayment(paymentId: String): Resource<Payment> =
        payments.find { it.id == paymentId }?.let { Resource.Success(it) } ?: Resource.Error("Payment not found")

    override suspend fun refundPayment(paymentId: String): Resource<Unit> {
        val index = payments.indexOfFirst { it.id == paymentId }
        if (index == -1) return Resource.Error("Payment not found")
        payments[index] = payments[index].copy(paymentStatus = PaymentStatus.REFUNDED.name)
        return Resource.Success(Unit)
    }

    override suspend fun fetchPaymentsByUser(userId: String): Resource<List<Payment>> =
        Resource.Success(payments.filter { it.userId == userId })

    fun reset() {
        payments.clear()
        nextProcessPaymentResult = null
        idCounter = 0
    }
}
