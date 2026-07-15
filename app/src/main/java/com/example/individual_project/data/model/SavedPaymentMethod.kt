package com.example.individual_project.data.model

/**
 * A demo-only record for the checkout method picker. There is no real payment gateway behind
 * this app (see PaymentRepositoryImpl), so this deliberately can never hold anything that
 * would matter if leaked: no full card number, no CVV, no raw credentials -- just a provider
 * tag, a user-chosen label, and a last-4-digits display string enforced at the ViewModel layer.
 */
data class SavedPaymentMethod(
    val id               : String  = "",
    val userId           : String  = "",
    val provider         : String  = "",   // PaymentMethod.key, e.g. "CARD" | "ESEWA" | "KHALTI"
    val displayName      : String  = "",   // user-chosen label, e.g. "My Visa"
    val maskedIdentifier : String  = "",   // last 4 digits only
    val isDefault        : Boolean = false,
    val createdAt        : Long    = 0L
)
