package com.example.individual_project.data.model

data class Payment(
    val id             : String = "",
    val bookingId      : String = "",
    val userId         : String = "",
    val subtotal       : Double = 0.0,
    val tax            : Double = 0.0,
    val serviceFee     : Double = 0.0,
    val discount       : Double = 0.0,
    val totalAmount    : Double = 0.0,
    val paymentMethod  : String = "",
    val paymentStatus  : String = "",
    val transactionDate: Long   = 0L
)
