package com.example.individual_project.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.individual_project.ui.components.CategoryChip
import com.example.individual_project.ui.components.EventCard
import com.example.individual_project.ui.components.FeaturedEventCard
import com.example.individual_project.ui.components.ProfileAvatar
import com.example.individual_project.ui.components.SectionHeader
import com.example.individual_project.ui.components.TmSearchBar
import com.example.individual_project.ui.model.EventUiModel
import com.example.individual_project.ui.model.sampleCategories
import com.example.individual_project.ui.model.sampleEvents
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmLightBlue
import com.example.individual_project.ui.theme.TmNavyBlue

@Composable
fun HomeScreen(
    selectedCategory : String,
    onCategoryChange : (String) -> Unit,
    // Phase 5: replace these with ViewModel state
    events           : List<EventUiModel> = sampleEvents,
    userName         : String             = "John Doe",
    userInitials     : String             = "JD"
) {
    val featured  = events.filter { it.isFeatured }
    val displayed = if (selectedCategory == "All") events
                    else events.filter { it.category == selectedCategory }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ────────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(TmNavyBlue, TmDarkBlue)))
                    .padding(
                        top   = Spacing.headerPaddingTop,
                        start = Spacing.screenHorizontal,
                        end   = Spacing.screenHorizontal,
                        bottom = Spacing.md
                    )
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text  = "Good evening 👋",
                            style = MaterialTheme.typography.bodySmall,
                            color = TmLightBlue
                        )
                        Text(
                            text  = userName,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Notifications, null, tint = Color.White)
                        }
                        ProfileAvatar(initials = userInitials, size = Spacing.avatarSm)
                    }
                }
            }
        }

        // ── Search bar (inside navy band) ─────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TmNavyBlue)
                    .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm)
            ) {
                TmSearchBar(value = "", onValueChange = {})
            }
        }

        // ── Featured Events ────────────────────────────────────────────────
        item {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                SectionHeader(
                    title         = "🔥 Featured Events",
                    onActionClick = {},
                    modifier      = Modifier.padding(
                        horizontal = Spacing.screenHorizontal,
                        vertical   = Spacing.md
                    )
                )
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = Spacing.screenHorizontal),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(featured, key = { it.id }) { event ->
                        FeaturedEventCard(event = event)
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
            }
        }

        // ── Categories ────────────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(top = Spacing.md)) {
                Text(
                    text     = "Browse Categories",
                    style    = MaterialTheme.typography.titleMedium,
                    color    = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = Spacing.screenHorizontal)
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = Spacing.screenHorizontal),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(sampleCategories) { cat ->
                        CategoryChip(
                            label      = cat,
                            isSelected = selectedCategory == cat,
                            onClick    = { onCategoryChange(cat) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.xs))
            }
        }

        // ── Trending Events ───────────────────────────────────────────────
        item {
            SectionHeader(
                title         = "Trending Events",
                onActionClick = {},
                modifier      = Modifier.padding(
                    horizontal = Spacing.screenHorizontal,
                    vertical   = Spacing.md
                )
            )
        }

        items(displayed, key = { it.id }) { event ->
            EventCard(
                event    = event,
                modifier = Modifier.padding(
                    horizontal = Spacing.screenHorizontal,
                    vertical   = Spacing.xs
                )
            )
        }

        item { Spacer(modifier = Modifier.height(Spacing.md)) }
    }
}
