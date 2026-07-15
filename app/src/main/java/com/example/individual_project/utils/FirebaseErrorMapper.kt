package com.example.individual_project.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.database.DatabaseException

/**
 * Non-auth counterpart to AuthErrorMapper. Realtime Database's DatabaseException.message
 * includes the exact denied path (e.g. "Permission denied at /events/abc123/price") -- useful
 * in Logcat, not something to show a user. Everywhere else, [fallback] (already a reasonable
 * human-readable string at each call site) is kept as-is rather than replaced with something
 * generic, since it's usually more specific than a blanket "Something went wrong."
 */
object FirebaseErrorMapper {

    fun map(exception: Exception, fallback: String): String = when {
        exception is FirebaseNetworkException ->
            "Network error. Please check your connection and try again."

        exception is FirebaseTooManyRequestsException ->
            "Too many attempts. Please wait a moment and try again."

        exception is DatabaseException &&
            exception.message?.contains("permission denied", ignoreCase = true) == true ->
            "You don't have permission to do that."

        else -> fallback
    }
}
