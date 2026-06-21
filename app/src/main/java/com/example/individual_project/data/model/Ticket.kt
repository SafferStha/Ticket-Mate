package com.example.individual_project.data.model

data class Ticket(
    val id           : String = "",
    val bookingId    : String = "",
    val paymentId    : String = "",
    val userId       : String = "",
    val eventId      : String = "",
    val eventTitle   : String = "",
    val venue        : String = "",
    val date         : String = "",
    val quantity     : Int    = 1,
    val totalPrice   : Double = 0.0,
    val ticketStatus : String = "ACTIVE",   // ACTIVE | USED | CANCELLED
    val generatedAt  : Long   = 0L
)
