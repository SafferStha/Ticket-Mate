package com.example.individual_project.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.individual_project.data.model.Event
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.model.categoryGradientColors
import com.example.individual_project.ui.model.categoryEmoji
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.ui.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController : NavController,
    viewModel     : EventViewModel = hiltViewModel()
) {
    val eventState by viewModel.eventState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = TmNavyBlue,
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            eventState.data?.let { event ->
                BookingBar(event = event)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                eventState.isLoading -> LoadingView()
                eventState.hasError  -> ErrorView(
                    message = eventState.error ?: "Failed to load event",
                    onRetry = { viewModel.loadEvent() }
                )
                eventState.data != null -> EventDetailContent(event = eventState.data!!)
            }
        }
    }
}

// ─── Banner + Info content ─────────────────────────────────────────────────────

@Composable
private fun EventDetailContent(event: Event) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // ── Banner ──────────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(categoryGradientColors(event.category))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text     = categoryEmoji(event.category),
                        fontSize = 80.sp
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    ) {
                        Text(
                            text  = event.category.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ── Title + organiser ──────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(Spacing.screenHorizontal)
            ) {
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text       = event.title,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                if (event.organizer.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person, null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Spacing.iconSm)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text  = "Organized by ${event.organizer}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.md))
            }
        }

        // ── Event meta chips (date / time / venue / city / seats) ──────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = Spacing.screenHorizontal)
            ) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                EventMetaRow(
                    icon  = Icons.Default.CalendarToday,
                    label = "Date",
                    value = event.date
                )
                EventMetaRow(
                    icon  = Icons.Default.AccessTime,
                    label = "Time",
                    value = event.time
                )
                EventMetaRow(
                    icon  = Icons.Default.LocationOn,
                    label = "Venue",
                    value = "${event.venue}, ${event.city}"
                )
                EventMetaRow(
                    icon  = Icons.Default.ConfirmationNumber,
                    label = "Seats available",
                    value = if (event.availableSeats > 0)
                                "${event.availableSeats} seats left"
                            else "Sold out"
                )
                Spacer(modifier = Modifier.height(Spacing.md))
            }
        }

        // ── Description ────────────────────────────────────────────────────
        if (event.description.isNotBlank()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(Spacing.screenHorizontal)
                ) {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text       = "About This Event",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text  = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Spacing.xl))
                }
            }
        }
    }
}

// ─── Meta row helper ──────────────────────────────────────────────────────────

@Composable
private fun EventMetaRow(icon: ImageVector, label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Spacing.thumbnailSize - Spacing.lg)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Spacing.iconMd)
            )
        }
        Spacer(modifier = Modifier.width(Spacing.md))
        Column {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

// ─── Sticky Book Now bar ──────────────────────────────────────────────────────

@Composable
private fun BookingBar(event: Event) {
    Surface(
        shadowElevation = 8.dp,
        color           = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text  = "Price",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = if (event.price <= 0.0) "Free" else "From \$${event.price.toInt()}",
                    style      = MaterialTheme.typography.titleLarge,
                    color      = TmGold,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick  = { /* Phase 4: navigate to BookingScreen */ },
                enabled  = event.availableSeats > 0,
                modifier = Modifier.height(52.dp),
                shape    = MaterialTheme.shapes.medium,
                colors   = ButtonDefaults.buttonColors(containerColor = TmBlue)
            ) {
                Icon(
                    Icons.Default.ConfirmationNumber, null,
                    modifier = Modifier.size(Spacing.iconMd)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text  = if (event.availableSeats > 0) "Book Now" else "Sold Out",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
