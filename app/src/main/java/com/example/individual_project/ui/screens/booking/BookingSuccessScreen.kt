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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.individual_project.data.model.Booking
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.ui.model.UiState
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.utils.PriceFormatter
import com.example.individual_project.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Lightweight ViewModel scoped to this screen only ─────────────────────────

@HiltViewModel
class BookingSuccessViewModel @Inject constructor(
    private val bookingRepository : BookingRepository,
    savedStateHandle              : SavedStateHandle
) : ViewModel() {

    private val bookingId : String = savedStateHandle.get<String>("bookingId") ?: ""

    private val _state = MutableStateFlow(UiState<Booking>())
    val state: StateFlow<UiState<Booking>> = _state.asStateFlow()

    init {
        if (bookingId.isNotBlank()) loadBooking()
    }

    fun loadBooking() {
        viewModelScope.launch {
            _state.value = UiState(isLoading = true)
            _state.value = when (val r = bookingRepository.getBookingById(bookingId)) {
                is Resource.Success -> UiState(data = r.data)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Failed to load booking")
            }
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun BookingSuccessScreen(
    navController : NavController,
    viewModel     : BookingSuccessViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                state.data != null -> {
                    val booking = state.data!!
                    Column(
                        modifier            = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(Spacing.screenHorizontal),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(Spacing.xxl))

                        // ── Success icon ───────────────────────────────────────────
                        Box(
                            modifier         = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(TmSuccess.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint               = TmSuccess,
                                modifier           = Modifier.size(64.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.lg))
                        Text(
                            text       = "Booking Confirmed!",
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign  = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text      = "Your tickets are confirmed. Enjoy the event!",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(Spacing.xl))

                        // ── Booking ID chip ────────────────────────────────────────
                        Surface(
                            shape         = MaterialTheme.shapes.medium,
                            color         = TmBlue.copy(alpha = 0.1f),
                            tonalElevation = 0.dp
                        ) {
                            Row(
                                modifier          = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ConfirmationNumber, null,
                                    tint     = TmBlue,
                                    modifier = Modifier.size(Spacing.iconMd)
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Column {
                                    Text(
                                        text  = "Booking ID",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text       = booking.id,
                                        style      = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color      = TmBlue
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xl))

                        // ── Booking summary card ───────────────────────────────────
                        Surface(
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = MaterialTheme.shapes.large,
                            color         = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(Spacing.md)) {
                                Text(
                                    text       = booking.eventTitle,
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(Spacing.sm))
                                if (booking.venue.isNotBlank()) {
                                    SuccessDetailRow("Venue", booking.venue)
                                }
                                if (booking.date.isNotBlank()) {
                                    SuccessDetailRow("Date", booking.date)
                                }
                                SuccessDetailRow("Quantity", "${booking.quantity} ticket${if (booking.quantity > 1) "s" else ""}")
                                SuccessDetailRow("Per ticket", PriceFormatter.format(booking.pricePerTicket))
                                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))
                                SuccessDetailRow(
                                    label      = "Total Paid",
                                    value      = PriceFormatter.format(booking.totalPrice),
                                    valueColor = TmGold,
                                    bold       = true
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xxl))

                        // ── CTAs ──────────────────────────────────────────────────
                        Button(
                            onClick  = {
                                navController.navigate(Screen.MyBookings.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = false }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Spacing.buttonHeight),
                            shape    = MaterialTheme.shapes.medium,
                            colors   = ButtonDefaults.buttonColors(containerColor = TmBlue)
                        ) {
                            Text("View My Tickets", style = MaterialTheme.typography.labelLarge)
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        OutlinedButton(
                            onClick  = {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Spacing.buttonHeight),
                            shape    = MaterialTheme.shapes.medium
                        ) {
                            Text("Back to Home", style = MaterialTheme.typography.labelLarge)
                        }

                        Spacer(modifier = Modifier.height(Spacing.xl))
                    }
                }
                state.hasError -> Column(
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(Spacing.screenHorizontal),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text      = state.error ?: "Failed to load booking",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Button(onClick = { viewModel.loadBooking() }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessDetailRow(
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
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodySmall,
            color      = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
