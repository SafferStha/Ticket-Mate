package com.example.individual_project.ui.screens.booking

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.ui.theme.TmWarning
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.viewmodel.BookingViewModel
import com.example.individual_project.utils.DateFormatter
import com.example.individual_project.utils.PriceFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    navController : NavController,
    viewModel     : BookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Tickets") },
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
            uiState.event?.let { event ->
                Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text  = "Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text       = PriceFormatter.format(uiState.totalPrice),
                                style      = MaterialTheme.typography.titleLarge,
                                color      = TmGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick  = {
                                navController.navigate(
                                    Screen.BookingConfirmation.createRoute(event.id, uiState.quantity)
                                )
                            },
                            enabled  = event.availableSeats > 0,
                            modifier = Modifier.height(Spacing.buttonHeight),
                            shape    = MaterialTheme.shapes.medium,
                            colors   = ButtonDefaults.buttonColors(containerColor = TmBlue)
                        ) {
                            Text(
                                text  = "Continue",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoadingEvent -> LoadingView()
                uiState.eventError != null -> ErrorView(
                    message = uiState.eventError!!,
                    onRetry = { viewModel.loadEvent() }
                )
                uiState.event != null -> {
                    val event = uiState.event!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ── Event image / banner ───────────────────────────────────────
                        if (event.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(event.imageUrl)
                                    .crossfade(300)
                                    .build(),
                                contentDescription = event.title,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }

                        // ── Event summary ──────────────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(Spacing.screenHorizontal)
                        ) {
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Text(
                                text       = event.title,
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn, null,
                                    tint     = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(Spacing.iconSm)
                                )
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Text(
                                    text  = "${event.venue}, ${event.city}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarToday, null,
                                    tint     = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(Spacing.iconSm)
                                )
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Text(
                                    text  = DateFormatter.formatDate(event.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(Spacing.md))
                        }

                        HorizontalDivider()

                        // ── Ticket price row ──────────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(Spacing.screenHorizontal)
                        ) {
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Text(
                                text       = "Ticket Price",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ConfirmationNumber, null,
                                        tint     = TmBlue,
                                        modifier = Modifier.size(Spacing.iconMd)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.sm))
                                    Text(
                                        text  = "Standard Ticket",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(
                                    text       = PriceFormatter.format(event.price),
                                    style      = MaterialTheme.typography.titleMedium,
                                    color      = TmGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(Spacing.md))
                        }

                        HorizontalDivider()

                        // ── Quantity selector ─────────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(Spacing.screenHorizontal)
                        ) {
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Text(
                                text       = "Select Quantity",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Decrease button
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (uiState.quantity > 1) TmBlue
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick  = { viewModel.decreaseQuantity() },
                                        enabled  = uiState.quantity > 1
                                    ) {
                                        Icon(
                                            Icons.Default.Remove, null,
                                            tint = Color.White
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text       = "${uiState.quantity}",
                                        style      = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text  = "ticket${if (uiState.quantity > 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Increase button
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (uiState.quantity < event.availableSeats) TmBlue
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick  = { viewModel.increaseQuantity() },
                                        enabled  = uiState.quantity < event.availableSeats
                                    ) {
                                        Icon(
                                            Icons.Default.Add, null,
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(Spacing.sm))

                            // Seat availability indicator
                            val seatsColor = when {
                                event.availableSeats <= 0  -> TmError
                                event.availableSeats <= 10 -> TmWarning
                                else                       -> TmSuccess
                            }
                            Text(
                                text  = "${event.availableSeats} seats available",
                                style = MaterialTheme.typography.bodySmall,
                                color = seatsColor,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(Spacing.lg))
                        }

                        HorizontalDivider()

                        // ── Order summary ─────────────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(Spacing.screenHorizontal)
                        ) {
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Text(
                                text       = "Order Summary",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            SummaryRow(
                                label = "${uiState.quantity} × ${PriceFormatter.format(event.price)}",
                                value = PriceFormatter.format(uiState.totalPrice)
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))
                            SummaryRow(
                                label      = "Total",
                                value      = PriceFormatter.format(uiState.totalPrice),
                                valueColor = TmGold,
                                bold       = true
                            )
                            Spacer(modifier = Modifier.height(Spacing.xl))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label      : String,
    value      : String,
    valueColor : Color  = Color.Unspecified,
    bold       : Boolean = false
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            color      = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
