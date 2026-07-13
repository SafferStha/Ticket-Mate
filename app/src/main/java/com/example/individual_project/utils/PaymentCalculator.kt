package com.example.individual_project.utils

data class PaymentBreakdown(
    val subtotal   : Double,
    val tax        : Double,
    val serviceFee : Double,
    val discount   : Double,
    val total      : Double
)

object PaymentCalculator {

    private const val TAX_RATE    = 0.13   // 13% VAT
    private const val SERVICE_FEE = 50.0   // fixed per-booking fee (NPR)

    fun calculate(
        ticketPrice : Double,
        quantity    : Int,
        discount    : Double = 0.0
    ): PaymentBreakdown {
        val subtotal   = ticketPrice * quantity
        val tax        = subtotal * TAX_RATE
        val serviceFee = SERVICE_FEE
        val total      = (subtotal + tax + serviceFee - discount).coerceAtLeast(0.0)
        return PaymentBreakdown(subtotal, tax, serviceFee, discount, total)
    }
}
