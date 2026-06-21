package com.example.individual_project.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.individual_project.ui.components.EmptyState
import com.example.individual_project.ui.components.EventCard
import com.example.individual_project.ui.components.TmSearchBar
import com.example.individual_project.ui.model.EventUiModel
import com.example.individual_project.ui.model.sampleEvents
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmNavyBlue

private val recentSearches  = listOf("Taylor Swift", "NBA Finals", "Coachella", "Hamilton – Musical")
private val popularCategories = listOf(
    "🎵" to "Music", "⚽" to "Sports", "🎭" to "Arts",
    "😂" to "Comedy", "👨‍👩‍👧" to "Family", "🎪" to "Theater"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchQuery : String,
    onQueryChange : (String) -> Unit,
    events      : List<EventUiModel> = sampleEvents
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header + search bar ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(TmNavyBlue, TmDarkBlue)))
                .padding(
                    top    = Spacing.headerPaddingTop,
                    start  = Spacing.screenHorizontal,
                    end    = Spacing.screenHorizontal,
                    bottom = Spacing.md
                )
        ) {
            Column {
                Text(
                    text  = "Discover Events",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                TmSearchBar(value = searchQuery, onValueChange = onQueryChange)
            }
        }

        // ── Results or discovery ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.screenHorizontal)
        ) {
            if (searchQuery.isEmpty()) {
                // Recent searches
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text  = "Recent Searches",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                recentSearches.forEach { term ->
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .clickable { onQueryChange(term) }
                            .padding(vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.History, null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(Spacing.iconMd)
                        )
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Text(
                            text     = term,
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.ChevronRight, null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(Spacing.iconMd)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Popular categories grid (3-column)
                Text(
                    text  = "Popular Categories",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                popularCategories.chunked(3).forEach { row ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        row.forEach { (emoji, name) ->
                            Card(
                                modifier  = Modifier
                                    .weight(1f)
                                    .height(Spacing.thumbnailSize + Spacing.sm),
                                shape     = MaterialTheme.shapes.large,
                                elevation = CardDefaults.cardElevation(2.dp),
                                colors    = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                onClick   = { onQueryChange(name) }
                            ) {
                                Column(
                                    modifier              = Modifier.fillMaxSize(),
                                    horizontalAlignment   = Alignment.CenterHorizontally,
                                    verticalArrangement   = Arrangement.Center
                                ) {
                                    Text(emoji, style = MaterialTheme.typography.headlineSmall)
                                    Text(
                                        text  = name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }
            } else {
                val results = events.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.venue.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true)
                }

                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text  = "${results.size} result${if (results.size == 1) "" else "s"} for \"$searchQuery\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Spacing.md))

                if (results.isEmpty()) {
                    EmptyState(
                        emoji    = "🔍",
                        title    = "No events found",
                        subtitle = "Try a different keyword or browse categories"
                    )
                } else {
                    results.forEach { event ->
                        EventCard(
                            event    = event,
                            modifier = Modifier.padding(vertical = Spacing.xs)
                        )
                    }
                }
            }
        }
    }
}

