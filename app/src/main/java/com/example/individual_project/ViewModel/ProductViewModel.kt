package com.example.individual_project.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.individual_project.model.ProductModel
import com.example.individual_project.repo.ProductRepo

class ProductViewModel(val repo: ProductRepo) : ViewModel() {

    fun addProduct(model: ProductModel, callback: (Boolean, String) -> Unit) {
        repo.addProduct(
            model = model,
            onSuccess = { callback(true, "Product added successfully") },
            onFailure = { error -> callback(false, error) }
        )
    }

    fun updateProduct(id: String, model: ProductModel, callback: (Boolean, String) -> Unit) {
        repo.updateProduct(
            productId = id,
            product = model.toMap(),
            onSuccess = { callback(true, "Product updated successfully") },
            onFailure = { error -> callback(false, error) }
        )
    }

    fun deleteProduct(id: String, callback: (Boolean, String) -> Unit) {
        repo.deleteProduct(
            productId = id,
            onSuccess = { callback(true, "Product deleted successfully") },
            onFailure = { error -> callback(false, error) }
        )
    }

    private val _products = MutableLiveData<ProductModel?>()
    val products: MutableLiveData<ProductModel?> get() = _products;

    private val _allProducts = MutableLiveData<List<ProductModel>?>()
    val allProducts: MutableLiveData<List<ProductModel>?> get() = _allProducts;

    private val _error = MutableLiveData<String?>()
    val error: MutableLiveData<String?> get() = _error;

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading;


    fun getAllProduct() {
        _loading.value = true
        repo.getProducts(
            onSuccess = { productMaps ->
                _allProducts.value = productMaps.map { it.toProductModel() }
                _error.value = null
                _loading.value = false
            },
            onFailure = { message ->
                _error.value = message
                _loading.value = false
            }
        )
    }

    fun getProductByID(id: String) {
        _loading.value = true
        repo.getProductById(
            productId = id,
            onSuccess = { productMap ->
                _products.value = productMap?.toProductModel()
                _loading.value = false
            },
            onFailure = { message ->
                _error.value = message
                _loading.value = false
                _products.value = null
            }
        )
    }

    private fun Map<String, Any>.toProductModel(): ProductModel {
        return ProductModel(
            id = this["id"] as? String ?: "",
            name = this["name"] as? String ?: "",
            email = this["email"] as? String ?: "",
            password = this["password"] as? String ?: "",
            contact = this["contact"] as? String ?: "",
            address = this["address"] as? String ?: "",
            imageUrl = this["imageUrl"] as? String ?: ""
        )
    }
}
