package com.example.individual_project.data.model

data class SavedLocation(
    val id        : String  = "",
    val userId    : String  = "",
    val label     : String  = "",
    val address   : String  = "",
    val city      : String  = "",
    val isDefault : Boolean = false,
    val createdAt : Long    = 0L,
    val updatedAt : Long    = 0L
)
