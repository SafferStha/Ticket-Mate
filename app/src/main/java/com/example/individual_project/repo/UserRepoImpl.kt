package com.example.individual_project.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserRepoImpl : UserRepo {

    val auth = FirebaseAuth.getInstance()

    val database = FirebaseDatabase.getInstance()

    val ref = database.getReference("users")

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
                    callback(false, "${it.exception?.message}")
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
        val userId = ref.push().key ?: ""
        if (userId.isBlank()) {
            callback(false, "Failed to generate user id")
            return
        }

        val user = mapOf(
            "id" to userId,
            "name" to name,
            "email" to email,
            "password" to password,
            "contact" to contact
        )

        ref.child(userId).setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "User registered")
            } else {
                callback(false, "${it.exception?.message}")
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
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    if (userId.isBlank()) {
                        callback(false, "Failed to read user id")
                        return@addOnCompleteListener
                    }

                    val user = mapOf(
                        "id" to userId,
                        "name" to name,
                        "email" to email,
                        "password" to password,
                        "contact" to contact
                    )

                    ref.child(userId).setValue(user).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            callback(true, "Registration successful")
                        } else {
                            callback(false, "${dbTask.exception?.message}")
                        }
                    }
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Reset link sent to $email")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun editProfile() {
        TODO("Not yet implemented")
    }

    override fun getUserById(
        id: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getAllUsers(callback: (Boolean, String, List<String>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut()
            callback(true, "Logout successful")
        } catch (e: Exception) {
            callback(false, e.toString())
        }
    }
}