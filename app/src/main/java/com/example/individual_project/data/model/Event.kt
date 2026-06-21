package com.example.individual_project.data.model

data class Event(
    val eventId        : String = "",
    val title          : String = "",
    val description    : String = "",
    val location       : String = "",
    val date           : String = "",
    val imageUrl       : String = "",
    val price          : Double = 0.0,
    val category       : String = "",
    val availableSeats : Int    = 0
)
