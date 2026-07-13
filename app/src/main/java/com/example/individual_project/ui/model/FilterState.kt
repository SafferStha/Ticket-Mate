package com.example.individual_project.ui.model

data class FilterState(
    val selectedCategories : Set<String> = emptySet(),
    val selectedCity       : String      = "",
    val minPrice           : Double      = 0.0,
    val maxPrice           : Double      = 50_000.0
) {
    val isActive: Boolean
        get() = selectedCategories.isNotEmpty() ||
                selectedCity.isNotBlank()        ||
                minPrice > 0.0                   ||
                maxPrice < 50_000.0
}
