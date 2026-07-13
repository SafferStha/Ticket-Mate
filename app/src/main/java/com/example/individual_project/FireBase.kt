package com.example.individual_project

import com.google.firebase.database.FirebaseDatabase
import kotlin.collections.Map
import kotlin.collections.toMutableMap

object FireBase {
    private val database by lazy { FirebaseDatabase.getInstance() }
    private val productsRef by lazy { database.getReference("products") }

    fun addProduct(product: Map<String, Any>, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val productId = product["id"] as? String
        val id = if (productId.isNullOrBlank()) productsRef.push().key ?: "" else productId
        
        val finalProduct = product.toMutableMap().apply { 
            this["id"] = id
        }

        productsRef.child(id).setValue(finalProduct)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to add product") }
    }

    fun updateProduct(productId: String, product: Map<String, Any>, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        productsRef.child(productId).updateChildren(product)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to update product") }
    }

    fun getProductById(productId: String, onSuccess: (Map<String, Any>?) -> Unit, onFailure: (String) -> Unit) {
        productsRef.child(productId).get()
            .addOnSuccessListener { snapshot ->
                @Suppress("UNCHECKED_CAST")
                onSuccess(snapshot.value as? Map<String, Any>)
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to get product") }
    }

    fun getProducts(onSuccess: (List<Map<String, Any>>) -> Unit, onFailure: (String) -> Unit) {
        productsRef.get()
            .addOnSuccessListener { snapshot ->
                @Suppress("UNCHECKED_CAST")
                val products = snapshot.children.mapNotNull { it.value as? Map<String, Any> }
                onSuccess(products)
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to get products") }
    }

    fun deleteProduct(productId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        productsRef.child(productId).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to delete product") }
    }
}
