package com.example.individual_project.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun categoryEmoji(category: String) = when (category) {
    "All"      -> "✨"; "Music"    -> "🎵"; "Sports"   -> "⚽"
    "Arts"     -> "🎭"; "Comedy"   -> "😂"; "Family"   -> "👨‍👩‍👧"
    "Theater"  -> "🎪"; "Festival" -> "🎉"; else       -> "🎫"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    label      : String,
    isSelected : Boolean,
    onClick    : () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick  = onClick,
        label    = {
            Text(
                text  = "${categoryEmoji(label)} $label",
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
            containerColor         = MaterialTheme.colorScheme.surfaceVariant,
            labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled          = true,
            selected         = isSelected,
            borderColor      = Color.Transparent,
            selectedBorderColor = Color.Transparent
        )
    )
}
