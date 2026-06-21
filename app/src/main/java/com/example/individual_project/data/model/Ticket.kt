package com.example.individual_project.data.model

data class Ticket(
    val id         : String = "",
    val eventId    : String = "",
    val userId     : String = "",
    val quantity   : Int    = 1,
    val totalPrice : Double = 0.0,
    val status     : String = "confirmed"   // "confirmed" | "cancelled"
)
