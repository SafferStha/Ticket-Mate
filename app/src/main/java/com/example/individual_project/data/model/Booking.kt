package com.example.individual_project.data.model

data class Booking(
    val bookingId  : String = "",
    val userId     : String = "",
    val eventId    : String = "",
    val quantity   : Int    = 1,
    val totalPrice : Double = 0.0,
    val status     : String = "confirmed"   // "confirmed" | "cancelled"
)
