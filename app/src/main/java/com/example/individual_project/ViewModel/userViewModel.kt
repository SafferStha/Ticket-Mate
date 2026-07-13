package com.example.individual_project.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.individual_project.model.UserModel
import com.example.individual_project.repo.UserRepo
import com.google.firebase.auth.FirebaseAuth

class UserViewModel(val repo: UserRepo) : ViewModel() {
    // Note: adaptors/stubs are used here to avoid depending on concrete repo callback
    // signatures. Replace these implementations with direct repo calls when repo
    // method signatures are stable.
    fun login(
        email: String, password: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        try {
            repo.login(email, password) { success, message ->
                _loading.value = false
                callback(success, message)
            }
        } catch (e: Exception) {
            _loading.value = false
            callback(false, e.message ?: "unknown error")
        }
    }

    fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        try {
            repo.forgetPassword(email) { success, message ->
                _loading.value = false
                callback(success, message)
            }
        } catch (e: Exception) {
            _loading.value = false
            callback(false, e.message ?: "unknown error")
        }
    }

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    private val _users = MutableLiveData<UserModel?>()
    val users: MutableLiveData<UserModel?> get() = _users


    fun getUserById(
        id: String
    ){
        _loading.value = true
        try {
            repo.getUserById(id) { success, message, data ->
                if (success) {
                    _users.value = data
                }
                _loading.value = false
            }
        } catch (e: Exception) {
            _loading.value = false
            _users.value = null
        }
    }

    private val _allUsers = MutableLiveData<List<UserModel?>>()
    val allUsers: MutableLiveData<List<UserModel?>> get() = _allUsers

    fun getAllUser() {
        _loading.value = true
        try {
            repo.getAllUsers { success, message, data ->
                if (success) {
                    _allUsers.value = data
                }
                _loading.value = false
            }
        } catch (e: Exception) {
            _loading.value = false
            _allUsers.value = emptyList()
        }
    }


    fun logout(callback: (Boolean, String) -> Unit) {
        try {
            repo.logout(callback)
        } catch (e: Exception) {
            callback(false, e.message ?: "unknown error")
        }
    }


    //authentication
    fun register(
        email: String, password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        _loading.value = true
        try {
            repo.register(email, password, "", "") { success, message ->
                _loading.value = false
                val userId = if (success) com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "" else ""
                callback(success, message, userId)
            }
        } catch (e: Exception) {
            _loading.value = false
            callback(false, e.message ?: "unknown", "")
        }
    }


    fun addUser(id: String, model: UserModel, callback: (Boolean, String) -> Unit) {
        try {
            repo.addUser(model.name, model.email, "", model.contact) { success, message ->
                callback(success, message)
            }
        } catch (e: Exception) {
            callback(false, e.message ?: "unknown error")
        }
    }

    fun editProfile(model: UserModel, callback: (Boolean, String) -> Unit) {
        _loading.value = true
        try {
            repo.editProfile(model) { success, message ->
                _loading.value = false
                callback(success, message)
            }
        } catch (e: Exception) {
            _loading.value = false
            callback(false, e.message ?: "unknown error")
        }
    }

    fun deleteUser(callback: (Boolean, String) -> Unit) {
        _loading.value = true
        try {
            repo.deleteUser { success, message ->
                _loading.value = false
                callback(success, message)
            }
        } catch (e: Exception) {
            _loading.value = false
            callback(false, e.message ?: "unknown error")
        }
    }

    // OTP Methods
    fun sendOtpEmail(
        email: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        _loading.value = true
        try {
            repo.sendOtpEmail(email) { success, message, otp ->
                _loading.value = false
                callback(success, message, otp)
            }
        } catch (e: Exception) {
            _loading.value = false
            callback(false, e.message ?: "unknown error", "")
        }
    }

    fun verifyOtp(
        email: String,
        otp: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        try {
            repo.verifyOtp(email, otp) { success, message ->
                _loading.value = false
                callback(success, message)
            }
        } catch (e: Exception) {
            _loading.value = false
            callback(false, e.message ?: "unknown error")
        }
    }

    // Email Verification
    fun sendVerificationEmail(callback: (Boolean, String) -> Unit) {
        _loading.value = true
        try {
            repo.sendVerificationEmail { success, message ->
                _loading.value = false
                callback(success, message)
            }
        } catch (e: Exception) {
            _loading.value = false
            callback(false, e.message ?: "unknown error")
        }
    }

    // Change Password
    fun changePassword(
        oldPassword: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        try {
            repo.changePassword(oldPassword, newPassword) { success, message ->
                _loading.value = false
                callback(success, message)
            }
        } catch (e: Exception) {
            _loading.value = false
            callback(false, e.message ?: "unknown error")
        }
    }

    fun changePasswordWithEmail(
        email: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        try {
            repo.changePasswordWithEmail(email, newPassword) { success, message ->
                _loading.value = false
                callback(success, message)
            }
        } catch (e: Exception) {
            _loading.value = false
            callback(false, e.message ?: "unknown error")
        }
    }
}