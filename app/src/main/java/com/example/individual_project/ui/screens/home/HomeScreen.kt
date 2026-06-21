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
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.individual_project.ui.components.CategoryChip
import com.example.individual_project.ui.components.EmptyState
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.EventCard
import com.example.individual_project.ui.components.FeaturedEventCard
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.components.ProfileAvatar
import com.example.individual_project.ui.components.SectionHeader
import com.example.individual_project.ui.components.TmSearchBar
import com.example.individual_project.ui.model.toUiModel
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmLightBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onEventClick : (String) -> Unit = {},
    viewModel    : HomeViewModel    = hiltViewModel()
) {
    val featuredState    by viewModel.featuredState.collectAsState()
    val eventsState      by viewModel.eventsState.collectAsState()
    val categories       by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val featuredList = featuredState.data?.map { it.toUiModel() } ?: emptyList()
    val eventsList   = eventsState.data?.map { it.toUiModel() }   ?: emptyList()

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
                        top    = Spacing.headerPaddingTop,
                        start  = Spacing.screenHorizontal,
                        end    = Spacing.screenHorizontal,
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
                            text  = "Discover Events 🎫",
                            style = MaterialTheme.typography.bodySmall,
                            color = TmLightBlue
                        )
                        Text(
                            text       = "TicketMate",
                            style      = MaterialTheme.typography.titleLarge,
                            color      = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Notifications, null,
                                tint     = Color.White,
                                modifier = Modifier.size(Spacing.iconMd)
                            )
                        }
                        ProfileAvatar(initials = "TM", size = Spacing.avatarSm)
                    }
                }
            }
        }

        // ── Search bar ────────────────────────────────────────────────────
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
                when {
                    featuredState.isLoading -> LoadingView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                    featuredState.hasError  -> ErrorView(
                        message  = featuredState.error ?: "Failed to load featured events",
                        onRetry  = { viewModel.refresh() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                    featuredList.isEmpty()  -> EmptyState(
                        emoji    = "🌟",
                        title    = "No featured events",
                        subtitle = "Check back soon for featured events"
                    )
                    else -> LazyRow(
                        contentPadding        = PaddingValues(horizontal = Spacing.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(featuredList, key = { it.id }) { event ->
                            FeaturedEventCard(
                                event   = event,
                                onClick = { onEventClick(event.id) }
                            )
                        }
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
                    items(categories) { cat ->
                        CategoryChip(
                            label      = cat,
                            isSelected = selectedCategory == cat,
                            onClick    = { viewModel.selectCategory(cat) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.xs))
            }
        }

        // ── Trending Events header ─────────────────────────────────────────
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

        // ── Events list (loading / error / empty / data) ───────────────────
        when {
            eventsState.isLoading -> item {
                LoadingView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            }
            eventsState.hasError  -> item {
                ErrorView(
                    message  = eventsState.error ?: "Failed to load events",
                    onRetry  = { viewModel.refresh() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            }
            eventsList.isEmpty()  -> item {
                EmptyState(
                    emoji    = "🎭",
                    title    = "No events found",
                    subtitle = "There are no events in this category yet"
                )
            }
            else -> items(eventsList, key = { it.id }) { event ->
                EventCard(
                    event    = event,
                    onClick  = { onEventClick(event.id) },
                    modifier = Modifier.padding(
                        horizontal = Spacing.screenHorizontal,
                        vertical   = Spacing.xs
                    )
                )
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.md)) }
    }
}
