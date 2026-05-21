package com.example.individual_project.repo

interface UserRepo {

    fun login(email: String, password: String, callback: (Boolean, String) -> Unit)

    fun register(email: String, password: String, name: String, contact: String, callback: (Boolean, String) -> Unit)

    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit)

    fun editProfile()

    fun getUserById(id: String, callback: (Boolean, String) -> Unit)

    fun getAllUsers(callback : (Boolean, String, List<String>) -> Unit)

    fun logout(callback: (Boolean, String) -> Unit)
}

