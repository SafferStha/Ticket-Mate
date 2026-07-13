package com.example.individual_project.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.individual_project.ui.model.FilterState
import com.example.individual_project.ui.theme.Spacing

private val filterCategories = listOf(
    "Concerts", "Sports", "Theater", "Comedy", "Family", "Festivals"
)

private val filterCities = listOf(
    "Kathmandu", "Pokhara", "Lalitpur", "Butwal", "Biratnagar"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilter : FilterState,
    onApply       : (FilterState) -> Unit,
    onDismiss     : () -> Unit
) {
    var selectedCategories by remember { mutableStateOf(currentFilter.selectedCategories) }
    var selectedCity       by remember { mutableStateOf(currentFilter.selectedCity) }
    var priceRange         by remember {
        mutableStateOf(currentFilter.minPrice.toFloat()..currentFilter.maxPrice.toFloat())
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xxl)
        ) {
            // ── Title + Reset ──────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Filter Events",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    selectedCategories = emptySet()
                    selectedCity       = ""
                    priceRange         = 0f..50_000f
                }) {
                    Text("Reset All")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(Spacing.md))

            // ── Category ──────────────────────────────────────────────────────
            Text(
                text       = "Category",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            // Two rows of 3 chips each (avoids nested LazyRow/scroll conflict)
            filterCategories.chunked(3).forEach { rowItems ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    rowItems.forEach { cat ->
                        FilterChip(
                            selected = cat in selectedCategories,
                            onClick  = {
                                selectedCategories = if (cat in selectedCategories)
                                    selectedCategories - cat
                                else
                                    selectedCategories + cat
                            },
                            label    = { Text(cat, style = MaterialTheme.typography.labelMedium) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
                Spacer(modifier = Modifier.height(Spacing.xs))
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // ── City ──────────────────────────────────────────────────────────
            Text(
                text       = "City",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                filterCities.take(3).forEach { city ->
                    FilterChip(
                        selected = selectedCity == city,
                        onClick  = { selectedCity = if (selectedCity == city) "" else city },
                        label    = { Text(city, style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                filterCities.drop(3).forEach { city ->
                    FilterChip(
                        selected = selectedCity == city,
                        onClick  = { selectedCity = if (selectedCity == city) "" else city },
                        label    = { Text(city, style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat((3 - (filterCities.size - 3)).coerceAtLeast(0)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // ── Price Range ───────────────────────────────────────────────────
            Text(
                text       = "Price Range",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text  = "NPR ${priceRange.start.toInt()} – NPR ${priceRange.endInclusive.toInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            RangeSlider(
                value         = priceRange,
                onValueChange = { priceRange = it },
                valueRange    = 0f..50_000f,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // ── Apply ─────────────────────────────────────────────────────────
            Button(
                onClick  = {
                    onApply(
                        FilterState(
                            selectedCategories = selectedCategories,
                            selectedCity       = selectedCity,
                            minPrice           = priceRange.start.toDouble(),
                            maxPrice           = priceRange.endInclusive.toDouble()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filters")
            }
        }
    }
}
