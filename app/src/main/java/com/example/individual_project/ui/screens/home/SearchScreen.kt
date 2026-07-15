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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.individual_project.ui.components.EmptyState
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.EventCard
import com.example.individual_project.ui.components.FilterBottomSheet
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.components.TmSearchBar
import com.example.individual_project.ui.model.toUiModel
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.SearchViewModel

private val popularCategories = listOf(
    "🎵" to "Concerts", "🏆" to "Sports", "🎭" to "Theater",
    "😂" to "Comedy",   "👨‍👩‍👧" to "Family", "🎪" to "Festivals"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onEventClick : (String) -> Unit = {},
    viewModel    : SearchViewModel  = hiltViewModel()
) {
    val query            by viewModel.query.collectAsState()
    val searchResults    by viewModel.searchResults.collectAsState()
    val searchHistory    by viewModel.searchHistory.collectAsState()
    val trendingKeywords by viewModel.trendingKeywords.collectAsState()
    val filterState      by viewModel.filterState.collectAsState()
    val isFilterVisible  by viewModel.isFilterSheetVisible.collectAsState()
    val favoriteIds      by viewModel.favoriteIds.collectAsState()

    val resultList = searchResults.data?.map { it.toUiModel().copy(isLiked = it.id in favoriteIds) } ?: emptyList()

    if (isFilterVisible) {
        FilterBottomSheet(
            currentFilter = filterState,
            onApply       = viewModel::applyFilter,
            onDismiss     = viewModel::hideFilter
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header: gradient + title + search bar + filter button ─────────────
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
                    text  = "Search Events",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    TmSearchBar(
                        value         = query,
                        onValueChange = viewModel::onQueryChange,
                        onSearch      = { viewModel.submitSearch() },
                        modifier      = Modifier.weight(1f)
                    )
                    BadgedBox(badge = { if (filterState.isActive) Badge() }) {
                        IconButton(onClick = viewModel::showFilter) {
                            Icon(
                                imageVector        = Icons.Default.Tune,
                                contentDescription = "Filters",
                                tint               = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ── Content ───────────────────────────────────────────────────────────
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Spacing.xl)
        ) {
            if (query.isEmpty()) {

                // ── Discovery state ──────────────────────────────────────────

                // Recent searches
                if (searchHistory.isNotEmpty()) {
                    item {
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text       = "Recent Searches",
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = viewModel::clearHistory) {
                                Text(
                                    text  = "Clear All",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    items(searchHistory.take(5), key = { it.id }) { historyItem ->
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectHistoryItem(historyItem.query) }
                                .padding(
                                    horizontal = Spacing.screenHorizontal,
                                    vertical   = Spacing.sm
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Default.History,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(Spacing.iconMd)
                            )
                            Spacer(modifier = Modifier.width(Spacing.md))
                            Text(
                                text     = historyItem.query,
                                style    = MaterialTheme.typography.bodyMedium,
                                color    = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector        = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(Spacing.iconMd)
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(start = Spacing.screenHorizontal + Spacing.iconMd + Spacing.md),
                            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }

                    item { Spacer(modifier = Modifier.height(Spacing.sm)) }
                }

                // Trending keywords
                if (trendingKeywords.isNotEmpty()) {
                    item {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(Spacing.iconMd)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text       = "Trending Now",
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    item {
                        LazyRow(
                            contentPadding        = PaddingValues(horizontal = Spacing.screenHorizontal),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            modifier              = Modifier.padding(bottom = Spacing.sm)
                        ) {
                            items(trendingKeywords) { keyword ->
                                SuggestionChip(
                                    onClick = { viewModel.submitSearch(keyword) },
                                    label   = {
                                        Text(
                                            text  = keyword,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Popular categories
                item {
                    Text(
                        text       = "Popular Categories",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.padding(
                            horizontal = Spacing.screenHorizontal,
                            vertical   = Spacing.sm
                        )
                    )
                }
                item {
                    Column(
                        modifier            = Modifier.padding(horizontal = Spacing.screenHorizontal),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
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
                                        onClick   = { viewModel.submitSearch(name) }
                                    ) {
                                        Column(
                                            modifier            = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text  = emoji,
                                                style = MaterialTheme.typography.headlineSmall
                                            )
                                            Text(
                                                text  = name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(Spacing.md)) }

            } else {

                // ── Search results state ─────────────────────────────────────

                // Active filter indicator
                if (filterState.isActive) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs)
                        ) {
                            InputChip(
                                selected     = true,
                                onClick      = viewModel::clearFilter,
                                label        = {
                                    Text(
                                        text  = "Filters active",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector        = Icons.Default.Close,
                                        contentDescription = "Clear filters",
                                        modifier           = Modifier.size(Spacing.iconSm)
                                    )
                                }
                            )
                        }
                    }
                }

                when {
                    searchResults.isLoading -> item {
                        LoadingView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        )
                    }
                    searchResults.hasError  -> item {
                        ErrorView(
                            message = searchResults.error ?: "Search failed",
                            onRetry = { viewModel.submitSearch(query) }
                        )
                    }
                    resultList.isEmpty()    -> item {
                        EmptyState(
                            emoji    = "🔍",
                            title    = "No events found",
                            subtitle = "Try a different keyword or adjust your filters"
                        )
                    }
                    else -> {
                        item {
                            Text(
                                text     = "${resultList.size} result${if (resultList.size == 1) "" else "s"} for \"$query\"",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    horizontal = Spacing.screenHorizontal,
                                    vertical   = Spacing.sm
                                )
                            )
                        }
                        items(resultList, key = { it.id }) { event ->
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
                }
            }
        }
    }
}
