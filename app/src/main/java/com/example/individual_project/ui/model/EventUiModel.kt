package com.example.individual_project.ui.model

/**
 * UI-layer representation of an event.
 * Includes display-only fields (emoji, isLiked, isFeatured) that have no place
 * in the data-layer Event model.
 * In Phase 6 this will be populated by mapping data.model.Event → EventUiModel.
 */
data class EventUiModel(
    val id         : String,
    val title      : String,
    val category   : String,
    val date       : String,
    val time       : String,
    val venue      : String,
    val location   : String,
    val price      : String,
    val emoji      : String,
    val isFeatured : Boolean = false,
    val isLiked    : Boolean = false
)
