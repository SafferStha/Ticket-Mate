package com.example.individual_project.repo

import com.example.individual_project.model.UserModel

interface UserRepo {

    fun login(email: String, password: String, callback: (Boolean, String) -> Unit)

    fun addUser(name: String, email: String, password: String, contact: String, callback: (Boolean, String) -> Unit)

    fun register(email: String, password: String, name: String, contact: String, callback: (Boolean, String) -> Unit)

    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit)

    fun editProfile(model: UserModel, callback: (Boolean, String) -> Unit)

    fun getUserById(id: String, callback: (Boolean, String, UserModel?) -> Unit)

    fun getAllUsers(callback : (Boolean, String, List<UserModel>) -> Unit)

    fun deleteUser(callback: (Boolean, String) -> Unit)

    fun logout(callback: (Boolean, String) -> Unit)

    // OTP Methods
    fun sendOtpEmail(email: String, callback: (Boolean, String, String) -> Unit)

    fun verifyOtp(email: String, otp: String, callback: (Boolean, String) -> Unit)

    // Email Verification
    fun sendVerificationEmail(callback: (Boolean, String) -> Unit)

    // Change Password
    fun changePassword(oldPassword: String, newPassword: String, callback: (Boolean, String) -> Unit)

    fun changePasswordWithEmail(email: String, newPassword: String, callback: (Boolean, String) -> Unit)
}

