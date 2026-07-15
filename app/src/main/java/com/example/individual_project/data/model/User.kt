package com.example.individual_project.data.model

data class User(
    val uid          : String = "",
    val name         : String = "",
    val email        : String = "",
    val contact      : String = "",
    val createdAt    : Long   = 0L,
    val profileImage : String = ""
    // No 'password' field — passwords are managed by Firebase Auth, never stored in the database.
)

/**
 * Generates up to two initials from the user's name.
 * Defaults to "TM" (TicketMate) if the name is blank.
 */
val User.initials: String
    get() = if (name.isBlank()) "TM"
    else name.trim().split("\\s+".toRegex())
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "TM" }
