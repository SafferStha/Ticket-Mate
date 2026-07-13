package com.example.individual_project.data.model

data class BookedTicket(
    val seatNumber : String  = "",
    val ticketType : String  = "STANDARD",
    val isValid    : Boolean = true
)
