package com.example.individual_project.data.model

data class Event(
    val id             : String  = "",
    val title          : String  = "",
    val description    : String  = "",
    val category       : String  = "",
    val venue          : String  = "",
    val city           : String  = "",
    val date           : String  = "",
    val time           : String  = "",
    val imageUrl       : String  = "",
    val price          : Double  = 0.0,
    val availableSeats : Int     = 0,
    val organizer      : String  = "",
    val featured       : Boolean = false
)
