package com.example.individual_project.utils

import java.util.Locale

object PriceFormatter {

    /** Full currency format: Free  |  $120.00  |  $9.99 */
    fun format(price: Double): String = when {
        price <= 0.0 -> "Free"
        else         -> "$${String.format(Locale.US, "%.2f", price)}"
    }

    /** Short format for cards: Free  |  $120  |  $9.99 */
    fun formatShort(price: Double): String = when {
        price <= 0.0                             -> "Free"
        price == kotlin.math.floor(price)        -> "$${ price.toInt() }"
        else                                     -> "$${String.format(Locale.US, "%.2f", price)}"
    }

    /** Prefix for listings: Free  |  From $120.00 */
    fun formatFrom(price: Double): String = when {
        price <= 0.0 -> "Free"
        else         -> "From ${format(price)}"
    }
}
