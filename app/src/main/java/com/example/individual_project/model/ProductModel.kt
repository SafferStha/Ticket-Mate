package com.example.individual_project.model

data class ProductModel (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val contact: String = "",
    val address: String = "",
    val imageUrl: String = "",
){
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "password" to password,
            "contact" to contact,
            "address" to address,
            "imageUrl" to imageUrl,
        )
    }
}

