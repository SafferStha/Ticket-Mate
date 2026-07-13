package com.example.individual_project.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.individual_project.data.model.Booking
import com.example.individual_project.data.model.BookingStatus
import com.example.individual_project.ui.components.EmptyState
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.screens.booking.MyBookingsViewModel
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.ui.theme.TmWarning
import com.example.individual_project.utils.PriceFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    navController: NavController,
    viewModel    : MyBookingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking History") },
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> LoadingView()
                state.hasError  -> ErrorView(
                    message = state.error ?: "Failed to load history",
                    onRetry = { viewModel.loadBookings() }
                )
                state.data != null -> {
                    val bookings = state.data!!
                    if (bookings.isEmpty()) {
                        EmptyState(
                            emoji       = "📋",
                            title       = "No Bookings Yet",
                            subtitle    = "Your booking history will appear here",
                            actionLabel = "Browse Events",
                            onAction    = { navController.navigate(Screen.Dashboard.route) }
                        )
                    } else {
                        LazyColumn(
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = PaddingValues(Spacing.screenHorizontal),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            item { Spacer(modifier = Modifier.height(Spacing.sm)) }
                            items(bookings, key = { it.id }) { booking ->
                                BookingHistoryCard(booking = booking)
                            }
                            item { Spacer(modifier = Modifier.height(Spacing.md)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingHistoryCard(booking: Booking) {
    val statusColor = when (booking.bookingStatus) {
        BookingStatus.CONFIRMED.name  -> TmSuccess
        BookingStatus.CANCELLED.name  -> TmError
        BookingStatus.COMPLETED.name  -> TmGold
        else                          -> TmWarning
    }

    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = MaterialTheme.shapes.large,
        color           = MaterialTheme.colorScheme.surface,
        tonalElevation  = 2.dp,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = booking.eventTitle.ifBlank { "Event" },
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.weight(1f)
                )
                StatusPill(status = booking.bookingStatus, color = statusColor)
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            if (booking.venue.isNotBlank()) {
                Text(
                    text  = booking.venue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (booking.date.isNotBlank()) {
                Text(
                    text  = booking.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "${booking.quantity} ticket${if (booking.quantity > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = PriceFormatter.format(booking.totalPrice),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = TmGold
                )
            }
        }
    }
}

@Composable
private fun StatusPill(status: String, color: Color) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text     = status,
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
        )
    }
}
