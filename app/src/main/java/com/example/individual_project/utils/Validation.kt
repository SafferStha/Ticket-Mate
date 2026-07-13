package com.example.individual_project.utils

import android.util.Patterns

object Validation {

    fun validateEmail(email: String): String? = when {
        email.isBlank()                                          -> "Email is required."
        !Patterns.EMAIL_ADDRESS.matcher(email).matches()         -> "Enter a valid email address."
        else                                                     -> null
    }

    fun validatePassword(password: String): String? = when {
        password.isBlank()                           -> "Password is required."
        password.length < 8                          -> "Password must be at least 8 characters."
        !password.any { it.isUpperCase() }           -> "Password must contain at least one uppercase letter."
        !password.any { it.isLowerCase() }           -> "Password must contain at least one lowercase letter."
        !password.any { it.isDigit() }               -> "Password must contain at least one number."
        !password.any { !it.isLetterOrDigit() }      -> "Password must contain at least one special character."
        else                                         -> null
    }

    fun validateName(name: String): String? = when {
        name.isBlank()          -> "Name is required."
        name.trim().length < 2  -> "Name must be at least 2 characters."
        else                    -> null
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? = when {
        confirmPassword.isBlank()       -> "Please confirm your password."
        password != confirmPassword     -> "Passwords do not match."
        else                            -> null
    }
}
