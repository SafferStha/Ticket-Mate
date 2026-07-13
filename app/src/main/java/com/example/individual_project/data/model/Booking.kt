package com.example.individual_project.data.model

data class Booking(
    val id             : String = "",
    val userId         : String = "",
    val eventId        : String = "",
    val eventTitle     : String = "",
    val eventImage     : String = "",
    val venue          : String = "",
    val date           : String = "",
    val quantity       : Int    = 0,
    val pricePerTicket : Double = 0.0,
    val totalPrice     : Double = 0.0,
    val bookingStatus  : String = BookingStatus.CONFIRMED.name,
    val bookingDate    : Long   = 0L
)
