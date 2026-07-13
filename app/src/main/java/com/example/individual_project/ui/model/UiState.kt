package com.example.individual_project.ui.model

data class UiState<T>(
    val isLoading : Boolean = false,
    val data      : T?      = null,
    val error     : String? = null
) {
    val isSuccess : Boolean get() = data != null && !isLoading
    val hasError  : Boolean get() = error != null && !isLoading
}
