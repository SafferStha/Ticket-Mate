package com.example.individual_project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.example.individual_project.ui.theme.Spacing

/**
 * Initials-based avatar. When Coil is integrated in Phase 6, add an imageUrl parameter
 * and use AsyncImage with this as the fallback.
 */
@Composable
fun ProfileAvatar(
    initials         : String,
    size             : Dp      = Spacing.avatarSm,
    backgroundColor  : Color   = MaterialTheme.colorScheme.primary,
    contentColor     : Color   = MaterialTheme.colorScheme.onPrimary,
    modifier         : Modifier = Modifier
) {
    Box(
        modifier         = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = initials.take(2).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}
