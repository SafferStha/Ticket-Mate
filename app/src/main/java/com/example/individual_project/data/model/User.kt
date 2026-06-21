package com.example.individual_project.data.model

data class User(
    val uid          : String = "",
    val name         : String = "",
    val email        : String = "",
    val contact      : String = "",
    val profileImage : String = ""
    // No 'password' field — passwords are managed by Firebase Auth, never stored in the database.
)
