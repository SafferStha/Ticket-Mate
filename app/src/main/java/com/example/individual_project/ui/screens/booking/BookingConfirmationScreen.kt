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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.BookingViewModel
import com.example.individual_project.utils.DateFormatter
import com.example.individual_project.utils.PriceFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreen(
    navController : NavController,
    eventId       : String,
    quantity      : Int,
    viewModel     : BookingViewModel
) {
    val uiState      by viewModel.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    // Navigate to Checkout once the PENDING booking is created
    LaunchedEffect(uiState.bookingId) {
        uiState.bookingId?.let { bookingId ->
            navController.navigate(Screen.Checkout.createRoute(bookingId)) {
                // Pop BookingScreen + ConfirmationScreen; EventDetail stays on stack
                popUpTo(Screen.Booking.route) { inclusive = true }
            }
            viewModel.resetBookingResult()
        }
    }

    // Show error in snackbar
    LaunchedEffect(uiState.bookingError) {
        uiState.bookingError?.let { error ->
            snackbarHost.showSnackbar(error)
            viewModel.clearBookingError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Booking") },
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
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = {
            uiState.event?.let {
                Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md)
                    ) {
                        Button(
                            onClick  = { viewModel.confirmBooking() },
                            enabled  = !uiState.isBooking,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Spacing.buttonHeight),
                            shape    = MaterialTheme.shapes.medium,
                            colors   = ButtonDefaults.buttonColors(containerColor = TmBlue)
                        ) {
                            if (uiState.isBooking) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.padding(end = Spacing.sm),
                                    color       = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Text("Processing…", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Text("Proceed to Checkout", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        OutlinedButton(
                            onClick  = { navController.popBackStack() },
                            enabled  = !uiState.isBooking,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Spacing.buttonHeight),
                            shape    = MaterialTheme.shapes.medium
                        ) {
                            Text("Back", style = MaterialTheme.typography.labelLarge)
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
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // ── Section: Event details ──────────────────────────────────
                        SectionCard(title = "Event Details") {
                            DetailRow(label = "Event",  value = event.title)
                            DetailRow(label = "Venue",  value = "${event.venue}, ${event.city}")
                            DetailRow(label = "Date",   value = DateFormatter.formatDate(event.date))
                            DetailRow(label = "Time",   value = DateFormatter.formatTime(event.time))
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        // ── Section: Ticket details ─────────────────────────────────
                        SectionCard(title = "Ticket Details") {
                            DetailRow(label = "Quantity",        value = "$quantity ticket${if (quantity > 1) "s" else ""}")
                            DetailRow(label = "Price per ticket", value = PriceFormatter.format(event.price))
                            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))
                            DetailRow(
                                label      = "Total",
                                value      = PriceFormatter.format(event.price * quantity),
                                valueColor = TmGold,
                                bold       = true
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.xl))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier      = Modifier.fillMaxWidth(),
        color         = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(Spacing.screenHorizontal)) {
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            content()
            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun DetailRow(
    label      : String,
    value      : String,
    valueColor : Color   = Color.Unspecified,
    bold       : Boolean = false
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
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
