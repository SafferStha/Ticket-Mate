package com.example.individual_project.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.individual_project.ui.model.toUiModel
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmLightBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onEventClick  : (String) -> Unit = {},
    onSearchClick : () -> Unit       = {},
    viewModel     : HomeViewModel    = hiltViewModel()
) {
    val featuredState    by viewModel.featuredState.collectAsState()
    val trendingState    by viewModel.trendingState.collectAsState()
    val recommendedState by viewModel.recommendedState.collectAsState()
    val nearbyState      by viewModel.nearbyState.collectAsState()
    val eventsState      by viewModel.eventsState.collectAsState()
    val categories       by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedCity     by viewModel.selectedCity.collectAsState()
    val favoriteIds      by viewModel.favoriteIds.collectAsState()

    fun List<com.example.individual_project.data.model.Event>.toUiModels() =
        map { it.toUiModel().copy(isLiked = it.id in favoriteIds) }

    val featuredList    = featuredState.data?.toUiModels()    ?: emptyList()
    val trendingList    = trendingState.data?.toUiModels()    ?: emptyList()
    val recommendedList = recommendedState.data?.toUiModels() ?: emptyList()
    val nearbyList      = nearbyState.data?.toUiModels()      ?: emptyList()
    val eventsList      = eventsState.data?.toUiModels()      ?: emptyList()

    val eventsHeader = if (selectedCategory == "All") "All Events" else selectedCategory

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // ── Header ────────────────────────────────────────────────────────────
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
                    ProfileAvatar(initials = "TM", size = Spacing.avatarSm)
                }
            }
        }

        // ── Search bar: tappable placeholder → navigates to Search tab ────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TmNavyBlue)
                    .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = MaterialTheme.shapes.medium,
                    color    = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(Spacing.iconMd)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text  = "Search events, artists, venues…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Transparent overlay intercepts all taps — navigates to Search tab
                Box(modifier = Modifier.matchParentSize().clickable { onSearchClick() })
            }
        }

        // ── Featured Events ───────────────────────────────────────────────────
        item {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                SectionHeader(
                    title         = "🌟 Featured Events",
                    modifier      = Modifier.padding(
                        horizontal = Spacing.screenHorizontal,
                        vertical   = Spacing.md
                    )
                )
                when {
                    featuredState.isLoading -> LoadingView(
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                    featuredState.hasError  -> ErrorView(
                        message  = featuredState.error ?: "Failed to load featured events",
                        onRetry  = { viewModel.refresh() },
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                    featuredList.isEmpty()  -> EmptyState(
                        emoji    = "🌟",
                        title    = "No featured events",
                        subtitle = "Check back soon for featured events"
                    )
                    else                    -> LazyRow(
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

        // ── Browse Categories ─────────────────────────────────────────────────
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

        // ── Trending Now ──────────────────────────────────────────────────────
        if (trendingState.isLoading || trendingList.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = Spacing.md)) {
                    SectionHeader(
                        title         = "🔥 Trending Now",
                            modifier      = Modifier.padding(
                            horizontal = Spacing.screenHorizontal,
                            vertical   = Spacing.sm
                        )
                    )
                    if (trendingState.isLoading) {
                        LoadingView(modifier = Modifier.fillMaxWidth().height(180.dp))
                    } else {
                        LazyRow(
                            contentPadding        = PaddingValues(horizontal = Spacing.screenHorizontal),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            items(trendingList, key = { it.id }) { event ->
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
        }

        // ── Recommended for You ───────────────────────────────────────────────
        if (recommendedList.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = Spacing.sm)) {
                    SectionHeader(
                        title         = "✨ Recommended for You",
                            modifier      = Modifier.padding(
                            horizontal = Spacing.screenHorizontal,
                            vertical   = Spacing.sm
                        )
                    )
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = Spacing.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(recommendedList, key = { it.id }) { event ->
                            EventCard(
                                event           = event,
                                onClick         = { onEventClick(event.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(event.id) },
                                modifier        = Modifier.width(300.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }
            }
        }

        // ── Explore by City ───────────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.md)) {
                SectionHeader(
                    title    = "📍 Explore by City",
                    modifier = Modifier.padding(
                        horizontal = Spacing.screenHorizontal,
                        vertical   = Spacing.sm
                    )
                )
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = Spacing.screenHorizontal),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(viewModel.availableCities) { city ->
                        CategoryChip(
                            label      = city,
                            isSelected = selectedCity == city,
                            onClick    = { viewModel.selectCity(city) }
                        )
                    }
                }

                if (selectedCity.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    when {
                        nearbyState.isLoading -> LoadingView(
                            modifier = Modifier.fillMaxWidth().height(140.dp)
                        )
                        nearbyList.isEmpty()  -> EmptyState(
                            emoji    = "📍",
                            title    = "No events in $selectedCity",
                            subtitle = "Check back soon for local events"
                        )
                        else                  -> LazyRow(
                            contentPadding        = PaddingValues(horizontal = Spacing.screenHorizontal),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            items(nearbyList, key = { it.id }) { event ->
                                EventCard(
                                    event           = event,
                                    onClick         = { onEventClick(event.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(event.id) },
                                    modifier        = Modifier.width(300.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── All / Category Events ─────────────────────────────────────────────
        item {
            SectionHeader(
                title         = eventsHeader,
                actionLabel   = null,
                modifier      = Modifier.padding(
                    horizontal = Spacing.screenHorizontal,
                    vertical   = Spacing.sm
                )
            )
        }

        when {
            eventsState.isLoading -> item {
                LoadingView(modifier = Modifier.fillMaxWidth().height(240.dp))
            }
            eventsState.hasError  -> item {
                ErrorView(
                    message  = eventsState.error ?: "Failed to load events",
                    onRetry  = { viewModel.refresh() },
                    modifier = Modifier.fillMaxWidth().height(240.dp)
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
                    event           = event,
                    onClick         = { onEventClick(event.id) },
                    onFavoriteClick = { viewModel.toggleFavorite(event.id) },
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
