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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.individual_project.data.model.Event
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.EventCard
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.components.SectionHeader
import com.example.individual_project.ui.model.categoryEmoji
import com.example.individual_project.ui.model.categoryGradientColors
import com.example.individual_project.ui.model.toUiModel
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.ui.theme.TmWarning
import com.example.individual_project.ui.viewmodel.EventDetailViewModel
import com.example.individual_project.utils.DateFormatter
import com.example.individual_project.utils.PriceFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController : NavController,
    viewModel     : EventDetailViewModel = hiltViewModel()
) {
    val eventState         by viewModel.eventState.collectAsState()
    val relatedEventsState by viewModel.relatedEventsState.collectAsState()
    val isFavorite         by viewModel.isFavorite.collectAsState()
    val favoriteLoading    by viewModel.favoriteLoading.collectAsState()
    val relatedFavoriteIds by viewModel.relatedFavoriteIds.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
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
                BookingBar(
                    event     = event,
                    onBookNow = {
                        navController.navigate(
                            com.example.individual_project.ui.navigation.Screen.Booking.createRoute(event.id)
                        )
                    }
                )
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
                eventState.data != null -> EventDetailContent(
                    event                  = eventState.data!!,
                    relatedEvents          = relatedEventsState.data ?: emptyList(),
                    isFavorite             = isFavorite,
                    favoriteLoading        = favoriteLoading,
                    relatedFavoriteIds     = relatedFavoriteIds,
                    onEventClick           = { id ->
                        navController.navigate(Screen.EventDetail.createRoute(id))
                    },
                    onFavoriteClick        = { viewModel.toggleFavorite() },
                    onRelatedFavoriteClick = { id -> viewModel.toggleRelatedFavorite(id) }
                )
            }
        }
    }
}

// ─── Main scrollable content ──────────────────────────────────────────────────

@Composable
private fun EventDetailContent(
    event                  : Event,
    relatedEvents          : List<Event>,
    isFavorite             : Boolean,
    favoriteLoading        : Boolean,
    relatedFavoriteIds     : Set<String>,
    onEventClick           : (String) -> Unit,
    onFavoriteClick        : () -> Unit,
    onRelatedFavoriteClick : (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {

        // ── Banner ──────────────────────────────────────────────────────────
        item {
            EventBanner(
                event           = event,
                isFavorite      = isFavorite,
                favoriteLoading = favoriteLoading,
                onFavoriteClick = onFavoriteClick
            )
        }

        // ── Title + Organiser ──────────────────────────────────────────────
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

        // ── Event meta rows ────────────────────────────────────────────────
        item {
            val seatsColor = when {
                event.availableSeats <= 0  -> TmError
                event.availableSeats <= 10 -> TmWarning
                else                       -> TmSuccess
            }
            val seatsLabel = when {
                event.availableSeats <= 0  -> "Sold Out"
                event.availableSeats <= 10 -> "Only ${event.availableSeats} seats left!"
                else                       -> "${event.availableSeats} seats available"
            }
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
                    value = DateFormatter.formatDate(event.date)
                )
                EventMetaRow(
                    icon  = Icons.Default.AccessTime,
                    label = "Time",
                    value = DateFormatter.formatTime(event.time)
                )
                EventMetaRow(
                    icon  = Icons.Default.LocationOn,
                    label = "Venue",
                    value = "${event.venue}, ${event.city}"
                )
                EventMetaRow(
                    icon       = Icons.Default.ConfirmationNumber,
                    label      = "Availability",
                    value      = seatsLabel,
                    valueColor = seatsColor
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

        // ── Related Events ─────────────────────────────────────────────────
        if (relatedEvents.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    SectionHeader(
                        title       = "You May Also Like",
                        actionLabel = null,
                        modifier    = Modifier.padding(
                            horizontal = Spacing.screenHorizontal,
                            vertical   = Spacing.md
                        )
                    )
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = Spacing.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(relatedEvents, key = { it.id }) { related ->
                            EventCard(
                                event           = related.toUiModel().copy(isLiked = related.id in relatedFavoriteIds),
                                onClick         = { onEventClick(related.id) },
                                onFavoriteClick = { onRelatedFavoriteClick(related.id) },
                                modifier        = Modifier.width(288.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.lg))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.md)) }
    }
}

// ─── Banner ───────────────────────────────────────────────────────────────────

@Composable
private fun EventBanner(
    event           : Event,
    isFavorite      : Boolean,
    favoriteLoading : Boolean,
    onFavoriteClick : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        if (event.imageUrl.isNotBlank()) {
            // Real image with crossfade
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.imageUrl)
                    .crossfade(400)
                    .build(),
                contentDescription = event.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
            // Readability overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.6f))
                        )
                    )
            )
            // Category pill — bottom center
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Spacing.lg)
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
        } else {
            // Emoji gradient fallback
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(categoryGradientColors(event.category))),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = categoryEmoji(event.category), fontSize = 80.sp)
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

        // ── SOLD OUT badge — top start ─────────────────────────────────────
        if (event.availableSeats <= 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Spacing.md)
                    .clip(MaterialTheme.shapes.small)
                    .background(TmError)
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs)
            ) {
                Text(
                    text       = "SOLD OUT",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
            }
        }

        // ── Favorite button — top end ──────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(Spacing.md)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(enabled = !favoriteLoading, onClick = onFavoriteClick),
            contentAlignment = Alignment.Center
        ) {
            if (favoriteLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    color       = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector        = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint               = if (isFavorite) TmError else Color.White,
                    modifier           = Modifier.size(Spacing.iconMd)
                )
            }
        }
    }
}

// ─── Meta row ─────────────────────────────────────────────────────────────────

@Composable
private fun EventMetaRow(
    icon       : ImageVector,
    label      : String,
    value      : String,
    valueColor : Color = Color.Unspecified
) {
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
                text       = value,
                style      = MaterialTheme.typography.bodyMedium,
                color      = if (valueColor == Color.Unspecified)
                                 MaterialTheme.colorScheme.onSurface
                             else
                                 valueColor,
                fontWeight = if (valueColor != Color.Unspecified) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

// ─── Sticky Book Now bar ──────────────────────────────────────────────────────

@Composable
private fun BookingBar(event: Event, onBookNow: () -> Unit) {
    Surface(
        shadowElevation = 8.dp,
        color           = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text  = "Price",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = PriceFormatter.formatFrom(event.price),
                    style      = MaterialTheme.typography.titleLarge,
                    color      = TmGold,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick  = onBookNow,
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
