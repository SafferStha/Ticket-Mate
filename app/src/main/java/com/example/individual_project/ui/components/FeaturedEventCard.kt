package com.example.individual_project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.individual_project.ui.model.EventUiModel
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmNavy

private fun categoryGradient(category: String): List<Color> = when (category) {
    "Music"  -> listOf(Color(0xFF1A0533), Color(0xFF6B21A8))
    "Sports" -> listOf(Color(0xFF0A1628), Color(0xFF1E40AF))
    "Arts"   -> listOf(Color(0xFF1A0A00), Color(0xFF92400E))
    "Comedy" -> listOf(Color(0xFF1A1000), Color(0xFF78350F))
    "Family" -> listOf(Color(0xFF001A0A), Color(0xFF065F46))
    else     -> listOf(TmNavy, Color(0xFF026CDF))
}

@Composable
fun FeaturedEventCard(
    event   : EventUiModel,
    onClick : () -> Unit = {},
    modifier: Modifier   = Modifier
) {
    Card(
        modifier  = modifier
            .width(280.dp)
            .height(180.dp)
            .clickable(onClick = onClick),
        shape     = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(categoryGradient(event.category)))
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(Spacing.cardPadding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: emoji + category badge
                Row(
                    modifier              = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(text = event.emoji, fontSize = 38.sp)
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
                    ) {
                        Text(
                            text  = event.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                // Bottom: title + date + price / location
                Column {
                    Text(
                        text     = event.title,
                        style    = MaterialTheme.typography.titleSmall,
                        color    = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday, null,
                            tint     = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(Spacing.iconSm)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text  = "${event.date} • ${event.time}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Row(
                        modifier          = Modifier.wrapContentWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = event.price,
                            style      = MaterialTheme.typography.titleSmall,
                            color      = TmGold,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.weight(1f)
                        )
                        Text(
                            text     = event.location,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
