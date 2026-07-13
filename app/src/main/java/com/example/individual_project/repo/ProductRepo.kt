package com.example.individual_project.repo

import com.example.individual_project.FireBase
import com.example.individual_project.model.ProductModel

interface ProductRepo {
    fun addProduct(model: ProductModel, onSuccess: () -> Unit, onFailure: (String) -> Unit)
    fun getProducts(onSuccess: (List<Map<String, Any>>) -> Unit, onFailure: (String) -> Unit)
    fun updateProduct(productId: String, product: Map<String, Any>, onSuccess: () -> Unit, onFailure: (String) -> Unit)
    fun getProductById(productId: String, onSuccess: (Map<String, Any>?) -> Unit, onFailure: (String) -> Unit)
    fun deleteProduct(productId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit)
}

class ProductRepoImpl : ProductRepo {
    override fun addProduct(
        model: ProductModel,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        FireBase.addProduct(
            product = model.toMap(),
            onSuccess = {
                onSuccess()
            },
            onFailure = { error ->
                onFailure(error)
            }
        )
    }

    override fun getProducts(onSuccess: (List<Map<String, Any>>) -> Unit, onFailure: (String) -> Unit) {
        FireBase.getProducts(
            onSuccess = { products ->
                onSuccess(products)
            },
            onFailure = { error ->
                onFailure(error)
            }
        )
    }

    override fun updateProduct(
        productId: String,
        product: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        FireBase.updateProduct(productId, product, onSuccess, onFailure)
    }

    override fun getProductById(
        productId: String,
        onSuccess: (Map<String, Any>?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        FireBase.getProductById(productId, onSuccess, onFailure)
    }

    override fun deleteProduct(productId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        FireBase.deleteProduct(
            productId = productId,
            onSuccess = {
                onSuccess()
            },
            onFailure = { error ->
                onFailure(error)
            }
        )
    }
}
