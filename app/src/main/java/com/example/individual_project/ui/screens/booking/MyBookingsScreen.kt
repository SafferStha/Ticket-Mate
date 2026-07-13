package com.example.individual_project.ui.screens.booking

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.individual_project.data.model.Booking
import com.example.individual_project.data.model.BookingStatus
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.ui.components.EmptyState
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.model.UiState
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.ui.theme.TmWarning
import com.example.individual_project.utils.PriceFormatter
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class MyBookingsViewModel @Inject constructor(
    private val bookingRepository : BookingRepository,
    private val firebaseAuth      : FirebaseAuth,
    savedStateHandle              : SavedStateHandle
) : ViewModel() {

    private val userId : String get() = firebaseAuth.currentUser?.uid ?: ""

    private val _state = MutableStateFlow(UiState<List<Booking>>())
    val state: StateFlow<UiState<List<Booking>>> = _state.asStateFlow()

    init {
        loadBookings()
    }

    fun loadBookings() {
        if (userId.isBlank()) {
            _state.value = UiState(error = "Not logged in")
            return
        }
        viewModelScope.launch {
            _state.value = UiState(isLoading = true)
            _state.value = when (val r = bookingRepository.getUserBookings(userId)) {
                is Resource.Success -> UiState(data = r.data.sortedByDescending { it.bookingDate })
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Failed to load bookings")
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.cancelBooking(bookingId)
            loadBookings()
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    navController : NavController,
    viewModel     : MyBookingsViewModel = hiltViewModel()
) {
    val state    by viewModel.state.collectAsState()
    var tabIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
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
            Text(
                text  = "My Tickets",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
        }

        // ── Tabs ──────────────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor   = MaterialTheme.colorScheme.surface,
            contentColor     = MaterialTheme.colorScheme.primary
        ) {
            listOf("Active", "Cancelled").forEachIndexed { index, label ->
                Tab(
                    selected = tabIndex == index,
                    onClick  = { tabIndex = index },
                    text     = { Text(label, style = MaterialTheme.typography.labelLarge) }
                )
            }
        }

        // ── Content ───────────────────────────────────────────────────────────
        when {
            state.isLoading -> LoadingView()
            state.hasError  -> ErrorView(
                message = state.error ?: "Failed to load tickets",
                onRetry = { viewModel.loadBookings() }
            )
            state.data != null -> {
                val allBookings = state.data!!
                val filtered = when (tabIndex) {
                    0 -> allBookings.filter { it.bookingStatus != BookingStatus.CANCELLED.name }
                    1 -> allBookings.filter { it.bookingStatus == BookingStatus.CANCELLED.name }
                    else -> allBookings
                }

                if (filtered.isEmpty()) {
                    EmptyState(
                        emoji    = "🎫",
                        title    = if (tabIndex == 0) "No active tickets" else "No cancelled tickets",
                        subtitle = if (tabIndex == 0) "Book an event to see your tickets here" else null,
                        actionLabel = if (tabIndex == 0) "Browse Events" else null,
                        onAction    = if (tabIndex == 0) {
                            { navController.navigate(Screen.Dashboard.route) }
                        } else null
                    )
                } else {
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.screenHorizontal),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        item { Spacer(modifier = Modifier.height(Spacing.xs)) }
                        items(filtered, key = { it.id }) { booking ->
                            BookingCard(
                                booking       = booking,
                                onClick       = {
                                    navController.navigate(
                                        Screen.BookingSuccess.createRoute(booking.id)
                                    )
                                },
                                onCancel      = { viewModel.cancelBooking(booking.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(Spacing.md)) }
                    }
                }
            }
        }
    }
}

// ── Booking card ──────────────────────────────────────────────────────────────

@Composable
private fun BookingCard(
    booking  : Booking,
    onClick  : () -> Unit,
    onCancel : () -> Unit
) {
    val statusColor = when (booking.bookingStatus) {
        BookingStatus.CONFIRMED.name  -> TmSuccess
        BookingStatus.CANCELLED.name  -> TmError
        BookingStatus.COMPLETED.name  -> TmGold
        else                          -> TmWarning
    }

    Surface(
        modifier      = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape         = MaterialTheme.shapes.large,
        color         = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            // ── Title + status badge ───────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = booking.eventTitle.ifBlank { "Event" },
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.weight(1f)
                )
                StatusBadge(status = booking.bookingStatus, color = statusColor)
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // ── Meta rows ─────────────────────────────────────────────────────
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

            // ── Qty + total ───────────────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
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

            // ── Cancel action (only for active bookings) ──────────────────────
            if (booking.bookingStatus == BookingStatus.CONFIRMED.name) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text     = "Cancel Booking",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = TmError,
                    modifier = Modifier.clickable { onCancel() }
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String, color: Color) {
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
