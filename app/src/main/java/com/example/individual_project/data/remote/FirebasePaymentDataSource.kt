package com.example.individual_project.data.remote

import com.example.individual_project.data.model.Payment
import com.example.individual_project.data.model.PaymentStatus
import com.example.individual_project.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns all Realtime Database operations under the "payments/" node.
 * Payment logic only — no booking or ticket mutations here.
 */
@Singleton
class FirebasePaymentDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val paymentsRef get() = database.getReference("payments")

    suspend fun createPayment(payment: Payment): Resource<String> = try {
        val key = paymentsRef.push().key
            ?: return Resource.Error("Failed to generate payment id")
        val withId = payment.copy(id = key)
        paymentsRef.child(key).setValue(withId).await()
        Resource.Success(key)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to create payment", e)
    }

    suspend fun getPaymentById(paymentId: String): Resource<Payment> = try {
        val snapshot = paymentsRef.child(paymentId).get().await()
        val payment  = snapshot.getValue(Payment::class.java)
            ?: return Resource.Error("Payment not found: $paymentId")
        Resource.Success(payment)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch payment", e)
    }

    suspend fun updatePaymentStatus(paymentId: String, status: PaymentStatus): Resource<Unit> = try {
        paymentsRef.child(paymentId).child("paymentStatus").setValue(status.name).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to update payment status", e)
    }

    suspend fun refundPayment(paymentId: String): Resource<Unit> =
        updatePaymentStatus(paymentId, PaymentStatus.REFUNDED)
}
