package com.example.individual_project.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Firebase's auth exception subclasses don't expose public constructors, so these are mocked
 * rather than constructed -- MockK can stand up a proxy for a final class without ever calling
 * its real constructor, which is exactly what's needed to control errorCode for each branch.
 */
class AuthErrorMapperTest {

    @Test
    fun `wrong password maps to a friendly message, not the raw Firebase text`() {
        val exception = mockk<FirebaseAuthInvalidCredentialsException>()
        every { exception.errorCode } returns "ERROR_WRONG_PASSWORD"

        val message = AuthErrorMapper.map(exception)

        assertEquals("Incorrect email or password. Please try again.", message)
    }

    @Test
    fun `invalid email maps to a specific message`() {
        val exception = mockk<FirebaseAuthInvalidCredentialsException>()
        every { exception.errorCode } returns "ERROR_INVALID_EMAIL"

        assertEquals("The email address is not valid.", AuthErrorMapper.map(exception))
    }

    @Test
    fun `user not found maps to a friendly message`() {
        val exception = mockk<FirebaseAuthInvalidUserException>()
        every { exception.errorCode } returns "ERROR_USER_NOT_FOUND"

        assertEquals("No account found with this email.", AuthErrorMapper.map(exception))
    }

    @Test
    fun `disabled account maps to a friendly message`() {
        val exception = mockk<FirebaseAuthInvalidUserException>()
        every { exception.errorCode } returns "ERROR_USER_DISABLED"

        assertEquals(
            "This account has been disabled. Please contact support.",
            AuthErrorMapper.map(exception)
        )
    }

    @Test
    fun `duplicate email maps to a friendly message`() {
        val exception = mockk<FirebaseAuthUserCollisionException>()

        val message = AuthErrorMapper.map(exception)

        assertEquals("An account with this email already exists. Try signing in instead.", message)
    }

    @Test
    fun `network exception maps to a friendly message`() {
        val exception = mockk<FirebaseNetworkException>()

        assertEquals(
            "Network error. Please check your connection and try again.",
            AuthErrorMapper.map(exception)
        )
    }

    @Test
    fun `too many requests maps to a friendly throttling message`() {
        val exception = mockk<FirebaseTooManyRequestsException>()

        assertEquals(
            "Too many attempts. Please wait a moment and try again.",
            AuthErrorMapper.map(exception)
        )
    }

    @Test
    fun `unrecognized exception types never leak a null or blank message`() {
        val message = AuthErrorMapper.map(IllegalStateException("boom"))
        assertFalse(message.isBlank())
    }
}
