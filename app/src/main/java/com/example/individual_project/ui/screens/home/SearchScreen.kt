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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.individual_project.ui.components.EmptyState
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.EventCard
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.components.TmSearchBar
import com.example.individual_project.ui.model.toUiModel
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.HomeViewModel

private val popularCategories = listOf(
    "🎵" to "Concerts", "🏆" to "Sports", "🎭" to "Theater",
    "😂" to "Comedy",   "👨‍👩‍👧" to "Family", "🎪" to "Festivals"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onEventClick : (String) -> Unit = {},
    viewModel    : HomeViewModel    = hiltViewModel()
) {
    val searchQuery   by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val resultList = searchResults.data?.map { it.toUiModel() } ?: emptyList()

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
                TmSearchBar(
                    value         = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) }
                )
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
                // ── Discovery state ──────────────────────────────────────
                Spacer(modifier = Modifier.height(Spacing.md))
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
                                onClick = { viewModel.onSearchQueryChange(name) }
                            ) {
                                Column(
                                    modifier            = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
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
                // ── Search results ────────────────────────────────────────
                Spacer(modifier = Modifier.height(Spacing.md))

                when {
                    searchResults.isLoading -> LoadingView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                    searchResults.hasError  -> ErrorView(
                        message = searchResults.error ?: "Search failed",
                        onRetry = { viewModel.onSearchQueryChange(searchQuery) }
                    )
                    resultList.isEmpty()    -> EmptyState(
                        emoji    = "🔍",
                        title    = "No events found",
                        subtitle = "Try a different keyword or browse categories"
                    )
                    else -> {
                        Text(
                            text  = "${resultList.size} result${if (resultList.size == 1) "" else "s"} for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        resultList.forEach { event ->
                            EventCard(
                                event    = event,
                                onClick  = { onEventClick(event.id) },
                                modifier = Modifier.padding(vertical = Spacing.xs)
                            )
                        }
                    }
                }
            }
        }
    }
}
