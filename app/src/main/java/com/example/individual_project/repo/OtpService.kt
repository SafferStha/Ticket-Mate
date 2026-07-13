package com.example.individual_project.repo

import kotlin.random.Random

data class OtpData(
    val code: String,
    val email: String,
    val createdAt: Long,
    val expiresAt: Long
)

class OtpService {
    companion object {
        private const val OTP_VALIDITY_MINUTES = 5
        private val otpStorage = mutableMapOf<String, OtpData>()
    }

    /**
     * Generate a 6-digit OTP for the given email
     */
    fun generateOtp(email: String): String {
        val otp = String.format("%06d", Random.nextInt(1000000))
        val now = System.currentTimeMillis()
        val expiresAt = now + (OTP_VALIDITY_MINUTES * 60 * 1000)

        otpStorage[email] = OtpData(
            code = otp,
            email = email,
            createdAt = now,
            expiresAt = expiresAt
        )
        return otp
    }

    /**
     * Verify OTP for the given email
     */
    fun verifyOtp(email: String, otp: String): Boolean {
        val storedOtp = otpStorage[email] ?: return false

        // Check if OTP is expired
        if (System.currentTimeMillis() > storedOtp.expiresAt) {
            otpStorage.remove(email)
            return false
        }

        // Check if OTP matches
        if (storedOtp.code != otp) {
            return false
        }

        // OTP verified successfully, remove it
        otpStorage.remove(email)
        return true
    }

    /**
     * Check if OTP is still valid (not expired)
     */
    fun isOtpValid(email: String): Boolean {
        val storedOtp = otpStorage[email] ?: return false
        return System.currentTimeMillis() <= storedOtp.expiresAt
    }

    /**
     * Get remaining time for OTP validity in seconds
     */
    fun getRemainingTime(email: String): Long {
        val storedOtp = otpStorage[email] ?: return 0
        val remaining = (storedOtp.expiresAt - System.currentTimeMillis()) / 1000
        return if (remaining > 0) remaining else 0
    }

    /**
     * Resend OTP (can only resend after 30 seconds)
     */
    fun canResendOtp(email: String): Boolean {
        val storedOtp = otpStorage[email] ?: return true
        val elapsedSeconds = (System.currentTimeMillis() - storedOtp.createdAt) / 1000
        return elapsedSeconds >= 30
    }

    /**
     * Clear OTP for email
     */
    fun clearOtp(email: String) {
        otpStorage.remove(email)
    }
}
