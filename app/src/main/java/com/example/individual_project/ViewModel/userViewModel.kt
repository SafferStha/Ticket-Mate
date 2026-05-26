package com.example.individual_project.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.individual_project.model.UserModel
import com.example.individual_project.repo.UserRepo

class UserViewModel(val repo: UserRepo) : ViewModel() {
    // Note: adaptors/stubs are used here to avoid depending on concrete repo callback
    // signatures. Replace these implementations with direct repo calls when repo
    // method signatures are stable.
    fun login(
        email: String, password: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        // Temporary local handling — forward to repo if signatures match.
        try {
            // If repo provides a compatible 'login', uncomment the following line:
            // repo.login(email, password, callback)
            _loading.value = false
            callback(false, "login not forwarded: update ViewModel when repo API is known")
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
            // repo.forgetPassword(email, callback)
            _loading.value = false
            callback(false, "forgetPassword not forwarded: update ViewModel when repo API is known")
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
        // Repo callback signature may differ across implementations. Keep this
        // method conservative until repo API is confirmed.
        try {
            // If repo.getUserById provides the user in the callback, forward it.
            // repo.getUserById(id) { success, message, data -> _users.value = data; _loading.value = false }
            _loading.value = false
            _users.value = null
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
            // repo.getAllUsers { success, message, data -> if (success) _allUsers.value = data }
            _loading.value = false
            _allUsers.value = emptyList()
        } catch (e: Exception) {
            _loading.value = false
            _allUsers.value = emptyList()
        }
    }


    fun logout(callback: (Boolean, String) -> Unit) {
        try {
            // repo.logout(callback)
            callback(false, "logout not forwarded: update ViewModel when repo API is known")
        } catch (e: Exception) {
            callback(false, e.message ?: "unknown error")
        }
    }


    //authentication
    fun register(
        email: String, password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        try {
            // If repo.register has an extra parameter (e.g. contact), update this call.
            // repo.register(email, password, contact, callback)
            callback(false, "", "register not forwarded: update ViewModel when repo API is known")
        } catch (e: Exception) {
            callback(false, e.message ?: "unknown", "")
        }
    }


    fun addUser(id: String, model: UserModel, callback: (Boolean, String) -> Unit) {
        try {
            // repo.addUser(id, model, callback)  // adapt when repo API is known
            callback(false, "addUser not forwarded: update ViewModel when repo API is known")
        } catch (e: Exception) {
            callback(false, e.message ?: "unknown error")
        }
    }

    fun editProfile(id: String, model: UserModel, callback: (Boolean, String) -> Unit) {
        try {
            // repo.editProfile(...) // adapt when repo API is known
            callback(false, "editProfile not forwarded: update ViewModel when repo API is known")
        } catch (e: Exception) {
            callback(false, e.message ?: "unknown error")
        }
    }

    fun deleteUser(id: String, callback: (Boolean, String) -> Unit) {
        try {
            // repo.deleteUser(id, callback)
            callback(false, "deleteUser not forwarded: update ViewModel when repo API is known")
        } catch (e: Exception) {
            callback(false, e.message ?: "unknown error")
        }
    }
}