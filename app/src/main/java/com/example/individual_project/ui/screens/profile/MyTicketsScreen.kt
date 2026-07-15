package com.example.individual_project.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.individual_project.data.model.Ticket
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.domain.repository.TicketRepository
import com.example.individual_project.notifications.EventReminderScheduler
import com.example.individual_project.ui.components.EmptyState
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.components.TicketCard
import com.example.individual_project.ui.model.UiState
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.utils.PriceFormatter
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class MyTicketsViewModel @Inject constructor(
    private val ticketRepository : TicketRepository,
    private val bookingRepository: BookingRepository,
    private val reminderScheduler: EventReminderScheduler,
    private val firebaseAuth     : FirebaseAuth,
    savedStateHandle             : SavedStateHandle
) : ViewModel() {

    private val uid: String get() = firebaseAuth.currentUser?.uid ?: ""

    private val _state = MutableStateFlow(UiState<List<Ticket>>())
    val state: StateFlow<UiState<List<Ticket>>> = _state.asStateFlow()

    // Booking ids currently being cancelled
    private val _cancellingIds = MutableStateFlow<Set<String>>(emptySet())
    val cancellingIds: StateFlow<Set<String>> = _cancellingIds.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    init {
        loadTickets()
    }

    fun loadTickets() {
        if (uid.isBlank()) {
            _state.value = UiState(error = "Not logged in")
            return
        }
        viewModelScope.launch {
            _state.value = UiState(isLoading = true)
            _state.value = when (val r = ticketRepository.getUserTickets(uid)) {
                is Resource.Success -> UiState(
                    data = r.data
                        .filter { it.ticketStatus == "ACTIVE" }
                        .sortedByDescending { it.generatedAt }
                )
                is Resource.Error -> UiState(error = r.message)
                else             -> UiState(error = "Failed to load tickets")
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        if (bookingId in _cancellingIds.value) return
        viewModelScope.launch {
            _cancellingIds.update { it + bookingId }
            when (val result = bookingRepository.cancelBooking(bookingId)) {
                is Resource.Error -> _actionError.value = result.message
                else              -> reminderScheduler.cancelReminder(bookingId)
            }
            loadTickets()
            _cancellingIds.update { it - bookingId }
        }
    }

    fun clearActionError() { _actionError.value = null }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    navController: NavController,
    viewModel    : MyTicketsViewModel = hiltViewModel()
) {
    val state         by viewModel.state.collectAsState()
    val cancellingIds by viewModel.cancellingIds.collectAsState()
    val actionError   by viewModel.actionError.collectAsState()
    var pendingCancelBookingId by remember { mutableStateOf<String?>(null) }
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(actionError) {
        actionError?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearActionError()
        }
    }

    pendingCancelBookingId?.let { bookingId ->
        AlertDialog(
            onDismissRequest = { pendingCancelBookingId = null },
            title            = { Text("Cancel booking?") },
            text             = { Text("This can't be undone. Your seats will be released and your ticket will be cancelled.") },
            confirmButton    = {
                TextButton(onClick = {
                    viewModel.cancelBooking(bookingId)
                    pendingCancelBookingId = null
                }) { Text("Cancel Booking", color = TmError) }
            },
            dismissButton = {
                TextButton(onClick = { pendingCancelBookingId = null }) { Text("Keep Booking") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("My Tickets") },
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
            // Gradient accent strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Spacing.xs)
                    .background(Brush.horizontalGradient(listOf(TmNavyBlue, TmDarkBlue)))
            )

            when {
                state.isLoading -> LoadingView()
                state.hasError  -> ErrorView(
                    message = state.error ?: "Failed to load tickets",
                    onRetry = { viewModel.loadTickets() }
                )
                state.data != null -> {
                    val tickets = state.data!!
                    if (tickets.isEmpty()) {
                        EmptyState(
                            emoji       = "🎫",
                            title       = "No Tickets Yet",
                            subtitle    = "Your confirmed event tickets will appear here",
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
                            items(tickets, key = { it.id }) { ticket ->
                                TicketCard(
                                    emoji         = "🎫",
                                    title         = ticket.eventTitle.ifBlank { "Event Ticket" },
                                    details       = buildTicketDetails(ticket),
                                    isUpcoming    = ticket.ticketStatus == "ACTIVE",
                                    onActionClick = {
                                        navController.navigate(
                                            Screen.TicketDetail.createRoute(ticket.id)
                                        )
                                    },
                                    onCancelClick = { pendingCancelBookingId = ticket.bookingId },
                                    isCancelling  = ticket.bookingId in cancellingIds
                                )
                            }
                            item { Spacer(modifier = Modifier.height(Spacing.md)) }
                        }
                    }
                }
            }
        }
    }
}

private fun buildTicketDetails(ticket: Ticket): String = buildString {
    if (ticket.venue.isNotBlank())    appendLine(ticket.venue)
    if (ticket.date.isNotBlank())     appendLine(ticket.date)
    appendLine("${ticket.quantity} ticket${if (ticket.quantity > 1) "s" else ""}")
    append(PriceFormatter.format(ticket.totalPrice))
}.trimEnd()
