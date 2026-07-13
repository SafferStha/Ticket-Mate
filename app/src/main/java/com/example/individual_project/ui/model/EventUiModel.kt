package com.example.individual_project.ui.model

import com.example.individual_project.data.model.Event

/**
 * UI-layer representation of an event.
 * Separates display concerns (emoji, formatted price, isLiked) from the domain Event model.
 * Populate via [Event.toUiModel] — never construct directly from Firebase data.
 */
data class EventUiModel(
    val id         : String,
    val title      : String,
    val category   : String,
    val date       : String,
    val time       : String,
    val venue      : String,
    val location   : String,    // maps to Event.city — "City, State" display string
    val price      : String,    // formatted, e.g. "From $99" or "Free"
    val emoji      : String,    // derived from category
    val isFeatured : Boolean = false,
    val isLiked    : Boolean = false
)

// ─── Mapper ───────────────────────────────────────────────────────────────────

fun Event.toUiModel(): EventUiModel = EventUiModel(
    id         = id,
    title      = title,
    category   = category,
    date       = date,
    time       = time,
    venue      = venue,
    location   = city,
    price      = if (price <= 0.0) "Free" else "From \$${price.toInt()}",
    emoji      = categoryEmoji(category),
    isFeatured = featured,
    isLiked    = false
)

fun categoryEmoji(category: String): String = when (category.trim().lowercase()) {
    "concerts", "concert", "music"       -> "🎵"
    "sports", "sport"                    -> "🏆"
    "theater", "theatre", "arts", "art"  -> "🎭"
    "comedy"                             -> "😂"
    "family", "kids"                     -> "👨‍👩‍👧"
    "festivals", "festival"              -> "🎪"
    "food", "dining"                     -> "🍕"
    "tech", "technology", "conference"   -> "💻"
    else                                 -> "🎫"
}

fun categoryGradientColors(category: String): List<androidx.compose.ui.graphics.Color> {
    val cat = category.trim().lowercase()
    return when {
        cat.contains("concert") || cat.contains("music") ->
            listOf(androidx.compose.ui.graphics.Color(0xFF1A0533), androidx.compose.ui.graphics.Color(0xFF6B21A8))
        cat.contains("sport") ->
            listOf(androidx.compose.ui.graphics.Color(0xFF0A1628), androidx.compose.ui.graphics.Color(0xFF1E40AF))
        cat.contains("theater") || cat.contains("theatre") || cat.contains("art") ->
            listOf(androidx.compose.ui.graphics.Color(0xFF1A0A00), androidx.compose.ui.graphics.Color(0xFF92400E))
        cat.contains("comedy") ->
            listOf(androidx.compose.ui.graphics.Color(0xFF1A1000), androidx.compose.ui.graphics.Color(0xFF78350F))
        cat.contains("family") ->
            listOf(androidx.compose.ui.graphics.Color(0xFF001A0A), androidx.compose.ui.graphics.Color(0xFF065F46))
        cat.contains("festival") ->
            listOf(androidx.compose.ui.graphics.Color(0xFF1A0010), androidx.compose.ui.graphics.Color(0xFF9D174D))
        else ->
            listOf(androidx.compose.ui.graphics.Color(0xFF0A0F1F), androidx.compose.ui.graphics.Color(0xFF026CDF))
    }
}
