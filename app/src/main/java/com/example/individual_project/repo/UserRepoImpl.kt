package com.example.individual_project.repo

import com.example.individual_project.model.UserModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.util.Log

class UserRepoImpl : UserRepo {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance() }
    private val ref by lazy { database.getReference("users") }
    private val otpService = OtpService()

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Login success")
                } else {
                    val errorMsg = it.exception?.message ?: "Login failed"
                    Log.e("LoginError", errorMsg)
                    callback(false, errorMsg)
                }
            }
    }

    override fun addUser(
        name: String,
        email: String,
        password: String,
        contact: String,
        callback: (Boolean, String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: ""
        if (userId.isBlank()) {
            callback(false, "User not authenticated")
            return
        }

        val user = mapOf(
            "id" to userId,
            "name" to name,
            "email" to email,
            "contact" to contact
        )

        ref.child(userId).setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "User added successfully")
            } else {
                val errorMsg = it.exception?.message ?: "Failed to add user"
                Log.e("AddUserError", errorMsg)
                callback(false, errorMsg)
            }
        }
    }

    override fun register(
        email: String,
        password: String,
        name: String,
        contact: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    if (userId.isBlank()) {
                        callback(false, "Failed to read user id")
                        return@addOnCompleteListener
                    }

                    // Send verification email
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                Log.d("Register", "Verification email sent to $email")
                            }
                        }

                    val user = mapOf(
                        "id" to userId,
                        "name" to name,
                        "email" to email,
                        "contact" to contact,
                        "emailVerified" to false,
                        "createdAt" to System.currentTimeMillis()
                    )

                    ref.child(userId).setValue(user).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            callback(true, "Registration successful")
                        } else {
                            val errorMsg = dbTask.exception?.message ?: "Failed to register"
                            Log.e("RegisterError", errorMsg)
                            callback(false, errorMsg)
                        }
                    }
                } else {
                    val errorMsg = task.exception?.message ?: "Registration failed"
                    Log.e("RegisterError", errorMsg)
                    callback(false, errorMsg)
                }
            }
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ForgotPassword", "Reset email sent to $email")
                    callback(true, "Password reset link sent to $email. Check your email.")
                } else {
                    val errorMsg = task.exception?.message ?: "Failed to send reset email"
                    Log.e("ForgotPasswordError", errorMsg)
                    callback(false, errorMsg)
                }
            }
    }

    override fun editProfile(model: UserModel, callback: (Boolean, String) -> Unit) {
        val userId = auth.currentUser?.uid ?: ""
        if (userId.isBlank()) {
            callback(false, "User not authenticated")
            return
        }

        val userUpdate = mapOf(
            "name" to model.name,
            "contact" to model.contact,
            "address" to model.address
        )

        ref.child(userId).updateChildren(userUpdate).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Profile updated successfully")
            } else {
                callback(false, it.exception?.message ?: "Failed to update profile")
            }
        }
    }

    override fun deleteUser(callback: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, "No user logged in")
            return
        }

        val userId = user.uid
        // Delete from database first
        ref.child(userId).removeValue().addOnCompleteListener { dbTask ->
            if (dbTask.isSuccessful) {
                // Then delete from Auth
                user.delete().addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        callback(true, "User deleted successfully")
                    } else {
                        callback(false, authTask.exception?.message ?: "Failed to delete auth account")
                    }
                }
            } else {
                callback(false, dbTask.exception?.message ?: "Failed to delete user data")
            }
        }
    }

    override fun getUserById(
        id: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        ref.child(id).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result.getValue(UserModel::class.java)
                callback(true, "User found", user)
            } else {
                callback(false, "User not found", null)
            }
        }
    }

    override fun getAllUsers(callback: (Boolean, String, List<UserModel>) -> Unit) {
        ref.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val users = mutableListOf<UserModel>()
                task.result.children.forEach { snapshot ->
                    snapshot.getValue(UserModel::class.java)?.let { users.add(it) }
                }
                callback(true, "Users retrieved", users)
            } else {
                callback(false, "Failed to retrieve users", emptyList())
            }
        }
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut()
            callback(true, "Logout successful")
        } catch (e: Exception) {
            callback(false, e.message ?: "Logout failed")
        }
    }

    // OTP Methods
    override fun sendOtpEmail(
        email: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        val otp = otpService.generateOtp(email)
        
        // TODO: Send OTP via email service (Firebase Cloud Functions or Email API)
        // For now, we're just generating and storing OTP
        Log.d("OTP", "OTP generated for $email: $otp (expires in 5 minutes)")
        
        callback(true, "OTP sent to your email", otp)
    }

    override fun verifyOtp(
        email: String,
        otp: String,
        callback: (Boolean, String) -> Unit
    ) {
        val isValid = otpService.verifyOtp(email, otp)
        if (isValid) {
            callback(true, "OTP verified successfully")
        } else {
            if (!otpService.isOtpValid(email)) {
                callback(false, "OTP expired. Please request a new one.")
            } else {
                callback(false, "Invalid OTP")
            }
        }
    }

    override fun sendVerificationEmail(callback: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, "No user logged in")
            return
        }

        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("EmailVerification", "Verification email sent to ${user.email}")
                    callback(true, "Verification email sent. Please check your inbox.")
                } else {
                    val errorMsg = task.exception?.message ?: "Failed to send verification email"
                    Log.e("EmailVerificationError", errorMsg)
                    callback(false, errorMsg)
                }
            }
    }

    override fun changePassword(
        oldPassword: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null || user.email == null) {
            callback(false, "No user logged in")
            return
        }

        // Re-authenticate user
        val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { reAuthTask ->
                if (reAuthTask.isSuccessful) {
                    // Update password
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d("ChangePassword", "Password changed successfully")
                                callback(true, "Password changed successfully")
                            } else {
                                val errorMsg = updateTask.exception?.message ?: "Failed to change password"
                                Log.e("ChangePasswordError", errorMsg)
                                callback(false, errorMsg)
                            }
                        }
                } else {
                    val errorMsg = reAuthTask.exception?.message ?: "Old password is incorrect"
                    Log.e("ReAuthError", errorMsg)
                    callback(false, errorMsg)
                }
            }
    }

    override fun changePasswordWithEmail(
        email: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Send password reset email
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ChangePasswordWithEmail", "Password reset email sent to $email")
                    callback(true, "Password reset link sent to your email. Please follow the link to reset your password.")
                } else {
                    val errorMsg = task.exception?.message ?: "Failed to send password reset email"
                    Log.e("ChangePasswordWithEmailError", errorMsg)
                    callback(false, errorMsg)
                }
            }
    }
}