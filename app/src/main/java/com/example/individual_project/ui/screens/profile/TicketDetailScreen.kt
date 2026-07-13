package com.example.individual_project.ui.screens.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode2
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.individual_project.data.model.Ticket
import com.example.individual_project.domain.repository.TicketRepository
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.model.UiState
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.utils.PriceFormatter
import com.example.individual_project.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class TicketDetailViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    savedStateHandle            : SavedStateHandle
) : ViewModel() {

    private val ticketId: String = savedStateHandle.get<String>("ticketId") ?: ""

    private val _state = MutableStateFlow(UiState<Ticket>())
    val state: StateFlow<UiState<Ticket>> = _state.asStateFlow()

    init {
        if (ticketId.isNotBlank()) load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState(isLoading = true)
            _state.value = when (val r = ticketRepository.getTicketById(ticketId)) {
                is Resource.Success -> UiState(data = r.data)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Failed to load ticket")
            }
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    navController: NavController,
    viewModel    : TicketDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ticket Details") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> LoadingView()
                state.hasError  -> ErrorView(
                    message = state.error ?: "Failed to load ticket",
                    onRetry = { viewModel.load() }
                )
                state.data != null -> {
                    val ticket = state.data!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = Spacing.screenHorizontal),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(Spacing.lg))

                        // ── Status badge ───────────────────────────────────────
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = TmSuccess.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier          = Modifier.padding(
                                    horizontal = Spacing.md,
                                    vertical   = Spacing.sm
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle, null,
                                    tint     = TmSuccess,
                                    modifier = Modifier.size(Spacing.iconMd)
                                )
                                Text(
                                    text       = ticket.ticketStatus,
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = TmSuccess
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xl))

                        // ── QR code placeholder ────────────────────────────────
                        Surface(
                            modifier      = Modifier.size(180.dp),
                            shape         = RoundedCornerShape(Spacing.md),
                            color         = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.QrCode2, null,
                                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(120.dp)
                                    )
                                    Text(
                                        text      = "Scan at Venue",
                                        style     = MaterialTheme.typography.labelSmall,
                                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xl))

                        // ── Ticket info card ───────────────────────────────────
                        Surface(
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = MaterialTheme.shapes.large,
                            color         = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(Spacing.md)) {
                                Text(
                                    text       = ticket.eventTitle.ifBlank { "Event Ticket" },
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(Spacing.md))

                                if (ticket.venue.isNotBlank()) {
                                    TicketDetailRow("Venue",   ticket.venue)
                                }
                                if (ticket.date.isNotBlank()) {
                                    TicketDetailRow("Date",    ticket.date)
                                }
                                TicketDetailRow(
                                    "Tickets",
                                    "${ticket.quantity} seat${if (ticket.quantity > 1) "s" else ""}"
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = Spacing.sm),
                                    color    = MaterialTheme.colorScheme.outlineVariant
                                )

                                TicketDetailRow(
                                    label      = "Total Paid",
                                    value      = PriceFormatter.format(ticket.totalPrice),
                                    valueColor = TmGold,
                                    bold       = true
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        // ── Reference IDs ──────────────────────────────────────
                        Surface(
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = MaterialTheme.shapes.large,
                            color         = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(Spacing.md)) {
                                Text(
                                    text  = "References",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(Spacing.sm))
                                TicketDetailRow("Ticket ID",  ticket.id.take(16) + "…",  valueColor = TmBlue)
                                TicketDetailRow("Booking ID", ticket.bookingId.take(16) + "…", valueColor = TmBlue)
                                TicketDetailRow("Payment ID", ticket.paymentId.take(16) + "…", valueColor = TmBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xxl))
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketDetailRow(
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
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color      = if (valueColor == Color.Unspecified)
                             MaterialTheme.colorScheme.onSurface
                         else valueColor
        )
    }
}
