package com.example.individual_project.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.database.DatabaseException
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FirebaseErrorMapperTest {

    @Test
    fun `network exception maps to a friendly message`() {
        val exception = mockk<FirebaseNetworkException>()

        assertEquals(
            "Network error. Please check your connection and try again.",
            FirebaseErrorMapper.map(exception, "fallback")
        )
    }

    @Test
    fun `too many requests maps to a friendly throttling message`() {
        val exception = mockk<FirebaseTooManyRequestsException>()

        assertEquals(
            "Too many attempts. Please wait a moment and try again.",
            FirebaseErrorMapper.map(exception, "fallback")
        )
    }

    @Test
    fun `permission denied never exposes the internal database path`() {
        val exception = mockk<DatabaseException>()
        every { exception.message } returns "Permission denied at /events/abc123/price"

        val message = FirebaseErrorMapper.map(exception, "fallback")

        assertEquals("You don't have permission to do that.", message)
        assertFalse(message.contains("/events/"))
    }

    @Test
    fun `an unrecognized database exception keeps the caller's fallback text`() {
        val exception = mockk<DatabaseException>()
        every { exception.message } returns "some other database error"

        assertEquals("fallback", FirebaseErrorMapper.map(exception, "fallback"))
    }

    @Test
    fun `a plain exception keeps the caller's fallback text`() {
        assertEquals("fallback", FirebaseErrorMapper.map(IllegalStateException("boom"), "fallback"))
    }
}
