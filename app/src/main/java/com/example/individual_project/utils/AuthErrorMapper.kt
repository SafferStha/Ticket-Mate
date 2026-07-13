package com.example.individual_project.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseTooManyRequestsException

object AuthErrorMapper {

    fun map(exception: Exception): String = when (exception) {

        is FirebaseAuthInvalidCredentialsException -> when (exception.errorCode) {
            "ERROR_INVALID_EMAIL"      -> "The email address is not valid."
            "ERROR_WRONG_PASSWORD",
            "ERROR_INVALID_CREDENTIAL" -> "Incorrect email or password. Please try again."
            else                       -> "Invalid credentials. Please try again."
        }

        is FirebaseAuthInvalidUserException -> when (exception.errorCode) {
            "ERROR_USER_NOT_FOUND" -> "No account found with this email."
            "ERROR_USER_DISABLED"  -> "This account has been disabled. Please contact support."
            else                   -> "Account error. Please try again."
        }

        is FirebaseAuthWeakPasswordException ->
            "Password is too weak. ${exception.reason ?: "Please choose a stronger password."}"

        is FirebaseAuthUserCollisionException ->
            "An account with this email already exists. Try signing in instead."

        is FirebaseNetworkException ->
            "Network error. Please check your connection and try again."

        is FirebaseTooManyRequestsException ->
            "Too many attempts. Please wait a moment and try again."

        else -> exception.localizedMessage ?: "An unexpected error occurred. Please try again."
    }
}
